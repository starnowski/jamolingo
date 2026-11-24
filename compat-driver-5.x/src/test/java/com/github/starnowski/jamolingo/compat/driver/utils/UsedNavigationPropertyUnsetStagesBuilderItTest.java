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
import com.github.starnowski.jamolingo.core.utils.UsedNavigationPropertyUnsetStagesBuilder;
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
public class UsedNavigationPropertyUnsetStagesBuilderItTest extends AbstractItTest {

  private static final KeyValue<String, String> TREETYPE4_MONGO_COLLECTION_USAGE_INFO =
      new KeyValue<>(TREETYPE4_COLLECTION, "treeType4s");

  @Inject protected MongoClient mongoClient;

  private static Stream<Arguments> provideFilterWithNavigationAndUnsetTestCases() {
    return Stream.of(
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3, 5),
            "$filter=category/name eq 'Category 2'",
            """
            [
              { "_id": 3, "index": 3, "parentId": 2, "categoryId": 2 }
            ]
            """),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3, 5),
            "$filter=parent/index eq 1",
            """
            [
              { "_id": 2, "index": 2, "parentId": 1, "categoryId": 1 },
              { "_id": 5, "index": 5, "parentId": 1, "categoryId": 1 }
            ]
            """),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3, 4),
            "$filter=parent/category/name eq 'Category 1'",
            """
            [
              { "_id": 2, "index": 2, "parentId": 1, "categoryId": 1 },
              { "_id": 3, "index": 3, "parentId": 2, "categoryId": 2 }
            ]
            """),
        Arguments.of(
            TREETYPE4_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3),
            "$filter=treeType3/treeType2/treeType1/category/name eq 'Category 1'",
            """
            [
              { "_id": 1, "index": 1, "parentId": null, "categoryId": 1, "treeType3Id": 1 },
              { "_id": 2, "index": 2, "parentId": 1, "categoryId": 1, "treeType3Id": 1 },
              { "_id": 3, "index": 3, "parentId": 2, "categoryId": 2, "treeType3Id": 1 }
            ]
            """),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3),
            "$filter=children/any(c:c/index eq 5)",
            """
            [
              { "_id": 1, "index": 1, "parentId": null, "categoryId": 1 }
            ]
            """),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3),
            "$filter=treeType2s/$count eq 3",
            """
            [
              { "_id": 1, "index": 1, "parentId": null, "categoryId": 1 },
              { "_id": 2, "index": 2, "parentId": 1, "categoryId": 1 }
            ]
            """),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 5),
            "$filter=children/any(c:c/category/name eq 'Category 2')",
            """
            [
              { "_id": 2, "index": 2, "parentId": 1, "categoryId": 1 }
            ]
            """));
  }

  @ParameterizedTest(name = "{2} - {0} - {1}")
  @MethodSource("provideFilterWithNavigationAndUnsetTestCases")
  public void shouldFilterDocumentsAndRemoveLookedUpNavigationPropertiesUsingUnsetStages(
      KeyValue<String, String> rootMongoCollection,
      Set<Integer> initialIds,
      String filterQuery,
      String expectedJson)
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
    UsedNavigationPropertyUnsetStagesBuilder unsetBuilder =
        new UsedNavigationPropertyUnsetStagesBuilder();

    // WHEN
    FilterOperatorResult filterResult = filterParser.parse(uriInfo.getFilterOption());
    List<Bson> pipeline = new ArrayList<>();
    pipeline.add(new Document("$match", new Document("_id", new Document("$in", initialIds))));
    pipeline.addAll(lookupBuilder.build(filterResult.getUsedNavigationProperties()));
    pipeline.addAll(filterResult.getStageObjects());
    pipeline.addAll(unsetBuilder.build(filterResult.getUsedNavigationProperties()));

    List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());

    // THEN
    String currentResult = new Document("value", results).toJson();
    JSONAssert.assertEquals(
        """
        {"value": %s }
        """.formatted(expectedJson),
        currentResult,
        JSONCompareMode.NON_EXTENSIBLE);
  }

  private static Stream<Arguments> provideOrderByWithNavigationAndUnsetTestCases() {
    return Stream.of(
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3, 4),
            "$orderby=category/name desc,index asc",
            """
            [
              { "_id": 3, "index": 3, "parentId": 2, "categoryId": 2 },
              { "_id": 4, "index": 4, "parentId": 3, "categoryId": 2 },
              { "_id": 1, "index": 1, "parentId": null, "categoryId": 1 },
              { "_id": 2, "index": 2, "parentId": 1, "categoryId": 1 }
            ]
            """),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(2, 3, 4),
            "$orderby=parent/category/name desc,index asc",
            """
            [
              { "_id": 4, "index": 4, "parentId": 3, "categoryId": 2 },
              { "_id": 2, "index": 2, "parentId": 1, "categoryId": 1 },
              { "_id": 3, "index": 3, "parentId": 2, "categoryId": 2 }
            ]
            """));
  }

  @ParameterizedTest(name = "{2} - {0} - {1}")
  @MethodSource("provideOrderByWithNavigationAndUnsetTestCases")
  public void shouldOrderDocumentsAndRemoveLookedUpNavigationPropertiesUsingUnsetStages(
      KeyValue<String, String> rootMongoCollection,
      Set<Integer> initialIds,
      String orderByQuery,
      String expectedJson)
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
    UsedNavigationPropertyUnsetStagesBuilder unsetBuilder =
        new UsedNavigationPropertyUnsetStagesBuilder();

    // WHEN
    OrderByOperatorResult orderByResult = orderByParser.parse(uriInfo.getOrderByOption());
    List<Bson> pipeline = new ArrayList<>();
    pipeline.add(new Document("$match", new Document("_id", new Document("$in", initialIds))));
    pipeline.addAll(lookupBuilder.build(orderByResult.getUsedNavigationProperties()));
    pipeline.addAll(orderByResult.getStageObjects());
    pipeline.addAll(unsetBuilder.build(orderByResult.getUsedNavigationProperties()));

    List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());

    // THEN
    String currentResult = new Document("value", results).toJson();
    JSONAssert.assertEquals(
        """
        {"value": %s }
        """.formatted(expectedJson),
        currentResult,
        JSONCompareMode.STRICT);
  }

  private static Stream<Arguments> provideApplyWithNavigationAndUnsetTestCases() {
    return Stream.of(
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 3),
            "$apply=filter(category/name eq 'Category 2')",
            """
            [{ "_id": 3, "index": 3, "parentId": 2, "categoryId": 2 }]
            """),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2, 5),
            "$apply=filter(children/any(c:c/category/name eq 'Category 2'))",
            """
            [{ "_id": 2, "index": 2, "parentId": 1, "categoryId": 1 }]
            """),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(2, 3),
            "$apply=compute(parent/index add index as combinedIndex)",
            """
            [
              { "_id": 2, "index": 2, "parentId": 1, "categoryId": 1, "combinedIndex": 3 },
              { "_id": 3, "index": 3, "parentId": 2, "categoryId": 2, "combinedIndex": 5 }
            ]
            """),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(2, 3, 4),
            "$apply=topcount(1,parent/index)",
            """
            [{ "_id": 4, "index": 4, "parentId": 3, "categoryId": 2 }]
            """));
  }

  @ParameterizedTest(name = "{2} - {0} - {1}")
  @MethodSource("provideApplyWithNavigationAndUnsetTestCases")
  public void shouldExecuteApplyAndRemoveLookedUpNavigationPropertiesUsingUnsetStages(
      KeyValue<String, String> rootMongoCollection,
      Set<Integer> initialIds,
      String applyQuery,
      String expectedJson)
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
    UsedNavigationPropertyUnsetStagesBuilder unsetBuilder =
        new UsedNavigationPropertyUnsetStagesBuilder();

    // WHEN
    ApplyOperatorResult applyResult =
        applyParser.parse(uriInfo.getApplyOption(), DefaultEdmMongoContextFacade.builder().build());
    List<Bson> pipeline = new ArrayList<>();
    pipeline.add(new Document("$match", new Document("_id", new Document("$in", initialIds))));
    pipeline.addAll(lookupBuilder.build(applyResult.getUsedNavigationProperties()));
    pipeline.addAll(applyResult.getStageObjects());
    pipeline.addAll(unsetBuilder.build(applyResult.getUsedNavigationProperties()));

    List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());

    // THEN
    String currentResult = new Document("value", results).toJson();
    JSONAssert.assertEquals(
        """
        {"value": %s }
        """.formatted(expectedJson),
        currentResult,
        JSONCompareMode.NON_EXTENSIBLE);
  }

  private static Stream<Arguments> provideCustomMappingFilterWithUnsetTestCases() {
    return Stream.of(
        Arguments.of(
            Set.of(1, 2, 3, 4),
            "$filter=category/name eq 'Category 2'",
            """
            [
              { "_id": 3, "renamed_index": 3, "renamed_parentId": 2, "renamed_categoryId": 2 },
              { "_id": 4, "renamed_index": 4, "renamed_parentId": 3, "renamed_categoryId": 2 }
            ]
            """),
        Arguments.of(
            Set.of(1, 2, 3, 4),
            "$filter=parent/category/name eq 'Category 1'",
            """
            [
              { "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1 },
              { "_id": 3, "renamed_index": 3, "renamed_parentId": 2, "renamed_categoryId": 2 }
            ]
            """));
  }

  @ParameterizedTest(name = "Custom mapping with unset: {1} - {0}")
  @MethodSource("provideCustomMappingFilterWithUnsetTestCases")
  public void shouldExecuteFilterWithCustomMappingsAndRemoveLookedUpNavigationProperties(
      Set<Integer> initialIds, String filterQuery, String expectedJson) throws Exception {
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
    t1.getProperties().get("parent").setMongoPath("custom_parent");
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
    UsedNavigationPropertyUnsetStagesBuilder unsetBuilder =
        new UsedNavigationPropertyUnsetStagesBuilder();

    // WHEN
    FilterOperatorResult filterResult = filterParser.parse(uriInfo.getFilterOption(), t1Facade);
    List<Bson> pipeline = new ArrayList<>();
    pipeline.add(new Document("$match", new Document("_id", new Document("$in", initialIds))));
    pipeline.addAll(lookupBuilder.build(filterResult.getUsedNavigationProperties(), lookupContext));
    pipeline.addAll(filterResult.getStageObjects());
    pipeline.addAll(unsetBuilder.build(filterResult.getUsedNavigationProperties(), lookupContext));

    List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());

    // THEN
    String currentResult = new Document("value", results).toJson();
    JSONAssert.assertEquals(
        """
        {"value": %s }
        """.formatted(expectedJson),
        currentResult,
        JSONCompareMode.NON_EXTENSIBLE);
  }
}
