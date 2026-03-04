# Jamolingo - Spring Boot Demo Webapp

This module provides a demo Spring Boot application that integrates the `jamolingo-core` library to translate OData queries into MongoDB aggregation pipelines.

## Features

The webapp provides a single endpoint `/query` that supports the following OData system query options:

*   **$filter**: Filter results based on entity properties (e.g., `plainString eq 'Poem'`).
*   **$select**: Choose specific fields to return (e.g., `plainString`).
*   **$orderby**: Sort results (e.g., `plainString desc`).
*   **$top**: Limit the number of returned items.
*   **$skip**: Skip a number of items.
*   **$count**: Return the total count of items matching the filter (e.g., `$count=true`).

## Technology Stack

*   **Java 21**
*   **Spring Boot 3.4.2**
*   **MongoDB** (via Spring Data MongoDB)
*   **Apache Olingo 5.0** (for OData parsing)
*   **Liquibase** (for MongoDB data loading)
*   **Jamolingo Core** (for OData to MongoDB translation)

## Configuration

The application is configured via `src/main/resources/application.properties`. By default, it connects to a MongoDB instance at `mongodb://localhost:27017/demos`.

```properties
spring.data.mongodb.database=demos
spring.data.mongodb.uri=mongodb://localhost:27017/demos
```

## Running the Application

### Prerequisites

*   A running MongoDB instance at `localhost:27017` (or update `application.properties`).

### Build and Run

1.  Build the project from the root:
    ```bash
    ./mvnw clean install -DskipTests
    ```
2.  Run the Spring Boot application:
    ```bash
    ./mvnw spring-boot:run -pl demos/spring-boot-webapp
    ```

## Usage Examples

Once the application is running, you can use `curl` or a web browser to query the data:

*   **Filter and Select:**
    `GET http://localhost:8080/query?filter=plainString eq 'Poem'&select=plainString`
*   **Ordering and Paging:**
    `GET http://localhost:8080/query?orderby=plainString desc&top=2&skip=1`
*   **Count:**
    `GET http://localhost:8080/query?filter=contains(plainString, 'e')&count=true`

## Running Tests

The module includes integration tests that use an embedded MongoDB instance.

```bash
./mvnw test -pl demos/spring-boot-webapp
```
