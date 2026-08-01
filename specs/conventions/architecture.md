# Arquitetura e Padrões de Código

Como o código é organizado hoje, e o que seguir ao escrever código novo.

## Stack

Java 21 · Spring Boot 3 (Web, Data JPA, Validation) · PostgreSQL · Lombok · JJWT · BCrypt · Maven

## Camadas

```
HTTP → Controller → Service → Repository → PostgreSQL
                       ↕
                  Entity / DTO
```

**AR-01 — Três camadas, sem UseCase.** `Controller → Service → Repository`. A regra de
negócio vive no **Service**. Controller não decide nada; Repository não decide nada.

**AR-02 — Controller é fino.** Responsabilidades: mapear rota, extrair `userId` do request
attribute, validar o DTO com `@Valid`, delegar, devolver. Zero `if` de negócio.

**AR-03 — Injeção por construtor, campos `final`.** Sem `@Autowired` em campo. Sem `new`
de dependência.

**AR-04 — Transações no Service.** `@Transactional` em escritas,
`@Transactional(readOnly = true)` em leituras. Nunca no controller nem no repository.

**AR-05 — DTOs são `record` imutáveis com factory estática.** Padrão:
`static XDto fromEntity(Entity e)` / `fromView(View v)`. A conversão mora no DTO, não no
service. (`AuthDto` é uma classe antiga que não segue o padrão.)

**AR-06 — Entidade JPA não atravessa a fronteira HTTP.** Toda resposta é DTO.
Duas violações conhecidas, ambas listadas em [open-questions.md](../open-questions.md):
`POST /stickers/open-package` (devolve `List<StickerEntity>`) e `POST /users` (recebe
`UserEntity` cru no corpo).

**AR-07 — Toda listagem é paginada com `PageResponseDto<T>`,** seguindo o contrato `CO-07`
(`page` 1-based, `limit` clampado em `[1, 100]`).

## Entidades

**AR-08 — Padrão de entidade:** Lombok `@Getter @Setter @NoArgsConstructor
@AllArgsConstructor @Builder`; `@Builder.Default` em todo campo com valor inicial (sem
isso, o builder o zera).

**AR-09 — Timestamps são gerenciados por callback JPA:** `@PrePersist` preenche `createdAt`
(`updatable = false`), `@PreUpdate` preenche `updatedAt`. Não use `@CreationTimestamp`.

**AR-10 — Soft delete em `users` e `stickers`,** via `@SQLDelete` + `@SQLRestriction`.
As demais tabelas são hard delete. Ao criar entidade nova, decida conscientemente qual das
duas — e registre aqui.

**AR-11 — Relacionamentos são `FetchType.LAZY`.** Sempre. Quem precisa do relacionado
carrega explicitamente (query dedicada ou `@EntityGraph`).

> Armadilha registrada: `@EntityGraph(attributePaths = ...)` usa **nomes de campo da
> entidade**, não nomes de coluna, e só funciona se o tipo de retorno for a entidade que
> possui aquele caminho. Já causou bug em `UserTradeInventoriesRepository`.

**AR-12 — Listas de IDs usam `bigint[]` do PostgreSQL**
(`@JdbcTypeCode(SqlTypes.ARRAY)`), não tabela de associação. Vale para
`availableStickerIds`, `requestedStickerIds`, `offeredStickerIds`. Consulta exige `unnest`.

## Repositórios

**AR-13 — Prefira query methods derivados.** `@Query` (JPQL) quando o nome derivado ficaria
absurdo; SQL nativo apenas quando precisa de recurso do PostgreSQL (ex.: `unnest`, `ILIKE`).

**AR-14 — Em SQL nativo, duas obrigações:**
1. Filtrar soft delete manualmente (`AND x.deleted_at IS NULL`) — `@SQLRestriction` não se
   aplica (`TI-12`).
2. Aspear aliases camelCase (`AS "stickerId"`), senão o PostgreSQL os rebaixa para
   minúsculas e a projeção de interface do Spring quebra (`TI-14`).

**AR-15 — Projeções nativas são interfaces `View`** (ex.: `AvailableTradeStickerView`),
convertidas para DTO por `fromView`.

## Nomenclatura

| Tipo | Padrão | Exemplo |
|---|---|---|
| Entity | `<Nome>Entity` (singular ou plural, seguindo o vizinho) | `UserTradeOffersEntity` |
| Repository | `<Nome>Repository` | `UserTradeOffersRepository` |
| Service | `<Nome>Service` | `UserTradeOffersService` |
| Controller | `<Nome>Controller` | `UserTradeOffersController` |
| DTO | `<Ação|Nome>Dto` | `MakeOfferDto`, `TradeOfferDto` |
| Enum | `<Nome>Enum` | `TradeStatusEnum` |
| Exception | `<Problema>Exception` | `StickersNotOwnedException` |

Pacotes: `entities`, `repositories`, `services`, `controller` (singular — inconsistência
existente), `dto`, `enums`, `exceptions`, `config`.

## Ao adicionar um endpoint

1. É público? Se não, ele já está protegido — o filtro é deny-by-default (`AU-06`).
2. Recebe identidade via `@RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE)`, **nunca**
   por body/path (`AU-07`).
3. DTO de entrada é `record` com Bean Validation + `@Valid` no controller.
4. Lógica no service, `@Transactional` conforme `AR-04`.
5. Erro esperado = `ApiException` dedicada (`EH-03`).
6. Resposta é DTO (`AR-06`); se for lista, `PageResponseDto` (`AR-07`).
7. **Atualize a spec do domínio e [api/endpoints.md](../api/endpoints.md) no mesmo commit.**
