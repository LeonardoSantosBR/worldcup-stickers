# ⚽ World Cup Stickers API

> **Projeto de estudo** desenvolvido para aprofundar meus conhecimentos como desenvolvedor **Java** com **Spring Boot**.

API REST que simula o clássico álbum de figurinhas da Copa do Mundo, com foco na funcionalidade que todo colecionador conhece: **trocar figurinhas repetidas com outros usuários**. A ideia é que cada usuário mantenha sua coleção e possa **propor, receber e negociar trocas** de figurinhas com os demais.

> 🚧 **Status: em desenvolvimento.** Este repositório está em construção e serve como laboratório prático para estudar boas práticas de back-end com o ecossistema Spring.

---

## 🎯 Objetivo do projeto

Este projeto foi criado como forma de **me aprimorar como desenvolvedor Java**, colocando em prática conceitos essenciais do desenvolvimento back-end moderno:

- Arquitetura em camadas (Controller → Service → Repository)
- Injeção de dependências e Inversão de Controle (IoC) do Spring
- Mapeamento objeto-relacional com JPA/Hibernate
- Autenticação stateless com JWT
- Tratamento centralizado de erros
- Modelagem de domínio com relacionamentos entre entidades

---

## 🃏 Sobre a troca de figurinhas

O coração da aplicação é o sistema de **compartilhamento e troca de figurinhas entre usuários**:

- Cada usuário possui sua própria coleção de figurinhas.
- Um usuário (**requester**) pode abrir uma **solicitação de troca** com outro usuário (**responder**).
- Uma solicitação de troca reúne vários **itens** — figurinhas oferecidas de um lado e desejadas do outro.
- Cada troca tem um **status** (ex.: `PENDING`), permitindo acompanhar a negociação do início ao fim.
- Figurinhas têm atributos como número, jogador, país, grupo, posição e **raridade** (`COMMON`, etc.), tornando a troca mais estratégica.

---

## 🛠️ Tecnologias e ferramentas

| Categoria | Tecnologia |
|---|---|
| Linguagem | **Java 21** |
| Framework | **Spring Boot 4.1** (Spring Web MVC, Spring Data JPA, Validation) |
| Persistência | **JPA / Hibernate** |
| Banco de dados | **PostgreSQL** |
| Autenticação | **JWT** (JJWT 0.12.6) + **BCrypt** (Spring Security Crypto) |
| Produtividade | **Lombok** |
| Build | **Maven** |

---

## 🏗️ Arquitetura e conceitos aplicados

- **Camadas bem definidas:** `controller`, `services`, `repositories`, `entities`, `dto`, `enums`, `exceptions` e `config`.
- **Autenticação JWT stateless:** login gera um token assinado; um *filtro* (`OncePerRequestFilter`) intercepta cada request, valida o token e disponibiliza o usuário autenticado. Rotas públicas (cadastro e login) são liberadas via whitelist.
- **Senhas com hash:** as senhas nunca são armazenadas em texto puro — utilizam **BCrypt**.
- **Tratamento global de exceções:** um `@RestControllerAdvice` traduz exceções de domínio para respostas HTTP consistentes (ex.: e-mail duplicado → `409 Conflict`).
- **Soft delete:** entidades como `User` e `Sticker` usam `@SQLDelete` + `@SQLRestriction`, marcando registros como deletados (`deleted_at`) em vez de removê-los fisicamente.
- **Auditoria de datas:** campos `createdAt` / `updatedAt` preenchidos automaticamente via `@PrePersist` / `@PreUpdate`.

---

## 📦 Modelo de domínio

- **User** — usuário do sistema, com papel (`Role`) e credenciais.
- **Sticker** — figurinha do álbum (número, jogador, país, grupo, posição, raridade).
- **UserSticker** — vínculo entre usuário e as figurinhas que ele possui (a coleção).
- **TradeRequest** — solicitação de troca entre dois usuários, com status e lista de itens.
- **TradeItem** — cada figurinha envolvida em uma troca, com sua direção (oferecida/desejada).

---

## 🔌 Endpoints (em evolução)

| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| `POST` | `/users` | Cadastro de usuário | Pública |
| `POST` | `/auth/signin` | Login e emissão de token JWT | Pública |

> Novos endpoints (coleção, solicitações de troca, aceitar/recusar trocas) estão sendo adicionados.

---

## 🚀 Como executar localmente

**Pré-requisitos:** Java 21, Maven e uma instância do PostgreSQL.

1. Clone o repositório:
   ```bash
   git clone https://github.com/<seu-usuario>/worldcup-stickers.git
   cd worldcup-stickers
   ```

2. Configure as credenciais do banco e o segredo JWT em `src/main/resources/application.properties`.

3. Rode a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```

A API sobe por padrão em `http://localhost:8080`.

---

## 👤 Autor

Desenvolvido por **Leonardo Santos** como projeto de estudo e aprimoramento em Java + Spring Boot.
