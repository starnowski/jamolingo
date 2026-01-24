# jamolingo-json

The `jamolingo-json` module provides utility classes for applying JSON-based modifications to Java objects. It acts as a bridge between JSON patch specifications and Java POJOs, leveraging the Jackson library for object serialization and the Jakarta JSON Processing (JSON-P) API for patch application.

This module is particularly useful for configuration overrides, test data setup, or any scenario where you need to modify a Java object structure using declarative JSON.

## Features

*   **JSON Merge Patch (RFC 7396):** Update an object by merging a JSON partial payload.
*   **JSON Patch (RFC 6902):** Modify an object using a sequence of operations (add, remove, replace, etc.).
*   **Type-Safe:** Applies changes directly to your Java classes (`Class<T>`).

## Components

### `JSONOverrideHelper`

The main entry point for using this library. It provides a simplified API to apply patches.

### `PatchHelper`

A low-level helper that handles the actual transformation of Java objects to JSON structures, applies the Jakarta JSON patches, and deserializes the result back to Java objects using Jackson.

## Usage

### Adding the Dependency

```xml
<dependency>
    <groupId>com.github.starnowski.jamolingo</groupId>
    <artifactId>json</artifactId>
    <version>0.4.0</version>
</dependency>
```

### Applying a JSON Merge Patch

This example demonstrates how to update specific fields of an existing Java object using a JSON payload. This pattern is used in the [`core`](../../core) module to override default OData-to-MongoDB mappings for testing purposes.

```java
import com.github.starnowski.jamolingo.common.json.JSONOverrideHelper;

public class Example {
    public void overrideConfiguration() throws IOException {
        // 1. Create your target object
        EntityMapping originalConfig = new EntityMapping();
        originalConfig.setMongoName("originalName");

        // 2. Define the merge patch (JSON)
        String mergePayload = "{"
                + "  \"mongoName\": \"newName\",