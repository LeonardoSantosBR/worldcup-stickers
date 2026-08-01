# Stickers — O Álbum

Prefixo de regra: `ST`

Código: [StickerEntity.java](../../src/main/java/com/leonardo/worldcup_stickers/entities/StickerEntity.java),
[enums/](../../src/main/java/com/leonardo/worldcup_stickers/enums/)

## Modelo

| Campo | Tipo | Regra |
|---|---|---|
| `id` | `Long` | Chave técnica. É o que trafega em todas as APIs de troca. |
| `number` | `Integer` | **Único.** O número impresso no álbum. É a ordem canônica de exibição. |
| `playerName` | `String` | Obrigatório. Base do filtro por nome na vitrine. |
| `country` | `String` | Obrigatório. |
| `group` | `StickerGroupEnum` | Obrigatório. Coluna `sticker_group` (`group` é palavra reservada em SQL). |
| `position` | `PositionEnum` | Obrigatório. |
| `rarity` | `RarityEnum` | Obrigatório, default `COMMON`. |
| `imageUrl` | `String` | Opcional. |

## Regras

**ST-01 — `number` é único e imutável na prática.** Identifica a posição no álbum. Duas
figurinhas nunca compartilham número.

**ST-02 — A ordenação canônica de figurinhas é por `number` crescente.** Vale para a
coleção do usuário (`Sort.by("sticker.number")`) e para a vitrine pública
(`ORDER BY s.number ASC, u.name ASC`). Qualquer listagem nova segue a mesma ordem, salvo
justificativa explícita.

**ST-03 — Figurinha é soft-deleted.** `@SQLDelete` + `@SQLRestriction` como em `users`.
Queries nativas precisam de `AND s.deleted_at IS NULL` explícito.

**ST-04 — O catálogo é somente leitura pela API.** Não existe endpoint de criação, edição
ou remoção de figurinha. A carga do álbum é feita fora da aplicação (seed/migration).
Se isso mudar, é área de `ADMIN` (ver `AU-10`).

## Enums

```
RarityEnum        COMMON | RARE | LEGENDARY
PositionEnum      GOALKEEPER | DEFENDER | MIDFIELDER | FORWARD | COACH | EMBLEM
StickerGroupEnum  A..L                    (12 grupos — formato de Copa com 48 seleções)
```

**ST-05 — Enums são persistidos como `STRING`, não ordinal.** `@Enumerated(EnumType.STRING)`
em todos. Reordenar constantes é seguro; **renomear não é** — exige migration.

**ST-06 — `PositionEnum` inclui não-jogadores.** `COACH` e `EMBLEM` são figurinhas válidas.
Nenhuma lógica deve assumir que toda figurinha é um jogador.

**ST-07 — `rarity` só afeta o sorteio do pacotinho.** Não altera valor de troca, não limita
quantidade, não restringe quem pode ter. Ver [packages.md](packages.md).
