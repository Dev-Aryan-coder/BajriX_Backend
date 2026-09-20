# BajriX - Multi-Seller Building Materials Marketplace

BajriX is a full-stack multi-seller marketplace for construction and home-building materials (cement, bricks, steel rebar, aggregates). The platform allows buyers to discover building materials and compare live seller offerings (rates, stock, MOQs) with an automatic **Best Price** guarantee, while giving material vendors an isolated portal to manage catalog listings with real-time optimistic locking safeguards.

This project was built to satisfy the **BajriX Full-Stack Engineering Challenge**. Below is the complete project documentation addressing the required six evaluation sections, architecture, and future scaling blueprints.

---

## 1. How to Run the Application

The project consists of two independent, clean repositories:
- **Backend**: Spring Boot 3/4 + Java 21 + MySQL
- **Frontend**: React 19 + Vite + Axios

### Prerequisites
- **Java 17+ or 21**
- **Node.js 18+ & npm**
- **MySQL 8.x** running locally on port `3306`

### Running the Backend
1. **Database Setup**: Ensure MySQL is running. The database `bajriX_db` will be auto-created on startup.
2. In the backend directory (`BajriX_Backend`):
   ```bash
   ./mvnw spring-boot:run
   ```
   *(Or in PowerShell: `.\mvnw.cmd spring-boot:run`)*
3. **Automated Seeding & BCrypt**: On startup, `DataInitializer` automatically seeds catalogue products (UltraTech Cement, Bricks, Steel, Sand) and multiple sellers with BCrypt-hashed credentials (`password123`) across `APPROVED`, `PENDING`, and `REJECTED` statuses.
4. **Running Test Suite**:
   ```bash
   ./mvnw test
   ```
   *(Executes 14 comprehensive tests verifying BCrypt auth, IDOR authorization barriers, optimistic concurrency, and duplicate protection).*

### Running the Frontend
1. In the frontend directory (`BajriX`):
   ```bash
   npm install
   npm run dev
   ```
2. Open your browser at `http://localhost:5173` (or `http://localhost:5174`).
3. Demo login accounts (all password: `password123`):
   - `shree@traders.com` (Status: **APPROVED** — full listing visibility)
   - `gupta@supply.com` (Status: **APPROVED** — multi-seller pricing)
   - `metro@infra.com` (Status: **PENDING** — private portal access, listings hidden from buyers)

---

## 2. Important Architectural Decisions

### Separation of Product and SellerListing (Core Domain Model)
- **Product** (Catalogue Entity): Represents *what* exists in the catalogue (title, category, brand, technical specifications, image). It contains no prices or inventory.
- **SellerListing** (Offer Junction Entity): Connects a verified `Seller` to a `Product` with commercial terms: `pricePerUnit`, `availableStock`, `minOrderQuantity`, and `isActive`. Multiple vendors list the same catalogue product without modifying each other's data.

### Cryptographic Wristband Session Token (IDOR Prevention)
- Instead of vulnerable client-supplied path variables (`/sellers/{sellerId}/...`), our backend implements a custom `SessionAuthFilter` extracting an `X-Session-Token` header.
- The filter validates the token, resolves the authenticated seller into a thread-safe `CurrentSellerContext`, and enforces ownership boundaries on every mutation.

### Database-Level Concurrency via Optimistic Locking
- High-volume building materials yards frequently update stock and pricing.
- We annotated `SellerListing` with `@Version private Integer version`.
- Every update checks `WHERE id = ? AND version = ?`. If a concurrent update occurred, Hibernate raises an `OptimisticLockException`, which `GlobalExceptionHandler` converts to an **HTTP 409 Conflict**. The frontend gracefully informs the user rather than silently overwriting data.

### Two-Tier Duplicate Prevention
- Application layer check via `existsBySellerIdAndProductId`.
- Database layer unique constraint:
  ```sql
  ALTER TABLE seller_listings ADD CONSTRAINT uq_seller_product UNIQUE (seller_id, product_id);
  ```

### N+1 Query Elimination & Seller Status Boundary
- `SellerListingRepo` uses explicit JPQL `JOIN FETCH` queries to load seller metadata alongside active listings in a single round-trip:
  ```sql
  SELECT l FROM SellerListing l JOIN FETCH l.seller 
  WHERE l.product.id = :productId AND l.isActive = true AND l.seller.status = 'APPROVED' 
  ORDER BY l.price ASC
  ```
- Unapproved (`PENDING`, `REJECTED`) sellers and inactive listings are filtered directly at the database engine level.

---

## 3. Assumptions Made

1. **Public Discovery**: Buyers do not need to register or log in to search products, filter categories, or compare vendor rates.
2. **Deactivation Over Hard Deletion**: "Stop Selling" toggles `isActive = false` to preserve audit history and avoid breaking historical order references.
3. **Automated Seed State**: The system automatically boots with ready-to-demo vendors across different approval tiers to simplify reviewer testing.
4. **Local Port Standardization**: Frontend communicates via a centralized `API_BASE` (`src/lib/apiBase.js`) targeting `http://localhost:8080/api/v1` with optional `.env` override.

---

## 4. Anything Intentionally Not Implemented

As advised by the challenge guidelines to maintain focus on multi-seller catalog and listing architecture:
- Payment processing and financial gateways (Stripe/Razorpay).
- End-to-end checkout and Request-for-Quote (RFQ) workflows.
- SMS / external third-party transactional email providers.
- Production container orchestration / Kubernetes clusters.

---

## 5. What We Would Improve With More Time

1. **Microservices Decomposition**:
   - Split the modular monolithic Spring Boot application into specialized microservices running on dedicated ports behind an API Gateway (e.g., Spring Cloud Gateway):
     - **Auth & Seller Service** (Port 8081): Registration, BCrypt auth, session issuance, merchant approval lifecycle.
     - **Catalogue & Search Service** (Port 8082): Master product catalogue, category taxonomy, media storage.
     - **Listing & Pricing Service** (Port 8083): Real-time inventory, live rate updates, optimistic concurrency handling.
2. **Interactive OpenAPI / Swagger Documentation**: Add SpringDoc OpenAPI 3 annotations for automated interactive API contract testing.
3. **Shared Route Guarding**: Refactor React seller route authentication from individual page checks into a centralized `<ProtectedRoute />` wrapper.

---

## 6. Reconsidering at Scale (Millions of Products & Listings)

If the platform expands to **1,000,000 products, 100,000 sellers, and 10,000,000 listings**:

### 1. Database CQRS & Multi-Instance Database Topology (Read/Write Split)
To eliminate database bottlenecks under extreme read/write contention, we would split the database topology into three instances:
1. **Primary Master Instance (Write DB)**: Handles all transactional writes (`INSERT`, `UPDATE`, `DELETE`), seller stock adjustments, and optimistic lock transactions.
2. **Read Replica Instance (Read DB)**: Read-only replica dedicated to high-volume buyer search queries, filtering, and product detail views via MySQL asynchronous replication.
3. **Analytics / Cold Storage Instance**: Dedicated replica for merchant reporting, inventory auditing, and BI without impacting live buyer browsing.

### 2. High-Performance In-Memory Caching with Redis
- Cache the most frequently queried catalogue products, category taxonomies, and computed **"Best Price"** summaries in **Redis**.
- Utilize Redis distributed caching with Cache-Aside pattern and TTLs (Time-to-Live) to serve 90%+ of buyer browsing requests in sub-millisecond time without querying the relational database.
- Utilize Redis Pub/Sub or Kafka events to invalidate product caches whenever a vendor updates their listing rate.

### 3. Dedicated Search Cluster (Elasticsearch / OpenSearch)
- Relational SQL `LIKE '%keyword%'` queries perform full table scans that degrade severely at millions of rows.
- Replace SQL search with an Elasticsearch/OpenSearch cluster supporting inverted indexes, tokenized full-text search, fuzzy spelling correction, and faceting by price range and geography.

### 4. Keyset / Cursor-Based Pagination
- Deep page offsets (`OFFSET 50000 LIMIT 20`) force the database to scan and discard thousands of rows.
- Transition to cursor-based pagination (`WHERE (created_at, id) < (:last_timestamp, :last_id) ORDER BY created_at DESC LIMIT 20`) for consistent $O(1)$ query performance at any depth.