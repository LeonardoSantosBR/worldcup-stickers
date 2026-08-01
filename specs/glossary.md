# Glossário — Linguagem Ubíqua

Termos do domínio. O nome usado aqui é o nome usado no código. Se um conceito precisa
de nome novo, ele nasce aqui primeiro.

| Termo | Código | Significado |
|---|---|---|
| **Sticker** | `StickerEntity` | Uma figurinha do álbum. Existe independente de qualquer usuário. Tem `number` único (a "posição no álbum"), jogador, país, grupo, posição e raridade. |
| **Álbum** | — | O conjunto completo de todas as `stickers` cadastradas. Não é uma entidade; é o `count()` da tabela `stickers`. |
| **Collection / Coleção** | `UserStickerEntity` | O vínculo entre um usuário e uma figurinha que ele possui, com `quantity`. Uma linha por par (user, sticker). |
| **Repetida** | `quantity > 1` | Não existe entidade "repetida". Uma figurinha é repetida quando a `quantity` na coleção é maior que 1. |
| **Package / Pacotinho** | — | Ato de sortear N figurinhas aleatórias e adicioná-las à coleção. Não é entidade — é a operação `openPackage()`. |
| **Rarity / Raridade** | `RarityEnum` | `COMMON`, `RARE`, `LEGENDARY`. Define a probabilidade de saída no pacotinho. |
| **Trade Inventory / Vitrine** | `UserTradeInventoryEntity` | A lista de `stickerIds` que um usuário declarou disponíveis para troca. Uma vitrine por usuário (1:1). Vitrine ≠ posse: é uma **declaração de intenção**. |
| **Trade Offer / Oferta** | `UserTradeOffersEntity` | Proposta de troca entre dois usuários: "eu te dou X, você me dá Y". |
| **Proposer** | `proposer` | Quem **cria** a oferta. É quem *oferece* as `offeredStickerIds`. |
| **Receiver** | `receiver` | Quem **recebe** a oferta e decide aceitar ou recusar. É o dono das `requestedStickerIds`. |
| **Requested stickers** | `requestedStickerIds` | O que o proposer **quer** — pertencem ao receiver. |
| **Offered stickers** | `offeredStickerIds` | O que o proposer **dá** — pertencem ao proposer. |
| **Trade Status** | `TradeStatusEnum` | `PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`. |
| **Trade Log** | `UserTradeOffersLogsEntity` | Registro imutável de cada transição de status de uma oferta. Trilha de auditoria. |
| **Invalidação em cascata** | `invalidateConflictingOffers` | Ao aceitar uma troca, outras ofertas `PENDING` que se tornaram impossíveis (alguém não possui mais o que prometeu) são recusadas automaticamente. |
| **Soft delete** | `deletedAt` | `users` e `stickers` nunca são apagados fisicamente — recebem `deleted_at` e somem de todas as queries via `@SQLRestriction`. |
