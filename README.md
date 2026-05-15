# Smart Investment & Asset Intelligence Platform

> A production-ready investment portfolio tracker built with Spring Boot 3, clean architecture principles, and real market data integration.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

---

## 📌 About the Project

Smart Investment Platform allows users to track their investment portfolio across multiple asset classes — **LEGO collectibles**, **gold**, and **stocks**. The system automatically fetches real-time market prices, calculates portfolio performance, and sends notifications when asset values change significantly.

This project was built as a portfolio piece to demonstrate production-level backend development skills, clean architecture, and real-world integrations.

---

## 🚀 Live Demo

```
API Documentation (Swagger UI):
http://localhost:8080/swagger-ui.html

Sample credentials:
POST /api/v1/auth/register → create account
POST /api/v1/auth/login    → get JWT token
```

---

## 🏗️ Architecture

The project follows **Hexagonal Architecture (Ports & Adapters)** with domain-driven package structure:

```
smart-investment-platform/
├── user/                    # Authentication & user management
│   ├── domain/              # User entity, repository port
│   ├── application/         # AuthService, UserService, DTOs
│   ├── infrastructure/      # JWT, Spring Security, JPA adapter
│   └── web/                 # REST controllers
│
├── asset/                   # Portfolio management
│   ├── domain/              # Asset entity, valuation logic
│   ├── application/         # AssetService, PortfolioService
│   ├── infrastructure/      # JPA adapter
│   └── web/                 # REST controllers
│
├── market/                  # Market data integration
│   ├── domain/              # MarketPrice entity, PriceProvider port
│   ├── application/         # MarketService, PriceSyncScheduler
│   ├── infrastructure/      # GoldApiClient, StooqClient adapters
│   └── web/                 # REST controllers
│
├── notification/            # Event-driven notifications
│   ├── domain/              # Notification entity
│   ├── application/         # NotificationListener, NotificationService
│   └── web/                 # REST controllers
│
└── shared/                  # Cross-cutting concerns
    ├── event/               # Domain events
    ├── exception/           # Global exception handling
    ├── audit/               # JPA auditing
    └── config/              # OpenAPI, RestClient config
```

### Key Architectural Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| Architecture | Hexagonal (Ports & Adapters) | Domain isolated from infrastructure, easy to swap implementations |
| Package structure | Domain-driven (by feature) | Better cohesion, easier to extract microservices later |
| Market data | Strategy Pattern | Multiple providers (Gold API, Stooq) without changing core logic |
| Notifications | Spring Application Events | Decoupled modules, ready for RabbitMQ/Kafka extraction |
| Database schema | Flyway migrations | Version-controlled, reproducible schema |

---

## ✨ Features

### 🔐 Authentication & Security
- JWT-based stateless authentication
- Role-based access control (USER / ADMIN)
- BCrypt password hashing
- Spring Security 6 with custom filter chain

### 📊 Portfolio Management
- CRUD operations for investment assets (LEGO, Gold, Stocks)
- Real-time profit/loss calculation per asset
- Portfolio summary with total value breakdown by asset type
- Gain/loss percentage tracking

### 🌍 Market Data Integration
- **Gold prices** — via [GoldAPI.io](https://www.goldapi.io) (real-time PLN prices)
- **Stock prices** — via [Stooq](https://stooq.com) (Polish & US markets, no API key required)
- Automatic price sync scheduler (twice daily for gold, weekdays for stocks)
- Price history stored in database

### 🔔 Event-Driven Notifications
- Domain events published on asset value change
- Async notification processing
- Configurable change threshold (default: 0.1%)
- Unread/read notification management

### 🛠️ Developer Experience
- OpenAPI 3.0 documentation (Swagger UI)
- Global exception handling with consistent error format
- Flyway database migrations
- Docker Compose for local development

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.4 |
| Security | Spring Security 6, JWT (jjwt 0.12.6) |
| Persistence | Spring Data JPA, Hibernate 6 |
| Database | PostgreSQL 16 |
| Migrations | Flyway 10 |
| Mapping | MapStruct 1.6.3 |
| Documentation | SpringDoc OpenAPI 3 (Swagger UI) |
| Build | Maven |
| Containerization | Docker, Docker Compose |
| Testing | JUnit 5, Mockito, AssertJ |

---

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven 3.8+

### 1. Clone the repository
```bash
git clone https://github.com/your-username/smart-investment-platform.git
cd smart-investment-platform
```

### 2. Set environment variables
```bash
export GOLD_API_KEY=your_goldapi_key
```
Get your free API key at [goldapi.io](https://www.goldapi.io) (100 requests/month free).

### 3. Start the database
```bash
docker compose up -d
```

### 4. Run the application
```bash
./mvnw spring-boot:run
```

### 5. Open Swagger UI
```
http://localhost:8080/swagger-ui.html
```

---

## 📡 API Overview

### Authentication
```
POST /api/v1/auth/register    Register new user
POST /api/v1/auth/login       Login and get JWT token
```

### Portfolio
```
GET    /api/v1/assets             Get all assets
POST   /api/v1/assets             Add new asset
GET    /api/v1/assets/{id}        Get asset by ID
PUT    /api/v1/assets/{id}        Update asset
DELETE /api/v1/assets/{id}        Delete asset
GET    /api/v1/assets/summary     Portfolio summary with totals
```

### Market Data
```
GET  /api/v1/market/price/{symbol}         Fetch current price
GET  /api/v1/market/price/{symbol}/latest  Get latest stored price
POST /api/v1/market/sync/gold              Trigger gold sync (ADMIN)
POST /api/v1/market/sync/stocks            Trigger stocks sync (ADMIN)
```

### Notifications
```
GET /api/v1/notifications              All notifications
GET /api/v1/notifications/unread       Unread notifications
GET /api/v1/notifications/unread/count Unread count
PUT /api/v1/notifications/read-all     Mark all as read
```

---

## 🧠 Design Patterns Used

### Strategy Pattern — Market Price Providers
```java
// Port (interface) in domain layer
public interface PriceProvider {
    Optional<MarketPrice> fetchCurrentPrice(String symbol);
    boolean supports(String symbol);
}

// Adapters — adding new provider requires zero changes to business logic
@Component public class GoldApiClient implements PriceProvider { ... }
@Component public class StooqClient implements PriceProvider { ... }
// Future: @Component public class BricklinkClient implements PriceProvider { ... }
```

### Domain Events — Decoupled Notifications
```java
// Publisher (market module) has no knowledge of notifications
eventPublisher.publishEvent(new AssetValueChangedEvent(...));

// Listener (notification module) reacts independently
@EventListener
@Async
public void onAssetValueChanged(AssetValueChangedEvent event) { ... }
```

### Guard Pattern — Ownership Validation
```java
// Centralized authorization check — no duplication across services
public Asset getAssetForUser(UUID assetId, UUID userId) {
    Asset asset = assetRepository.findById(assetId)
        .orElseThrow(() -> new ResourceNotFoundException(...));
    if (!asset.getUser().getId().equals(userId))
        throw new BusinessException("Access denied", HttpStatus.FORBIDDEN);
    return asset;
}
```

---

## 🗺️ Roadmap

### Phase 1 — Core Backend ✅
- [x] JWT Authentication & Authorization
- [x] Asset Portfolio CRUD
- [x] Real-time Gold prices (GoldAPI)
- [x] Stock prices (Stooq — Polish & US markets)
- [x] Automated price sync scheduler
- [x] Domain Events & Notifications

### Phase 2 — Completed ✅
- [x] Unit tests (67 tests)
- [x] Integration tests (17 tests, Testcontainers)
- [x] LEGO manual price update endpoint (`PATCH /api/v1/assets/{id}/value`)
- [x] `/price/{symbol}/latest` endpoint
- [x] `AccessDeniedException` handling (403 vs 500)

### Phase 3 — In Progress 🔄
- [ ] Angular frontend

### Phase 3 — Planned 📋
- [ ] LEGO price tracking via Bricklink scraping
- [ ] WebSocket real-time price updates
- [ ] Price history charts
- [ ] Docker multi-stage build
- [ ] GCP Cloud Run deployment
- [ ] RabbitMQ — extract notification to separate microservice
- [ ] Mobile push notifications (Firebase)

---

## 🧪 Testing

The project has **84 tests** — all passing.

| Type | Count | Description |
|------|-------|-------------|
| Unit tests | 67 | Service, domain, and infrastructure layer tests with Mockito |
| Integration tests | 17 | Full HTTP stack tests with real PostgreSQL via Testcontainers |

```bash
# Run all tests (requires Docker Desktop running)
./mvnw test

# Run specific test class
./mvnw test -Dtest="AuthServiceTest"

# Run only integration tests
./mvnw test -Dtest="*IntegrationTest"
```

### Integration Test Setup
Integration tests use **Testcontainers** to spin up a real PostgreSQL 16 instance per test class. Spring context is reused where possible via `@DirtiesContext`.

```
AssetControllerIntegrationTest     — 8 tests (CRUD, auth, ownership)
MarketControllerIntegrationTest    — 4 tests (prices, sync, auth)
NotificationControllerIntegrationTest — 5 tests (list, count, mark-read)
AuthControllerIntegrationTest      — 4 tests (register, login, JWT)
```

---

## 📁 Database Schema

```sql
users           — user accounts and roles
assets          — investment portfolio items
market_prices   — price history from external APIs
notifications   — user notifications from domain events
```

Flyway manages all schema migrations versioned under `src/main/resources/db/migration/`.

---

## 👨‍💻 Author

**Bartłomiej Gajewski**
- GitHub: [@Sp33dster](https://github.com/your-username)
- LinkedIn: [linkedin.com/in/bartłomiej-gajewski-10aa66167](https://linkedin.com/in/your-profile)
- Email: bartlomiejgajewski90@gmail.com

