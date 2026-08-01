# Specs — Regras de Negócio

Fonte da verdade da **regra de negócio** do WorldCup Stickers. Escrito para ser lido
por agentes de IA (Claude Code) **antes** de implementar ou alterar qualquer feature,
e por humanos que precisem entender o domínio sem ler o código inteiro.

## Como usar

**Antes de implementar** uma feature: leia a spec do domínio afetado. Se a spec não
cobre o caso, ela é incompleta — pergunte ao dono do projeto e **atualize a spec junto
com o código**, no mesmo commit.

**Ao mudar comportamento**: spec e código andam juntos. Uma spec desatualizada é pior
que spec nenhuma, porque a IA vai confiar nela.

**Regra de precedência**: se spec e código divergirem, o **código é o que roda**, mas a
divergência é um bug — ou o código está errado, ou a spec está desatualizada. Reporte.

## Índice

| Arquivo | Conteúdo |
|---|---|
| [glossary.md](glossary.md) | Linguagem ubíqua — termos do domínio e seu significado exato |
| [domain/users-and-auth.md](domain/users-and-auth.md) | Cadastro, login, JWT, autorização |
| [domain/stickers.md](domain/stickers.md) | O álbum: figurinhas, raridade, grupo, posição |
| [domain/packages.md](domain/packages.md) | Abertura de pacotinho (gacha) e sorteio por raridade |
| [domain/collection.md](domain/collection.md) | Coleção do usuário, quantidades, repetidas, progresso |
| [domain/trade-inventory.md](domain/trade-inventory.md) | Vitrine: figurinhas que o usuário disponibiliza para troca |
| [domain/trade-offers.md](domain/trade-offers.md) | Ofertas de troca: criar, aceitar, recusar, invalidação em cascata |
| [conventions/architecture.md](conventions/architecture.md) | Camadas, padrões de código, o que seguir ao escrever código novo |
| [conventions/error-handling.md](conventions/error-handling.md) | Exceptions do domínio → status HTTP |
| [api/endpoints.md](api/endpoints.md) | Contrato HTTP completo de todos os endpoints |
| [open-questions.md](open-questions.md) | Lacunas conhecidas e decisões ainda não tomadas |

## Convenções de escrita

- **Termos do domínio em inglês** (sticker, trade offer, proposer, receiver), texto em
  português. Os nomes devem casar exatamente com o código.
- Cada regra é uma afirmação verificável. "O proposer deve possuir tudo que oferece" —
  não "o sistema valida a oferta".
- Regras recebem **ID estável** (`TO-03`, `PK-01`) para poderem ser referenciadas em
  commits, testes e issues.
- Onde o comportamento atual é discutível, marque com **⚠️ Comportamento atual** em vez
  de descrevê-lo como se fosse intencional.
