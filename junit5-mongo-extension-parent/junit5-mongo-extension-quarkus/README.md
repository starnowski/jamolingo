# JUnit 5 Mongo Extension Quarkus

This module provides a specialized JUnit 5 extension for **Quarkus** applications. It builds upon the [Generic Core Extension](../junit5-mongo-extension/README.md) to provide automatic `MongoClient` resolution.

## Features

*   **Quarkus Integration**: Automatically retrieves the `MongoClient` from the Quarkus `Arc` (CDI) container.
*   **Declarative Data Setup**: Use the `@MongoSetup` annotation to specify BSON files for test data.
*   **Automatic Cleanup**: Clears target collections before each test.

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.github.starnowski.jamolingo</groupId>
    <artifactId>junit5-mongo-extension-quarkus</artifactId>
    <version>0.6.0</version>
    <scope>test</scope>
</dependency>
```

## Usage

Annotate your Quarkus integration test with `@QuarkusTest` and use the `@MongoSetup` annotation. You must also register the extension using `@ExtendWith`.

### Empty `bsonFilePath` Behavior
If you leave `bsonFilePath` as an empty string (the default), the extension will simply erase the specified collection. This is useful for ensuring a collection is empty before a test.
* If multiple `@MongoDocument` entries are provided for the same collection, the collection is erased **only once** at the start of the setup.
* If any entry for that collection provides a file path, the collection is cleared and then all files are inserted.

### Example
import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import com.github.starnowski.jamolingo.junit5.QuarkusMongoDataLoaderExtension;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@QuarkusTest
@ExtendWith(QuarkusMongoDataLoaderExtension.class)
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
        // The 'users' collection in 'my_db' is now populated
    }
}
```

## Requirements

*   **Quarkus**: Designed for use with `@QuarkusTest`.
*   **CDI**: Requires a `MongoClient` bean to be available in the container.
