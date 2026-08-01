# Contrato HTTP

Base: `/` · Auth: `Authorization: Bearer <jwt>` em tudo, exceto onde marcado **público**.
Erros: ver [error-handling.md](../conventions/error-handling.md).

## Auth

### `POST /auth/signin` — **público**
```json
→ { "email": "a@b.com", "password": "secret" }
← 200 { "token": "eyJ..." }
```
`401` se e-mail ou senha inválidos (`AU-04`).

## Usuários

### `POST /users` — **público**
Cadastro. ⚠️ Recebe `UserEntity` cru (`AR-06`); só `name`, `email`, `password` são usados.
```json
→ { "name": "Leo", "email": "a@b.com", "password": "secret" }
← 200 true
```
`409` se o e-mail já existe. Cria também a vitrine vazia (`AU-03`).

### `GET /users/my-profile`
```json
← 200 { "email": "a@b.com", "name": "Leo", "completePercentage": 42.86 }
```
Percentual = figurinhas distintas possuídas / total do álbum (`AU-11`).

### `GET /users/my-stickers`
Query: `page` (1), `limit` (20, máx. 100).
Toda a coleção, ordenada por `sticker.number`, com `quantity`. `PageResponseDto<MyStickerDto>`.

### `GET /users/my-stickers-available-trade`
Query: `page`, `limit`. Apenas as figurinhas que **eu** coloquei na vitrine (`CO-10`).
Vitrine vazia → página vazia.

## Figurinhas

### `POST /stickers/open-package`
Sem corpo. Sorteia 7 figurinhas e adiciona à coleção (`PK-01`…`PK-04`).
```json
← 200 [ { "id": 12, "number": 34, "playerName": "...", "country": "...",
          "group": "C", "position": "FORWARD", "rarity": "RARE", "imageUrl": null,
          "createdAt": "...", "updatedAt": null, "deletedAt": null }, ... ]
```
⚠️ Devolve entidade JPA crua (`PK-09`). Pode conter IDs repetidos.

### `POST /stickers/make-available-trade`
**Substitui** a vitrine inteira (`TI-03`).
```json
→ { "stickerIds": [12, 45, 78] }
← 200 true
```
`stickerIds` é `@NotEmpty`, itens `@NotNull @Positive`. `400` se alguma não for possuída (`TI-04`).

### `GET /stickers/available-trades`
Query: `page` (1), `limit` (20), `name` (opcional).
Vitrine de **todos os outros** usuários (`TI-09`, `TI-10`). `name` filtra `playerName` por
substring case-insensitive (`TI-11`).
```json
← 200 { "content": [ { "stickerId": 12, "stickerName": "...", "rarity": "RARE",
                       "position": "FORWARD", "ownerId": 7, "ownerName": "Ana",
                       "ownerEmail": "ana@b.com" } ], ... }
```
`ownerId` é o `receiverId` para criar uma oferta.

## Ofertas de troca

### `GET /trade-offers/inbox`
Query: `page` (1), `limit` (20, máx. 100), `status` (opcional: `PENDING` | `ACCEPTED` |
`REJECTED` | `CANCELLED`).

Ofertas em que o usuário autenticado é o **receiver**, mais recentes primeiro
(`TO-28`…`TO-36`). Sem `status` → todas.
```json
← 200 { "items": [ { "id": 3,
                     "proposerId": 7,
                     "proposerName": "Ana",
                     "receiverId": 1,
                     "requestedStickers": [ { "id": 1255, "name": "Vinícius Júnior" } ],
                     "offeredStickers":   [ { "id": 811,  "name": "Lionel Messi" } ],
                     "status": "PENDING",
                     "message": "topa?",
                     "createdAt": "2026-08-01T10:00:00",
                     "respondedAt": null } ],
        "page": 1, "limit": 20, "totalItems": 1, "totalPages": 1, "hasNext": false }
```
`TradeOfferDetailDto` — diferente do `TradeOfferDto` devolvido por criar/aceitar/recusar
(`TO-33`). `name` é o `playerName`; vem `null` se a figurinha foi soft-deleted (`TO-36`).

Inbox vazio → `200` com `items: []`. `status` inválido → `400` (formato padrão do Spring,
case-sensitive).

### `POST /trade-offers/make-offer` → **201 Created**
```json
→ { "receiverId": 7,
    "requestedStickerIds": [12, 45],
    "offeredStickerIds": [88],
    "message": "topa?" }
← 201 TradeOfferDto
```
Validações em ordem: `TO-03` → `TO-04` → `TO-06` → `TO-07` → `TO-08`.
`message` máx. 255. Ambas as listas `@NotEmpty`.

### `POST /trade-offers/{offerId}/accept`
Corpo **opcional**: `{ "note": "fechado" }` (máx. 255).
Só o receiver, só se `PENDING`. Transfere as figurinhas, sincroniza vitrines e recusa em
cascata as ofertas quebradas (`TO-12`…`TO-20`).
```
← 200 TradeOfferDto (status ACCEPTED)
```

### `POST /trade-offers/{offerId}/reject`
Corpo opcional idêntico. Nada muda de mãos (`TO-21`).
```
← 200 TradeOfferDto (status REJECTED)
```

## Não implementado

Existe suporte no repositório, mas **nenhum endpoint** para:

- listar ofertas enviadas (outbox) — `findByProposerIdAndStatus`
- cancelar a própria oferta (`CANCELLED`) — `findByIdAndProposerId`

Ver [open-questions.md](../open-questions.md).
