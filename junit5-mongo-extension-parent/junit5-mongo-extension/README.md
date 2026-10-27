# JUnit 5 Mongo Extension (Core)

This is the generic core module for the Jamolingo JUnit 5 extension. It provides the base logic for declaratively loading BSON data, managing standard MongoDB indexes, and configuring and synchronizing MongoDB Atlas Search indexes before tests.

## Features

*   **Declarative Data Setup**: Define data files using `@MongoSetup` and `@MongoDocument` (supports both single BSON/JSON documents and JSON arrays of documents).
*   **Automatic Cleanup**: Collections are cleared before new data is inserted.
*   **Standard MongoDB Index Management**: Declaratively drop and recreate single-field or complex BSON indexes via `@SearchIndex`.
*   **Atlas Search Index Management & Sync Validation**: Create, cache, and await synchronization of Atlas Search indexes via `@AtlasSearchIndex` and `@AtlasSearchIndexValidation`.
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

`AbstractMongoDataLoaderExtension` also provides constructors accepting a custom `Map<AtlasSearchIndexKey, CachedAtlasSearchIndexDefinition>` (to share cached Atlas Search index definitions across extension instances) and a default `AtlasSearchIndexValidationConfig`.

### 2. Annotate Your Test

`@MongoSetup` and `@AtlasSearchIndexValidation` can be placed at the test method or test class level (method-level annotations take precedence).

```java
@ExtendWith(SpringMongoDataLoaderExtension.class)
@AtlasSearchIndexValidation(maxWaitDuration = 45, maxWaitUnit = TimeUnit.SECONDS)
class MyTest {

    @Test
    @MongoSetup(
        indexes = {
            @SearchIndex(
                database = "test_db",
                collection = "items",
                fieldName = "category",
                indexValue = 1
            )
        },
        mongoDocuments = {
            @MongoDocument(
                database = "test_db",
                collection = "items",
                bsonFilePath = "data/items.json" // Can be a single document or a JSON array
            )
        },
        atlasSearchIndexes = {
            @AtlasSearchIndex(
                name = "default",
                database = "test_db",
                collection = "items",
                bsonFilePath = "search/items_search_index.json",
                validationProperty = {"name", "description"}
            )
        }
    )
    void testSomething() {
        // Indexes are created, data is inserted, and Atlas Search index is synchronized
    }
}
```

## Annotations

### `@MongoSetup`
The primary entry point, applicable to test methods or test classes.
*   `mongoDocuments`: Array of `@MongoDocument` entries (default `{}`).
*   `indexes`: Array of `@SearchIndex` entries for standard MongoDB indexes (default `{}`).
*   `atlasSearchIndexes`: Array of `@AtlasSearchIndex` entries for Atlas Search indexes (default `{}`).
*   `batchInsertToCollection`: Whether documents for a collection should be inserted in batch mode using `insertMany` (default `false`).
*   `writeConcernW`: Write concern `w` option used when clearing collections (default `1`).

### `@MongoDocument`
*   `database`: Name of the database.
*   `collection`: Name of the collection.
*   `bsonFilePath`: Path to the JSON/BSON file on the classpath. The file may contain either a single document (`{...}`) or an array of documents (`[{...}, {...}]`). **If left empty (default), the collection will be erased.** If you provide multiple `@MongoDocument` entries for the same collection and at least one has a file path, the collection is cleared once first and then all specified files are loaded.

### `@SearchIndex`
Defines a standard MongoDB index on a target collection. Before creating the specified indexes for a collection, existing indexes on that collection are dropped.
*   `database`: Name of the database.
*   `collection`: Name of the collection.
*   `fieldName`: Optional field name for a simple single-field index key definition (default `""`). Takes precedence over `bsonSupplier` when non-empty.
*   `indexValue`: Index direction/value associated with `fieldName` (default `1` for ascending).
*   `bsonSupplier`: Optional `Supplier<Bson>` class providing a custom index key specification (ignored if `fieldName` is non-empty).
*   `indexOptionsSupplier`: Optional `Supplier<IndexOptions>` class providing custom `IndexOptions`.

### `@AtlasSearchIndex`
Defines a MongoDB Atlas Search index to be created, cached, and synchronized after document insertion.
*   `name`: Name of the Atlas Search index (default `"default"`).
*   `database`: Name of the database.
*   `collection`: Name of the collection.
*   `bsonFilePath`: Classpath path to the JSON/BSON file containing the search index definition.
*   `validationProperty`: Array of document property names (supports nested dot-notation paths) used to verify index synchronization (default `{}`).
*   `validationStrategies`: Array of `AtlasSearchIndexSyncValidationStrategy` classes executed in order to verify synchronization. Defaults to:
    1.  `AtlasSearchIndexReadinessSyncValidationStrategy` – waits until the search index status reaches `"READY"`.
    2.  `AtlasSearchIndexDocumentCountSyncValidationStrategy` – waits until the `$searchMeta` count for each `validationProperty` matches the collection document count containing that property.
    3.  `AtlasSearchIndexValuesSyncValidationStrategy` – waits until `$search` text queries for each document's `validationProperty` value return the expected document `_id`.

### `@AtlasSearchIndexValidation`
Optional annotation (applicable to test classes or methods) to customize polling timeouts during Atlas Search index synchronization validation:
*   `maxWaitDuration`: Maximum duration to wait (default `30`).
*   `maxWaitUnit`: Time unit for maximum wait duration (default `TimeUnit.SECONDS`).
*   `pollInterval`: Polling interval between checks (default `1`).
*   `pollIntervalUnit`: Time unit for polling interval (default `TimeUnit.SECONDS`).

## Requirements

*   **MongoDB Java Driver (Sync)**: Must be present on the classpath.
*   **JUnit 5**: Must be present on the classpath.
