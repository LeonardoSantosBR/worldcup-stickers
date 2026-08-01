# WorldCup Stickers

API REST de álbum de figurinhas da Copa: colecionar, abrir pacotinhos e trocar figurinhas
com outros usuários. Java 21 + Spring Boot 3 + PostgreSQL. Projeto de aprendizado do
ecossistema Spring.

## Leia as specs antes de codar

**A regra de negócio vive em [specs/](specs/), não no código.** Antes de implementar ou
alterar qualquer feature, leia a spec do domínio afetado:

| Vou mexer em... | Leia |
|---|---|
| login, cadastro, JWT, rotas públicas | [specs/domain/users-and-auth.md](specs/domain/users-and-auth.md) |
| catálogo de figurinhas, raridade, enums | [specs/domain/stickers.md](specs/domain/stickers.md) |
| abrir pacote, sorteio | [specs/domain/packages.md](specs/domain/packages.md) |
| coleção, quantidade, repetidas, paginação | [specs/domain/collection.md](specs/domain/collection.md) |
| vitrine de troca | [specs/domain/trade-inventory.md](specs/domain/trade-inventory.md) |
| ofertas: criar, aceitar, recusar | [specs/domain/trade-offers.md](specs/domain/trade-offers.md) |
| qualquer código novo | [specs/conventions/architecture.md](specs/conventions/architecture.md) |
| qualquer erro/exception | [specs/conventions/error-handling.md](specs/conventions/error-handling.md) |

Comece por [specs/README.md](specs/README.md) e [specs/glossary.md](specs/glossary.md) se o
contexto do domínio ainda não estiver claro.

## Regras de trabalho

1. **Spec e código andam juntos.** Mudou comportamento? Atualize a spec no **mesmo commit**.
   Spec desatualizada é pior que spec nenhuma — a IA confia nela.
2. **Regra que não está na spec não existe.** Se a spec não cobre o caso, **pergunte** antes
   de decidir. Não invente regra de negócio.
3. **Divergência entre spec e código é bug.** O código é o que roda, mas a divergência deve
   ser reportada, não silenciosamente aceita.
4. **Lacunas conhecidas estão em [specs/open-questions.md](specs/open-questions.md).**
   Consulte antes de "consertar" algo — pode ser uma decisão pendente, não um descuido.
5. **Siga o padrão do vizinho.** Convenções em
   [specs/conventions/architecture.md](specs/conventions/architecture.md); o checklist de
   "adicionar um endpoint" está no fim do arquivo.

## Comandos

```bash
./mvnw compile              # compilar
./mvnw test                 # testes (⚠️ ainda não há cobertura de regra de negócio)
./mvnw spring-boot:run      # subir a API
```

## Convenções rápidas

- Identidade **sempre** vem do token, via `@RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE)`
  — nunca de body, path ou query.
- Toda rota é autenticada por padrão; exceções ficam em `JwtAuthFilter.PUBLIC_PATHS`.
- Erro esperado = `ApiException` dedicada, com o `HttpStatus` no construtor.
- Resposta HTTP é DTO (`record` + factory `fromEntity`), nunca entidade JPA.
- Listagem é `PageResponseDto`, `page` 1-based, `limit` clampado em `[1, 100]`.
- `@Transactional` no service — nunca no controller.
