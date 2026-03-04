# JUnit 5 Mongo Extension (Core)

This is the generic core module for the Jamolingo JUnit 5 extension. It provides the base logic for declaratively loading BSON data into MongoDB before tests.

## Features

*   **Declarative Data Setup**: Define data files using `@MongoSetup` and `@MongoDocument`.
*   **Automatic Cleanup**: Collections are cleared before new data is inserted.
*   **Environment Agnostic**: Can be easily adapted to any Java dependency injection framework.

## Usage

### 1. Implement the Extension

Since this is a core module, you must provide a way to resolve the `MongoClient`. Create a class that extends `AbstractMongoDataLoaderExtension`:

```java
public class SpringMongoDataLoaderExtension extends AbstractMongoDataLoaderExtension {
    @Override
    protected MongoClient resolveMongoClient() {
        // Example: Resolve from Spring ApplicationContext
        return context.getBean(MongoClient.class);
    }
}
```

### 2. Annotate Your Test

```java
@ExtendWith(SpringMongoDataLoaderExtension.class)
class MyTest {

    @Test
    @MongoSetup(mongoDocuments = {
        @MongoDocument(
            database = "test_db",
            collection = "items",
            bsonFilePath = "data/items.json"
        )
    })
    void testSomething() {
        // Data is now in MongoDB
    }
}
```

## Annotations

### `@MongoSetup`
The primary entry point. It contains an array of `mongoDocuments`.

### `@MongoDocument`
*   `database`: Name of the database.
*   `collection`: Name of the collection.
*   `bsonFilePath`: Path to the JSON/BSON file on the classpath.

## Requirements

*   **MongoDB Java Driver (Sync)**: Must be present on the classpath.
*   **JUnit 5**: Must be present on the classpath.
