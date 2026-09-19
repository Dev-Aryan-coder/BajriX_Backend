# AI Usage Document

## Tools Used
- **AI Coding Assistant (Antigravity IDE / Gemini)**

## Purpose & Scope of Use
AI was used as an assistive pair-programming companion for:
- Accelerating initial boilerplate generation for DTOs and standard JPA entities.
- Validating Hibernate mapping syntax and constraint configurations.
- Assisting in drafting comprehensive markdown documentation and API route outlines.

## Key Developer Decisions & Corrections
1. **Architecture & Package Structure:**
   - The AI initially proposed a feature-based folder layout (`com.bajrix.product`, `com.bajrix.seller`, etc.).
   - I restructured and guided the codebase into a clean, traditional **Layered MVC architecture** (`Controller`, `Service`, `Repo`, `Entity`, `dto`, `config`) to align with standard Spring Boot conventions and ensure maintainability.

2. **Scoping & Avoiding Over-Engineering:**
   - Rejected complex security dependencies (such as full Spring Security JWT filters) suggested during initial planning, keeping authentication lightweight and mocked to stay strictly within the challenge scope.

3. **Data Integrity & Seeder Logic:**
   - Refined the `DataInitializer` to explicitly test edge cases called out in the brief: ensuring pending/rejected sellers' listings exist in the database but are properly excluded from buyer-facing comparison queries.