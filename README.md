[![Java CI with Maven](https://github.com/starnowski/jamolingo/actions/workflows/maven.yml/badge.svg)](https://github.com/starnowski/jamolingo/actions/workflows/maven.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.starnowski.jamolingo/parent.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.starnowski.jamolingo%22%20AND%20a:%22parent%22)

# jamolingo

Implementation of OData specification for Mongo with usage of Olingo integration in JAVA.

## Modules

The project is organized into several modules, each serving a specific purpose:

### [Core](core/README.md)
The `core` module contains the primary logic for translating OData concepts and queries into MongoDB-compatible formats. It provides the essential building blocks for mapping OData Entity Data Models (EDM) to MongoDB document structures and parsing OData system query options.

**Key Features:**
*   Translates `$filter` to MongoDB `$match` stages with support for:
    *   Comparison (`eq`, `ne`, `in`, etc.) and Logical (`and`, `or`, `not`) operators.
    *   String, Math, and Date/Time functions.
    *   Collection operators (`any`, `all`) and `/$count`.
*   Translates `$select` to MongoDB `$project` stages.
*   Translates `$orderby`, `$top`, and `$skip` to corresponding MongoDB stages (`$sort`, `$limit`, `$skip`).
*   Handles OData-to-MongoDB mapping configuration and supports customizing mappings via overrides.

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
The `compat-driver-5.x` module serves as an integration testing suite to ensure the compatibility of the [`core`](core/README.md) module with the **MongoDB Java Driver version 5.x**. It verifies that OData-to-MongoDB translation remains functional with the newer driver versions.

### [Performance](perf/README.md)
The `perf` module provides tools and utilities for analyzing and verifying the performance of MongoDB queries. It allows for parsing MongoDB explain outputs to check for index usage and query efficiency.

**Key Features:**
*   Parses MongoDB explain results into a simplified `ExplainAnalyzeResult`.
*   Identifies index usage types (IXSCAN, COLLSCAN, etc.).
*   Experimental support for resolving index match stages.