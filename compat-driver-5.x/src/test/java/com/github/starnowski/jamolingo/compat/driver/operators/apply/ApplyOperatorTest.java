package com.github.starnowski.jamolingo.compat.driver.operators.apply;

import com.github.starnowski.jamolingo.AbstractItTest;
import com.github.starnowski.jamolingo.EmbeddedMongoResource;
import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.operators.apply.ApplyOperatorResult;
import com.github.starnowski.jamolingo.core.operators.apply.ODataApplyToMongoAggregationPipelineParser;
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

@QuarkusTest
@QuarkusTestResource(value = EmbeddedMongoResource.class, restrictToAnnotatedClass = true)
public class ApplyOperatorTest extends AbstractItTest {

  @Inject MongoClient mongoClient;

  @ParameterizedTest
  @MethodSource("provideApplyTestCases")
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_1.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_2.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_3.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_4.json")
      })
  public void shouldReturnExpectedDocumentsBasedOnApplyOperator(
      String applyQuery,
      String expectedJson,
      org.skyscreamer.jsonassert.JSONCompareMode jsonCompareMode)
      throws Exception {
    shouldReturnExpectedDocumentsBasedOnApplyOperator(
        applyQuery, expectedJson, jsonCompareMode, "edm/edm6_filter_main.xml");
  }

  @ParameterizedTest
  @MethodSource("provideApplyTestCasesForGroupByAndAggregationOperations")
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/apply/product1.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/apply/product2.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/apply/product3.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/apply/product4.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/apply/product5.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/apply/product6.json")
      })
  public void shouldReturnExpectedDocumentsBasedOnApplyOperatorWithGroupByAndAggregationOperations(
      String applyQuery,
      String expectedJson,
      org.skyscreamer.jsonassert.JSONCompareMode jsonCompareMode)
      throws Exception {
    shouldReturnExpectedDocumentsBasedOnApplyOperator(
        applyQuery, expectedJson, jsonCompareMode, "edm/edm8_apply_aggregate.xml");
  }

  private void shouldReturnExpectedDocumentsBasedOnApplyOperator(
      String applyQuery,
      String expectedJson,
      org.skyscreamer.jsonassert.JSONCompareMode jsonCompareMode,
      String edmContextFile)
      throws Exception {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("testdb");
    MongoCollection<Document> collection = database.getCollection("Items");
    Edm edm = loadEmdProvider(edmContextFile);

    UriInfo uriInfo =
        new Parser(edm, OData.newInstance())
            .parseUri("examples2", "$apply=" + applyQuery, null, null);
    ODataApplyToMongoAggregationPipelineParser parser =
        new ODataApplyToMongoAggregationPipelineParser();

    // WHEN
    ApplyOperatorResult result =
        parser.parse(uriInfo.getApplyOption(), DefaultEdmMongoContextFacade.builder().build());
    List<Bson> pipeline = new ArrayList<>(result.getStageObjects());

    List<Document> results = new ArrayList<>();
    System.out.println(wrapDocumentsList(pipeline).toJson());
    collection.aggregate(pipeline).into(results);

    // THEN
    String currentResult = wrapDocumentsList(results).toJson();
    System.out.println(currentResult);
    org.skyscreamer.jsonassert.JSONAssert.assertEquals(
        """
                    {"value": %s }
                    """.formatted(expectedJson),
        currentResult,
        jsonCompareMode);
  }

  private Document wrapDocumentsList(List<? extends Bson> docs) {
    return new Document("value", docs);
  }

  private static Stream<Arguments> provideApplyTestCases() {
    return Stream.of(
        Arguments.of(
            "filter(plainString eq 'eOMtThyhVNLWUZNRcBaQKxI')",
            "[{\"plainString\": \"eOMtThyhVNLWUZNRcBaQKxI\"}]",
            org.skyscreamer.jsonassert.JSONCompareMode.LENIENT),
        Arguments.of(
            "filter(plainString eq 'Some text')",
            "[{\"plainString\": \"Some text\"}]",
            org.skyscreamer.jsonassert.JSONCompareMode.LENIENT),
        Arguments.of(
            // TODO It tests nothing right now
            "identity",
            """
                            [
                              {"plainString": "eOMtThyhVNLWUZNRcBaQKxI"},
                              {"plainString": "Some text"},
                              {"plainString": "Mario"},
                              {"plainString": "Poem"}
                            ]
                            """,
            org.skyscreamer.jsonassert.JSONCompareMode.LENIENT),
        Arguments.of(
            "groupby((plainString))",
            """
                            [
                              {"plainString": "eOMtThyhVNLWUZNRcBaQKxI"},
                              {"plainString": "Some text"},
                              {"plainString": "Mario"},
                              {"plainString": "Poem"}
                            ]
                            """,
            org.skyscreamer.jsonassert.JSONCompareMode.LENIENT),
        Arguments.of(
            "orderby(smallInteger,plainString)/top(2)",
            // TODO Document with the "Mario" text does not have the smallInteger property
            """
                            [
                              {"plainString": "Mario"},
                              {"smallInteger": -1188957731, "plainString": "Some text"}
                            ]
                            """,
            org.skyscreamer.jsonassert.JSONCompareMode.LENIENT),
        Arguments.of(
            "orderby(smallInteger desc,plainString)/top(1)",
            """
                            [
                              {"smallInteger": -113, "plainString": "Poem"}
                            ]
                            """,
            org.skyscreamer.jsonassert.JSONCompareMode.LENIENT),
        Arguments.of(
            "orderby(smallInteger,plainString)/skip(1)/top(1)",
            """
                            [
                               {"smallInteger": -1188957731, "plainString": "Some text"}
                            ]
                            """,
            org.skyscreamer.jsonassert.JSONCompareMode.LENIENT),
        Arguments.of(
            "filter(plainString eq 'Mario')/groupby((plainString))",
            "[{\"plainString\": \"Mario\"}]",
            org.skyscreamer.jsonassert.JSONCompareMode.LENIENT),
        Arguments.of(
            "filter(plainString eq 'Poem')/groupby((plainString,smallInteger))",
            "[{\"plainString\": \"Poem\", \"smallInteger\": -113}]",
            org.skyscreamer.jsonassert.JSONCompareMode.LENIENT));
  }

  private static Stream<Arguments> provideApplyTestCasesForGroupByAndAggregationOperations() {
    return Stream.of(
        Arguments.of(
            "groupby((plainString2),aggregate(genericInteger with sum as genericIntegerSum))",
            """
                    [
                      {"plainString2": "Electronics", "genericIntegerSum": 350},
                      {"plainString2": "Books", "genericIntegerSum": 950}
                    ]
                    """,
            org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE),
        Arguments.of(
            "groupby((plainString2),aggregate(genericInteger with max as genericIntegerMax))",
            """
                            [
                              {"plainString2": "Electronics", "genericIntegerMax": 200},
                              {"plainString2": "Books", "genericIntegerMax": 500}
                            ]
                            """,
            org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE),
        Arguments.of(
            "groupby((plainString2),aggregate(genericInteger with min as genericIntegerMin))",
            """
                                    [
                                      {"plainString2": "Electronics", "genericIntegerMin": 50},
                                      {"plainString2": "Books", "genericIntegerMin": 150}
                                    ]
                                    """,
            org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE),
            Arguments.of(
                    "groupby((plainString2),aggregate(genericInteger with average as genericIntegerAvg))",
                    """
                                            [
                                              {"plainString2": "Electronics", "genericIntegerMin": 167},
                                              {"plainString2": "Books", "genericIntegerMin": 300}
                                            ]
                                            """,
                    org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE));
  }
}
