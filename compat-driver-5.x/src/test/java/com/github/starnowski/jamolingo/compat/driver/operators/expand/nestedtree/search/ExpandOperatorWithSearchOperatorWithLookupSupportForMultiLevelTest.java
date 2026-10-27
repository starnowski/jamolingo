package com.github.starnowski.jamolingo.compat.driver.operators.expand.nestedtree.search;

import static com.github.starnowski.jamolingo.AbstractItTest.TEST_DATABASE;
import static com.github.starnowski.jamolingo.compat.driver.operators.expand.nestedtree.ExpandOperatorWithHandlingTreeRelationsTestProperties.*;

import com.github.starnowski.jamolingo.AbstractItTest;
import com.github.starnowski.jamolingo.MongoAtlasResource;
import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.operators.expand.ExpandOperatorResult;
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser;
import com.github.starnowski.jamolingo.junit5.*;
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

// TODO
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
public class ExpandOperatorWithSearchOperatorWithLookupSupportForMultiLevelTest
    extends AbstractItTest {

  private static Stream<Arguments> provideData() {
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
            JSONCompareMode.LENIENT),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=5;$search=Vehicle)",
            """
                                                    [
                                                        {
                                                            "_id": 1,
                                                            "index": 1,
                                                            "parentId": null,
                                                            "categoryId": 1,
                                                            "children": [
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
                                                    """,
            JSONCompareMode.LENIENT));
  }

  @Inject protected MongoClient mongoClient;

  @ParameterizedTest(name = "{2} - {0} - {1}")
  @MethodSource({"provideData"})
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
    Edm edm = loadEmdProvider("edm/tree_types_atlas_search.xml");
    UriInfo uriInfo =
        new Parser(edm, OData.newInstance())
            .parseUri(rootMongoCollection.getValue(), expandPart, null, null);
    ODataExpandToMongoAggregationPipelineParser tested =
        new ODataExpandToMongoAggregationPipelineParser();

    // WHEN
    ExpandOperatorResult result =
        tested.parse(
            uriInfo.getExpandOption(),
            ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(true)
                .build());
    List<Bson> pipeline = new ArrayList<>();
    pipeline.add(new Document("$match", new Document("_id", new Document("$in", ids))));
    pipeline.addAll(result.getStageObjects());
    pipeline.add(new Document("$sort", new Document("index", 1)));
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
