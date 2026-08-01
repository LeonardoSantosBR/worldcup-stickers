# Usuários e Autenticação

Prefixo de regra: `AU`

Código: [UsersService.java](../../src/main/java/com/leonardo/worldcup_stickers/services/UsersService.java),
[AuthService.java](../../src/main/java/com/leonardo/worldcup_stickers/services/AuthService.java),
[JwtService.java](../../src/main/java/com/leonardo/worldcup_stickers/services/JwtService.java),
[JwtAuthFilter.java](../../src/main/java/com/leonardo/worldcup_stickers/config/JwtAuthFilter.java)

## Modelo

`UserEntity`: `id`, `name`, `email` (único), `password` (hash), `role`, timestamps, `deletedAt`.

`RoleEnum`: `USER` (padrão), `ADMIN`.

## Regras

**AU-01 — E-mail é a identidade de login.** Único na tabela `users`. Cadastro com e-mail
já existente falha com `EmailAlreadyExistsException` → **409 Conflict**.

**AU-02 — Senha nunca é persistida em texto puro.** `UsersService.create()` passa a senha
por `HashService.hash()` (BCrypt) antes do `save()`. Nenhum fluxo deve gravar senha crua.

**AU-03 — Todo usuário nasce com uma vitrine de troca vazia.** O cadastro cria, na mesma
transação, um `UserTradeInventoryEntity` vinculado ao usuário com `availableStickerIds = []`.
Isso garante que nenhum fluxo posterior precise lidar com "usuário sem vitrine".
Ver [trade-inventory.md](trade-inventory.md).

**AU-04 — Login devolve um JWT.** `POST /auth/signin` valida e-mail + senha e retorna
`{ "token": "<jwt>" }`. E-mail inexistente e senha errada produzem **exatamente a mesma
resposta** (`InvalidCredentialsException` → **401**) — não vaze qual dos dois falhou.

**AU-05 — O JWT carrega o `userId` no `sub`.** Claims: `sub` = id do usuário (string),
`email`, `iat`, `exp`. Assinado com HMAC-SHA a partir de `security.jwt.secret`
(base64). Expiração vem de `security.jwt.expiration-ms`.

**AU-06 — Toda rota é privada por padrão.** `JwtAuthFilter` intercepta tudo. A lista de
exceções é explícita e curta:

- `POST /auth/signin`
- `POST /users`

Qualquer endpoint novo é autenticado a menos que seja adicionado a `PUBLIC_PATHS`.

**AU-07 — A identidade nunca vem do corpo da requisição.** O filtro extrai o `userId` do
token e o injeta como request attribute `user_id`. Controllers o recebem via
`@RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) Long userId`.

> Consequência de segurança: **nunca** aceite `userId` como parâmetro de path, query ou
> body para identificar *quem está agindo*. Um `receiverId` no body é o **alvo** da ação,
> nunca o autor.

**AU-08 — Token ausente, malformado ou expirado → 401.** O filtro responde
`{"error":"Missing token"}` ou `{"error":"Invalid or expired token"}` e interrompe a
cadeia. Esse corpo **não** passa pelo `GlobalExceptionHandler` e portanto tem formato
diferente do resto dos erros da API — ver [error-handling.md](../conventions/error-handling.md).

**AU-09 — Usuário é removido por soft delete.** `@SQLDelete` grava `deleted_at`;
`@SQLRestriction("deleted_at IS NULL")` esconde o registro de todas as queries JPA.
Queries nativas precisam repetir `AND u.deleted_at IS NULL` manualmente.

**AU-10 — `role` existe mas ainda não autoriza nada.** Nenhum endpoint checa `ADMIN` hoje.
Ao introduzir área administrativa, a checagem deve ficar no filtro ou em um interceptor,
não espalhada nos services.

## Perfil

**AU-11 — Progresso do álbum é por figurinha distinta, não por unidade.**
`MyProfileDto.completePercentage` = `count(user_stickers do usuário) / count(stickers) * 100`,
arredondado para 2 casas. Repetidas **não** aumentam o percentual. Álbum vazio (`total = 0`)
resulta em `0`, não em divisão por zero.

**AU-12 — O perfil nunca expõe a senha.** `MyProfileDto` devolve apenas `email`, `name`,
`completePercentage`.
