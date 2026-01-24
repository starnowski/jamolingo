# Jamolingo - Performance Module

The `perf` module provides tools and utilities for analyzing and verifying the performance of MongoDB queries. It primarily focuses on parsing and interpreting MongoDB `explain` results to ensure that queries are executed efficiently (e.g., using indexes).

## Key Components

### ExplainAnalyzeResultFactory

The `ExplainAnalyzeResultFactory` is responsible for parsing the `Document` returned by a MongoDB `explain()` command and creating an `ExplainAnalyzeResult` object. It navigates through the complex explain output (handling both standard and `$cursor` stages) to extract the winning plan.

### ExplainAnalyzeResult

An interface that provides a simplified view of the query's execution plan.

**Key Features:**
*   **Index Usage:** Identify if the query resulted in an `IXSCAN`, `COLLSCAN`, or a `FETCH` with an underlying index scan.
*   **Index Match Stages (Experimental):** Attempts to reverse-engineer the `$match` stages that would be equivalent to the index bounds used in the query. This information can be used to automatically generate more performant aggregation pipelines in libraries like Jamolingo by ensuring filters are applied as early as possible using index-compatible structures.
*   **Resolution Exceptions (Experimental):** Provides access to any exceptions that occurred during the experimental resolution of index match stages.

## Usage Example

```java
import com.github.starnowski.jamolingo.perf.ExplainAnalyzeResult;
import com.github.starnowski.jamolingo.perf.ExplainAnalyzeResultFactory;
import org.bson.Document;

// 1. Execute an explain command in MongoDB
Document explainOutput = collection.find(query).explain();

// 2. Use the factory to parse the result
ExplainAnalyzeResultFactory factory = new ExplainAnalyzeResultFactory();
ExplainAnalyzeResult result = factory.build(explainOutput);

// 3. Verify index usage
if (result.getIndexValue() == ExplainAnalyzeResult.IndexValueRepresentation.IXSCAN) {
    // Query is covered by an index
}
```

## Experimental Features

The following methods in `ExplainAnalyzeResult` are currently considered **experimental**:
*   `getIndexMatchStages()`
*   `getResolutionIndexMatchStagesException()`

Their behavior, accuracy, and API signature may change in future releases as we refine the logic for resolving complex index bounds back into OData-compatible match stages.
