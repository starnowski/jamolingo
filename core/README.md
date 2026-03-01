# Jamolingo - Core Module

The `core` module contains the primary logic for translating OData concepts and queries into MongoDB-compatible formats. It provides the essential building blocks for mapping OData Entity Data Models (EDM) to MongoDB document structures and parsing OData system query options.

## Features

### OData Operators Support

#### $select

The `$select` operator allows clients to specify a subset of properties to be returned by the service. The `core` module translates this into a MongoDB `$project` aggregation stage.

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

The `$filter` operator allows clients to filter a collection of resources. The `core` module translates this into a MongoDB `$match` aggregation stage.

**Translation Details:**
- Translates to a `$match` aggregation stage.
- Supports a wide range of OData filter expressions, including:
    - **Comparison operators:** `eq`, `ne`, `gt`, `ge`, `lt`, `le`, `in`.
    - **Logical operators:** `and`, `or`, `not`.
    - **String functions:** `tolower`, `toupper`, `trim`, `contains`, `startswith`, `endswith`, `length`.
    - **Math functions:** `floor`, `ceiling`, `round`.
    - **Date and time functions:** `year`, `month`, `day`, `hour`, `minute`, `second`.
    - **Collection operators:** `any()`, `all()`.
        - **all operator:** Applies a Boolean expression to each member of a collection and returns true if the expression is true for all members of the collection, otherwise it returns false. As per [OData specification](https://docs.oasis-open.org/odata/odata/v4.01/os/part2-url-conventions/odata-v4.01-os-part2-url-conventions.html#sec_all), the `all` operator always returns true for an empty collection. This also applies when the collection property is `null` or does not exist in the document.
        - **any operator:** Applies a Boolean expression to each member of a collection and returns true if and only if the expression is true for any member of the collection, otherwise it returns false. This implies that the `any` operator always returns false for an empty collection. The `any` operator can also be used without an argument expression (e.g., `any()`); this short form returns true if the collection is not empty and false if the collection is empty, as per [OData specification](https://docs.oasis-open.org/odata/odata/v4.01/os/part2-url-conventions/odata-v4.01-os-part2-url-conventions.html#sec_any). This makes `any()` a useful way to check if a collection is not empty.
    - **Collection count:** `/$count`.
- Handles nested property paths and complex types.
- Supports mapping overrides for MongoDB field names and nested structures (e.g., wrapper objects).

**Usage:**

The `ODataFilterToMongoMatchParser` class is responsible for this translation.

```java
import com.github.starnowski.jamolingo.core.operators.filter.ODataFilterToMongoMatchParser;
import com.github.starnowski.jamolingo.core.operators.filter.FilterOperatorResult;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.commons.api.edm.Edm;
// ... other imports

// 1. Initialize the parser
ODataFilterToMongoMatchParser parser = new ODataFilterToMongoMatchParser();

// 2. Obtain the FilterOption and Edm
FilterOption filterOption = uriInfo.getFilterOption();
Edm edm = ...;

// 3. (Optional) Provide a context facade if you have custom mappings
// EdmPropertyMongoPathResolver contextFacade = ...; 

// 4. Parse the option
// If using default context:
FilterOperatorResult result = parser.parse(filterOption, edm);
// If using custom context:
// FilterOperatorResult result = parser.parse(filterOption, edm, contextFacade);

// 5. Use the result in your MongoDB aggregation pipeline
List<Bson> stages = result.getStageObjects();
// e.g. collection.aggregate(stages);
```

#### $orderby

The `$orderby` operator specifies the sort order of the returned items. The `core` module translates this into a MongoDB aggregation stage.

**Translation Details:**
- Translates to a `$sort` aggregation stage.

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

#### $apply
TODO

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
