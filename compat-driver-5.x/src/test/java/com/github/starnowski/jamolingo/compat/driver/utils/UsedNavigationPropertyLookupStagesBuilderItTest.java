package com.github.starnowski.jamolingo.compat.driver.utils;

import static com.github.starnowski.jamolingo.AbstractItTest.TEST_DATABASE;
import static com.github.starnowski.jamolingo.compat.driver.operators.expand.nestedtree.ExpandOperatorWithHandlingTreeRelationsTestProperties.*;

import com.github.starnowski.jamolingo.AbstractItTest;
import com.github.starnowski.jamolingo.EmbeddedMongoResource;
import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContextBuilder;
import com.github.starnowski.jamolingo.core.mapping.EntityMapping;
import com.github.starnowski.jamolingo.core.mapping.ODataMongoMapping;
import com.github.starnowski.jamolingo.core.mapping.ODataMongoMappingFactory;
import com.github.starnowski.jamolingo.core.operators.apply.ApplyOperatorResult;
import com.github.starnowski.jamolingo.core.operators.apply.ODataApplyToMongoAggregationPipelineParser;
import com.github.starnowski.jamolingo.core.operators.filter.FilterOperatorResult;
import com.github.starnowski.jamolingo.core.operators.filter.ODataFilterToMongoMatchParser;
import com.github.starnowski.jamolingo.core.operators.orderby.OdataOrderByToMongoSortParser;
import com.github.starnowski.jamolingo.core.operators.orderby.OrderByOperatorResult;
import com.github.starnowski.jamolingo.core.utils.DefaultNavigationPropertyLookupContext;
import com.github.starnowski.jamolingo.core.utils.NavigationPropertyLookupContext;
import com.github.starnowski.jamolingo.core.utils.UsedNavigationPropertyLookupStagesBuilder;
import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Assertions;
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
          collection = CATEGORY_COLLECTION,
          bsonFilePath = "bson/expand/tree/category1.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = CATEGORY_COLLECTION,
          bsonFilePath = "bson/expand/tree/category2.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_1.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_2.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_3.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_4.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_5.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_6.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_7.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_8.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_10.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_11_p_t1_10.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE2_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_1_t2_1.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE2_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_1_t2_2.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE2_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_1_t2_3.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE2_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_2_t2_4.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE2_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_2_t2_5.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE2_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_2_t2_6.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE3_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_1_t2_1_t3_1.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE3_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_1_t2_1_t3_2.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE3_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_1_t2_1_t3_3.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE4_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_1_t2_1_t3_1_t4_1.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE4_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_1_t2_1_t3_1_t4_2.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE4_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_1_t2_1_t3_1_t4_3.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = "custom_categories_col",
          bsonFilePath = "bson/expand/tree/category1.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = "custom_categories_col",
          bsonFilePath = "bson/expand/tree/category2.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = "custom_treetype1_col",
          bsonFilePath = "bson/expand/tree/mapping/t1_1.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = "custom_treetype1_col",
          bsonFilePath = "bson/expand/tree/mapping/t1_2.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = "custom_treetype1_col",
          bsonFilePath = "bson/expand/tree/mapping/t1_3.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = "custom_treetype1_col",
          bsonFilePath = "bson/expand/tree/mapping/t1_4.json")
    })
@QuarkusTest
@QuarkusTestResource(value = EmbeddedMongoResource.class, restrictToAnnotatedClass = true)
public class UsedNavigationPropertyLookupStagesBuilderItTest extends AbstractItTest {

  private static final KeyValue<String, String> TREETYPE4_MONGO_COLLECTION_USAGE_INFO =
      new KeyValue<>(TREETYPE4_COLLECTION, "treeType4s");

  @Inject protected MongoClient mongoClient;

  private static Stream<Arguments> provideFilterWithNavigationTestCases() {
    return Stream.of(
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3, 5),
            "$filter=category/name eq 'Category 2'",
            Set.of(3)),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3, 5),
            "$filter=parent/index eq 1",
            Set.of(2, 5)),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3),
            "$filter=parent eq null",
            Set.of(1)),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3, 4),
            "$filter=parent/category/name eq 'Category 1'",
            Set.of(2, 3)),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3, 4),
            "$filter=parent/parent/index eq 1",
            Set.of(3)),
        Arguments.of(
            TREETYPE4_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3),
            "$filter=treeType3/treeType2/treeType1/category/name eq 'Category 1'",
            Set.of(1, 2, 3)),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3),
            "$filter=children/any(c:c/index eq 5)",
            Set.of(1)),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2),
            "$filter=children/all(c:c/categoryId eq 2)",
            Set.of(2)),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3),
            "$filter=treeType2s/$count eq 3",
            Set.of(1, 2)),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 5),
            "$filter=children/any(c:c/category/name eq 'Category 2')",
            Set.of(2)));
  }

  @ParameterizedTest(name = "{2} - {0} - {1}")
  @MethodSource("provideFilterWithNavigationTestCases")
  public void shouldFilterDocumentsUsingGeneratedLookupStages(
      KeyValue<String, String> rootMongoCollection,
      Set<Integer> initialIds,
      String filterQuery,
      Set<Integer> expectedIds)
      throws Exception {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase(TEST_DATABASE);
    MongoCollection<Document> collection = database.getCollection(rootMongoCollection.getKey());
    Edm edm = loadEmdProvider("edm/tree_types.xml");
    UriInfo uriInfo =
        new Parser(edm, OData.newInstance())
            .parseUri(rootMongoCollection.getValue(), filterQuery, null, null);

    ODataFilterToMongoMatchParser filterParser = new ODataFilterToMongoMatchParser();
    UsedNavigationPropertyLookupStagesBuilder lookupBuilder =
        new UsedNavigationPropertyLookupStagesBuilder();

    // WHEN
    FilterOperatorResult filterResult = filterParser.parse(uriInfo.getFilterOption());
    List<Bson> pipeline = new ArrayList<>();
    pipeline.add(new Document("$match", new Document("_id", new Document("$in", initialIds))));
    pipeline.addAll(lookupBuilder.build(filterResult.getUsedNavigationProperties()));
    pipeline.addAll(filterResult.getStageObjects());

    List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());

    // THEN
    Set<Integer> actualIds =
        results.stream()
            .map(doc -> doc.getInteger("_id"))
            .collect(java.util.stream.Collectors.toSet());
    Assertions.assertEquals(expectedIds, actualIds);
  }

  private static Stream<Arguments> provideOrderByWithNavigationTestCases() {
    return Stream.of(
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3, 4),
            "$orderby=category/name desc,index asc",
            List.of(3, 4, 1, 2)),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(2, 3, 4),
            "$orderby=parent/category/name desc,index asc",
            List.of(4, 2, 3)));
  }

  @ParameterizedTest(name = "{2} - {0} - {1}")
  @MethodSource("provideOrderByWithNavigationTestCases")
  public void shouldOrderDocumentsUsingGeneratedLookupStages(
      KeyValue<String, String> rootMongoCollection,
      Set<Integer> initialIds,
      String orderByQuery,
      List<Integer> expectedOrderedIds)
      throws Exception {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase(TEST_DATABASE);
    MongoCollection<Document> collection = database.getCollection(rootMongoCollection.getKey());
    Edm edm = loadEmdProvider("edm/tree_types.xml");
    UriInfo uriInfo =
        new Parser(edm, OData.newInstance())
            .parseUri(rootMongoCollection.getValue(), orderByQuery, null, null);

    OdataOrderByToMongoSortParser orderByParser = new OdataOrderByToMongoSortParser();
    UsedNavigationPropertyLookupStagesBuilder lookupBuilder =
        new UsedNavigationPropertyLookupStagesBuilder();

    // WHEN
    OrderByOperatorResult orderByResult = orderByParser.parse(uriInfo.getOrderByOption());
    List<Bson> pipeline = new ArrayList<>();
    pipeline.add(new Document("$match", new Document("_id", new Document("$in", initialIds))));
    pipeline.addAll(lookupBuilder.build(orderByResult.getUsedNavigationProperties()));
    pipeline.addAll(orderByResult.getStageObjects());

    List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());

    // THEN
    List<Integer> actualIds = results.stream().map(doc -> doc.getInteger("_id")).toList();
    Assertions.assertEquals(expectedOrderedIds, actualIds);
  }

  private static Stream<Arguments> provideApplyWithNavigationTestCases() {
    return Stream.of(
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3),
            "$apply=filter(category/name eq 'Category 2')",
            """
            [{ "_id": 3, "index": 3, "parentId": 2, "categoryId": 2 }]
            """,
            JSONCompareMode.LENIENT),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 5),
            "$apply=filter(children/any(c:c/category/name eq 'Category 2'))",
            """
            [{ "_id": 2, "index": 2, "parentId": 1, "categoryId": 1 }]
            """,
            JSONCompareMode.LENIENT),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(2, 3, 4),
            "$apply=groupby((categoryId),aggregate(parent/index with sum as totalParentIndex))",
            """
            [
              { "categoryId": 1, "totalParentIndex": 1 },
              { "categoryId": 2, "totalParentIndex": 5 }
            ]
            """,
            JSONCompareMode.LENIENT),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(2, 3, 4),
            "$apply=groupby((categoryId),aggregate(parent/category/name with countdistinct as distinctParentCategories))",
            """
            [
              { "categoryId": 1, "distinctParentCategories": 1 },
              { "categoryId": 2, "distinctParentCategories": 2 }
            ]
            """,
            JSONCompareMode.LENIENT),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(2, 3, 4),
            "$apply=aggregate(parent/index with sum as parentIndexSum)",
            """
            [{ "parentIndexSum": 6 }]
            """,
            JSONCompareMode.NON_EXTENSIBLE),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2),
            "$apply=aggregate(children/$count as totalChildrenCount)",
            """
            [{ "totalChildrenCount": 3 }]
            """,
            JSONCompareMode.NON_EXTENSIBLE),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(2, 3),
            "$apply=compute(parent/index add index as combinedIndex)",
            """
            [
              { "_id": 2, "index": 2, " combinedIndex": 3 },
              { "_id": 3, "index": 3, "combinedIndex": 5 }
            ]
            """
                .replace("\" combinedIndex\"", "\"combinedIndex\""),
            JSONCompareMode.LENIENT),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(2, 3, 4),
            "$apply=topcount(1,parent/index)",
            """
            [{ "_id": 4, "index": 4, "parentId": 3, "categoryId": 2 }]
            """,
            JSONCompareMode.LENIENT));
  }

  @ParameterizedTest(name = "{2} - {0} - {1}")
  @MethodSource("provideApplyWithNavigationTestCases")
  public void shouldExecuteApplyWithGeneratedLookupStages(
      KeyValue<String, String> rootMongoCollection,
      Set<Integer> initialIds,
      String applyQuery,
      String expectedJson,
      JSONCompareMode jsonCompareMode)
      throws Exception {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase(TEST_DATABASE);
    MongoCollection<Document> collection = database.getCollection(rootMongoCollection.getKey());
    Edm edm = loadEmdProvider("edm/tree_types.xml");
    UriInfo uriInfo =
        new Parser(edm, OData.newInstance())
            .parseUri(rootMongoCollection.getValue(), applyQuery, null, null);

    ODataApplyToMongoAggregationPipelineParser applyParser =
        new ODataApplyToMongoAggregationPipelineParser();
    UsedNavigationPropertyLookupStagesBuilder lookupBuilder =
        new UsedNavigationPropertyLookupStagesBuilder();

    // WHEN
    ApplyOperatorResult applyResult =
        applyParser.parse(uriInfo.getApplyOption(), DefaultEdmMongoContextFacade.builder().build());
    List<Bson> pipeline = new ArrayList<>();
    pipeline.add(new Document("$match", new Document("_id", new Document("$in", initialIds))));
    pipeline.addAll(lookupBuilder.build(applyResult.getUsedNavigationProperties()));
    pipeline.addAll(applyResult.getStageObjects());

    List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());

    // THEN
    String currentResult = new Document("value", results).toJson();
    JSONAssert.assertEquals(
        """
        {"value": %s }
        """.formatted(expectedJson),
        currentResult,
        jsonCompareMode);
  }

  private static Stream<Arguments> provideCustomMappingFilterTestCases() {
    return Stream.of(
        Arguments.of(Set.of(1, 2, 3, 4), "$filter=category/name eq 'Category 2'", Set.of(3, 4)),
        Arguments.of(
            Set.of(1, 2, 3, 4), "$filter=parent/category/name eq 'Category 1'", Set.of(2, 3)));
  }

  @ParameterizedTest(name = "Custom mapping: {1} - {0}")
  @MethodSource("provideCustomMappingFilterTestCases")
  public void shouldExecuteFilterWithCustomCollectionAndPropertyMappings(
      Set<Integer> initialIds, String filterQuery, Set<Integer> expectedIds) throws Exception {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase(TEST_DATABASE);
    MongoCollection<Document> collection = database.getCollection("custom_treetype1_col");
    Edm edm = loadEmdProvider("edm/tree_types.xml");

    ODataMongoMappingFactory factory = new ODataMongoMappingFactory();
    ODataMongoMapping odataMapping = factory.build(edm.getSchema("MyService"));
    EntityPropertiesMongoPathContextBuilder pathContextBuilder =
        new EntityPropertiesMongoPathContextBuilder();

    EntityMapping t1 = odataMapping.getEntities().get("TreeType1");
    t1.getProperties().get("index").setMongoPath("renamed_index");
    t1.getProperties().get("parentId").setMongoPath("renamed_parentId");
    t1.getProperties().get("categoryId").setMongoPath("renamed_categoryId");
    EdmMongoContextFacade t1Facade =
        DefaultEdmMongoContextFacade.builder()
            .withEntityPropertiesMongoPathContext(pathContextBuilder.build(t1))
            .build();

    EntityMapping category = odataMapping.getEntities().get("Category");
    EdmMongoContextFacade categoryFacade =
        DefaultEdmMongoContextFacade.builder()
            .withEntityPropertiesMongoPathContext(pathContextBuilder.build(category))
            .build();

    Map<String, EdmMongoContextFacade> edmTypeMapping = new HashMap<>();
    edmTypeMapping.put("MyService.TreeType1", t1Facade);
    edmTypeMapping.put("MyService.Category", categoryFacade);

    Map<KeyValue<String, String>, String> collectionMapping = new HashMap<>();
    collectionMapping.put(new KeyValue<>("MyService", "TreeType1"), "custom_treetype1_col");
    collectionMapping.put(new KeyValue<>("MyService", "Category"), "custom_categories_col");

    NavigationPropertyLookupContext lookupContext =
        DefaultNavigationPropertyLookupContext.builder()
            .withRootEdmEntityTypeName("MyService.TreeType1")
            .withRootEdmMongoContextFacade(t1Facade)
            .withEdmTypeMapping(edmTypeMapping)
            .withEdmTablesToMongoDBCollections(collectionMapping)
            .build();

    UriInfo uriInfo =
        new Parser(edm, OData.newInstance()).parseUri("treeType1s", filterQuery, null, null);
    ODataFilterToMongoMatchParser filterParser = new ODataFilterToMongoMatchParser();
    UsedNavigationPropertyLookupStagesBuilder lookupBuilder =
        new UsedNavigationPropertyLookupStagesBuilder();

    // WHEN
    FilterOperatorResult filterResult = filterParser.parse(uriInfo.getFilterOption(), t1Facade);
    List<Bson> pipeline = new ArrayList<>();
    pipeline.add(new Document("$match", new Document("_id", new Document("$in", initialIds))));
    pipeline.addAll(lookupBuilder.build(filterResult.getUsedNavigationProperties(), lookupContext));
    pipeline.addAll(filterResult.getStageObjects());

    List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());

    // THEN
    Set<Integer> actualIds =
        results.stream()
            .map(doc -> doc.getInteger("_id"))
            .collect(java.util.stream.Collectors.toSet());
    Assertions.assertEquals(expectedIds, actualIds);
  }
}
