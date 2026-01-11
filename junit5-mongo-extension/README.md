# JUnit 5 Mongo Extension

This module provides a JUnit 5 extension designed to simplify the process of setting up MongoDB data for tests in a **Quarkus** application. It allows you to declaratively define the data that should be loaded into your MongoDB collections before each test method execution.

## Features

*   **Declarative Data Setup**: Use annotations to specify JSON files containing the data to be inserted into MongoDB collections.
*   **Automatic Cleanup**: Automatically clears the target collections before inserting new data, ensuring a clean state for each test.
*   **Quarkus Integration**: Seamlessly integrates with Quarkus `Arc` container to retrieve the `MongoClient`.
*   **Support for Extended JSON**: Handles JSON files that may contain BSON types (Extended JSON) via the MongoDB driver's parsing logic.

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.github.starnowski.jamolingo</groupId>
    <artifactId>junit5-mongo-extension</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

## Usage

### 1. Prepare Data Files

Create your data files (e.g., JSON) in your test resources directory (e.g., `src/test/resources`).

**Example `src/test/resources/data/users.json`:**
```json
{
  "_id": "user1",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "createdAt": {
    "$date": "2023-10-26T10:00:00Z"
  }
}
```

### 2. Annotate Your Test

Use the `@MongoSetup` annotation on your test class or test method to define the data to load.

```java
import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class MyMongoTest {

    @Test
    @MongoSetup(mongoDocuments = {
        @MongoDocument(
            database = "my_db",
            collection = "users",
            bsonFilePath = "data/users.json"
        )
    })
    void shouldFindUser() {
        // Test logic here...
        // The 'users' collection in 'my_db' will contain the document from 'users.json'
    }
}
```

## Components

### `QuarkusMongoDataLoaderExtension`

The core JUnit 5 extension (`BeforeEachCallback`) that orchestrates the data loading process. It:
1.  Inspects the test method for the `@MongoSetup` annotation.
2.  Retrieves the `MongoClient` instance from the Quarkus bean container.
3.  Clears the collections specified in the annotation.
4.  Reads the data files from the classpath and inserts the documents into the respective collections.

### `@MongoSetup`

An annotation used to configure the data setup. It accepts an array of `@MongoDocument` definitions.

### `@MongoDocument`

Defines a single data import operation.
*   `database()`: The target database name.
*   `collection()`: The target collection name.
*   `bsonFilePath()`: The path to the file on the classpath containing the document(s). Ideally, this should contain a valid JSON/Extended JSON object.

## Requirements

*   **Quarkus**: This extension relies on `io.quarkus.arc.Arc` to locate the `MongoClient`. It is intended for use within Quarkus tests (e.g., annotated with `@QuarkusTest`).
*   **MongoDB**: A running MongoDB instance (or embedded/test container) configured in your Quarkus application.
