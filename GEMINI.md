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

The project is built using standard Maven commands. It also use the Maven wrapper. The following command will clean the project, compile the source code, run all tests, and install the artifact into your local Maven repository.

```bash
mvnw clean install
```

Important! Consider of invoking the "spotless:apply" goal before build, check "Code Style and Formatting" section below.

### Testing

Tests are written in Groovy using the [Spock Framework](https://spockframework.org/). They are located in `core/src/test/groovy` and are automatically executed as part of the `mvnw clean install` command.

## Development Conventions

### Code Style and Formatting

The project enforces the [Google Java Format](https://github.com/google/google-java-format) code style using the `spotless-maven-plugin`. The build will fail if the code is not formatted correctly.

*   **To check for formatting issues:**
    ```bash
    mvnw spotless:check
    ```

*   **To automatically apply the correct formatting:**
    ```bash
    mvnw spotless:apply
    ```
    
Important! After each code change that is store in the {module}/main/src/java folders we have to run the spotless:apply goal to format correctly

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

import static com.github.starnowski.jamolingo.context.Constants.MONGO_HARDCODED_BSON_DOCUMENT_NESTING_LIMIT;

import java.util.Objects;

public class DefaultEdmPathContextSearch implements EdmPathContextSearch {
    public DefaultEdmPathContextSearch(
            Integer mongoPathMaxDepth,
            Integer maxCircularLimitPerEdmPath,
            Integer maxCircularLimitForAllEdmPaths) {
        this.mongoPathMaxDepth = mongoPathMaxDepth;
        this.maxCircularLimitPerEdmPath = maxCircularLimitPerEdmPath;
        this.maxCircularLimitForAllEdmPaths = maxCircularLimitForAllEdmPaths;
    }

    private final Integer mongoPathMaxDepth;
    private final Integer maxCircularLimitPerEdmPath;
    private final Integer maxCircularLimitForAllEdmPaths;

    @Override
    public Integer getMongoPathMaxDepth() {
        return mongoPathMaxDepth;
    }

    @Override
    public Integer getMaxCircularLimitPerEdmPath() {
        return maxCircularLimitPerEdmPath;
    }

    @Override
    public Integer getMaxCircularLimitForAllEdmPaths() {
        return maxCircularLimitForAllEdmPaths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultEdmPathContextSearch that = (DefaultEdmPathContextSearch) o;
        return Objects.equals(mongoPathMaxDepth, that.mongoPathMaxDepth)
                && Objects.equals(maxCircularLimitPerEdmPath, that.maxCircularLimitPerEdmPath)
                && Objects.equals(maxCircularLimitForAllEdmPaths, that.maxCircularLimitForAllEdmPaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                mongoPathMaxDepth, maxCircularLimitPerEdmPath, maxCircularLimitForAllEdmPaths);
    }

    @Override
    public String toString() {
        return "DefaultEdmPathContextSearch{"
                + "mongoPathMaxDepth="
                + mongoPathMaxDepth
                + ", maxCircularLimitPerEdmPath="
                + maxCircularLimitPerEdmPath
                + ", maxCircularLimitForAllEdmPaths="
                + maxCircularLimitForAllEdmPaths
                + '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer mongoPathMaxDepth = MONGO_HARDCODED_BSON_DOCUMENT_NESTING_LIMIT;
        private Integer maxCircularLimitPerEdmPath;
        private Integer maxCircularLimitForAllEdmPaths;

        public Builder withDefaultEdmPathContextSearch(DefaultEdmPathContextSearch defaultEdmPathContextSearch) {
            this.maxCircularLimitPerEdmPath = defaultEdmPathContextSearch.maxCircularLimitPerEdmPath;
            this.mongoPathMaxDepth = defaultEdmPathContextSearch.mongoPathMaxDepth;
            this.maxCircularLimitForAllEdmPaths = defaultEdmPathContextSearch.maxCircularLimitForAllEdmPaths;
            return this;
        }

        public Builder withMaxCircularLimitPerEdmPath(Integer maxCircularLimitPerEdmPath) {
            this.maxCircularLimitPerEdmPath = maxCircularLimitPerEdmPath;
            return this;
        }

        public Builder withMongoPathMaxDepth(Integer mongoPathMaxDepth) {
            this.mongoPathMaxDepth = mongoPathMaxDepth;
            return this;
        }

        public Builder withMaxCircularLimitForAllEdmPaths(Integer maxCircularLimitForAllEdmPaths) {
            this.maxCircularLimitForAllEdmPaths = maxCircularLimitForAllEdmPaths;
            return this;
        }

        public DefaultEdmPathContextSearch build() {
            return new DefaultEdmPathContextSearch(
                    mongoPathMaxDepth, maxCircularLimitPerEdmPath, maxCircularLimitForAllEdmPaths);
        }
    }
}

```


### Adding javadoc

When you add javadoc do not use html tags because some maven plugin do not allow them

### README.md file rule

When referring to different maven module in README.md file that is belongs to different, generate relative hyper link that point to 
maven module root.
For example:
```
This class is part of the [json module](/common/json)
```

### CHANGELOG.md

Use the https://github.com/starnowski/posjsonhelper/blob/master/CHANGELOG.md file as reference.

Before adding entries to CHANGELOG.md analyze if the change in code is new, modified or removed.
Try to group changes to fit below sections:
- Added
- Changed
- Removed

To every entry add at the end the link to Github issue like on below example:

Below example with Github issue with id 160.

```markdown
### Changed

  - Added generic parameter type that extends java.lang.Comparable to type com.github.starnowski.posjsonhelper.hibernate6.operators.JsonArrayFunction [160](https://github.com/starnowski/posjsonhelper/issues/160)
```