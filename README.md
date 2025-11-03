
# 🧩 Parse-to-JSON

[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/royh2uscala/parse-to-json)
[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A sample **multi-module Spring Boot** project demonstrating:
- JSON parsing and data persistence
- Integration with **PostgreSQL**
- Use of **TestContainers** for database testing
- **WireMock** for mocking external REST services

---

## 📂 Repository

GitHub Repository:  
👉 [https://github.com/royh2uscala/parse-to-json.git](https://github.com/royh2uscala/parse-to-json.git)

Clone the repository into your current directory:

```bash
git clone https://github.com/royh2uscala/parse-to-json.git .

````

---

## 🧪 Integration Test

Run the **end-to-end integration test** by executing this class:

```
/parse-to-json/bootstrap/src/test/java/com/sc/sample/parsetojson/bootstrap/end2end/IntegrationTest.java
```

This test:

* Uses **WireMock** to mock the `ip-api` REST service
* Uses a **PostgreSQL TestContainer** to validate the persistence layer

---

## 🧩 Unit Tests

Additional **unit tests** are available in the following modules:

* `adapter`
* `application`

---

## 🚀 Live Launch

You can launch the full application (without mocks) using Docker and Spring Boot.

### 1️⃣ Start PostgreSQL via Docker

From the `bootstrap` module directory:

```bash
cd /parse-to-json/bootstrap
docker compose -f docker-compose.yml up -d
```

### 2️⃣ Launch the Spring Boot Application

**Option A: From your IDE**

Run the following class:

```
com.sc.sample.parsetojson.BootStrapLauncher
```

with program arguments:

```
--spring.profiles.active=dev.postgresdocker
```

**Option B: From the command line**

```bash
cd /parse-to-json/bootstrap
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev.postgresdocker
```

---

## 🧾 Live Test Run

From the project root directory:

```bash
cd /parse-to-json
curl -X POST "http://localhost:8081/api/somebizdomain/upload" \
  -F "file=@EntryFile.txt" \
  -o OutcomeFile.json
```

View the result file:

```bash
cat OutcomeFile.json
```
Alternatively you can use tools like Postman to submit the test REST calls.

---

## 🗄️ Inspect PostgreSQL Database

Access the running Postgres container:

```bash
docker exec -it postgres-db psql -U devco01 -d requestinfodb
```

Inside `psql`, list tables:

```sql
\dt
```

Example output:

```
Schema |     Name     | Type  |  Owner  
--------+--------------+-------+---------
public | request_info | table | devco01
```

View persisted records:

```sql
select * from request_info;
```

This displays the records saved during your latest test run.

---

## 🧱 Project Structure

```
parse-to-json/
├── adapter/                      # Outbound & inbound adapters (e.g. REST, persistence)
│   ├── src/main/java/com/sc/sample/parsetojson/adapter
│   └── src/test/java/...         # Unit tests for adapters
│
├── application/                  # Core business/application layer (use cases, services)
│   ├── src/main/java/com/sc/sample/parsetojson/application
│   └── src/test/java/...         # Unit tests for business logic
│
├── model/                        # Core business domain model layer
│   ├── src/main/java/com/sc/sample/parsetojson/model
│   └── src/test/java/...         # Unit tests for domain model
│
├── bootstrap/                    # Entry point module (Spring Boot launcher, configuration)
│   ├── docker-compose.yml        # PostgreSQL container setup
│   ├── src/main/java/com/sc/sample/parsetojson/bootstrap
│   └── src/test/java/...         # Integration tests (WireMock + TestContainers)
│
├── pom.xml                       # Parent POM (module aggregator)
└── README.md                     # Project documentation

```

This modular layout follows a **Hexagonal (Ports and Adapters)** architecture,
keeping business logic decoupled from infrastructure and entry points.

---

## ✅ Summary

| Feature              | Description                                               |
| -------------------- | --------------------------------------------------------- |
| **Integration Test** | Full E2E test using WireMock + TestContainers             |
| **Unit Tests**       | Located in `adapter` and `application` modules            |
| **Live Launch**      | Profile `dev.postgresdocker` with Docker-based PostgreSQL |
| **Live Test**        | File upload endpoint and persistence validation           |

---

### 🧰 Requirements

* **Java 17+**
* **Spring Boot 3.4.11**
* **Maven 3.8+**
* **Docker (for PostgreSQL container Live Test)**
* **Wire Mock (for mocking external REST service ip-api.com)**
* **Test Containers (Postgres for end 2 end Integration Test)**


---

### 🧑‍💻 Author

**Delroy Hughes**
📦 [GitHub: royh2uscala](https://github.com/royh2uscala)

---

### 📄 License

This project is licensed under the [MIT License](LICENSE).

