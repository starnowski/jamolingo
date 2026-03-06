[![Java CI with Maven](https://github.com/starnowski/jamolingo/actions/workflows/maven.yml/badge.svg)](https://github.com/starnowski/jamolingo/actions/workflows/maven.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.starnowski.jamolingo/parent.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.starnowski.jamolingo%22%20AND%20a:%22parent%22)

# jamolingo

A Java library for translating OData queries and concepts into MongoDB aggregation pipelines, leveraging Apache Olingo. It is primarily an OData query translator and not a full OData server implementation, but it can be used as a building block to implement one.

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
*   Translates `$orderby`, `$top`, `$skip`, and `$count` to corresponding MongoDB stages (`$sort`, `$limit`, `$skip`, `$count`).
*   Handles OData-to-MongoDB mapping configuration and supports customizing mappings via overrides.

### [Common JSON](common/json/README.md)
The `jamolingo-json` module provides utility classes for applying JSON-based modifications to Java objects. It acts as a bridge between JSON patch specifications (Merge Patch, JSON Patch) and Java POJOs.

**Key Features:**
*   JSON Merge Patch (RFC 7396) support.
*   Type-safe application of changes to Java classes.
*   Used for configuration overrides and test data setup.

### [JUnit 5 Mongo Extension](junit5-mongo-extension-parent/README.md)
This module provides a suite of JUnit 5 extensions designed to simplify setting up MongoDB data for tests. It includes a generic, environment-agnostic core and a pre-configured extension for Quarkus.

**Key Features:**
*   Declarative data setup using `@MongoSetup` and `@MongoDocument` annotations.
*   Automatic cleanup of collections before tests.
*   **Generic Core**: Can be used in any Java environment (Spring, Micronaut, etc.) by implementing a simple `MongoClient` resolver.
*   **Quarkus Support**: A specialized extension that integrates with the Quarkus `Arc` container.
*   **Spring Support**: A specialized extension that integrates with the Spring `ApplicationContext`.

### [Compatibility Driver 5.x](compat-driver-5.x/README.md)
The `compat-driver-5.x` module serves as an integration testing suite to ensure the compatibility of the [`core`](core/README.md) module with the **MongoDB Java Driver version 5.x**. It verifies that OData-to-MongoDB translation remains functional with the newer driver versions.

### [Performance](perf/README.md)
The `perf` module provides tools and utilities for analyzing and verifying the performance of MongoDB queries. It allows for parsing MongoDB explain outputs to check for index usage and query efficiency.

**Key Features:**
*   Parses MongoDB explain results into a simplified `ExplainAnalyzeResult`.
*   Identifies index usage types (IXSCAN, COLLSCAN, etc.).
*   Experimental support for resolving index match stages.

### [Demos](demos/README.md)
The `demos` module contains example applications that demonstrate how to use `jamolingo` in real-world scenarios.

**Available Demos:**
*   **[Spring Boot Webapp](demos/spring-boot-webapp/README.md)**: A complete Spring Boot application with a `/query` endpoint supporting OData filtering, selection, ordering, and paging.