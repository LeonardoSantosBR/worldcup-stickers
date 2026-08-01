# Trade Offers — Ofertas de Troca

Prefixo de regra: `TO`

Código: [UserTradeOffersService.java](../../src/main/java/com/leonardo/worldcup_stickers/services/UserTradeOffersService.java),
[UserTradeOffersEntity.java](../../src/main/java/com/leonardo/worldcup_stickers/entities/UserTradeOffersEntity.java),
[UserTradeOffersLogsEntity.java](../../src/main/java/com/leonardo/worldcup_stickers/entities/UserTradeOffersLogsEntity.java)

É o núcleo do domínio. Leia isto por inteiro antes de mexer em qualquer coisa de troca.

## Papéis

```
PROPOSER  cria a oferta   →  dá   offeredStickerIds    (dele)
RECEIVER  responde        →  dá   requestedStickerIds  (dele)
```

Perspectiva: os nomes dos campos são sempre **do ponto de vista do proposer**.
"Requested" = o que o proposer *pede*. "Offered" = o que o proposer *entrega*.

## Máquina de estados

```
                 ┌──────────► ACCEPTED     (receiver aceita)
   PENDING ──────┤
   (criação)     ├──────────► REJECTED     (receiver recusa)
                 │                          ou invalidação automática
                 └──────────► CANCELLED    ⚠️ estado previsto, sem transição implementada
```

**TO-01 — Só ofertas `PENDING` podem ser respondidas.** Qualquer tentativa sobre oferta já
finalizada → `InvalidTradeOfferException` → **400** com a mensagem
`"Trade offer {id} is already {status}"`. Estados finais são imutáveis.

**TO-02 — `CANCELLED` existe no enum e no comentário da entidade de log, mas nenhum código
o produz.** Não há endpoint de cancelamento pelo proposer. ⚠️ Ver
[open-questions.md](../open-questions.md).

## Criar oferta — `POST /trade-offers/make-offer`

Validações, **na ordem em que rodam** (a primeira que falhar aborta):

**TO-03 — Não se ofertar a si mesmo.** `proposerId == receiverId` →
`InvalidTradeOfferException` → **400**.

**TO-04 — Nenhuma figurinha pode estar dos dois lados.** A interseção entre
`requestedStickerIds` e `offeredStickerIds` deve ser vazia. Interseção não-vazia → **400**,
com os IDs conflitantes na mensagem.

**TO-05 — Duplicatas dentro de cada lista são colapsadas.** `LinkedHashSet` em ambas,
preservando a ordem de envio. Consequência: **não é possível oferecer 2 unidades da mesma
figurinha numa oferta** — a troca é sempre 1 unidade por ID distinto.

**TO-06 — Proposer e receiver devem existir.** `UserNotFoundException` → **404**.

**TO-07 — O proposer deve possuir tudo que oferece.** Checado contra
`findStickerIdsByUserId(proposerId)`. Faltando algo → `StickersNotOwnedException` → **400**.

**TO-08 — O receiver deve ter listado na vitrine tudo que é pedido.** Não basta possuir —
precisa estar em `availableStickerIds` (`TI-03`). Faltando algo →
`StickersNotAvailableForTradeException` → **400**, com `receiverId` e IDs na mensagem.

> Assimetria deliberada: o **proposer** é validado por **posse**; o **receiver**, por
> **disponibilidade declarada**. Isso impede pedir algo que a pessoa nunca ofereceu.

**TO-09 — A oferta nasce `PENDING` e já grava o primeiro log** (`"Offer created"`,
`changedBy = proposer`), na mesma transação.

**TO-10 — Não há limite de ofertas simultâneas.** O mesmo proposer pode ter várias ofertas
`PENDING` para o mesmo receiver, e a mesma figurinha pode estar prometida em várias ofertas
ao mesmo tempo. Nada é reservado. O repositório expõe
`existsByProposerIdAndReceiverIdAndStatus`, mas ele **não é usado** para bloquear duplicatas.

**TO-11 — `message` é opcional, máx. 255 caracteres.**

Resposta: **201 Created** com `TradeOfferDto`.

## Aceitar — `POST /trade-offers/{offerId}/accept`

**TO-12 — Só o receiver aceita.** A busca é `findByIdAndReceiverId(offerId, receiverId)` —
se o usuário autenticado não for o receiver, o resultado é `TradeOfferNotFoundException` →
**404** (e não 403: a existência da oferta não vaza).

**TO-13 — A posse é revalidada no momento do aceite, dos dois lados.** O mundo pode ter
mudado desde a criação (`TO-10`). `assertOwns(proposer, offered)` e
`assertOwns(receiver, requested)`. Falha → `StickersNotOwnedException` → **400**, e a
oferta permanece `PENDING`.

> A vitrine **não** é revalidada no aceite — só a posse. Quem aceita já está consentindo.

**TO-14 — A troca move exatamente 1 unidade de cada ID, nos dois sentidos.**
`requested`: receiver → proposer. `offered`: proposer → receiver.
Cada movimento segue `CO-03` / `CO-04` (upsert no destino, decremento-ou-delete na origem).

**TO-15 — Aceitar é atômico.** `@Transactional` cobre: transferências, sync das duas
vitrines, mudança de status, log e invalidação em cascata. Qualquer exceção reverte tudo.

**TO-16 — Após transferir, as vitrines de ambos são reconciliadas** (`TI-07`).

**TO-17 — Status vira `ACCEPTED`, `respondedAt` é carimbado, e um log é gravado** com
`changedBy = receiver` e o `note` opcional do corpo.

### Invalidação em cascata

**TO-18 — Aceitar uma troca recusa automaticamente outras ofertas que se tornaram
impossíveis.** Após o aceite, o sistema:

1. Recalcula a posse atual dos **dois** usuários envolvidos.
2. Busca todas as ofertas `PENDING` em que qualquer um deles participe.
3. Para cada uma (exceto a recém-aceita), marca como **quebrada** se:
   - o proposer não possui mais tudo que ofereceu, **ou**
   - o receiver não possui mais tudo que foi pedido.
4. Ofertas quebradas viram `REJECTED`, com `respondedAt` e log
   `"Automatically rejected: stickers no longer available"`.

**TO-19 — O `changedBy` do log de invalidação é o receiver que aceitou**, não o dono da
oferta invalidada. É quem causou a mudança de estado.

**TO-20 — A checagem de posse só cobre os dois usuários da troca.** Se o outro lado de uma
oferta invalidada é um terceiro, sua posse não é reconsultada (`ownedByUser.get(...)` é
`null` e a condição é ignorada para ele). Isso é correto: a posse de terceiros não mudou.

## Recusar — `POST /trade-offers/{offerId}/reject`

**TO-21 — Recusar não move nada.** Só troca o status para `REJECTED`, carimba
`respondedAt` e grava o log. Sem revalidação de posse, sem sync de vitrine, sem cascata.

**TO-22 — Mesmas regras de autorização e de estado do aceite** (`TO-12`, `TO-01`).

## Corpo de resposta (accept/reject)

**TO-23 — O corpo é opcional.** `@RequestBody(required = false) RespondOfferDto`. Se ausente,
`note = null`. Se presente, `note` tem máx. 255 caracteres.

## Auditoria

**TO-24 — Toda transição de status gera exatamente uma linha em `user_trade_offers_logs`.**
Campos: `tradeOffer`, `status`, `changedBy`, `note`, `createdAt`.

**TO-25 — Logs são imutáveis.** Só há `@PrePersist`; nenhum fluxo atualiza ou apaga log.
A trilha completa de uma oferta é a sequência de logs ordenada por `createdAt`.

**TO-26 — Logs seguem a oferta em cascata.** `cascade = ALL`, `orphanRemoval = true` no lado
da oferta. Deletar uma oferta apaga a trilha — o que na prática nunca deve acontecer.

## Concorrência

**TO-27 — ⚠️ Não há lock otimista nem pessimista.** Dois aceites concorrentes envolvendo a
mesma figurinha dependem apenas do isolamento da transação do banco. A revalidação de posse
(`TO-13`) reduz a janela, mas não a fecha. Ver [open-questions.md](../open-questions.md).
