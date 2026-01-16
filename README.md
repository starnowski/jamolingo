# jamolingo

Implementation of OData specification for Mongo with usage of Olingo integration in JAVA.

## Modules

The project is organized into several modules, each serving a specific purpose:

### [Core](core/README.md)
The `core` module contains the primary logic for translating OData concepts and queries into MongoDB-compatible formats. It provides the essential building blocks for mapping OData Entity Data Models (EDM) to MongoDB document structures and parsing OData system query options.

**Key Features:**
*   Translates `$select` to MongoDB `$project` stages.
*   Handles OData-to-MongoDB mapping configuration.
*   Supports customizing mappings via overrides.

### [Common JSON](common/json/README.md)
The `jamolingo-json` module provides utility classes for applying JSON-based modifications to Java objects. It acts as a bridge between JSON patch specifications (Merge Patch, JSON Patch) and Java POJOs.

**Key Features:**
*   JSON Merge Patch (RFC 7396) support.
*   Type-safe application of changes to Java classes.
*   Used for configuration overrides and test data setup.

### [JUnit 5 Mongo Extension](junit5-mongo-extension/README.md)
This module provides a JUnit 5 extension designed to simplify the process of setting up MongoDB data for tests in a **Quarkus** application.

**Key Features:**
*   Declarative data setup using `@MongoSetup` and `@MongoDocument` annotations.
*   Automatic cleanup of collections before tests.
*   Seamless integration with Quarkus `Arc` container to retrieve the `MongoClient`.

### [Compatibility Driver 5.x](compat-driver-5.x/README.md)
The `compat-driver-5.x` module serves as an integration testing suite to ensure the compatibility of the `core` module with the **MongoDB Java Driver version 5.x**. It verifies that OData-to-MongoDB translation remains functional with the newer driver versions.