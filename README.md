# AffiliateLink – Affiliation Management Platform

## Overview

AffiliateLink is a REST API built with Spring Boot for managing an affiliate marketing platform.

The application handles three main areas:

- **Profiles:** Publisher and Advertiser registration, retrieval, and administrative status management.
- **Offers:** Offer creation, updating, retrieval, and filtered discovery.
- **Campaigns:** Campaign creation, retrieval, and lifecycle management.

The application follows a modular architecture using Spring Modulith to separate Profile and Offer responsibilities while allowing them to communicate through explicit module APIs and application events.

## Technology Stack

- Java 17
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- PostgreSQL 16
- Flyway
- Spring Modulith
- Maven
- Docker Compose
- Testcontainers
- JUnit 5 and Mockito
- JaCoCo
- SonarQube

## Architecture

The application is organized into modules:

- **Profile module:** Manages publishers, advertisers, and administrative profile operations.
- **Offer module:** Manages offers, campaigns, and publisher notifications.
- **Common package:** Contains shared API and exception-handling components.

Each module exposes a public API for communication with other modules. Service contracts are separated from their implementations, allowing controllers and other consumers to depend on interfaces rather than concrete classes.

The Profile module also publishes a `ProfileSuspended` event. The Offer module listens for this event and handles the corresponding campaign suspension workflow.

## Prerequisites

Before running the application, make sure you have:

- JDK 17
- Docker Desktop with Docker Compose
- Git
- PowerShell or another terminal

Docker must be running when executing integration tests that use Testcontainers.

## Configuration

### Environment variables

Create a `.env` file in the project root:

```env
POSTGRES_DB=DB-NAME
POSTGRES_USER=USER-NAME
POSTGRES_PASSWORD=DB-PASSWORD
POSTGRES_PORT=5432
```

### Database configuration

The application connects to PostgreSQL using:

```text
jdbc:postgresql://localhost:${POSTGRES_PORT}/${POSTGRES_DB}
```

Docker Compose runs PostgreSQL 16 and exposes the database through the configured host port. Database data is persisted using the `postgres_data` volume.

### Start PostgreSQL

```powershell
docker compose up -d
```

Check the container status:

```powershell
docker compose ps
```

Validate the Compose configuration:

```powershell
docker compose config --quiet
```

## Running the Application

Start the application with Maven:

```powershell
mvn spring-boot:run
```

The default application port is `8080`.

To use another port:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=YOUR-DESIRED-PORT"
```

### Build the application

```powershell
mvn clean package
```

Run the generated JAR:

```powershell
java -jar target/AffiliateLink-0.0.1-SNAPSHOT.jar
```

## REST API

Base URL:

```text
http://localhost:8080
```

### 1. Profile Management

The Profile module manages Publisher and Advertiser profiles.

#### Publisher endpoints

| Method | Endpoint | Description | Success |
|---|---|---|---|
| GET | `/api/publishers` | Retrieve all publishers | 200 |
| GET | `/api/publishers/{id}` | Retrieve a publisher by ID | 200 |
| POST | `/api/publishers` | Register a publisher | 201 |

**Create a publisher**

```http
POST /api/publishers
Content-Type: application/json
```

Request body:

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

The `niche` field must contain a valid value from the `Niche` enum.

#### Advertiser endpoints

| Method | Endpoint | Description | Success |
|---|---|---|---|
| GET | `/api/advertisers` | Retrieve all advertisers | 200 |
| GET | `/api/advertisers/{id}` | Retrieve an advertiser by ID | 200 |
| POST | `/api/advertisers` | Register an advertiser | 201 |

**Create an advertiser**

```http
POST /api/advertisers
Content-Type: application/json
```

Request body:

```json
{
  "email": "advertiser@example.com",
  "companyName": "Example Company",
  "sector": "Technology",
  "website": "https://example.com"
}
```

#### Administrative profile management

| Method | Endpoint | Description | Success |
|---|---|---|---|
| PATCH | `/api/admin/profiles/{id}/status` | Change a profile's status | 204 |

**Change profile status**

```http
PATCH /api/admin/profiles/1/status
Content-Type: application/json
```

Request body:

```json
{
  "status": "SUSPENDED"
}
```

The status must be a valid value from the `ProfileStatus` enum. Profile status transitions are handled by the administrative service.

### 2. Offer Management

The Offer module allows advertisers to create and maintain offers, while users can discover offers through filtering and pagination.

#### Offer endpoints

| Method | Endpoint | Description | Success |
|---|---|---|---|
| POST | `/api/offers` | Create an offer | 201 |
| GET | `/api/offers` | Search and retrieve offers | 200 |
| GET | `/api/offers/{id}` | Retrieve an offer by ID | 200 |
| PUT | `/api/offers/{id}` | Update an offer | 200 |

**Create an offer**

```http
POST /api/offers
Content-Type: application/json
```

Request body:

```json
{
  "advertiserId": 19,
  "title": "Beauty Essentials",
  "description": "Essential products for everyday care.",
  "landingUrl": "https://example.com/beauty",
  "niche": "BEAUTY",
  "tags": [
    "Makeup",
    "Wellness"
  ]
}
```

The request requires a positive advertiser ID, a title, description, valid landing URL, niche, and tags.

**Update an offer**

```http
PUT /api/offers/9
Content-Type: application/json
```

Request body:

```json
{
  "title": "Updated Beauty Essentials",
  "description": "Updated offer description.",
  "landingUrl": "https://example.com/updated-beauty",
  "niche": "BEAUTY",
  "tags": [
    "Makeup",
    "Skincare"
  ]
}
```

**Search offers**

```http
GET /api/offers
```

Supported query parameters:

| Parameter | Type | Description |
|---|---|---|
| `niche` | String | Filter offers by niche |
| `advertiserId` | Long | Filter by advertiser |
| `tag` | String | Filter by tag |
| `keyword` | String | Search using a keyword |
| `page` | Integer | Page number |
| `size` | Integer | Number of results per page |
| `sort` | String | Sorting criteria |

Default pagination:

- Page size: `20`
- Sort field: `createdAt`
- Sort direction: `DESC`

Example:

```http
GET /api/offers?niche=BEAUTY&advertiserId=19&tag=Makeup&keyword=beauty&page=0&size=10&sort=createdAt,desc
```

**Retrieve an offer**

```http
GET /api/offers/9
```

An offer response contains its ID, advertiser ID, title, description, landing URL, niche, creation date, tags, and associated campaigns.

### 3. Campaign Management

Campaigns are associated with offers and contain scheduling, budget, and payout information.

#### Campaign endpoints

| Method | Endpoint | Description | Success |
|---|---|---|---|
| POST | `/api/offres/{id}/campaigns` | Create a campaign for an offer | 201 |
| GET | `/api/campaigns` | Retrieve campaigns, optionally filtered by status | 200 |
| PATCH | `/api/campaigns/{id}/status` | Update a campaign's status | 204 |

**Create a campaign**

```http
POST /api/offres/9/campaigns
Content-Type: application/json
```

Request body:

```json
{
  "name": "Summer Promotion",
  "startDate": "2026-12-01",
  "endDate": "2026-12-31",
  "budget": 10000.00,
  "payoutModel": "<VALID_PAYOUT_MODEL>",
  "payoutValue": 25.00
}
```

The request requires:

- A campaign name.
- A start date and an end date.
- A positive budget.
- A valid payout model.
- A non-negative payout value.

The end date must be after the start date.

Replace `<VALID_PAYOUT_MODEL>` with a value supported by the application's `PayoutModel` enum.

**Retrieve campaigns**

```http
GET /api/campaigns
```

Campaigns can optionally be filtered by status:

```http
GET /api/campaigns?status=ACTIVE
```

The status parameter is case-insensitive and must match a value from the `CampaignStatus` enum.

**Update campaign status**

```http
PATCH /api/campaigns/2/status
Content-Type: application/json
```

Request body:

```json
{
  "status": "PAUSED"
}
```

The campaign status must be a valid `CampaignStatus` value. Invalid lifecycle transitions are rejected by the campaign service.

## Database Migrations

Flyway manages database schema changes.

Migration scripts are located in:

```text
src/main/resources/db/migration/
```

The initial profile migration is:

```text
V1__create_profile_tables.sql
```

It creates the profile-related tables:

- `profile`
- `publisher`
- `advertiser`

The application uses Flyway to apply migrations and Hibernate's `ddl-auto: validate` setting to validate the entity mappings against the existing schema.

## Error Handling

The application uses a centralized `GlobalExceptionHandler` to handle API errors.

Handled scenarios include:

- Invalid request bodies and validation failures.
- Missing resources.
- Duplicate email addresses.
- Invalid profile or campaign status transitions.
- Invalid campaign status parameters.
- Unexpected server-side errors.

HTTP status codes depend on the exception and operation. For example, successful creation returns `201 Created`, successful status changes return `204 No Content`, and missing resources return `404 Not Found`.

## Testing

Run the complete verification lifecycle:

```powershell
mvn clean verify
```

The test suite includes unit tests and integration tests.

Integration tests use Testcontainers to provision temporary PostgreSQL databases. Docker Desktop must be running before executing the full test suite.

Check Docker availability:

```powershell
docker info
```

### JaCoCo coverage report

After verification, open:

```text
target/site/jacoco/index.html
```

JaCoCo measures test coverage and the Maven verification lifecycle enforces the configured coverage thresholds.

## SonarQube Analysis

SonarQube provides static code analysis to help identify:

- Bugs and potential vulnerabilities.
- Security hotspots.
- Code smells.
- Code duplication.
- Test coverage.
- Maintainability issues.

JaCoCo and SonarQube serve different purposes: JaCoCo measures test coverage, while SonarQube provides a broader view of code quality and maintainability.

Run the analysis using the SonarQube configuration available in the project:

```powershell
mvn clean verify sonar:sonar
```

Make sure your SonarQube server and project configuration are available before running the analysis.

## Project Structure

```text
src/
└── main/
    ├── java/
    │   └── ma/affilatelink/
    │       ├── common/
    │       │   └── exceptions/
    │       ├── offer/
    │       │   ├── api/
    │       │   ├── controllers/
    │       │   ├── dto/
    │       │   ├── entities/
    │       │   ├── enums/
    │       │   ├── listeners/
    │       │   ├── repositories/
    │       │   └── services/
    │       │       └── implementation/
    │       ├── profil/
    │       │   ├── api/
    │       │   ├── controllers/
    │       │   ├── dto/
    │       │   ├── entities/
    │       │   ├── enums/
    │       │   ├── events/
    │       │   ├── repositories/
    │       │   └── services/
    │       │       └── implementation/
    │       └── AffiliateLinkApplication.java
    └── resources/
        ├── application.yaml
        └── db/
            └── migration/
```

## License

This project is developed as part of an educational software engineering project.