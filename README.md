# BajriX - Backend REST API

Backend service for the **BajriX Multi-Seller Product Marketplace** — a platform connecting buyers and multiple local sellers for construction and building materials (cement, bricks, steel, aggregates).

Built with **Java 21**, **Spring Boot 4.x / 3.x**, and **MySQL 8.x**.

---

## 1. How to Run the Application

### Prerequisites
- **Java 17+** (or Java 21)
- **MySQL 8.x** running locally on port `3306`
- **Maven** (or use the included `./mvnw` wrapper)

### Setup & Launch
1. **Database Setup:**
   Ensure MySQL is running. The application is configured to automatically create the database `bajriX_db` on startup if it does not already exist:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/bajriX_db?createDatabaseIfNotExist=true
   spring.datasource.username=root
   spring.datasource.password=7474
   ```
   *(Update credentials in `src/main/resources/application.properties` if your local MySQL configuration differs).*

2. **Run via Eclipse / IDE:**
   - Import the project as an **Existing Maven Project**.
   - Right-click `BajriXApplication.java` -> **Run As** -> **Spring Boot App**.

3. **Run via Terminal:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Verify Boot:**
   The service starts on `http://localhost:8080`.
   On startup, `DataInitializer` automatically seeds sample catalogue products and multi-seller listings (UltraTech Cement, Bricks, Steel, Sand) across `APPROVED`, `PENDING`, and `REJECTED` sellers.

---

## 2. Important Architectural Decisions

- **Separation of Product and Seller Listing:**
  - `Product`: Centralized catalogue entry representing *what is being sold* (specifications, category, name).
  - `SellerListing`: Represents *who is selling it and on what terms* (price, available stock, minimum order quantity, active status). Multiple sellers can independently list the same product without modifying the catalogue.
- **Strict Seller Isolation & Ownership:**
  - Every update, deactivation, or listing read is scoped to the seller ID (`/api/v1/sellers/{sellerId}/listings`). A seller cannot modify or tamper with another seller's listing.
- **Buyer Visibility Filtering:**
  - Buyer-facing endpoints (`/api/v1/products`) strictly filter and return listings where `seller.status = APPROVED` and `listing.isActive = true`. Listings from `PENDING` or `REJECTED` sellers are never exposed to buyers.
- **Concurrency & Optimistic Locking:**
  - `SellerListing` contains a JPA `@Version` column. When concurrent updates happen to the same listing, conflicts are caught and surfaced with a `409 Conflict` status, preventing silent overwrites.
- **Layered MVC Architecture:**
  - Clean separation into `Controller`, `Service`, `Repo`, `Entity`, and `dto` packages for clarity, maintainability, and ease of explaining during code reviews.

---

## 3. Assumptions Made

- **Buyer Browsing is Public:** Buyers do not need an account or login to search products and compare seller prices.
- **Mocked Seller Session:** Authentication uses a simplified session mechanism (`/api/v1/auth/*` and seller ID scoping) rather than full JWT/OAuth per project guidelines.
- **Demo Password Reset:** Password reset link tokens are generated, stored with expiry, and returned/logged in demo mode without requiring real external SMTP delivery.
- **Deactivation over Deletion:** "Stop selling" marks a listing as `isActive = false` to maintain historical consistency rather than performing hard deletions.

---

## 4. Deliberately Not Implemented

- Payment gateways and checkout / order processing.
- Request-for-Quote (RFQ) workflows.
- Production-grade authentication (OAuth2 / multi-factor / email verification).
- Cloud deployment and container orchestration.

---

## 5. Improvements with More Time

- **Automated Test Suite:** Add unit tests for service-layer validation and integration tests using Testcontainers against real MySQL.
- **Swagger / OpenAPI Documentation:** Add SpringDoc OpenAPI annotations for interactive API exploration.
- **Rate Limiting:** Protect public search endpoints against scraping with bucket4j or Spring Cloud Gateway.

---

## 6. Reconsidering at Scale (1M+ Products, 10M+ Listings)

- **Search Engine:** Replace SQL keyword filtering with a dedicated search cluster (Elasticsearch / OpenSearch) for full-text indexing, fuzzy matching, and faceting.
- **Read Replicas & Caching:** Cache popular products and their lowest-price summaries in Redis to offload read traffic from the primary database.
- **Pagination Strategy:** Switch from offset-based pagination to keyset / cursor pagination to eliminate performance degradation on deep page offsets.