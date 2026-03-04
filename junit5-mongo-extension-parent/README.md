# JUnit 5 Mongo Extension Parent

This project provides a suite of JUnit 5 extensions for loading and managing MongoDB test data. It is structured as a multi-module project to support different environments while sharing a common, generic core.

## Modules

### [Generic Core (junit5-mongo-extension)](junit5-mongo-extension/README.md)
The foundational module containing the generic data loading logic. It provides the `@MongoSetup` annotation and an abstract JUnit 5 extension that can be implemented for any Java framework (Spring, Micronaut, etc.).

### [Quarkus Support (junit5-mongo-extension-quarkus)](junit5-mongo-extension-quarkus/README.md)
A specialized module that provides a ready-to-use extension for **Quarkus** applications. It automatically resolves the `MongoClient` from the Quarkus `Arc` container.

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
