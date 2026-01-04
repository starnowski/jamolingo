# Gemini Project Context: jamolingo

## Project Overview

`jamolingo` is a Java library designed to translate OData queries and concepts into a format compatible with MongoDB. It leverages the Apache Olingo 5.0 library for handling the OData specification and provides mapping capabilities to bridge the gap between OData's Entity Data Model (EDM) and MongoDB's document structure.

The project is structured as a multi-module Maven project, with the primary logic contained within the `core` module.

**Key Technologies:**
*   **Language:** Java 17
*   **Build:** Apache Maven
*   **OData:** Apache Olingo 5.0
*   **Database:** MongoDB (via the BSON library)
*   **Testing:** Spock Framework, Groovy

## Building and Running

This is a library project and is not intended to be run standalone. It should be built and included as a dependency in another application.

### Build Command

The project is built using standard Maven commands. The following command will clean the project, compile the source code, run all tests, and install the artifact into your local Maven repository.

```bash
mvn clean install
```

### Testing

Tests are written in Groovy using the [Spock Framework](https://spockframework.org/). They are located in `core/src/test/groovy` and are automatically executed as part of the `mvn clean install` command.

## Development Conventions

### Code Style and Formatting

The project enforces the [Google Java Format](https://github.com/google/google-java-format) code style using the `spotless-maven-plugin`. The build will fail if the code is not formatted correctly.

*   **To check for formatting issues:**
    ```bash
    mvn spotless:check
    ```

*   **To automatically apply the correct formatting:**
    ```bash
    mvn spotless:apply
    ```

### Dependency Management

All dependency versions are managed centrally in the `<dependencyManagement>` section of the root `pom.xml` file. This ensures that all modules use consistent versions of libraries.

### Programing tips

The assumption for production code is that the core and other potential modules (unless the module will be used solely for testing purposes) should have as few external dependencies as possible.
One of the example of external dependency which we want to avoid is to add lombok library.
Although it generates code in compile mode there was in the past issues related to this library.

### Coding practices
#### Builder pattern for type

Below there is an example of immutable type DefaultEdmPathContextSearch that contains inner static builder type.
The name of builder class is similar with base class but with suffix "Builder".
Also generally the assumption is that all immutable objects in project that stores only data should have implement below methods:
- toString()
- equals()
- hashCode()

And what is important all those methods should return values computed based on all their declared properties.
No property should be omitted.

```java
package com.github.starnowski.jamolingo.context;

import java.util.Objects;

public class DefaultEdmPathContextSearch implements EdmPathContextSearch {
    public DefaultEdmPathContextSearch(Integer mongoPathMaxDepth) {
        this.mongoPathMaxDepth = mongoPathMaxDepth;
    }

    private final Integer mongoPathMaxDepth;

    @Override
    public Integer getMongoPathMaxDepth() {
        return mongoPathMaxDepth;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultEdmPathContextSearch that = (DefaultEdmPathContextSearch) o;
        return Objects.equals(mongoPathMaxDepth, that.mongoPathMaxDepth);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mongoPathMaxDepth);
    }

    @Override
    public String toString() {
        return "DefaultEdmPathContextSearch{" + "mongoPathMaxDepth=" + mongoPathMaxDepth + '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer mongoPathMaxDepth;

        public Builder withMongoPathMaxDepth(Integer mongoPathMaxDepth) {
            this.mongoPathMaxDepth = mongoPathMaxDepth;
            return this;
        }

        public DefaultEdmPathContextSearch build() {
            return new DefaultEdmPathContextSearch(mongoPathMaxDepth);
        }
    }
}

```
