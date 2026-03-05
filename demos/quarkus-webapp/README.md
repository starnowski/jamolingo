# Jamolingo - Quarkus Demo Webapp

This module provides a demo Quarkus application that integrates the `core` library to translate OData queries into MongoDB aggregation pipelines.

## Features

The webapp provides two endpoints that support OData system query options:

*   **/query**: Standard OData query endpoint.
*   **/query-index-check**: OData query endpoint with index validation. Returns 400 if no index is used.

Supported OData system query options:

*   **$filter**: Filter results based on entity properties (e.g., `plainString eq 'Poem'`).
*   **$select**: Choose specific fields to return (e.g., `plainString`).
*   **$orderby**: Sort results (e.g., `plainString desc`).
*   **$top**: Limit the number of returned items.
*   **$skip**: Skip a number of items.
*   **$count**: Return the total count of items matching the filter (e.g., `$count=true`).

## Technology Stack

*   **Java 21**
*   **Quarkus 3.22.2**
*   **MongoDB** (via Quarkus MongoDB Client)
*   **Apache Olingo 5.0** (for OData parsing)
*   **Jamolingo Core** (for OData to MongoDB translation)

## Configuration

The application is configured via `src/main/resources/application.properties`. By default, it connects to a MongoDB instance at `mongodb://localhost:27017/demos`.

```properties
quarkus.mongodb.connection-string=mongodb://localhost:27017
quarkus.mongodb.database=demos
```

## Running the Application

### Prerequisites

*   A running MongoDB instance at `localhost:27017` (or update `application.properties`).

### Build and Run

1.  Build the project from the root:
    ```bash
    ./mvnw clean install -DskipTests
    ```
2.  Run the Quarkus application in dev mode:
    ```bash
    ./mvnw quarkus:dev -pl demos/quarkus-webapp
    ```

## Usage Examples

Once the application is running, you can use `curl` or a web browser to query the data:

*   **Filter and Select:**
    `GET http://localhost:8081/query?filter=plainString eq 'Poem'&select=plainString`
*   **Ordering and Paging:**
    `GET http://localhost:8081/query?orderby=plainString desc&top=2&skip=1`
*   **Count:**
    `GET http://localhost:8081/query?filter=contains(plainString, 'e')&count=true`
*   **Index Check (returns 400 if no index):**
    `GET http://localhost:8081/query-index-check?filter=plainString eq 'Poem'`

## Running Tests

The module includes integration tests that use a containerized or embedded MongoDB instance managed by Quarkus Dev Services or the `junit5-mongo-extension-quarkus`.

```bash
./mvnw test -pl demos/quarkus-webapp
```
