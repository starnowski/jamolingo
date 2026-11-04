# JUnit 5 Mongo Extension Parent

This project provides a suite of JUnit 5 extensions for loading and managing MongoDB test data, standard MongoDB indexes, and MongoDB Atlas Search indexes. It is structured as a multi-module project to support different environments while sharing a common, generic core.

## Modules

### [Generic Core (junit5-mongo-extension)](junit5-mongo-extension)
The foundational module containing the generic data loading and index management logic. It provides the `@MongoSetup`, `@MongoDocument`, `@SearchIndex`, `@AtlasSearchIndex`, and `@AtlasSearchIndexValidation` annotations along with an abstract JUnit 5 extension (`AbstractMongoDataLoaderExtension`) that can be implemented for any Java framework (Spring, Micronaut, Quarkus, etc.).

### [Quarkus Support (junit5-mongo-extension-quarkus)](junit5-mongo-extension-quarkus)
A specialized module that provides a ready-to-use extension for **Quarkus** applications. It automatically resolves the `MongoClient` from the Quarkus `Arc` container.

### [Spring Support (junit5-mongo-extension-spring)](junit5-mongo-extension-spring)
A specialized module that provides a ready-to-use extension for **Spring** and **Spring Boot** applications. It automatically resolves the `MongoClient` from the Spring `ApplicationContext`.

## Key Capabilities

*   **Declarative Data Loading**: Load single BSON/JSON documents or arrays of documents from classpath resources, or clear collections before test execution.
*   **MongoDB Index Management**: Drop existing indexes and recreate single-field or custom BSON indexes with custom `IndexOptions` before tests.
*   **Atlas Search Index Management & Synchronization**: Create, cache, and validate MongoDB Atlas Search indexes using configurable synchronization strategies (index readiness, document count matching, and searchable field value verification).

## Architecture

The system uses an abstract base class `AbstractMongoDataLoaderExtension`. To use this tool in a new environment, you simply need to extend this class and implement the `resolveMongoClient()` method.

```java
public class MyCustomExtension extends AbstractMongoDataLoaderExtension {
    @Override
    protected MongoClient resolveMongoClient() {
        // Return your environment-specific MongoClient
    }
}
```
