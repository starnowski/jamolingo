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
