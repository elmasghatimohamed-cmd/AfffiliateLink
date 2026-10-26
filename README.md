# AffiliateLink — Profile Module

## 1. Overview

AffiliateLink is a Spring Boot REST API for managing user profiles, including Publishers and Advertisers, with administrative profile-status management.

### Technology stack

- Java 17
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- PostgreSQL 16
- Flyway
- Maven
- Docker Compose

## 2. Prerequisites

Install the following tools:

- JDK 17
- Docker Desktop with Docker Compose
- PowerShell or another terminal

## 3. Environment configuration

The project uses a `.env` file to configure PostgreSQL.

Example configuration:

```dotenv
POSTGRES_DB=DB-NAME
POSTGRES_USER=USER-NAME
POSTGRES_PASSWORD=USER-PASSWORD
POSTGRES_PORT=5431
```

Replace `your_local_password` with your local database password.

**Security:** Do not commit real passwords or other secrets to version control.

The application connects to PostgreSQL using the following URL pattern:

```text
jdbc:postgresql://localhost:${POSTGRES_PORT}/${POSTGRES_DB}
```

## 4. Start PostgreSQL

From the project root, run:

```powershell
docker compose up -d
```

Verify the container status:

```powershell
docker compose ps
```

Validate the Compose configuration:

```powershell
docker compose config --quiet
```

The PostgreSQL service uses the `postgres:16` image and exposes the configured host port, `5431` by default in the local environment.

The named volume `postgres_data` persists database data across ordinary container restarts and recreation.

## 5. Database initialization and migrations

Flyway manages the database schema.

The initial migration is located at:

```text
src/main/resources/db/migration/V1__create_profile_tables.sql
```

It creates the following tables:

- `profile`: shared profile attributes, including email, status and creation timestamp.
- `publisher`: publisher-specific attributes.
- `advertiser`: advertiser-specific attributes.

The subtype tables use their `id` columns as both primary keys and foreign keys referencing `profile(id)`.

The database enforces email uniqueness, required fields, primary keys and foreign-key relationships.

Flyway is enabled through `application.yaml`:

```yaml
spring:
  flyway:
    enabled: true

  jpa:
    hibernate:
      ddl-auto: validate
```

Flyway applies versioned migrations. Hibernate validates the resulting schema against the entity mappings rather than creating or modifying the schema automatically.

## 6. Run the application

### Run with Maven

```powershell
.\mvnw.cmd spring-boot:run
```

### Build the executable JAR

```powershell
.\mvnw.cmd clean package
```

The executable artifact is:

```text
target/AffiliateLink-0.0.1-SNAPSHOT.jar
```

Run the packaged application:

```powershell
java -jar target/AffiliateLink-0.0.1-SNAPSHOT.jar
```

By default, the application listens on port `8080`.

To use another port:

```powershell
java -jar target/AffiliateLink-0.0.1-SNAPSHOT.jar --server.port=8081
```

Ensure the PostgreSQL container is running and the required environment variables are available to the application process.

## 7. REST API endpoints

The Profile module exposes seven endpoints.

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/publishers` | Retrieve publishers |
| GET | `/api/publishers/{id}` | Retrieve a publisher by ID |
| POST | `/api/publishers` | Create a publisher |
| GET | `/api/advertisers` | Retrieve advertisers |
| GET | `/api/advertisers/{id}` | Retrieve an advertiser by ID |
| POST | `/api/advertisers` | Create an advertiser |
| PATCH | `/api/admin/profiles/{id}/status` | Change a profile's status |

### Publisher creation

Example request:

```http
POST /api/publishers
Content-Type: application/json
```

```json
{
  "email": "publisher@example.com",
  "firstname": "Mohamed",
  "lastname": "El Masghati",
  "niche": "TECH",
  "bio": "Java developer",
  "country": "Morocco"
}
```

### Advertiser creation

Example request:

```http
POST /api/advertisers
Content-Type: application/json
```

```json
{
  "email": "advertiser@example.com",
  "companyName": "Example Company",
  "sector": "Technology",
  "website": "https://example.com"
}
```

### Change profile status

Example request:

```http
PATCH /api/admin/profiles/13/status
Content-Type: application/json
```

```json
{
  "status": "INACTIVE"
}
```

Use the status values and permitted transitions defined by the application's `ProfileStatus` and `AdminService` implementation. Invalid transitions are rejected.

## 8. Error handling

The application centralizes exception handling through `GlobalExceptionHandler`.

The API handles cases including:

- Invalid creation requests.
- Profiles that do not exist.
- Duplicate emails across profile types.
- Invalid profile-status transitions.
- Unexpected server errors.

For example, requesting a nonexistent publisher:

```http
GET /api/publishers/999
```

returns `404 Not Found` with a message identifying the missing publisher.

The exact response structure and status codes should follow the implemented exception handler and validated API behavior.

## 9. Verification and delivery

The following checks have been completed during validation of the Profile module:

- All seven endpoints and the required business-rule scenarios were tested under Ticket 06.1.
- PostgreSQL connectivity was verified.
- The packaged JAR was started successfully.
- Flyway applied migration V1 to a separate, initially empty test database.
- Hibernate initialized successfully with `ddl-auto: validate`.
- Primary keys, the unique email constraint, required fields and subtype foreign keys were inspected.
- Docker Compose configuration validation completed without errors.

The clean-database test used `affiliatelink_clean_test`, leaving the original `affiliatelink_db` intact.

## 10. Project structure

```text
src/main/java/ma/affilatelink/
├── common/
│   ├── ApiResponse.java
│   ├── EmailAlreadyExistsException.java
│   ├── GlobalExceptionHandler.java
│   └── InvalidProfileStatusException.java
├── profil/
│   ├── AdminController.java
│   ├── AdminService.java
│   ├── Advertiser.java
│   ├── AdvertiserController.java
│   ├── AdvertiserRepository.java
│   ├── AdvertiserRequest.java
│   ├── AdvertiserResponse.java
│   ├── AdvertiserService.java
│   ├── ChangeStatusRequest.java
│   ├── Niche.java
│   ├── Profile.java
│   ├── ProfileRepository.java
│   ├── ProfileStatus.java
│   ├── Publisher.java
│   ├── PublisherController.java
│   ├── PublisherRepository.java
│   ├── PublisherRequest.java
│   ├── PublisherResponse.java
│   └── PublisherService.java
└── AffiliateLinkApplication.java

src/main/resources/
├── application.yaml
└── db/migration/
    └── V1__create_profile_tables.sql
```
