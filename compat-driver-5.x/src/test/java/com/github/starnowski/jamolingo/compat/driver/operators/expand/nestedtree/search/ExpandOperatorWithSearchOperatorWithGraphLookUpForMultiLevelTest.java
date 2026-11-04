package com.github.starnowski.jamolingo.compat.driver.operators.expand.nestedtree.search;

import static com.github.starnowski.jamolingo.AbstractItTest.TEST_DATABASE;
import static com.github.starnowski.jamolingo.compat.driver.operators.expand.nestedtree.ExpandOperatorWithHandlingTreeRelationsTestProperties.TREETYPE1_COLLECTION;
import static com.github.starnowski.jamolingo.compat.driver.operators.expand.nestedtree.ExpandOperatorWithHandlingTreeRelationsTestProperties.TREETYPE1_MONGO_COLLECTION_USAGE_INFO;

import com.github.starnowski.jamolingo.AbstractItTest;
import com.github.starnowski.jamolingo.MongoAtlasResource;
import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.operators.apply.ApplySearchToMongoPipelineParser;
import com.github.starnowski.jamolingo.core.operators.apply.ODataApplyToMongoAtlasSearchPipelineParser;
import com.github.starnowski.jamolingo.core.operators.expand.ExpandOperatorResult;
import com.github.starnowski.jamolingo.core.operators.expand.GraphLookUpToLookUpStrategyResultsTransformer;
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser;
import com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchLookupParser;
import com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchOptions;
import com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchParser;
import com.github.starnowski.jamolingo.core.operators.search.SearchDocumentForQueryStringFactory;
import com.github.starnowski.jamolingo.junit5.AtlasSearchIndex;
import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.search.SearchExpression;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

@MongoSetup(
    batchInsertToCollection = true,
    mongoDocuments = {
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/search/type1_array.json")
    },
    atlasSearchIndexes = {
      @AtlasSearchIndex(
          name = "search_idx",
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/search/text_index.json",
          validationProperty = "name")
    })
@QuarkusTest
@QuarkusTestResource(value = MongoAtlasResource.class, restrictToAnnotatedClass = true)
public class ExpandOperatorWithSearchOperatorWithGraphLookUpForMultiLevelTest
    extends AbstractItTest {

  private static ODataSearchToMongoAtlasSearchParser createSearchParser(String defaultPath) {
    return new ODataSearchToMongoAtlasSearchParser(
        new SearchDocumentForQueryStringFactory() {
          @Override
          public Bson build(
              SearchExpression searchExpression,
              QueryStringParsingResult queryStringParsingResult,
              ODataSearchToMongoAtlasSearchOptions options) {
            return new Document("index", "search_idx")
                .append(
                    "queryString",
                    new Document("query", queryStringParsingResult.getQuery())
                        .append("defaultPath", defaultPath));
          }
        });
  }

  private static Stream<Arguments> provideData() {
    ODataSearchToMongoAtlasSearchParser validSearchParser = createSearchParser("name");
    ODataSearchToMongoAtlasSearchParser invalidFieldSearchParser =
        createSearchParser("nonExistentField");
    ApplySearchToMongoPipelineParser validApplySearchParser =
        new ODataApplyToMongoAtlasSearchPipelineParser(validSearchParser);
    ApplySearchToMongoPipelineParser invalidFieldApplySearchParser =
        new ODataApplyToMongoAtlasSearchPipelineParser(invalidFieldSearchParser);
    String expectedVehicleSearchJson =
        """
                                                    [
                                                        {
                                                            "_id": 1,
                                                            "index": 1,
                                                            "parentId": null,
                                                            "categoryId": 1,
                                                            "children": [
                                                                {
                                                                    "_id": 2,
                                                                    "index": 2,
                                                                    "parentId": 1,
                                                                    "categoryId": 1,
                                                                    "children": [],
                                                                    "name": "Ground Vehicle"
                                                                },
                                                                {
                                                                    "_id": 5,
                                                                    "index": 5,
                                                                    "parentId": 1,
                                                                    "categoryId": 1,
                                                                    "children": [],
                                                                    "name": "Flying Vehicle"
                                                                }
                                                            ]
                                                        }
                                                    ]
                                                    """;
    String complexSearchQueryExpandResult =
        """
                                                    [
                                                        {
                                                            "_id": 1,
                                                            "index": 1,
                                                            "parentId": null,
                                                            "categoryId": 1,
                                                            "children": [
                                                                {
                                                                    "_id": 2,
                                                                    "index": 2,
                                                                    "parentId": 1,
                                                                    "categoryId": 1,
                                                                    "children": [
                                                                        {
                                                                            "_id": 3,
                                                                            "index": 3,
                                                                            "parentId": 2,
                                                                            "categoryId": 2,
                                                                            "children": [
                                                                                {
                                                                                    "_id": 4,
                                                                                    "index": 4,
                                                                                    "parentId": 3,
                                                                                    "categoryId": 2,
                                                                                    "children": []
                                                                                }
                                                                            ]
                                                                        }
                                                                    ]
                                                                },
                                                                {
                                                                    "_id": 5,
                                                                    "index": 5,
                                                                    "parentId": 1,
                                                                    "categoryId": 1,
                                                                    "children": []
                                                                }
                                                            ]
                                                        }
                                                    ]
                                                    """;
    // TODO Check in for category (that is single property and not collection) we can specify the
    // $levels > 1
    return Stream.of(
        // Level without filters
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=5)",
            """
                                            [
                                                {
                                                    "_id": 1,
                                                    "index": 1,
                                                    "parentId": null,
                                                    "categoryId": 1,
                                                    "children": [
                                                        {
                                                            "_id": 2,
                                                            "index": 2,
                                                            "parentId": 1,
                                                            "categoryId": 1,
                                                            "children": [
                                                                {
                                                                    "_id": 3,
                                                                    "index": 3,
                                                                    "parentId": 2,
                                                                    "categoryId": 2,
                                                                    "children": [
                                                                        {
                                                                            "_id": 4,
                                                                            "index": 4,
                                                                            "parentId": 3,
                                                                            "categoryId": 2,
                                                                            "children": [
                                                                                {
                                                                                    "_id": 6,
                                                                                    "index": 6,
                                                                                    "parentId": 4,
                                                                                    "categoryId": 2,
                                                                                    "children": [
                                                                                        {
                                                                                            "_id": 7,
                                                                                            "index": 7,
                                                                                            "parentId": 6,
                                                                                            "categoryId": 2
                                                                                        }
                                                                                    ]
                                                                                }
                                                                            ]
                                                                        }
                                                                    ]
                                                                }
                                                            ]
                                                        },
                                                        {
                                                            "_id": 5,
                                                            "index": 5,
                                                            "parentId": 1,
                                                            "categoryId": 1,
                                                            "children": []
                                                        }
                                                    ]
                                                }
                                            ]
                                            """,
            JSONCompareMode.LENIENT,
            (Consumer<
                    ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder>)
                b -> b.withGlobalSearchParser(validSearchParser)),
        // Global search parser
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=5;$search=Vehicle)",
            expectedVehicleSearchJson,
            JSONCompareMode.LENIENT,
            (Consumer<
                    ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder>)
                b -> b.withGlobalSearchParser(validSearchParser)),
        // Entity-type-specific search parser (takes precedence over global parser)
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=5;$search=Vehicle)",
            expectedVehicleSearchJson,
            JSONCompareMode.LENIENT,
            (Consumer<
                    ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder>)
                b ->
                    b.withGlobalSearchParser(invalidFieldSearchParser)
                        .withEdmTablesToSearchParsers(
                            Map.of(new KeyValue<>("MyService", "TreeType1"), validSearchParser))),
        // Navigation-property-specific search parser (takes precedence over entity-type and global
        // parsers)
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=5;$search=Vehicle)",
            expectedVehicleSearchJson,
            JSONCompareMode.LENIENT,
            (Consumer<
                    ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder>)
                b ->
                    b.withGlobalSearchParser(invalidFieldSearchParser)
                        .withEdmTablesToSearchParsers(
                            Map.of(
                                new KeyValue<>("MyService", "TreeType1"), invalidFieldSearchParser))
                        .withNavigationPropertySearchParsers(
                            Map.of("children", validSearchParser))),
        // $search with more complex query
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=5;$search=Vehicle OR Car)",
            complexSearchQueryExpandResult,
            JSONCompareMode.LENIENT,
            (Consumer<
                    ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder>)
                b -> b.withGlobalSearchParser(validSearchParser)),
        // search for $apply system query parameter with more complex query (global apply search
        // parser)
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=5;$apply=search(Vehicle OR Car))",
            complexSearchQueryExpandResult,
            JSONCompareMode.LENIENT,
            (Consumer<
                    ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder>)
                b -> b.withGlobalApplySearchParser(validApplySearchParser)),
        // Entity-type-specific apply search parser (takes precedence over global apply search
        // parser)
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=5;$apply=search(Vehicle OR Car))",
            complexSearchQueryExpandResult,
            JSONCompareMode.LENIENT,
            (Consumer<
                    ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder>)
                b ->
                    b.withGlobalApplySearchParser(invalidFieldApplySearchParser)
                        .withEdmTablesToApplySearchParsers(
                            Map.of(
                                new KeyValue<>("MyService", "TreeType1"), validApplySearchParser))),
        // Navigation-property-specific apply search parser (takes precedence over entity-type and
        // global apply search parsers)
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=5;$apply=search(Vehicle OR Car))",
            complexSearchQueryExpandResult,
            JSONCompareMode.LENIENT,
            (Consumer<
                    ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder>)
                b ->
                    b.withGlobalApplySearchParser(invalidFieldApplySearchParser)
                        .withEdmTablesToApplySearchParsers(
                            Map.of(
                                new KeyValue<>("MyService", "TreeType1"),
                                invalidFieldApplySearchParser))
                        .withNavigationPropertyApplySearchParsers(
                            Map.of("children", validApplySearchParser))));
  }

  private static Stream<Arguments> provideDataWithSearchQueryParameterAsNotFirstInUrl() {
    ODataSearchToMongoAtlasSearchLookupParser validLookupSearchParser =
        new ODataSearchToMongoAtlasSearchLookupParser(
            TREETYPE1_COLLECTION, createSearchParser("name"));
    ODataSearchToMongoAtlasSearchLookupParser invalidFieldLookupSearchParser =
        new ODataSearchToMongoAtlasSearchLookupParser(
            TREETYPE1_COLLECTION, createSearchParser("nonExistentField"));
    String expectedResults =
        """
                                                        [
                                                            {
                                                                "_id": 1,
                                                                "index": 1,
                                                                "parentId": null,
                                                                "categoryId": 1,
                                                                "children": [
                                                                    {
                                                                        "_id": 2,
                                                                        "index": 2,
                                                                        "indexPlus10": 12,
                                                                        "parentId": 1,
                                                                        "categoryId": 1,
                                                                        "children": [
                                                                            {
                                                                                "_id": 3,
                                                                                "index": 3,
                                                                                "indexPlus10": 13,
                                                                                "parentId": 2,
                                                                                "categoryId": 2,
                                                                                "children": [
                                                                                    {
                                                                                        "_id": 4,
                                                                                        "index": 4,
                                                                                        "indexPlus10": 14,
                                                                                        "parentId": 3,
                                                                                        "categoryId": 2,
                                                                                        "children": []
                                                                                    }
                                                                                ]
                                                                            }
                                                                        ]
                                                                    },
                                                                    {
                                                                        "_id": 5,
                                                                        "index": 5,
                                                                        "indexPlus10": 15,
                                                                        "parentId": 1,
                                                                        "categoryId": 1,
                                                                        "children": []
                                                                    }
                                                                ]
                                                            }
                                                        ]
                                                        """;
    return Stream.of(
        // Global lookup search parser
        // $levels = 5, complex query with the $search query system operator
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=5;$apply=compute(index add 10 as indexPlus10);$search=Vehicle OR Car)",
            expectedResults,
            JSONCompareMode.LENIENT,
            (Consumer<
                    ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder>)
                b -> b.withGlobalLookupSearchParser(validLookupSearchParser)),
        // Entity-type-specific lookup search parser (takes precedence over global lookup search
        // parser)
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=5;$apply=compute(index add 10 as indexPlus10);$search=Vehicle OR Car)",
            expectedResults,
            JSONCompareMode.LENIENT,
            (Consumer<
                    ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder>)
                b ->
                    b.withGlobalLookupSearchParser(invalidFieldLookupSearchParser)
                        .withEdmTablesToLookupSearchParsers(
                            Map.of(
                                new KeyValue<>("MyService", "TreeType1"),
                                validLookupSearchParser))),
        // Navigation-property-specific lookup search parser (takes precedence over entity-type and
        // global lookup search parsers)
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=5;$apply=compute(index add 10 as indexPlus10);$search=Vehicle OR Car)",
            expectedResults,
            JSONCompareMode.LENIENT,
            (Consumer<
                    ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder>)
                b ->
                    b.withGlobalLookupSearchParser(invalidFieldLookupSearchParser)
                        .withEdmTablesToLookupSearchParsers(
                            Map.of(
                                new KeyValue<>("MyService", "TreeType1"),
                                invalidFieldLookupSearchParser))
                        .withNavigationPropertyLookupSearchParsers(
                            Map.of("children", validLookupSearchParser))),
        // $levels = 5, complex query with the search operator that is part of the $apply system
        // query operator (global lookup search parser)
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=5;$apply=compute(index add 10 as indexPlus10)/search(Vehicle OR Car))",
            expectedResults,
            JSONCompareMode.LENIENT,
            (Consumer<
                    ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder>)
                b -> b.withGlobalLookupSearchParser(validLookupSearchParser)),
        // $levels = 5, complex query with the search operator inside $apply (entity-type-specific
        // lookup search parser)
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=5;$apply=compute(index add 10 as indexPlus10)/search(Vehicle OR Car))",
            expectedResults,
            JSONCompareMode.LENIENT,
            (Consumer<
                    ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder>)
                b ->
                    b.withGlobalLookupSearchParser(invalidFieldLookupSearchParser)
                        .withEdmTablesToLookupSearchParsers(
                            Map.of(
                                new KeyValue<>("MyService", "TreeType1"),
                                validLookupSearchParser))),
        // $levels = 5, complex query with the search operator inside $apply
        // (navigation-property-specific lookup search parser)
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=5;$apply=compute(index add 10 as indexPlus10)/search(Vehicle OR Car))",
            expectedResults,
            JSONCompareMode.LENIENT,
            (Consumer<
                    ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder>)
                b ->
                    b.withGlobalLookupSearchParser(invalidFieldLookupSearchParser)
                        .withEdmTablesToLookupSearchParsers(
                            Map.of(
                                new KeyValue<>("MyService", "TreeType1"),
                                invalidFieldLookupSearchParser))
                        .withNavigationPropertyLookupSearchParsers(
                            Map.of("children", validLookupSearchParser))));
  }

  @Inject protected MongoClient mongoClient;

  @ParameterizedTest(name = "{2} - {0} - {1}")
  @MethodSource({"provideData"})
  public void shouldReturnExpectedDocumentsForExpandOperator(
      KeyValue<String, String> rootMongoCollection,
      Set<Integer> ids,
      String expandPart,
      String expectedJson,
      JSONCompareMode jsonCompareMode,
      Consumer<ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder>
          contextConfigurer)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException,
          JSONException {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase(TEST_DATABASE);
    MongoCollection<Document> collection = database.getCollection(rootMongoCollection.getKey());
    Edm edm = loadEmdProvider("edm/tree_types_atlas_search.xml");
    UriInfo uriInfo =
        new Parser(edm, OData.newInstance())
            .parseUri(rootMongoCollection.getValue(), expandPart, null, null);
    ODataExpandToMongoAggregationPipelineParser tested =
        new ODataExpandToMongoAggregationPipelineParser();
    ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder contextBuilder =
        ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.builder()
            .withUseLookupForLevelGreaterThanOne(
                false) // GraphLookUp is suppose to be used to resolve
            .withPropagateGraphLookUpJoinKeys(true);
    contextConfigurer.accept(contextBuilder);

    // WHEN
    ExpandOperatorResult result = tested.parse(uriInfo.getExpandOption(), contextBuilder.build());
    List<Bson> pipeline = new ArrayList<>();
    pipeline.add(new Document("$match", new Document("_id", new Document("$in", ids))));
    pipeline.addAll(result.getStageObjects());
    pipeline.add(new Document("$sort", new Document("index", 1)));
    System.out.println(wrapBsonList(pipeline).toJson());

    List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());
    GraphLookUpToLookUpStrategyResultsTransformer graphLookUpToLookUpStrategyResultsTransformer =
        new GraphLookUpToLookUpStrategyResultsTransformer();
    results = graphLookUpToLookUpStrategyResultsTransformer.transform(results, result);
    String currentResult = wrapDocumentsList(results).toJson();
    System.out.println(currentResult);
    JSONAssert.assertEquals(
        """
            {"value": %s }
            """.formatted(expectedJson),
        currentResult,
        jsonCompareMode);
  }

  @ParameterizedTest(name = "{2} - {0} - {1}")
  @MethodSource({"provideDataWithSearchQueryParameterAsNotFirstInUrl"})
  public void shouldReturnExpectedDocumentsForExpandOperatorWithSearchQueryParameterAsNotFirstOne(
      KeyValue<String, String> rootMongoCollection,
      Set<Integer> ids,
      String expandPart,
      String expectedJson,
      JSONCompareMode jsonCompareMode,
      Consumer<ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder>
          contextConfigurer)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException,
          JSONException {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase(TEST_DATABASE);
    MongoCollection<Document> collection = database.getCollection(rootMongoCollection.getKey());
    Edm edm = loadEmdProvider("edm/tree_types_atlas_search.xml");
    UriInfo uriInfo =
        new Parser(edm, OData.newInstance())
            .parseUri(rootMongoCollection.getValue(), expandPart, null, null);
    ODataExpandToMongoAggregationPipelineParser tested =
        new ODataExpandToMongoAggregationPipelineParser();
    ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder contextBuilder =
        ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.builder()
            .withUseLookupForLevelGreaterThanOne(
                false) // GraphLookUp is suppose to be used to resolve
            .withPropagateGraphLookUpJoinKeys(true);
    contextConfigurer.accept(contextBuilder);

    // WHEN
    ExpandOperatorResult result = tested.parse(uriInfo.getExpandOption(), contextBuilder.build());
    List<Bson> pipeline = new ArrayList<>();
    pipeline.add(new Document("$match", new Document("_id", new Document("$in", ids))));
    pipeline.addAll(result.getStageObjects());
    pipeline.add(new Document("$sort", new Document("index", 1)));
    System.out.println(wrapBsonList(pipeline).toJson());
    List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());
    GraphLookUpToLookUpStrategyResultsTransformer graphLookUpToLookUpStrategyResultsTransformer =
        new GraphLookUpToLookUpStrategyResultsTransformer();
    results = graphLookUpToLookUpStrategyResultsTransformer.transform(results, result);
    String currentResult = wrapDocumentsList(results).toJson();
    System.out.println(currentResult);
    JSONAssert.assertEquals(
        """
                {"value": %s }
                """.formatted(expectedJson),
        currentResult,
        jsonCompareMode);
  }

  private Document wrapBsonList(List<Bson> docs) {
    return new Document("value", docs);
  }

  private Document wrapDocumentsList(List<Document> docs) {
    return new Document("value", docs);
  }
}
