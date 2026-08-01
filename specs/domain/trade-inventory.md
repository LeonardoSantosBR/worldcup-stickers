# Trade Inventory — A Vitrine

Prefixo de regra: `TI`

Código: [UserTradeInventoriesService.java](../../src/main/java/com/leonardo/worldcup_stickers/services/UserTradeInventoriesService.java),
[UserTradeInventoriesRepository.java](../../src/main/java/com/leonardo/worldcup_stickers/repositories/UserTradeInventoriesRepository.java)

## Conceito

A vitrine é a **declaração pública** de quais figurinhas o usuário aceita trocar. É o que
torna as figurinhas dele visíveis e "ofertáveis" para os outros.

Vitrine **não** é posse. É intenção. As duas podem divergir temporariamente, e a
reconciliação é explícita (`TI-06`).

## Modelo

`UserTradeInventoryEntity` = tabela `user_trade_inventories`.

| Campo | Regra |
|---|---|
| `user` | `@OneToOne`, **unique** — exatamente uma vitrine por usuário |
| `availableStickerIds` | `bigint[]` do PostgreSQL (`@JdbcTypeCode(SqlTypes.ARRAY)`), default `[]` |

**TI-01 — A vitrine é um array de IDs, não uma tabela de associação.** Escolha deliberada:
a lista inteira é substituída de uma vez, e não há metadado por item. Consultá-la exige
`unnest` em SQL nativo.

## Regras

**TI-02 — Um usuário, uma vitrine.** Criada no cadastro (`AU-03`). `loadOrCreateInventory`
existe como rede de segurança para usuários anteriores a essa regra.

**TI-03 — `POST /stickers/make-available-trade` substitui a vitrine inteira.** Não é
"adicionar". A lista enviada **vira** a vitrine; o que não estiver nela sai. Para remover
tudo é preciso enviar a lista completa menos o item — e o DTO exige `@NotEmpty`, então
**não existe forma de esvaziar a vitrine pela API**. ⚠️ Ver [open-questions.md](../open-questions.md).

**TI-04 — Só se pode disponibilizar o que se possui.** Todo ID enviado precisa existir na
coleção do usuário. Qualquer ID não possuído → `StickersNotOwnedException` → **400**, e
a lista dos IDs inválidos vai na mensagem. Validação é tudo-ou-nada.

**TI-05 — Duplicatas na requisição são colapsadas.** `new LinkedHashSet<>(stickerIds)`
remove repetições preservando a ordem de envio.

**TI-06 — Disponibilizar não reserva nem bloqueia.** A figurinha continua na coleção,
continua contando no progresso do álbum, e pode ser oferecida em quantas ofertas
simultâneas o usuário quiser. Não há lock. Conflitos são resolvidos no momento do aceite
(ver `TO-11` e `TO-14` em [trade-offers.md](trade-offers.md)).

**TI-07 — A vitrine é reconciliada automaticamente após cada troca aceita.**
`syncTradeInventory(userId)` remove da vitrine todo ID que o usuário deixou de possuir.
Roda para **ambos** os lados da troca. Só grava se algo mudou.

> Isso é o único mecanismo de limpeza. Nenhum outro fluxo remove item da vitrine.

**TI-08 — Consultar figurinhas quantidade-1 na vitrine é permitido.** Colocar na vitrine a
única cópia que se tem é válido; o sistema não impede negociar uma figurinha não-repetida.

## Vitrine pública

**TI-09 — `GET /stickers/available-trades` lista a vitrine de todos os outros usuários.**
Query nativa com `CROSS JOIN LATERAL unnest(i.available_sticker_ids)`: cada figurinha
disponível de cada usuário vira uma linha.

**TI-10 — O usuário nunca vê a própria vitrine nesse endpoint.** `WHERE i.user_id <> :userId`.
Para ver a própria, use `GET /users/my-stickers-available-trade` (`CO-10`).

**TI-11 — Filtro opcional por nome do jogador.** Query param `name`, case-insensitive,
substring (`ILIKE '%nome%'` sobre `s.player_name`). `null` ou string em branco → sem
filtro (o service normaliza com `trim()` antes de passar adiante).

**TI-12 — Soft delete é respeitado manualmente na query nativa.** Os `JOIN`s carregam
`AND u.deleted_at IS NULL` e `AND s.deleted_at IS NULL` explicitamente, porque
`@SQLRestriction` **não** se aplica a SQL nativo. Toda query nativa nova precisa repetir isso.

**TI-13 — Ordenação: `s.number ASC, u.name ASC`.**

**TI-14 — Aliases camelCase precisam de aspas duplas.** O PostgreSQL rebaixa identificadores
não-aspeados para minúsculas, o que quebra a projeção de interface do Spring
(`AvailableTradeStickerView`). Por isso `s.id AS "stickerId"`, `u.name AS "ownerName"`, etc.
Regra obrigatória para qualquer projeção nativa nova.

**TI-15 — A resposta identifica o dono.** `AvailableTradeStickerDto` traz `stickerId`,
`stickerName`, `rarity`, `position` + `ownerId`, `ownerName`, `ownerEmail`. O `ownerId` é
o que vai em `receiverId` ao criar uma oferta.
