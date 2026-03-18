# Jamolingo - Performance Module

The `perf` module provides tools and utilities for analyzing and verifying the performance of MongoDB queries. It primarily focuses on parsing and interpreting MongoDB `explain` results to ensure that queries are executed efficiently (e.g., using indexes).

## Key Components

### ExplainAnalyzeResultFactory

The `ExplainAnalyzeResultFactory` is responsible for parsing the `Document` returned by a MongoDB `explain()` command and creating an `ExplainAnalyzeResult` object. It navigates through the complex explain output (handling both standard and `$cursor` stages) to extract the winning plan.

### ExplainAnalyzeResult

An interface that provides a simplified view of the query's execution plan.

**Key Features:**
*   **Index Usage:** Identify if the query resulted in an `IXSCAN`, `COLLSCAN`, a `FETCH` with an underlying index scan, or a `SEARCH` stage (Atlas Search).
*   **Index Match Stages (Experimental):** Attempts to reverse-engineer the `$match` stages that would be equivalent to the index bounds used in the query. For Atlas Search queries, it extracts the `$search` stage itself. This information can be used to automatically generate more performant aggregation pipelines in libraries like Jamolingo by ensuring filters are applied as early as possible using index-compatible structures.
*   **Resolution Exceptions (Experimental):** Provides access to any exceptions that occurred during the experimental resolution of index match stages.

## Atlas Search Support

The `perf` module includes support for analyzing queries that utilize MongoDB Atlas Search via the `$search` stage. The `ExplainAnalyzeResultFactory` can detect when an Atlas Search index is being used and represent it as `SEARCH` in the result's index value.

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
