
#### Adding support for the $expand operator without $level or with $level=1

In standard MongoDB, the $lookup aggregation stage is designed to perform a "left outer join" to a collection within the same database. If you try to reference a collection in a different database inside a standard $lookup query, MongoDB will throw an error because it doesn't have cross-database context during the aggregation pipeline.

https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/#mongodb-pipeline-pipe.-lookup

_Performs a left outer join to a collection in the same database to filter in documents from the foreign collection for processing._ 

#### Lack of built-in support for hierarchical transformations in Olingo 5.0.0

The `42` issue originally included a requirement for supporting hierarchical transformations within the `$apply` query option, specifically for `ancestors`, `descendants`, and `traverse`.
However, analysis of the bundled Apache Olingo 5.0.0 library (`org.apache.olingo.server.core.uri.parser.ApplyParser` and `UriTokenizer`) revealed that these operators are not supported as built-in tokens or AST nodes.
Therefore, native support for these transformations is not present, and they have been removed from the plan.

#### Lack of support for filtering by dynamic properties in Olingo 5.0.0

https://issues.apache.org/jira/browse/OLINGO-1303
https://issues.apache.org/jira/browse/OLINGO-737

Olingo 4.5.0 includes openType in the metadata, however I cannot see how to enable serializing of properties that are not included in the metadata (dynamic properties).

It appears that the results serializer will only serialize those properties that are declared in the metadata
