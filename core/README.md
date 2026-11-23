# Jamolingo - Core Module

The `core` module is primarily an OData query translator and not a full OData server implementation, but it can be used as a building block to implement one. It contains the logic for translating OData system query options and concepts into MongoDB-compatible formats. It provides the essential building blocks for mapping OData Entity Data Models (EDM) to MongoDB document structures.

## Features

### OData Operators Support

#### $select

The `$select` operator allows clients to specify a subset of properties to be returned by the service. The `core` module translates this into a MongoDB `$project` aggregation stage.

**Translation Details:**
- Translates to a `$project` aggregation stage.
- Supports primitive properties, complex property paths, and navigation properties (including navigation properties nested inside complex properties).
- Reports any referenced navigation properties via `getUsedNavigationProperties()`.

**Usage:**

The `OdataSelectToMongoProjectParser` class is responsible for this translation.

```java
import com.github.starnowski.jamolingo.core.operators.select.OdataSelectToMongoProjectParser;
import com.github.starnowski.jamolingo.core.operators.select.SelectOperatorResult;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
// ... other imports

// 1. Initialize the parser
OdataSelectToMongoProjectParser parser = new OdataSelectToMongoProjectParser();

        // 2. Obtain the SelectOption from the Olingo UriInfo
        SelectOption selectOption = uriInfo.getSelectOption();

// 3. (Optional) Provide a context facade if you have custom mappings
// EdmMongoContextFacade contextFacade = ...; 

        // 4. Parse the option
// If using default context:
// SelectOperatorResult result = parser.parse(selectOption);
// If using custom context:
        SelectOperatorResult result = parser.parse(selectOption, contextFacade);

        // 5. Use the result in your MongoDB aggregation pipeline
        Bson projectStage = result.getStageObject();
// e.g. collection.aggregate(Arrays.asList(matchStage, projectStage));
```

#### $filter

The `$filter` operator allows clients to filter a collection of resources. The `core` module translates this into a MongoDB `$match` aggregation stage or a standalone query object.

**Translation Details:**
- Translates to a `$match` aggregation stage or a Bson query object.
- Supports a wide range of OData filter expressions, including:
    - **Comparison operators:** `eq`, `ne`, `gt`, `ge`, `lt`, `le`, `in`.
    - **Logical operators:** `and`, `or`, `not`.
    - **Arithmetic operators:** `add`, `sub`, `mul`, `div`, `mod`.
    - **String functions:** `tolower`, `toupper`, `trim`, `contains`, `startswith`, `endswith`, `length`.
    - **Math functions:** `floor`, `ceiling`, `round`.
    - **Date and time functions:** `year`, `month`, `day`, `hour`, `minute`, `second`.
    - **Collection operators:** `any()`, `all()`.
        - **all operator:** Applies a Boolean expression to each member of a collection and returns true if the expression is true for all members of the collection, otherwise it returns false. As per [OData specification](https://docs.oasis-open.org/odata/odata/v4.01/os/part2-url-conventions/odata-v4.01-os-part2-url-conventions.html#sec_all), the `all` operator always returns true for an empty collection. This also applies when the collection property is `null` or does not exist in the document.
        - **any operator:** Applies a Boolean expression to each member of a collection and returns true if and only if the expression is true for any member of the collection, otherwise it returns false. This implies that the `any` operator always returns false for an empty collection. The `any` operator can also be used without an argument expression (e.g., `any()`); this short form returns true if the collection is not empty and false if the collection is empty, as per [OData specification](https://docs.oasis-open.org/odata/odata/v4.01/os/part2-url-conventions/odata-v4.01-os-part2-url-conventions.html#sec_any). This makes `any()` a useful way to check if a collection is not empty.
    - **Collection count:** `/$count`.
- Handles nested property paths, complex types, and navigation property paths (single-valued, multi-hop, navigation properties nested inside complex types, entity `null` comparisons, and collection-valued navigation properties via `any()`, `all()`, and `/$count`).
- Reports all referenced navigation properties via `getUsedNavigationProperties()`.
- Supports mapping overrides for MongoDB field names and nested structures (e.g., wrapper objects).

**Usage:**

The `ODataFilterToMongoMatchParser` class is responsible for this translation.

```java
import com.github.starnowski.jamolingo.core.operators.filter.ODataFilterToMongoMatchParser;
import com.github.starnowski.jamolingo.core.operators.filter.FilterOperatorResult;
import com.github.starnowski.jamolingo.core.operators.filter.FilterOperatorQueryObjectResult;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.commons.api.edm.Edm;
// ... other imports

// 1. Initialize the parser
ODataFilterToMongoMatchParser parser = new ODataFilterToMongoMatchParser();

// 2. Obtain the FilterOption and Edm
FilterOption filterOption = uriInfo.getFilterOption();

// 3. (Optional) Provide a context facade if you have custom mappings
// EdmPropertyMongoPathResolver contextFacade = ...; 

// 4. Parse the option to an aggregation pipeline result
// If using default context:
FilterOperatorResult result = parser.parse(filterOption);
// If using custom context:
// FilterOperatorResult result = parser.parse(filterOption, edm, contextFacade);

// 5. Use the result in your MongoDB aggregation pipeline
List<Bson> stages = result.getStageObjects();
// e.g. collection.aggregate(stages);

// Alternatively, you can parse the filter to a raw Bson query object (e.g., for find queries):
// FilterOperatorQueryObjectResult queryObjectResult = parser.parseQueryObject(filterOption);
// if (!queryObjectResult.isAggregationPipelineRequired()) {
//     Bson query = queryObjectResult.getQueryObject();
//     collection.find(query);
// }
```

#### $search

The `$search` operator allows clients to perform full-text search. The `core` module translates this into MongoDB Atlas Search aggregation stages.

**Translation Details:**
- Translates to a `$search` aggregation stage.
- Optionally adds `$set` and `$match` stages to filter by search score.
- Supports logical operators (`AND`, `OR`, `NOT`) within the search expression.
- Allows specifying a minimum score and a custom field name for the score value.

**Usage:**

The `ODataSearchToMongoAtlasSearchParser` class is responsible for this translation.

```java
import com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchParser;
import com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchOptions;
import com.github.starnowski.jamolingo.core.operators.search.DefaultODataSearchToMongoAtlasSearchOptions;
import com.github.starnowski.jamolingo.core.operators.search.SearchOperatorResult;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
// ... other imports

// 1. Initialize the parser with a SearchDocumentFactory
// SearchDocumentFactory factory = ...;
ODataSearchToMongoAtlasSearchParser parser = new ODataSearchToMongoAtlasSearchParser(factory);

// 2. Obtain the SearchOption from the Olingo UriInfo
SearchOption searchOption = uriInfo.getSearchOption();

// 3. (Optional) Provide search options (e.g., minimum score)
ODataSearchToMongoAtlasSearchOptions options = DefaultODataSearchToMongoAtlasSearchOptions.builder()
    .withDefaultTextScore(0.05)
    .withScoreFieldName("search_score")
    .build();

// 4. Parse the option
SearchOperatorResult result = parser.parse(searchOption, options);

// 5. Use the result in your MongoDB aggregation pipeline
List<Bson> stages = result.getStageObjects();
// Or get specific stages
List<Bson> searchStages = result.getSearchStages();
List<Bson> scoreFilterStages = result.getScoreFilterStages();
// e.g. collection.aggregate(stages);
```

#### $expand

The `$expand` operator allows clients to include related resources in the response. The `core` module translates this into MongoDB `$lookup` or `$graphLookup` aggregation stages.

**Translation Details:**
- Translates to a `$lookup` stage for simple expansions.
- Translates to a `$graphLookup` stage when the `$levels` option is used (for recursive expansion).
- Supports nested system query options within `$expand`:
    - `$filter`: Filters related resources.
    - `$orderby`: Sorts related resources.
    - `$select`: Projects specific properties of related resources.
    - `$top` and `$skip`: Implements pagination for related resources.
    - `$apply`: Applies a sequence of transformations (e.g., grouping, aggregating, filtering) to related resources.
- Handles complex "orphan" removal logic in `$graphLookup` to ensure integrity when `$filter` is used.
- Allows limiting the maximum number of explicitly nested `$expand` operations to prevent performance issues (throws `NestedExpandLevelExceededException` when exceeded).
- Throws `CannotHandleExpandWithApplyByGraphLookupException` when an `$expand` operator contains an inner `$apply` option and is configured to be handled by the `$graphLookup` stage (e.g., when `$levels` > 1 or `$levels=max` and `useLookupForLevelGreaterThanOne` is `false`), as `$graphLookup` does not support inner pipeline transformations. To translate inner `$apply` operations for multi-level expansions, configure `useLookupForLevelGreaterThanOne(true)` on `ExpandParserContext`.
- Reports all expanded navigation properties as well as navigation properties referenced in nested `$filter`, `$orderby`, `$select`, `$apply`, and nested `$expand` options via `getUsedNavigationProperties()`.

**Usage:**

The `ODataExpandToMongoAggregationPipelineParser` class is responsible for this translation.

```java
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser;
import com.github.starnowski.jamolingo.core.operators.expand.ExpandOperatorResult;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
// ... other imports

// 1. Initialize the parser
ODataExpandToMongoAggregationPipelineParser parser = new ODataExpandToMongoAggregationPipelineParser();

// 2. Obtain the ExpandOption from the Olingo UriInfo
ExpandOption expandOption = uriInfo.getExpandOption();

// 3. (Optional) Provide an ExpandParserContext for type and collection mapping and nested limits
// import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext;
// ExpandParserContext context = DefaultExpandParserContext.builder()
//     .withMaxAllowedNestedExpandLevel(2)
//     .build();

// 4. Parse the option
ExpandOperatorResult result = parser.parse(expandOption, context);

// 5. Use the result in your MongoDB aggregation pipeline
List<Bson> stages = result.getStageObjects();
// e.g. collection.aggregate(stages);
```

#### $orderby

The `$orderby` operator specifies the sort order of the returned items. The `core` module translates this into a MongoDB aggregation stage.

**Translation Details:**
- Translates to a `$sort` aggregation stage.
- Supports sorting by direct properties, nested complex properties, and navigation property paths (including multi-hop navigation paths).
- Reports any referenced navigation properties via `getUsedNavigationProperties()`.

**Usage:**

The `OdataOrderByToMongoSortParser` class is responsible for this translation.

```java
import com.github.starnowski.jamolingo.core.operators.orderby.OdataOrderByToMongoSortParser;
import com.github.starnowski.jamolingo.core.operators.orderby.OrderByOperatorResult;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
// ... other imports

// 1. Initialize the parser
OdataOrderByToMongoSortParser parser = new OdataOrderByToMongoSortParser();

        // 2. Obtain the OrderByOption from the Olingo UriInfo
        OrderByOption orderByOption = uriInfo.getOrderByOption();

        // 3. Parse the option
        OrderByOperatorResult result = parser.parse(orderByOption);

        // 4. Use the result in your MongoDB aggregation pipeline
        List<Bson> stages = result.getStageObjects();
// e.g. collection.aggregate(stages);
```

#### $top

The `$top` operator specifies the maximum number of items to return. The `core` module translates this into a MongoDB aggregation stage.

**Translation Details:**
- **Positive values:** Translates to a `$limit` aggregation stage.

**Usage:**

The `OdataTopToMongoLimitParser` class is responsible for this translation.

```java
import com.github.starnowski.jamolingo.core.operators.top.OdataTopToMongoLimitParser;
import com.github.starnowski.jamolingo.core.operators.top.TopOperatorResult;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
// ... other imports

// 1. Initialize the parser
OdataTopToMongoLimitParser parser = new OdataTopToMongoLimitParser();

        // 2. Obtain the TopOption from the Olingo UriInfo
        TopOption topOption = uriInfo.getTopOption();

        // 3. Parse the option
        TopOperatorResult result = parser.parse(topOption);

        // 4. Use the result in your MongoDB aggregation pipeline
        List<Bson> stages = result.getStageObjects();
// e.g. collection.aggregate(stages);
```

#### $skip

The `$skip` operator specifies the number of items to skip at the start of the collection. The `core` module translates this into a MongoDB aggregation stage.

**Translation Details:**
- Translates to a `$skip` aggregation stage.

**Usage:**

The `OdataSkipToMongoSkipParser` class is responsible for this translation.

```java
import com.github.starnowski.jamolingo.core.operators.skip.OdataSkipToMongoSkipParser;
import com.github.starnowski.jamolingo.core.operators.skip.SkipOperatorResult;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
// ... other imports

// 1. Initialize the parser
OdataSkipToMongoSkipParser parser = new OdataSkipToMongoSkipParser();

        // 2. Obtain the SkipOption from the Olingo UriInfo
        SkipOption skipOption = uriInfo.getSkipOption();

        // 3. Parse the option
        SkipOperatorResult result = parser.parse(skipOption);

        // 4. Use the result in your MongoDB aggregation pipeline
        List<Bson> stages = result.getStageObjects();
// e.g. collection.aggregate(stages);
```

#### $count

The `$count` operator specifies whether the total number of items in the result-set should be returned along with the results. The `core` module translates this into a MongoDB `$count` aggregation stage.

**Translation Details:**
- Translates to a `$count` aggregation stage.
- Handles the `$count` system query option (e.g., `?$count=true`).
- Allows specifying a custom field name for the count result (default is "count").

**Usage:**

The `OdataCountToMongoCountParser` class is responsible for this translation.

```java
import com.github.starnowski.jamolingo.core.operators.count.OdataCountToMongoCountParser;
import com.github.starnowski.jamolingo.core.operators.count.CountOperatorResult;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
// ... other imports

// 1. Initialize the parser
OdataCountToMongoCountParser parser = new OdataCountToMongoCountParser();

// 2. Obtain the CountOption from the Olingo UriInfo
CountOption countOption = uriInfo.getCountOption();

// 3. Parse the option
// If using default count field name ("count"):
CountOperatorResult result = parser.parse(countOption);
// If using custom count field name:
// CountOperatorResult result = parser.parse(countOption, "totalCount");

// 4. Use the result in your MongoDB aggregation pipeline
List<Bson> stages = result.getStageObjects();
// e.g. collection.aggregate(stages);
```

#### $apply

The `$apply` operator specifies a sequence of set transformations, such as grouping, aggregating, and filtering. The `core` module translates this into a sequence of MongoDB aggregation stages.

**Translation Details:**
- Translates to multiple aggregation stages based on the transformations used, such as `$match`, `$group`, `$sort`, `$limit`, `$skip`, `$set`, `$project`, `$lookup`, etc.
- Supported apply item kinds include: `FILTER`, `IDENTITY`, `GROUP_BY`, `ORDERBY`, `TOP`, `SKIP`, `AGGREGATE`, `COMPUTE`, `CONCAT`, `SEARCH`, `BOTTOM_TOP`, `EXPAND`.
- Supports navigation property paths across transformations, including:
    - `filter`, `orderby`, `compute`, `bottomtop` (`topcount`, `bottomcount`, `topsum`, `bottomsum`, `toppercent`, `bottompercent`), `expand`, and `concat`.
    - `groupby`: Supports multi-segment complex and navigation property paths as well as `$rollup` items.
    - `aggregate`: Supports single-valued navigation property paths, arithmetic and scalar expressions across navigation properties, inline aggregations over collection-valued navigation properties (e.g., `Orders(Amount with sum as Total)`), collection navigation `/$count` (with or without standard aggregation methods), and `from` clauses with navigation paths.
- Reports all referenced navigation properties across all transformations via `getUsedNavigationProperties()`.
- The `SEARCH` item kind requires providing an `ApplySearchToMongoPipelineParser` implementation if it's used.
- The `EXPAND` item kind optionally accepts an `ODataExpandToMongoAggregationPipelineParser` implementation (a default parser is used if not provided).

**Usage:**

The `ODataApplyToMongoAggregationPipelineParser` class is responsible for this translation.

```java
import com.github.starnowski.jamolingo.core.operators.apply.ODataApplyToMongoAggregationPipelineParser;
import com.github.starnowski.jamolingo.core.operators.apply.ApplyOperatorResult;
import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import org.apache.olingo.server.api.uri.queryoption.ApplyOption;
import org.bson.conversions.Bson;
import java.util.List;
// ... other imports

// 1. Initialize the parser
ODataApplyToMongoAggregationPipelineParser parser = new ODataApplyToMongoAggregationPipelineParser();

// 2. Obtain the ApplyOption from the Olingo UriInfo
ApplyOption applyOption = uriInfo.getApplyOption();
EdmPropertyMongoPathResolver contextFacade = ...; // Your context facade

// 3. Parse the option
ApplyOperatorResult result = parser.parse(applyOption, contextFacade);

// 4. Use the result in your MongoDB aggregation pipeline
List<Bson> stages = result.getStageObjects();
// e.g. collection.aggregate(stages);
```

### Navigation Properties and Lookup Stages Builder

Operators that can reference OData navigation properties (`$filter`, `$orderby`, `$select`, `$expand`, and `$apply`) expose metadata about all traversed navigation property hops via `OlingoOperatorResult.getUsedNavigationProperties()`, which returns a list of `UsedNavigationProperty` instances.

Each `UsedNavigationProperty` captures:
- `edmPath`: The EDM path to the navigation property (using `/` separator).
- `mongoPath`: The resolved MongoDB document path (using `.` notation).
- `edmPropertyName`: The name of the navigation property segment.
- `edmEntityFullName`: The full qualified name of the target EDM entity type.
- `collection`: Whether the navigation property is collection-valued.
- `localKeyProperty` and `foreignKeyProperty`: The local and foreign referential constraint property names.

When navigation properties are referenced in operators such as `$filter`, `$orderby`, or `$apply`, you can use `UsedNavigationPropertyLookupStagesBuilder` to generate the required MongoDB `$lookup` (and `$unwind` for single-valued navigation properties, or nested `$lookup` pipelines for multi-hop collection navigation paths) stages to prepend before the operator stages.

**Usage:**

```java
import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.operators.UsedNavigationProperty;
import com.github.starnowski.jamolingo.core.operators.filter.FilterOperatorResult;
import com.github.starnowski.jamolingo.core.utils.DefaultNavigationPropertyLookupContext;
import com.github.starnowski.jamolingo.core.utils.NavigationPropertyLookupContext;
import com.github.starnowski.jamolingo.core.utils.UsedNavigationPropertyLookupStagesBuilder;
import org.bson.conversions.Bson;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 1. Parse an operator that references navigation properties (e.g., $filter=Category/Name eq 'Books')
FilterOperatorResult filterResult = filterParser.parse(filterOption, contextFacade);
List<UsedNavigationProperty> usedNavProps = filterResult.getUsedNavigationProperties();

// 2. Configure the lookup context with entity-to-collection and property mappings
NavigationPropertyLookupContext lookupContext = DefaultNavigationPropertyLookupContext.builder()
    .withRootEdmEntityTypeName("MyNamespace.Product")
    .withRootEdmMongoContextFacade(contextFacade)
    .withEdmTypeMapping(Map.of(
        "MyNamespace.Category", categoryContextFacade
    ))
    .withEdmTablesToMongoDBCollections(Map.of(
        new KeyValue<>("MyNamespace", "Category"), "categories"
    ))
    .build();

// 3. Build the $lookup (and $unwind) stages for the used navigation properties
UsedNavigationPropertyLookupStagesBuilder lookupBuilder = new UsedNavigationPropertyLookupStagesBuilder();
List<Bson> lookupStages = lookupBuilder.build(usedNavProps, lookupContext);

// 4. Combine the lookup stages with the operator stages
List<Bson> pipeline = new ArrayList<>();
pipeline.addAll(lookupStages);
pipeline.addAll(filterResult.getStageObjects());
```

## Configuration and Mapping

The library requires a mapping between your OData EDM and your MongoDB database schema. This is handled by the `ODataMongoMappingFactory` and related classes.

### Loading EDM Configuration

The `ODataMongoMappingFactory` builds a mapping configuration from your Olingo `Edm` or `EdmSchema` provider.

```java
import com.github.starnowski.jamolingo.core.mapping.ODataMongoMappingFactory;
import com.github.starnowski.jamolingo.core.mapping.ODataMongoMapping;
import com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContextBuilder;
import com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContext;

// 1. Load your Olingo Edm
Edm edm = ...;

        // 2. Build the initial mapping for a specific schema
        ODataMongoMappingFactory factory = new ODataMongoMappingFactory();
        ODataMongoMapping odataMapping = factory.build(edm.getSchema("MyNamespace"));

        // 3. Get mapping for a specific entity
        var entityMapping = odataMapping.getEntities().get("MyEntityName");

        // 4. Build the context required for parsing
        EntityPropertiesMongoPathContextBuilder contextBuilder = new EntityPropertiesMongoPathContextBuilder();
        EntityPropertiesMongoPathContext context = contextBuilder.build(entityMapping);
```

### Overriding Configuration

You can customize the default mapping (e.g., change MongoDB field names, handle nested structures differently) by overriding the generated `EntityMapping`. This is typically done by merging a JSON configuration payload into the default mapping.

**Example Override JSON:**

```json
{
    "properties": {
        "plainString": {
            "mongoName": "customMongoFieldName"
        },
        "ComplexProperty": {
            "properties": {
                "NestedProperty": {
                     "mongoName": "shortName"
                }
            }
        }
    }
}
```

**Applying the Override:**

You can use a helper (like `JSONOverrideHelper` in the [`json`](../common/json) module, if available) or manually manipulate the `EntityMapping` object before building the context.

```java
// Assuming you have a helper to merge JSON into the EntityMapping object
// JSONOverrideHelper helper = new JSONOverrideHelper();
// entityMapping = helper.applyChangesToJson(entityMapping, overrideJsonPayload, EntityMapping.class, JSONOverrideHelper.PatchType.MERGE);

// Then rebuild the context with the modified mapping
EntityPropertiesMongoPathContext context = contextBuilder.build(entityMapping);
```



### Links

[OData 4.0.1 specification](https://docs.oasis-open.org/odata/odata/v4.01/os/part2-url-conventions/odata-v4.01-os-part2-url-conventions.html)
