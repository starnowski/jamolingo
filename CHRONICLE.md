
#### Adding support for the $expand operator without $level or with $level=1

In standard MongoDB, the $lookup aggregation stage is designed to perform a "left outer join" to a collection within the same database. If you try to reference a collection in a different database inside a standard $lookup query, MongoDB will throw an error because it doesn't have cross-database context during the aggregation pipeline.

https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/#mongodb-pipeline-pipe.-lookup

_Performs a left outer join to a collection in the same database to filter in documents from the foreign collection for processing._ 