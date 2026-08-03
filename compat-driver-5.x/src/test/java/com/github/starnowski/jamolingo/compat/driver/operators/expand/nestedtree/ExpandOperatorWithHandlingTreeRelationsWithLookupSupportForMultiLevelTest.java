package com.github.starnowski.jamolingo.compat.driver.operators.expand.nestedtree;

import static com.github.starnowski.jamolingo.AbstractItTest.TEST_DATABASE;
import static com.github.starnowski.jamolingo.compat.driver.operators.expand.nestedtree.ExpandOperatorWithHandlingTreeRelationsTestProperties.*;

import com.github.starnowski.jamolingo.AbstractItTest;
import com.github.starnowski.jamolingo.EmbeddedMongoResource;
import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.operators.expand.ExpandOperatorResult;
import com.github.starnowski.jamolingo.core.operators.expand.GraphLookUpToLookUpStrategyResultsTransformer;
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
public class ExpandOperatorWithHandlingTreeRelationsWithLookupSupportForMultiLevelTest
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
        // Level with max=5
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=max)",
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
        // Level with max=5 with root documents with id 1 and 2
        // The document with id 1 is an ancestor for document with id 8 but because max=5 the query
        // is not to return it,
        // because the recursion level is 6.
        // The document with id 2 is also an ancestor for document with id 8 but because level
        // recursion in this is 5
        // then the document is going to be returned by query
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1, 2),
            "$expand=children($levels=max)",
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
                                                                },
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
                                                                }
                                                            ]
                                                            """,
            JSONCompareMode.LENIENT),
        // Level with filters
        // Filter with condition that TreeType2 grandfather and parent only pass, the child not
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=treeType2s($levels=5;$filter=index in (1, 2))",
            """
                                                    [{ "_id": 1, "index": 1, "parentId": null, "categoryId": 1,
                                                     "treeType2s": [{ "_id": 1, "index": 1, "parentId": null, "categoryId": 1, "treeType1Id": 1 },
                                                     { "_id": 2, "index": 2, "parentId": 1, "categoryId": 1, "treeType1Id": 1 }
                                                     ]
                                                    }]
                                                    """,
            JSONCompareMode.LENIENT),
        // Filter with condition that TreeType1 grandfather and the child not, parent does not pass
        // condition.
        // Important! Because OData filters on the level context then because parent does not pass
        // condition then children should not be returned

        // Level with filters that condition pass for all children in the specified depth
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=3;$filter=index in (2, 3, 4))",
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
                                                                                            "categoryId": 2
                                                                                        }
                                                                                    ]
                                                                                }
                                                                            ]
                                                                        }
                                                                    ]
                                                                }
                                                            ]
                                                            """,
            JSONCompareMode.NON_EXTENSIBLE),
        // Level with filters that condition pass for document with id 2 and 4, which means that 4
        // is not going be
        // returned in array because it parent document with id 3 does not pass condition.
        // Parent document with id 3 is not going to be returned in the list
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=3;$filter=index in (2, 4))",
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
                                                                                    "children": []
                                                                                }
                                                                            ]
                                                                        }
                                                                    ]
                                                                    """,
            JSONCompareMode.NON_EXTENSIBLE),
        // Level with filters that condition pass for document with id 3 and 4.
        // Parent document with id 2 is not going to be returned in the list, that means that
        // documents with ids
        // 3 and 4 are not going to be returned. The children array should be empty
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=3;$filter=index in (3, 4))",
            """
                                                                    [{ "_id": 1, "index": 1, "parentId": null, "categoryId": 1,
                                                                     "children": [
                                                                     ]
                                                                    }]
                                                                    """,
            JSONCompareMode.NON_EXTENSIBLE),
        // Level with max=5 with asc ordering by index
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=max;$orderby=index asc)",
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
            JSONCompareMode.STRICT_ORDER),
        // Level with max=5 with asc ordering by index
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=max;$orderby=index desc)",
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
                                                                            "children": []
                                                                        },
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
                                                                        }
                                                                    ]
                                                                }
                                                            ]
                                                            """,
            JSONCompareMode.STRICT_ORDER),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(10),
            "$expand=children($levels=max)",
            """
                            [
                                {
                                    "_id": 10,
                                    "index": 10,
                                    "parentId": null,
                                    "categoryId": 1,
                                    "children": [
                                        {
                                            "_id": 11,
                                            "index": 11,
                                            "parentId": 10,
                                            "categoryId": 1,
                                            "children": [
                                                {
                                                    "_id": 14,
                                                    "index": 14,
                                                    "parentId": 11,
                                                    "categoryId": 1,
                                                    "children": [
                                                        {
                                                            "_id": 21,
                                                            "index": 21,
                                                            "parentId": 14,
                                                            "categoryId": 1,
                                                            "children": []
                                                        },
                                                        {
                                                            "_id": 22,
                                                            "index": 22,
                                                            "parentId": 14,
                                                            "categoryId": 1,
                                                            "children": []
                                                        },
                                                        {
                                                            "_id": 23,
                                                            "index": 23,
                                                            "parentId": 14,
                                                            "categoryId": 1,
                                                            "children": []
                                                        },
                                                        {
                                                            "_id": 24,
                                                            "index": 24,
                                                            "parentId": 14,
                                                            "categoryId": 1,
                                                            "children": []
                                                        }
                                                    ]
                                                },
                                                {
                                                    "_id": 15,
                                                    "index": 15,
                                                    "parentId": 11,
                                                    "categoryId": 1,
                                                    "children": []
                                                },
                                                {
                                                    "_id": 16,
                                                    "index": 16,
                                                    "parentId": 11,
                                                    "categoryId": 1,
                                                    "children": []
                                                }
                                            ]
                                        },
                                        {
                                            "_id": 12,
                                            "index": 12,
                                            "parentId": 10,
                                            "categoryId": 1,
                                            "children": [
                                                {
                                                    "_id": 17,
                                                    "index": 17,
                                                    "parentId": 12,
                                                    "categoryId": 1,
                                                    "children": []
                                                },
                                                {
                                                    "_id": 18,
                                                    "index": 18,
                                                    "parentId": 12,
                                                    "categoryId": 1,
                                                    "children": []
                                                },
                                                {
                                                    "_id": 19,
                                                    "index": 19,
                                                    "parentId": 12,
                                                    "categoryId": 1,
                                                    "children": []
                                                }
                                            ]
                                        },
                                        {
                                            "_id": 13,
                                            "index": 13,
                                            "parentId": 10,
                                            "categoryId": 1,
                                            "children": [
                                                {
                                                    "_id": 20,
                                                    "index": 20,
                                                    "parentId": 13,
                                                    "categoryId": 1,
                                                    "children": []
                                                }
                                            ]
                                        }
                                    ]
                                }
                            ]
                            """,
            JSONCompareMode.NON_EXTENSIBLE),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(10),
            "$expand=children($levels=max;$top=2;$orderby=index asc)",
            """
                            [
                                {
                                    "_id": 10,
                                    "index": 10,
                                    "parentId": null,
                                    "categoryId": 1,
                                    "children": [
                                        {
                                            "_id": 11,
                                            "index": 11,
                                            "parentId": 10,
                                            "categoryId": 1,
                                            "children": [
                                                {
                                                    "_id": 14,
                                                    "index": 14,
                                                    "parentId": 11,
                                                    "categoryId": 1,
                                                    "children": [
                                                        {
                                                            "_id": 21,
                                                            "index": 21,
                                                            "parentId": 14,
                                                            "categoryId": 1,
                                                            "children": []
                                                        },
                                                        {
                                                            "_id": 22,
                                                            "index": 22,
                                                            "parentId": 14,
                                                            "categoryId": 1,
                                                            "children": []
                                                        }
                                                    ]
                                                },
                                                {
                                                    "_id": 15,
                                                    "index": 15,
                                                    "parentId": 11,
                                                    "categoryId": 1,
                                                    "children": []
                                                }
                                            ]
                                        },
                                        {
                                            "_id": 12,
                                            "index": 12,
                                            "parentId": 10,
                                            "categoryId": 1,
                                            "children": [
                                                {
                                                    "_id": 17,
                                                    "index": 17,
                                                    "parentId": 12,
                                                    "categoryId": 1,
                                                    "children": []
                                                },
                                                {
                                                    "_id": 18,
                                                    "index": 18,
                                                    "parentId": 12,
                                                    "categoryId": 1,
                                                    "children": []
                                                }
                                            ]
                                        }
                                    ]
                                }
                            ]
                            """,
            JSONCompareMode.STRICT),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(10),
            "$expand=children($levels=max;$top=2;$orderby=index desc)",
            """
                                    [
                                        {
                                            "_id": 10,
                                            "index": 10,
                                            "parentId": null,
                                            "categoryId": 1,
                                            "children": [
                                                {
                                                    "_id": 13,
                                                    "index": 13,
                                                    "parentId": 10,
                                                    "categoryId": 1,
                                                    "children": [
                                                        {
                                                            "_id": 20,
                                                            "index": 20,
                                                            "parentId": 13,
                                                            "categoryId": 1,
                                                            "children": []
                                                        }
                                                    ]
                                                },
                                                {
                                                    "_id": 12,
                                                    "index": 12,
                                                    "parentId": 10,
                                                    "categoryId": 1,
                                                    "children": [
                                                        {
                                                            "_id": 19,
                                                            "index": 19,
                                                            "parentId": 12,
                                                            "categoryId": 1,
                                                            "children": []
                                                        },
                                                        {
                                                            "_id": 18,
                                                            "index": 18,
                                                            "parentId": 12,
                                                            "categoryId": 1,
                                                            "children": []
                                                        }
                                                    ]
                                                }
                                            ]
                                        }
                                    ]
                                    """,
            JSONCompareMode.STRICT),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(10),
            "$expand=children($levels=max;$skip=1;$top=2;$orderby=index asc)",
            """
                                    [
                                        {
                                            "_id": 10,
                                            "index": 10,
                                            "parentId": null,
                                            "categoryId": 1,
                                            "children": [
                                                {
                                                    "_id": 12,
                                                    "index": 12,
                                                    "parentId": 10,
                                                    "categoryId": 1,
                                                    "children": [
                                                        {
                                                            "_id": 18,
                                                            "index": 18,
                                                            "parentId": 12,
                                                            "categoryId": 1,
                                                            "children": []
                                                        },
                                                        {
                                                            "_id": 19,
                                                            "index": 19,
                                                            "parentId": 12,
                                                            "categoryId": 1,
                                                            "children": []
                                                        }
                                                    ]
                                                },
                                                {
                                                    "_id": 13,
                                                    "index": 13,
                                                    "parentId": 10,
                                                    "categoryId": 1,
                                                    "children": []
                                                }
                                            ]
                                        }
                                    ]
                                    """,
            JSONCompareMode.STRICT),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(10),
            "$expand=children($levels=max;$skip=1;$top=2;$orderby=index asc;$select=index)",
            """
                                            [
                                                {
                                                    "_id": 10,
                                                    "index": 10,
                                                    "parentId": null,
                                                    "categoryId": 1,
                                                    "children": [
                                                        {
                                                            "index": 12,
                                                            "children": [
                                                                {
                                                                    "index": 18,
                                                                    "children": []
                                                                },
                                                                {
                                                                    "index": 19,
                                                                    "children": []
                                                                }
                                                            ]
                                                        },
                                                        {
                                                            "index": 13,
                                                            "children": []
                                                        }
                                                    ]
                                                }
                                            ]
                                            """,
            JSONCompareMode.STRICT),
        // TODO $expand=children($level=max;$expand=treeType2s)
        // TODO Fix assertions for the treeType2s
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=max;$expand=treeType2s)",
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
                                                                            ],
                                                                            "treeType2s": [
                                                                                {
                                                                                    "_id": 4,
                                                                                    "index": 4,
                                                                                    "parentId": null,
                                                                                    "categoryId": 1,
                                                                                    "treeType1Id": 2
                                                                                },
                                                                                {
                                                                                    "_id": 5,
                                                                                    "index": 5,
                                                                                    "parentId": 4,
                                                                                    "categoryId": 1,
                                                                                    "treeType1Id": 2
                                                                                },
                                                                                {
                                                                                    "_id": 6,
                                                                                    "index": 6,
                                                                                    "parentId": 5,
                                                                                    "categoryId": 2,
                                                                                    "treeType1Id": 2
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
            "$expand=children($levels=max;$expand=treeType2s($expand=children($levels=max)))",
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
                                                                                    ],
                                                                                    "treeType2s": [
                                                                                        {
                                                                                            "_id": 4,
                                                                                            "index": 4,
                                                                                            "parentId": null,
                                                                                            "categoryId": 1,
                                                                                            "treeType1Id": 2
                                                                                        },
                                                                                        {
                                                                                            "_id": 5,
                                                                                            "index": 5,
                                                                                            "parentId": 4,
                                                                                            "categoryId": 1,
                                                                                            "treeType1Id": 2
                                                                                        },
                                                                                        {
                                                                                            "_id": 6,
                                                                                            "index": 6,
                                                                                            "parentId": 5,
                                                                                            "categoryId": 2,
                                                                                            "treeType1Id": 2
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
            Set.of(5, 7),
            "$expand=parent($levels=2;$expand=children($levels=2))",
            """
                                                                            [
                                                                                {
                                                                                    "_id": 5,
                                                                                    "index": 5,
                                                                                    "parentId": 1,
                                                                                    "categoryId": 1,
                                                                                    "parent": {
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
                                                                                                        "categoryId": 2
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
                                                                                },
                                                                                {
                                                                                    "_id": 7,
                                                                                    "index": 7,
                                                                                    "parentId": 6,
                                                                                    "categoryId": 2,
                                                                                    "parent": {
                                                                                        "_id": 6,
                                                                                        "index": 6,
                                                                                        "parentId": 4,
                                                                                        "categoryId": 2,
                                                                                        "parent": {
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
                                                                                        },
                                                                                        "children": [
                                                                                            {
                                                                                                "_id": 7,
                                                                                                "index": 7,
                                                                                                "parentId": 6,
                                                                                                "categoryId": 2,
                                                                                                "children": [
                                                                                                    {
                                                                                                        "_id": 8,
                                                                                                        "index": 8,
                                                                                                        "parentId": 7,
                                                                                                        "categoryId": 2
                                                                                                    }
                                                                                                ]
                                                                                            }
                                                                                        ]
                                                                                    }
                                                                                }
                                                                            ]
                                                                            """,
            JSONCompareMode.LENIENT),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(5, 7),
            "$expand=parent($levels=2;$expand=children($levels=2),treeType2s)",
            """
                                                                                    [
                                                                                        {
                                                                                            "_id": 5,
                                                                                            "index": 5,
                                                                                            "parentId": 1,
                                                                                            "categoryId": 1,
                                                                                            "parent": {
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
                                                                                                                "categoryId": 2
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
                                                                                                ],
                                                                                                "treeType2s": [
                                                                                                    {
                                                                                                        "_id": 1,
                                                                                                        "index": 1,
                                                                                                        "parentId": null,
                                                                                                        "categoryId": 1,
                                                                                                        "treeType1Id": 1
                                                                                                    },
                                                                                                    {
                                                                                                        "_id": 2,
                                                                                                        "index": 2,
                                                                                                        "parentId": 1,
                                                                                                        "categoryId": 1,
                                                                                                        "treeType1Id": 1
                                                                                                    },
                                                                                                    {
                                                                                                        "_id": 3,
                                                                                                        "index": 3,
                                                                                                        "parentId": 2,
                                                                                                        "categoryId": 2,
                                                                                                        "treeType1Id": 1
                                                                                                    }
                                                                                                ]
                                                                                            }
                                                                                        },
                                                                                        {
                                                                                            "_id": 7,
                                                                                            "index": 7,
                                                                                            "parentId": 6,
                                                                                            "categoryId": 2,
                                                                                            "parent": {
                                                                                                "_id": 6,
                                                                                                "index": 6,
                                                                                                "parentId": 4,
                                                                                                "categoryId": 2,
                                                                                                "parent": {
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
                                                                                                    ],
                                                                                                    "treeType2s": []
                                                                                                },
                                                                                                "children": [
                                                                                                    {
                                                                                                        "_id": 7,
                                                                                                        "index": 7,
                                                                                                        "parentId": 6,
                                                                                                        "categoryId": 2,
                                                                                                        "children": [
                                                                                                            {
                                                                                                                "_id": 8,
                                                                                                                "index": 8,
                                                                                                                "parentId": 7,
                                                                                                                "categoryId": 2
                                                                                                            }
                                                                                                        ]
                                                                                                    }
                                                                                                ],
                                                                                                "treeType2s": []
                                                                                            }
                                                                                        }
                                                                                    ]
                                                                                    """,
            JSONCompareMode.LENIENT),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(5, 7),
            "$expand=parent($levels=2;$expand=children($levels=2;$orderby=index asc),treeType2s($select=_id,index,treeType1Id;$orderby=index asc);$select=_id,index;$orderby=index asc)&$orderby=index desc",
            """
                    [
                    	{
                    		"_id": 5,
                    		"index": 5,
                    		"parentId": 1,
                    		"categoryId": 1,
                    		"parent": {
                    			"_id": 1,
                    			"index": 1,
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
                    							"categoryId": 2
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
                    			],
                    			"treeType2s": [
                    				{
                    					"_id": 1,
                    					"index": 1,
                    					"treeType1Id": 1
                    				},
                    				{
                    					"_id": 2,
                    					"index": 2,
                    					"treeType1Id": 1
                    				},
                    				{
                    					"_id": 3,
                    					"index": 3,
                    					"treeType1Id": 1
                    				}
                    			]
                    		}
                    	},
                    	{
                    		"_id": 7,
                    		"index": 7,
                    		"parentId": 6,
                    		"categoryId": 2,
                    		"parent": {
                    			"_id": 6,
                    			"index": 6,
                    			"parent": {
                    				"_id": 4,
                    				"index": 4,
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
                    				],
                    				"treeType2s": []
                    			},
                    			"children": [
                    				{
                    					"_id": 7,
                    					"index": 7,
                    					"parentId": 6,
                    					"categoryId": 2,
                    					"children": [
                    						{
                    							"_id": 8,
                    							"index": 8,
                    							"parentId": 7,
                    							"categoryId": 2
                    						}
                    					]
                    				}
                    			],
                    			"treeType2s": []
                    		}
                    	}
                    ]
            """,
            JSONCompareMode.STRICT));
  }

  // TODO Add tests that contains the depth level property, that property is rendred with document
  // and can be used to
  // create response that compatible with OData specification which is tree structure and not the
  // flat array.

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

  @ParameterizedTest(name = "{2} - {0} - {1}")
  @MethodSource("provideData")
  public void
      shouldReturnExpectedDocumentsForExpandOperatorWithGraphLookUpStrategyThenMapResultsToLookUpStrategy(
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
    ExpandOperatorResult result =
        tested.parse(
            uriInfo.getExpandOption(),
            ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(
                    false) // GraphLookUp is suppose to be used to resolve
                .withPropagateGraphLookUpJoinKeys(true)
                .build());
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

    // THEN
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
