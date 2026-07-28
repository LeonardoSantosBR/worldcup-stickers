# ⚽ World Cup Stickers API

<img width="1820" height="980" alt="Image" src="https://github.com/user-attachments/assets/969cca56-21af-4765-915a-a28f5a6900bd" />

> A **study project** built to deepen my skills as a **Java / Spring Boot** back-end developer.

A REST API that recreates the classic World Cup sticker album, centered on the one thing every collector remembers: **trading duplicate stickers with other people**. Each user opens packs, builds a collection, lists their spare stickers, and negotiates trades with other users.

> 🚧 **Status: work in progress.** This repository is a hands-on lab for practicing back-end fundamentals with the Spring ecosystem.

---

## 🎯 Why this project exists

I didn't want another CRUD tutorial. I wanted a domain with **real state transitions, concurrency hazards and consistency rules**, because that's where Spring Boot stops being annotations and starts being engineering.

Trading stickers turned out to be a great excuse to practice:

- **Layered architecture** — Controller → Service → Repository, with DTOs at the boundary
- **Dependency Injection / IoC** — constructor injection, no `new`, no field autowiring
- **JPA / Hibernate mapping** — relationships, collection tables, soft delete, lifecycle callbacks
- **Transactional integrity** — `@Transactional` around a multi-step trade that must fully succeed or fully roll back
- **Stateless JWT authentication** — a servlet filter that resolves the caller before the controller runs
- **Centralized error handling** — domain exceptions that map cleanly onto HTTP status codes
- **Custom queries** — JPQL and native PostgreSQL queries with projections and pagination
- **Invariant enforcement** — you can't offer what you don't own, and accepted trades invalidate offers they broke

The interesting problems here aren't "how do I save a row" — they're "what happens to every other pending offer when this one is accepted?"

---

## 🃏 How the trading works

The heart of the application is a three-step flow:

1. **Open a pack** — `POST /stickers/open-package` draws 7 random stickers weighted by rarity (60% `COMMON`, 30% `RARE`, 10% `LEGENDARY`) and adds them to the user's collection, incrementing the quantity for duplicates.
2. **List spares for trade** — a user publishes stickers into their **trade inventory**, which is what other users can browse via `GET /stickers/available-trades` (the listing excludes your own offers).
3. **Negotiate** — the **proposer** creates an offer naming a **receiver**, the stickers they *want*, and the stickers they *give*. The receiver accepts or rejects it.

### What happens on `accept`

Accepting is the most interesting operation in the codebase. Inside a single transaction it:

- Re-validates ownership on **both** sides — state may have changed since the offer was created
- Transfers one unit of each sticker in both directions, deleting the row when quantity hits zero
- Syncs both trade inventories, dropping stickers the user no longer owns
- Writes an immutable **audit log** entry for the status transition
- Scans every other `PENDING` offer involving either party and **auto-rejects the ones that just became impossible**, logging each one

That last step is the reason this project is worth building: a naive implementation leaves the database full of offers that can never be honored.

### Guard rails

| Rule | Result |
|---|---|
| Offering a trade to yourself | `400 Bad Request` |
| The same sticker both requested and offered | `400 Bad Request` |
| Offering a sticker you don't own | `400 Bad Request` |
| Requesting a sticker the receiver never listed | `400 Bad Request` |
| Responding to an offer that isn't yours | `404 Not Found` |
| Responding to an offer that's already resolved | `400 Bad Request` |

---

## 🛠️ Tech stack

| Category | Technology |
|---|---|
| Language | **Java 21** |
| Framework | **Spring Boot 4.1** (Web MVC, Data JPA, Validation) |
| Persistence | **JPA / Hibernate** |
| Database | **PostgreSQL** |
| Auth | **JWT** (JJWT 0.12.6) + **BCrypt** (Spring Security Crypto) |
| Boilerplate | **Lombok** |
| Build | **Maven** |

---

## 🏗️ Architecture and applied concepts

```
controller/    HTTP layer — thin, no business logic
services/      business rules, transactions, invariants
repositories/  Spring Data JPA interfaces + custom queries
entities/      JPA-mapped domain model
dto/           request/response records with Bean Validation
enums/         Rarity, Position, Group, Role, TradeStatus
exceptions/    domain exceptions carrying their own HTTP status
config/        JWT filter, global exception handler
```

- **Stateless JWT auth** — `JwtAuthFilter` (a `OncePerRequestFilter`) validates the `Bearer` token on every request and exposes the caller as a `user_id` request attribute, which controllers read with `@RequestAttribute`. Public routes (`POST /users`, `POST /auth/signin`) are whitelisted via `AntPathMatcher`.
- **Passwords are never stored in plain text** — hashed with BCrypt.
- **Global exception handling** — `@RestControllerAdvice` turns any `ApiException` into a consistent JSON body (`timestamp`, `status`, `error`, `message`). Each domain exception declares its own status, so services just `throw` and stay HTTP-agnostic.
- **Soft delete** — `User` and `Sticker` use `@SQLDelete` + `@SQLRestriction`, flagging `deleted_at` instead of removing rows.
- **Date auditing** — `createdAt` / `updatedAt` filled automatically via `@PrePersist` / `@PreUpdate`.
- **Pagination** — a generic `PageResponseDto<T>` wraps Spring's `Page`, converting to 1-based page numbers for the API.
- **Projections** — the global trade listing uses a native PostgreSQL query with `LATERAL unnest` over the inventory's sticker array, mapped into a Spring interface projection.

---

## 📦 Domain model

| Entity | Role |
|---|---|
| **User** | account, role (`USER` / `ADMIN`), credentials |
| **Sticker** | album sticker — number, player, country, group (`A`–`L`), position, rarity |
| **UserSticker** | a user's owned sticker and its quantity (duplicates) |
| **UserTradeInventory** | the stickers a user has published as available for trade |
| **UserTradeOffer** | an offer between proposer and receiver, with requested/offered sticker IDs and a status |
| **UserTradeOfferLog** | immutable audit trail of every status transition, who caused it, and an optional note |

**Trade status:** `PENDING` → `ACCEPTED` | `REJECTED` | `CANCELLED`

---

## 🔌 Endpoints

All routes except the two public ones require `Authorization: Bearer <token>`.

### Auth & users

| Method | Route | Description | Auth |
|---|---|---|---|
| `POST` | `/users` | Register a new user | Public |
| `POST` | `/auth/signin` | Log in, returns a JWT | Public |
| `GET` | `/users/my-profile` | Authenticated user's profile | 🔒 |
| `GET` | `/users/my-stickers?page=1&limit=20` | Paginated collection | 🔒 |

### Stickers & trade listing

| Method | Route | Description | Auth |
|---|---|---|---|
| `POST` | `/stickers/open-package` | Open a pack of 7 random stickers | 🔒 |
| `POST` | `/stickers/make-available-trade` | Publish stickers to your trade inventory | 🔒 |
| `GET` | `/stickers/available-trades?page=1&limit=20` | Browse everyone else's listed stickers | 🔒 |

### Trade offers

| Method | Route | Description | Auth |
|---|---|---|---|
| `POST` | `/trade-offers/make-offer` | Create an offer → `201 Created` | 🔒 |
| `POST` | `/trade-offers/{offerId}/accept` | Accept — executes the transfer | 🔒 |
| `POST` | `/trade-offers/{offerId}/reject` | Reject — nothing changes hands | 🔒 |

<details>
<summary>Example: creating an offer</summary>

```http
POST /trade-offers/make-offer
Authorization: Bearer <token>
Content-Type: application/json

{
  "receiverId": 42,
  "requestedStickerIds": [101, 102],
  "offeredStickerIds": [77],
  "message": "Two of yours for my legendary — deal?"
}
```

```json
{
  "id": 9,
  "proposerId": 7,
  "receiverId": 42,
  "requestedStickerIds": [101, 102],
  "offeredStickerIds": [77],
  "status": "PENDING",
  "message": "Two of yours for my legendary — deal?",
  "createdAt": "2026-07-27T16:40:11",
  "respondedAt": null
}
```

Accept and reject both take an optional body — `{ "note": "thanks!" }` — which is stored on the audit log entry.
</details>

---

## 🚀 Running locally

**Requirements:** Java 21, Maven, and a running PostgreSQL instance.

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-user>/worldcup-stickers.git
   cd worldcup-stickers
   ```

2. Create the database:
   ```sql
   CREATE DATABASE worldcup_stickers;
   ```

3. Set your database credentials in `src/main/resources/application.properties`, and export a JWT secret (a base64-encoded key of at least 32 bytes):
   ```bash
   export JWT_SECRET=<your-base64-secret>
   ```

4. Run it:
   ```bash
   ./mvnw spring-boot:run     # Linux / macOS
   .\mvnw.cmd spring-boot:run # Windows
   ```

The API starts on `http://localhost:8080`. Schema is generated by Hibernate (`ddl-auto=update`) — no migrations yet. You'll need to seed the `stickers` table before opening packs.

---

## 🗺️ Next steps

- [ ] `cancel` endpoint so the proposer can withdraw a pending offer (`CANCELLED` already exists in the enum)
- [ ] Inbox / outbox endpoints to list received and sent offers (repository queries are already in place)
- [ ] Database migrations (Flyway) to replace `ddl-auto=update`
- [ ] Sticker seeding
- [ ] Integration tests for the accept flow, especially the auto-rejection of conflicting offers

---

## 👤 Author

Built by **Leonardo Santos** as a study project to sharpen Java + Spring Boot skills.
