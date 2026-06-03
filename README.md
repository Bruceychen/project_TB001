# project_TB001

Spring Boot REST API for managing Users, Products, ProductCategory, and Orders.
Java 21, Spring Boot 3.5.14, PostgreSQL 16.

---

## Quick start

### 1. Start PostgreSQL + pgAdmin

~~~bash
docker compose -f docker-compose.yml -f docker-compose.tools.yml up -d
~~~

Wait until both containers report healthy ('docker ps').

### 2. Run the application

~~~bash
./mvnw spring-boot:run
~~~

Default Spring profile is 'postgres' (see 'application.yaml'). The app starts on 'http://localhost:8080'.

**Database credentials.** 'application-postgres.yaml' reads 
'POSTGRES_HOST' 
'POSTGRES_PORT'  
'POSTGRES_DB' 
'POSTGRES_USER' 
'POSTGRES_PASSWORD' 
from the environment, with safe local-dev defaults if the variables are not set. The app boots out-of-the-box against the bundled docker-compose Postgres without any env work; to override:

| Run mode | How to supply env |
|---|---|
| Terminal | 'set -a; source .env; set +a; ./mvnw spring-boot:run' |
| IntelliJ (manual) | Run Configuration → Environment variables → paste 'POSTGRES_USER=...;POSTGRES_PASSWORD=...;...' |
| IntelliJ (EnvFile plugin) | Install plugin → Run Configuration → "Enable EnvFile" → select '.env' |
| 'docker compose' | Picks up '.env' automatically (already wired) |

Real credentials live in '.env' (gitignored). '.env.example' documents the variables.

### 3. Try the API

Open 'src/main/resources/api-test.http' in IntelliJ HTTP Client (or convert to curl) and run requests in order.

> **Optional — mock data.** 'src/main/resources/mockData.sql' ships with 10 FK-safe rows per table and IDENTITY-sequence resets. Apply it after the Spring Boot app has booted at least once (so Hibernate 'ddl-auto: update' has created the tables) via pgAdmin, 'psql', or:
> ~~~bash
> docker exec -i tb001_postgres psql -U tb001 -d tb001 < src/main/resources/mockData.sql
> ~~~

---

## API endpoints

All endpoints align with the assignment spec.

| # | Method | Path | Purpose |
|---|---|---|---|
| 1 | GET | '/api/product/{product_id}' | Get product (with category + tax_rate) |
| 2 | POST | '/api/order' | Create order |
| 3 | PATCH | '/api/order/{order_id}' | Partial update (productId / orderAmount) |
| 4 | DELETE | '/api/order/{order_id}' | Delete order |
| 5 | DELETE | '/api/user/{userId}' | Delete user (cascades to user's orders) |
| 6 | GET | '/api/order/{userId}' | List user's orders, each with computed totalCost |

### 'totalCost' formula

~~~
totalCost = order_amount × unit_price × (1 + tax_rate)
~~~

Computed at response time inside 'OrderService.toResponse()'. Not persisted on the Order row, to avoid stale-cache and update-anomaly issues if tax_rate or unit_price ever changes (see Order entity Javadoc).

### Supporting endpoints (not in spec, but kept for testing convenience)

| Method | Path | Purpose |
|---|---|---|
| GET / POST | '/api/user' | List / create users |
| GET | '/api/user/{id}' | Get one user |
| GET | '/api/product' | List products |
| GET / GET | '/api/category', '/api/category/{id}' | List / get categories |

These are not required by the spec — they exist so a tester can populate, inspect, and reset state via the REST API without dropping into SQL.

---

## Architecture

~~~
src/main/java/com/bruceychen/tb001/
├── ProjectTb001Application.java     (entry point, @EnableScheduling)
├── entity/                          (User, ProductCategory, Product, Order)
├── repository/                      (4 JpaRepository interfaces)
├── dto/                             (OrderResponse, OrderCreateRequest, OrderPatchRequest, UserResponse, UserCreateRequest)
├── service/                         (OrderService, UserService, PurchaseDataExportJob)
├── controller/                      (OrderController, UserController, ProductController, ProductCategoryController, GlobalExceptionHandler)
├── exception/                       (ApiException base, ResourceNotFoundException)
├── event/                           (OrderCreatedEvent — see Design Q1)
└── listener/                        (OrderNotificationListener — consumes OrderCreatedEvent)
~~~

### Spring profile layout

- 'application.yaml' — common settings, default active profile = 'postgres'
- 'application-postgres.yaml' — PostgreSQL datasource (production-style run)
- 'application-h2.yaml' — H2 in-memory (used by integration tests via '@ActiveProfiles("h2")')

To run with a different profile:

~~~bash
SPRING_PROFILES_ACTIVE=h2 ./mvnw spring-boot:run    # rare; mostly for local sandbox
~~~

---

## Tests

~~~bash
./mvnw test
~~~

Three layers:

- **Unit tests** ('@ExtendWith(MockitoExtension.class)') — 'OrderServiceTest', 'UserServiceTest'. Pure logic, no Spring context. Fast.
- **Controller slice tests** ('@WebMvcTest') — 'OrderControllerTest', 'UserControllerTest'. Load only MVC layer, mock the service.
- **JPA integration test** ('@DataJpaTest' + '@ActiveProfiles("h2")') — 'UserCascadeDeleteIntegrationTest'. Boots a real persistence context against H2 to verify JPA cascade behaviour for spec #5.

H2 is used for the integration test layer so the suite runs on any machine without Docker. For production-grade integration testing against real PostgreSQL, the codebase has Testcontainers dependencies on the classpath ready to use; see Design Q in 'design-answers.md'.

---

## Design decisions (where I made a judgement call)

| Decision | What I chose | Why |
|---|---|---|
| 'Order' table name | 'orders' | 'order' is a SQL reserved word; using the plural avoids Hibernate having to quote it on every query (Order entity Javadoc notes this). |
| PATCH allowed fields | 'productId', 'orderAmount' | 'userId' deliberately not patchable — an order belongs to who placed it; allowing reassignment opens an audit hole. |
| GET '/api/order/{userId}' on missing user | Returns '404 Not Found' | More informative for API consumer than an empty '200' list. The user-doesn't-exist case is distinguishable from user-exists-but-has-no-orders. |
| 'totalCost' rounding | 'HALF_UP' to 2 decimal places | Standard for currency; not specified in spec. |
| Embedded DB for integration tests | H2 with 'MODE=PostgreSQL' | Portability over fidelity for the assignment; Testcontainers documented as the production-grade upgrade path. |
| Cascade-delete implementation | JPA 'cascade = ALL, orphanRemoval = true' on 'User.orders' | Single source of truth for the relationship; verified by 'UserCascadeDeleteIntegrationTest'. |

Anything else open is captured in 'assumptions.md'.

---

## Bonus implementations (related to Design questions)

The six design questions can be answered in essay form (see 'design-answers.md'); a few of them I also implemented to demonstrate the pattern:

- **Q1 (notification center):** 'OrderCreatedEvent' + 'OrderNotificationListener' using Spring's 'ApplicationEventPublisher'. In a real system the listener would push to a notification microservice via broker; here it logs.
- **Q3 (scalability of totalCost calculation):** 'OrderRepository.findByUser_UserId' uses '@EntityGraph' to fetch 'product' + 'product.category' in one query, avoiding N+1.
- **Q5 (custom exceptions):** 'ApiException' abstract base with 'httpStatus' + 'errorCode'. 'ResourceNotFoundException' is the first subclass; 'GlobalExceptionHandler' translates the whole hierarchy uniformly. Adding a new exception type is one new class + zero handler changes.
- **Q6 (30-minute scheduled job):** 'PurchaseDataExportJob' with '@Scheduled(cron = "0 */30 * * * *")'. Application class enables scheduling with '@EnableScheduling'. Logs only — would POST a batch in production.

---
