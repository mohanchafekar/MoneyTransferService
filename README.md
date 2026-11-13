# Money Transfer Service

A robust, contract-first **Money Transfer Microservice** built with **Spring Boot 3.4+** and **Java 25**. This service facilitates secure account-to-account money transfers, supports cross-currency conversions using the external Frankfurter API, and maintains an immutable audit trail of all transactions.
 
This application has the APIs to create users, create account for the users, transfer money from one account to another and convert to different currency if required.

* **GitHub Repository:** [https://github.com/mohanchafekar/MoneyTransferService.git](https://github.com/mohanchafekar/MoneyTransferService.git)

---

## Architecture & Tech Stack
* **Framework:** Spring Boot 3.4+
* **Language:** Java 25
* **Database:** H2 In-Memory Database (`transferdb`) with JPA/Hibernate
* **API Documentation:** OpenAPI / Swagger UI
* **External Integration:** Frankfurter API (for live foreign exchange rates)
* **Testing:** JUnit 5, Mockito, MockMvc, and Spring Boot Test (supporting fully offline build execution)

---

## Getting Started (For Reviewers)

Follow these steps to clone, build, and run the application locally.

### Prerequisites
* **Java 25** installed and configured in your path (or compatible JDK 17+ environment).
* **Maven** (or use the included `./mvnw` wrapper).

### 1. Clone the Repository
Open your terminal and run:
```bash
git clone https://github.com/mohanchafekar/MoneyTransferService.git
```

### 2. Build and Run Tests
To verify the build and run all unit and integration tests successfully (fully isolated and offline-capable):
```bash
mvn clean install
```

### 3. Run the Application
Start the Spring Boot server locally:

The application will start on `http://localhost:8080`.

---

## Developer Consoles & Interactive Tools

Once the application is running, you can access the following built-in developer tools:

### 1. Swagger UI (API Documentation & Testing)
You can inspect all API contracts, schemas, and execute test requests directly through the browser.
* **URL:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
* **Instructions:** Open the link in any browser to explore the interactive OpenAPI documentation and test out transfer endpoints.

### 2. H2 Database Console
The app uses an embedded H2 database configured for running and debugging.
* **URL:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
* **Instructions to Log In:**
  1. Open the link above in your browser.
  2. Set **JDBC URL** to: `jdbc:h2:mem:transferdb`
  3. Set **User Name** to: `sa`
  4. Leave **Password** completely blank (`""`).
  5. Click **Connect**.

---

## Design Decisions & Known Omissions

For evaluation and transparency, please note the following architectural scope limitations and items intentionally omitted from the current proof-of-concept build:

1. **Security & Password Encryption:** 
   * Authentication, authorization, and password/credential hashing mechanisms (such as Spring Security) are not implemented in this version. All endpoints are currently public for ease of evaluation.
2. **Business Limits Configuration:** 
   * Parameter thresholds—such as daily transfer limits, maximum single-transfer limits, and maximum cross-currency conversion thresholds—are handled via basic validation or hardcoded defaults rather than being externalized into dynamic configuration properties or a database rules engine.
3. **API Idempotency:** 
   * Idempotency keys or duplicate request protection guards (to prevent double-posting if a client sends duplicate transfer requests due to network timeouts) are not implemented in this iteration.