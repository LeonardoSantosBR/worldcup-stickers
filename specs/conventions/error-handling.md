# Tratamento de Erros

Código: [ApiException.java](../../src/main/java/com/leonardo/worldcup_stickers/config/ApiException.java),
[GlobalExceptionHandler.java](../../src/main/java/com/leonardo/worldcup_stickers/config/GlobalExceptionHandler.java),
[exceptions/](../../src/main/java/com/leonardo/worldcup_stickers/exceptions/)

## Padrão

Toda exceção de domínio estende `ApiException`, que é uma `RuntimeException` carregando um
`HttpStatus`. O `GlobalExceptionHandler` (`@RestControllerAdvice`) converte qualquer
`ApiException` na resposta padrão:

```json
{
  "timestamp": "2026-08-01T13:20:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Stickers not owned by user: 12, 45"
}
```

**Regra:** o status HTTP é decidido **na exceção**, no construtor. Services e controllers
nunca montam `ResponseEntity` de erro à mão.

## Catálogo

| Exception | HTTP | Quando |
|---|---|---|
| `EmailAlreadyExistsException` | 409 | Cadastro com e-mail já usado (`AU-01`) |
| `InvalidCredentialsException` | 401 | E-mail inexistente **ou** senha errada (`AU-04`) |
| `UserNotFoundException` | 404 | `userId` não existe / soft-deleted |
| `TradeOfferNotFoundException` | 404 | Oferta inexistente **ou** o usuário não é o receiver (`TO-12`) |
| `StickersNotOwnedException` | 400 | Usuário não possui alguma das figurinhas (`TI-04`, `TO-07`, `TO-13`) |
| `StickersNotAvailableForTradeException` | 400 | Figurinha pedida não está na vitrine do receiver (`TO-08`) |
| `InvalidTradeOfferException` | 400 | Oferta a si mesmo, sobreposição de listas, oferta já finalizada (`TO-01`, `TO-03`, `TO-04`) |

## Convenções

**EH-01 — Mensagens listam os IDs problemáticos.** `"Stickers not owned by user: 12, 45"`.
O cliente precisa saber *o quê* falhou, não só *que* falhou.

**EH-02 — Não confirmar existência de recurso alheio.** Responder a oferta de outra pessoa
devolve **404**, não 403 (`TO-12`). Login errado não diz se o e-mail existe (`AU-04`).

**EH-03 — Nova exceção de domínio = nova classe.** Estenda `ApiException` com o status
correto no construtor, mensagem formatada nele, e adicione a linha no catálogo acima.
Não reaproveite `InvalidTradeOfferException` como lixeira genérica de erros de troca.

**EH-04 — Validação de entrada usa Bean Validation.** DTOs são `record` com
`@NotNull`, `@NotEmpty`, `@Positive`, `@Size`; controllers marcam `@Valid`. Violação vira
**400** com o formato padrão do Spring — que **não** é o formato acima.

**EH-05 — ⚠️ Duas respostas de erro fora do padrão.** Estão documentadas para não surpreender:

1. **`JwtAuthFilter`** responde `{"error":"Missing token"}` / `{"error":"Invalid or expired token"}`
   com 401. Roda antes do `@RestControllerAdvice`, então não é normalizado (`AU-08`).
2. **Bean Validation** (`MethodArgumentNotValidException`) usa o formato padrão do Spring.

Unificar os três formatos é um item aberto — ver [open-questions.md](../open-questions.md).

**EH-06 — Exceção não tratada → 500 com stacktrace do Spring.** Não há handler genérico
para `Exception`. Casos conhecidos que caem aqui: pool de raridade vazio (`PK-08`).
