# JUnit 5 Mongo Extension Spring

This module provides a specialized JUnit 5 extension for **Spring** and **Spring Boot** applications. It builds upon the [Generic Core Extension](../junit5-mongo-extension/README.md) to provide automatic `MongoClient` resolution from the Spring `ApplicationContext`.

## Features

*   **Spring Integration**: Automatically retrieves the `MongoClient` bean from the Spring `ApplicationContext`.
*   **Declarative Data Setup**: Use the `@MongoSetup` annotation to specify BSON files for test data.
*   **Automatic Cleanup**: Clears target collections before each test.

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.github.starnowski.jamolingo</groupId>
    <artifactId>junit5-mongo-extension-spring</artifactId>
    <version>0.8.0</version>
    <scope>test</scope>
</dependency>
```

## Usage

Annotate your Spring integration test with `@SpringBootTest` (or any other annotation that starts a Spring context) and use the `@MongoSetup` annotation. You must also register the extension using `@ExtendWith`.

```java
import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import com.github.starnowski.jamolingo.junit5.SpringMongoDataLoaderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(SpringMongoDataLoaderExtension.class)
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

*   **Spring**: Designed for use with Spring's JUnit 5 support.
*   **ApplicationContext**: Requires a `MongoClient` bean to be available in the Spring context.
