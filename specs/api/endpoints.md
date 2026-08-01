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

- listar ofertas recebidas (inbox) — `findByReceiverIdAndStatus`
- listar ofertas enviadas (outbox) — `findByProposerIdAndStatus`
- cancelar a própria oferta (`CANCELLED`) — `findByIdAndProposerId`

Ou seja: hoje o receiver **não tem como descobrir** que recebeu uma oferta pela API.
Ver [open-questions.md](../open-questions.md).
