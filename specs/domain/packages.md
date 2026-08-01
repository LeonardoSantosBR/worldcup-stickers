# Pacotinhos — Abertura e Sorteio

Prefixo de regra: `PK`

Código: [StickersService.java](../../src/main/java/com/leonardo/worldcup_stickers/services/StickersService.java)

Endpoint: `POST /stickers/open-package`

## Parâmetros do sorteio

Constantes em `StickersService`:

```java
PACKAGE_SIZE  = 7    // figurinhas por pacote
COMMON_CHANCE = 60   // %
RARE_CHANCE   = 30   // %
                     // LEGENDARY = resto = 10%
```

## Regras

**PK-01 — Um pacote contém exatamente 7 figurinhas.** Sempre 7, sem exceção, sem bônus.

**PK-02 — Cada figurinha é sorteada em duas etapas independentes.**
1. Sorteia-se a **raridade** pela distribuição de probabilidade.
2. Sorteia-se **uniformemente** uma figurinha dentro do pool daquela raridade.

Consequência: dentro de uma raridade, todas as figurinhas são igualmente prováveis. Uma
raridade com poucas figurinhas cadastradas torna cada uma delas individualmente mais comum.

**PK-03 — Distribuição de raridade por figurinha sorteada:**

| Raridade | Chance | Faixa do roll (`0..99`) |
|---|---|---|
| `COMMON` | 60% | `0–59` |
| `RARE` | 30% | `60–89` |
| `LEGENDARY` | 10% | `90–99` |

O sorteio é por figurinha, não por pacote — um pacote pode conter 0 ou várias lendárias.

**PK-04 — Repetidas são permitidas e esperadas.** Não há dedupe: o mesmo `stickerId` pode
sair duas vezes no mesmo pacote. Repetida incrementa `quantity` na coleção; não gera
linha nova. É o que alimenta o sistema de trocas.

**PK-05 — Não há custo, cooldown ou limite.** Um usuário autenticado pode abrir pacotes
indefinidamente. ⚠️ **Comportamento atual** — não é uma decisão de game design deliberada;
é ausência de regra. Ver [open-questions.md](../open-questions.md).

**PK-06 — Abrir pacote é atômico.** `@Transactional`: ou as 7 figurinhas entram na coleção,
ou nenhuma entra.

**PK-07 — Usuário inexistente → 404.** `UserNotFoundException`. Na prática só acontece se
o token for válido para um usuário já soft-deleted.

## Pré-condições operacionais

**PK-08 — Cada raridade precisa de ao menos uma figurinha cadastrada.** `drawStickerByRarity`
faz `pool.get(random.nextInt(pool.size()))`. Pool vazio → `IllegalArgumentException` /
`IndexOutOfBoundsException` → **500**, não um erro de domínio tratado.

> Se o álbum puder ser carregado parcialmente, essa é a primeira validação a adicionar.

## Retorno

**PK-09 — ⚠️ Comportamento atual: o endpoint devolve `List<StickerEntity>` cru.** É a única
rota que vaza entidade JPA no contrato HTTP; todo o resto usa DTO. Expõe `createdAt`,
`updatedAt`, `deletedAt`. Ver [open-questions.md](../open-questions.md).

**PK-10 — A lista devolvida preserva a ordem do sorteio e inclui duplicatas.** Se a mesma
figurinha saiu 2×, ela aparece 2× na resposta. O cliente consegue distinguir "3 novas +
4 repetidas" comparando com o estado anterior — a API não informa isso.
