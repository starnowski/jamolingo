# Jamolingo - Demos

This module contains various demo applications showcasing how to use the Jamolingo library in different environments.

## Available Demos

### [Spring Boot Webapp](spring-boot-webapp/README.md)
A Spring Boot 3 application that provides a `/query` endpoint for executing OData queries against a MongoDB database.

**Key Features:**
*   Translates OData system query options (`$filter`, `$select`, `$orderby`, `$top`, `$skip`, `$count`) into MongoDB aggregation stages.
*   Demonstrates integration with Spring Data MongoDB.
*   Uses Liquibase for initial data loading.
*   Includes integration tests with embedded MongoDB.
