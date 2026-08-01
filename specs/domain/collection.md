# Coleção do Usuário

Prefixo de regra: `CO`

Código: [UserStickerEntity.java](../../src/main/java/com/leonardo/worldcup_stickers/entities/UserStickerEntity.java),
[UsersService.java](../../src/main/java/com/leonardo/worldcup_stickers/services/UsersService.java)

## Modelo

`UserStickerEntity` = tabela `user_stickers`, com **unique constraint em `(user_id, sticker_id)`**.

| Campo | Regra |
|---|---|
| `user` | FK obrigatória, `LAZY` |
| `sticker` | FK obrigatória, `LAZY` |
| `quantity` | Obrigatório, default `1` |

## Regras

**CO-01 — Uma linha por par (usuário, figurinha).** Posse de múltiplas unidades é expressa
em `quantity`, nunca em linhas duplicadas. A constraint de unicidade garante isso no banco.

**CO-02 — `quantity` é sempre ≥ 1 enquanto a linha existir.** Não existe posse com
quantidade zero: quando a última unidade sai numa troca, a **linha é deletada**
(`transferStickers` em [trade-offers.md](trade-offers.md)).

> Invariante para código novo: `quantity == 0` é estado inválido. Delete a linha.

**CO-03 — Ganhar uma figurinha é upsert.** Padrão em `addToUserCollection` e
`transferStickers`: procura a linha `(user, sticker)`; se não existe, cria com
`quantity = 0`; incrementa; salva. Todo fluxo que dá figurinha a alguém deve seguir esse
mesmo padrão.

**CO-04 — Perder uma figurinha é decremento-ou-delete.**
```
if (quantity <= 1) delete(linha);
else               quantity -= 1;
```

**CO-05 — A coleção não é soft-deleted.** `user_stickers` não tem `deletedAt`. Perder uma
figurinha apaga o registro de verdade. O histórico de quem trocou o quê vive em
`user_trade_offers_logs`, não aqui.

**CO-06 — Listagens da coleção são paginadas e ordenadas por `sticker.number`.**
Ver `CO-07` para os limites.

**CO-07 — Contrato de paginação (vale para toda a API).**

| Parâmetro | Default | Regra |
|---|---|---|
| `page` | `1` | **1-based** na API; convertido para 0-based com `max(page - 1, 0)`. `page ≤ 0` cai para a primeira página. |
| `limit` | `20` | Clampado em `[1, 100]` via `min(max(limit, 1), MAX_LIMIT)`. `MAX_LIMIT = 100`. |

Valores fora da faixa **não** geram erro — são silenciosamente ajustados.

**CO-08 — Formato de resposta paginada.** `PageResponseDto<T>`, usado por todos os
endpoints de lista. Nenhum endpoint devolve `Page` do Spring diretamente.

## Endpoints

**CO-09 — `GET /users/my-stickers` lista tudo que o usuário possui.** Toda a coleção,
paginada, incluindo repetidas (com `quantity`).

**CO-10 — `GET /users/my-stickers-available-trade` lista apenas o que o usuário colocou na
vitrine.** Não é "minhas repetidas" — é a interseção entre a coleção e o
`availableStickerIds` da vitrine. Implementado em duas etapas:

1. Lê os `availableStickerIds` da vitrine.
2. Se vazio → devolve página vazia **sem tocar o banco de novo**.
3. Senão → `findByUserIdAndStickerIdIn(userId, ids, pageable)`.

> Dois métodos com nomes parecidos e semânticas diferentes:
> `findMyStickersAvailableTrade` (**minha** vitrine, DTO `MyStickerDto`) vs.
> `findAllAvailableForTrade` (vitrine **dos outros**, DTO `AvailableTradeStickerDto`).
> Ver [trade-inventory.md](trade-inventory.md).
