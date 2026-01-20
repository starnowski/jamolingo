# Jamolingo - Core Module

The `core` module contains the primary logic for translating OData concepts and queries into MongoDB-compatible formats. It provides the essential building blocks for mapping OData Entity Data Models (EDM) to MongoDB document structures and parsing OData system query options.

## Features

### OData Operators Support

#### $select

The `$select` operator allows clients to specify a subset of properties to be returned by the service. The `core` module translates this into a MongoDB `$project` aggregation stage.

**Usage:**

The `OdataSelectToMongoProjectParser` class is responsible for this translation.

```java
import com.github.starnowski.jamolingo.select.OdataSelectToMongoProjectParser;
import com.github.starnowski.jamolingo.select.SelectOperatorResult;
import com.github.starnowski.jamolingo.context.DefaultEdmMongoContextFacade;
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
TODO

#### $orderby
TODO

#### $top

The `$top` operator specifies the maximum number of items to return. The `core` module translates this into a MongoDB aggregation stage.

**Translation Details:**
- **Positive values:** Translates to a `$limit` aggregation stage.

**Usage:**

The `OdataTopToMongoLimitParser` class is responsible for this translation.

```java
import com.github.starnowski.jamolingo.top.OdataTopToMongoLimitParser;
import com.github.starnowski.jamolingo.top.TopOperatorResult;
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
import com.github.starnowski.jamolingo.skip.OdataSkipToMongoSkipParser;
import com.github.starnowski.jamolingo.skip.SkipOperatorResult;
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
import com.github.starnowski.jamolingo.context.ODataMongoMappingFactory;
import com.github.starnowski.jamolingo.context.ODataMongoMapping;
import com.github.starnowski.jamolingo.context.EntityPropertiesMongoPathContextBuilder;
import com.github.starnowski.jamolingo.context.EntityPropertiesMongoPathContext;

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
