package com.github.starnowski.jamolingo.compat.driver.operators.expand.nestedtree;

import com.github.starnowski.jamolingo.AbstractItTest;
import com.github.starnowski.jamolingo.EmbeddedMongoResource;
import com.github.starnowski.jamolingo.core.operators.expand.ExpandOperatorResult;
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

@QuarkusTest
@QuarkusTestResource(value = EmbeddedMongoResource.class, restrictToAnnotatedClass = true)
public class ExpandOperatorWithHandlingTreeRelationsTest extends AbstractItTest {

  private static Stream<Arguments> provideData() {
    // TODO
    return Stream.of(
        Arguments.of(
            Set.of(1),
            "$expand=category",
            """
                            [{ "_id": 1, "index": 1, "parentId": null, "categoryId": 1,
                             "category": { "_id": 1, "name": "Category 1" }
                            }]
                            """,
            JSONCompareMode.LENIENT),
        Arguments.of(
            Set.of(1),
            "$expand=category,children",
            """
                                    [{ "_id": 1, "index": 1, "parentId": null, "categoryId": 1,
                                     "category": { "_id": 1, "name": "Category 1" },
                                     "children": [{ "_id": 2, "index": 2, "parentId": 1, "categoryId": 1 }]
                                    }]
                                    """,
            JSONCompareMode.LENIENT));
  }

  @Inject protected MongoClient mongoClient;

  @ParameterizedTest
  @MethodSource("provideData")
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = "MyService",
            collection = "Category",
            bsonFilePath = "bson/expand/tree/category1.json"),
        @MongoDocument(
            database = "MyService",
            collection = "Category",
            bsonFilePath = "bson/expand/tree/category2.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType1",
            bsonFilePath = "bson/expand/tree/t1_1.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType1",
            bsonFilePath = "bson/expand/tree/t1_2.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType1",
            bsonFilePath = "bson/expand/tree/t1_3.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType2",
            bsonFilePath = "bson/expand/tree/t1_1_t2_1.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType2",
            bsonFilePath = "bson/expand/tree/t1_1_t2_2.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType2",
            bsonFilePath = "bson/expand/tree/t1_1_t2_3.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType2",
            bsonFilePath = "bson/expand/tree/t1_2_t2_4.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType2",
            bsonFilePath = "bson/expand/tree/t1_2_t2_5.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType2",
            bsonFilePath = "bson/expand/tree/t1_2_t2_6.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType3",
            bsonFilePath = "bson/expand/tree/t1_1_t2_1_t3_1.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType3",
            bsonFilePath = "bson/expand/tree/t1_1_t2_1_t3_2.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType3",
            bsonFilePath = "bson/expand/tree/t1_1_t2_1_t3_3.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType3",
            bsonFilePath = "bson/expand/tree/t1_2_t2_4_t3_4.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType3",
            bsonFilePath = "bson/expand/tree/t1_2_t2_4_t3_5.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType3",
            bsonFilePath = "bson/expand/tree/t1_2_t2_4_t3_6.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType4",
            bsonFilePath = "bson/expand/tree/t1_1_t2_1_t3_1_t4_1.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType4",
            bsonFilePath = "bson/expand/tree/t1_1_t2_1_t3_1_t4_2.json"),
        @MongoDocument(
            database = "MyService",
            collection = "TreeType4",
            bsonFilePath = "bson/expand/tree/t1_1_t2_1_t3_1_t4_3.json")
      })
  public void shouldReturnExpectedDocumentsForExpandOperator(
      Set<Integer> ids, String expandPart, String expectedJson, JSONCompareMode jsonCompareMode)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException,
          JSONException {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("MyService");
    MongoCollection<Document> collection = database.getCollection("TreeType1");
    Edm edm = loadEmdProvider("edm/tree_types.xml");
    UriInfo uriInfo =
        new Parser(edm, OData.newInstance()).parseUri("treeType1s", expandPart, null, null);
    ODataExpandToMongoAggregationPipelineParser tested =
        new ODataExpandToMongoAggregationPipelineParser();

    // WHEN
    ExpandOperatorResult result =
        tested.parse(
            uriInfo.getExpandOption(),
            ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.builder()
                .build());
    List<Bson> pipeline = new ArrayList<>();
    pipeline.add(new Document("$match", new Document("_id", new Document("$in", ids))));
    pipeline.addAll(result.getStageObjects());
    List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());

    JSONAssert.assertEquals(
        """
            {"value": %s }
            """.formatted(expectedJson),
        wrapDocumentsList(results).toJson(),
        jsonCompareMode);
  }

  private Document wrapDocumentsList(List<Document> docs) {
    return new Document("value", docs);
  }
}
