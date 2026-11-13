package com.github.starnowski.jamolingo.compat.driver.operators.expand.nestedtree;

import static com.github.starnowski.jamolingo.AbstractItTest.TEST_DATABASE;
import static com.github.starnowski.jamolingo.compat.driver.operators.expand.nestedtree.ExpandOperatorWithHandlingTreeRelationsTestProperties.*;

import com.github.starnowski.jamolingo.AbstractItTest;
import com.github.starnowski.jamolingo.EmbeddedMongoResource;
import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.operators.apply.ApplyOperatorResult;
import com.github.starnowski.jamolingo.core.operators.apply.ODataApplyToMongoAggregationPipelineParser;
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser;
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
import java.util.Set;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
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
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_12_p_t1_10.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_13_p_t1_10.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_14_p_t1_11.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_15_p_t1_11.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_16_p_t1_11.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_17_p_t1_12.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_18_p_t1_12.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_19_p_t1_12.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_20_p_t1_13.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_21_p_t1_14.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_22_p_t1_14.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_23_p_t1_14.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_24_p_t1_14.json"),
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
          collection = TREETYPE3_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_2_t2_4_t3_4.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE3_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_2_t2_4_t3_5.json"),
      @MongoDocument(
          database = TEST_DATABASE,
          collection = TREETYPE3_COLLECTION,
          bsonFilePath = "bson/expand/tree/t1_2_t2_4_t3_6.json"),
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
          bsonFilePath = "bson/expand/tree/t1_1_t2_1_t3_1_t4_3.json")
    })
@QuarkusTest
@QuarkusTestResource(value = EmbeddedMongoResource.class, restrictToAnnotatedClass = true)
public class ExpandOperatorInsideApplySystemOperatorTest extends AbstractItTest {

  private static Stream<Arguments> provideData() {
    return Stream.of(
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$apply=expand(children,expand(treeType2s))",
            """
                                                                [{ "_id": 1, "index": 1, "parentId": null,
             "categoryId": 1,
                                                                 "children": [
                                                                    { "_id": 2, "index": 2, "parentId": 1,
             "categoryId": 1,
                                                                        "treeType2s": [

             {"_id":4,"index":4,"categoryId":1},

             {"_id":5,"index":5,"parentId":4,"categoryId":1,"treeType1Id":2},

             {"_id":6,"index":6,"parentId":5,"categoryId":2,"treeType1Id":2}
                                                                        ]
                                                                    },
                                                                    { "_id": 5, "index": 5, "parentId": 1,
             "categoryId": 1
                                                                     }
                                                                 ]
                                                                }]
             """,
            JSONCompareMode.LENIENT),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(2, 5, 3, 4, 6, 7),
            "$apply=expand(treeType2s)",
            """
                                                                        [ { "_id": 2, "index": 2,
             "parentId": 1, "categoryId": 1,
                                                                                "treeType2s": [

             {"_id":4,"index":4,"categoryId":1},

             {"_id":5,"index":5,"parentId":4,"categoryId":1,"treeType1Id":2},

             {"_id":6,"index":6,"parentId":5,"categoryId":2,"treeType1Id":2}
                                                                                ]
                                                                            },
                                                                                { "_id": 5, "index": 5,
             "parentId": 1, "categoryId": 1
                                                                                 },
                                                                             { "_id": 3, "index": 3,
             "parentId": 2, "categoryId": 2 },
                                                                             { "_id": 4, "index": 4,
             "parentId": 3, "categoryId": 2 },
                                                                             { "_id": 6, "index": 6,
             "parentId": 4, "categoryId": 2 },
                                                                             { "_id": 7, "index": 7,
             "parentId": 6, "categoryId": 2 }
                                                                         ]
            """,
            JSONCompareMode.LENIENT),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(2, 5, 3, 4, 6, 7),
            "$apply=expand(treeType2s,filter(index gt 5))",
            """
                                                                    [ { "_id": 2, "index": 2,
         "parentId": 1, "categoryId": 1,
                                                                            "treeType2s": [

         {"_id":6,"index":6,"parentId":5,"categoryId":2,"treeType1Id":2}
                                                                            ]
                                                                        },
                                                                            { "_id": 5, "index": 5,
         "parentId": 1, "categoryId": 1
                                                                             },
                                                                         { "_id": 6, "index": 6,
         "parentId": 4, "categoryId": 2 },
                                                                         { "_id": 7, "index": 7,
         "parentId": 6, "categoryId": 2 }
                                                                     ]
        """,
            JSONCompareMode.LENIENT));
  }

  @Inject protected MongoClient mongoClient;

  @ParameterizedTest(name = "{2} - {0} - {1}")
  @MethodSource("provideData")
  public void shouldReturnExpectedDocumentsForExpandOperator(
      KeyValue<String, String> rootMongoCollection,
      Set<Integer> ids,
      String expandPart,
      String expectedJson,
      JSONCompareMode jsonCompareMode)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException,
          JSONException {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase(TEST_DATABASE);
    MongoCollection<Document> collection = database.getCollection(rootMongoCollection.getKey());
    Edm edm = loadEmdProvider("edm/tree_types.xml");
    UriInfo uriInfo =
        new Parser(edm, OData.newInstance())
            .parseUri(rootMongoCollection.getValue(), expandPart, null, null);
    ODataExpandToMongoAggregationPipelineParser tested =
        new ODataExpandToMongoAggregationPipelineParser();

    // WHEN
    ODataApplyToMongoAggregationPipelineParser parser =
        new ODataApplyToMongoAggregationPipelineParser();

    // WHEN
    ApplyOperatorResult result =
        parser.parse(uriInfo.getApplyOption(), DefaultEdmMongoContextFacade.builder().build());
    List<Bson> pipeline = new ArrayList<>(result.getStageObjects());
    pipeline.add(new Document("$match", new Document("_id", new Document("$in", ids))));
    pipeline.addAll(result.getStageObjects());
    System.out.println(wrapBsonList(pipeline).toJson());
    List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());
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
