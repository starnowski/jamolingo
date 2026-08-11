package com.github.starnowski.jamolingo.compat.driver.operators.expand.nestedtree.mapping;

import static com.github.starnowski.jamolingo.compat.driver.operators.expand.nestedtree.ExpandOperatorWithHandlingTreeRelationsTestProperties.*;

import com.github.starnowski.jamolingo.AbstractItTest;
import com.github.starnowski.jamolingo.EmbeddedMongoResource;
import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
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

@MongoSetup(
    batchInsertToCollection = true,
    mongoDocuments = {
      @MongoDocument(
          database = "testdb_mapping",
          collection = CATEGORY_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/category1.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = CATEGORY_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/category2.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_1.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_2.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_3.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_4.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_5.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_6.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_7.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_8.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_10.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_11_p_t1_10.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_12_p_t1_10.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_13_p_t1_10.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_14_p_t1_11.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_15_p_t1_11.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_16_p_t1_11.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_17_p_t1_12.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_18_p_t1_12.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_19_p_t1_12.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_20_p_t1_13.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_21_p_t1_14.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_22_p_t1_14.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_23_p_t1_14.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE1_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_24_p_t1_14.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE2_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_1_t2_1.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE2_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_1_t2_2.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE2_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_1_t2_3.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE2_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_2_t2_4.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE2_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_2_t2_5.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE2_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_2_t2_6.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE3_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_1_t2_1_t3_1.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE3_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_1_t2_1_t3_2.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE3_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_1_t2_1_t3_3.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE3_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_2_t2_4_t3_4.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE3_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_2_t2_4_t3_5.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE3_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_2_t2_4_t3_6.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE4_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_1_t2_1_t3_1_t4_1.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE4_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_1_t2_1_t3_1_t4_2.json"),
      @MongoDocument(
          database = "testdb_mapping",
          collection = TREETYPE4_COLLECTION,
          bsonFilePath = "bson/expand/tree/mapping/t1_1_t2_1_t3_1_t4_3.json")
    })
@QuarkusTest
@QuarkusTestResource(value = EmbeddedMongoResource.class, restrictToAnnotatedClass = true)
public class ExpandOperatorWithHandlingTreeRelationsTest extends AbstractItTest {

  private static Stream<Arguments> provideData() {
    // TODO Check in for category (that is single property and not collection) we can specify the
    // $levels > 1
    return Stream.of(
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=category",
            """
                            [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
                             "category": { "_id": 1, "renamed_name": "Category 1" }
                            }]
                            """,
            JSONCompareMode.LENIENT),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=category,children",
            """
                                    [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
                                     "category": { "_id": 1, "renamed_name": "Category 1" },
                                     "children": [{ "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1 },
                                      { "_id": 5, "renamed_index": 5, "renamed_parentId": 1, "renamed_categoryId": 1 }
                                     ]
                                    }]
                                    """,
            JSONCompareMode.LENIENT),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=category,children,treeType2s",
            """
                                            [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
                                             "category": { "_id": 1, "renamed_name": "Category 1" },
                                             "children": [{ "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1 },
                                              { "_id": 5, "renamed_index": 5, "renamed_parentId": 1, "renamed_categoryId": 1 }],
                                             "treeType2s": [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1, "renamed_treeType1Id": 1 },
                                             { "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1, "renamed_treeType1Id": 1 },
                                             { "_id": 3, "renamed_index": 3, "renamed_parentId": 2, "renamed_categoryId": 2, "renamed_treeType1Id": 1 }
                                             ]
                                            }]
                                            """,
            JSONCompareMode.LENIENT),
        // Level without filters
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=5)",
            """
                                            [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
                                             "children": [{ "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1 },
                                             { "_id": 5, "renamed_index": 5, "renamed_parentId": 1, "renamed_categoryId": 1 },
                                             { "_id": 3, "renamed_index": 3, "renamed_parentId": 2, "renamed_categoryId": 2 },
                                             { "_id": 4, "renamed_index": 4, "renamed_parentId": 3, "renamed_categoryId": 2 },
                                             { "_id": 6, "renamed_index": 6, "renamed_parentId": 4, "renamed_categoryId": 2 },
                                             { "_id": 7, "renamed_index": 7, "renamed_parentId": 6, "renamed_categoryId": 2 }
                                             ]
                                            }]
                                            """,
            JSONCompareMode.LENIENT),
        // Level with max=5
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=max)",
            """
                                                    [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
                                                     "children": [{ "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1 },
                                                     { "_id": 5, "renamed_index": 5, "renamed_parentId": 1, "renamed_categoryId": 1 },
                                                     { "_id": 3, "renamed_index": 3, "renamed_parentId": 2, "renamed_categoryId": 2 },
                                                     { "_id": 4, "renamed_index": 4, "renamed_parentId": 3, "renamed_categoryId": 2 },
                                                     { "_id": 6, "renamed_index": 6, "renamed_parentId": 4, "renamed_categoryId": 2 },
                                                     { "_id": 7, "renamed_index": 7, "renamed_parentId": 6, "renamed_categoryId": 2 }
                                                     ]
                                                    }]
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
                                                            [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
                                                             "children": [
                                                             { "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1 },
                                                             { "_id": 5, "renamed_index": 5, "renamed_parentId": 1, "renamed_categoryId": 1 },
                                                             { "_id": 3, "renamed_index": 3, "renamed_parentId": 2, "renamed_categoryId": 2 },
                                                             { "_id": 4, "renamed_index": 4, "renamed_parentId": 3, "renamed_categoryId": 2 },
                                                             { "_id": 6, "renamed_index": 6, "renamed_parentId": 4, "renamed_categoryId": 2 },
                                                             { "_id": 7, "renamed_index": 7, "renamed_parentId": 6, "renamed_categoryId": 2 }
                                                             ]
                                                            },
                                                            { "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1,
                                                             "children": [
                                                             { "_id": 3, "renamed_index": 3, "renamed_parentId": 2, "renamed_categoryId": 2 },
                                                             { "_id": 4, "renamed_index": 4, "renamed_parentId": 3, "renamed_categoryId": 2 },
                                                             { "_id": 6, "renamed_index": 6, "renamed_parentId": 4, "renamed_categoryId": 2 },
                                                             { "_id": 7, "renamed_index": 7, "renamed_parentId": 6, "renamed_categoryId": 2 },
                                                             { "_id": 8, "renamed_index": 8, "renamed_parentId": 7, "renamed_categoryId": 2 }
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
                                                    [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
                                                     "treeType2s": [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1, "renamed_treeType1Id": 1 },
                                                     { "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1, "renamed_treeType1Id": 1 }
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
                                                            [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
                                                             "children": [
                                                             { "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1 },
                                                             { "_id": 3, "renamed_index": 3, "renamed_parentId": 2, "renamed_categoryId": 2 },
                                                             { "_id": 4, "renamed_index": 4, "renamed_parentId": 3, "renamed_categoryId": 2 }
                                                             ]
                                                            }]
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
                                                                    [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
                                                                     "children": [
                                                                     { "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1 }
                                                                     ]
                                                                    }]
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
                                                                    [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
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
                                                                    "renamed_index": 1,
                                                                    "renamed_parentId": null,
                                                                    "renamed_categoryId": 1,
                                                                    "children": [
                                                                        {
                                                                            "_id": 2,
                                                                            "renamed_index": 2,
                                                                            "renamed_parentId": 1,
                                                                            "renamed_categoryId": 1
                                                                        },
                                                                        {
                                                                            "_id": 5,
                                                                            "renamed_index": 5,
                                                                            "renamed_parentId": 1,
                                                                            "renamed_categoryId": 1
                                                                        },
                                                                        {
                                                                            "_id": 3,
                                                                            "renamed_index": 3,
                                                                            "renamed_parentId": 2,
                                                                            "renamed_categoryId": 2
                                                                        },
                                                                        {
                                                                            "_id": 4,
                                                                            "renamed_index": 4,
                                                                            "renamed_parentId": 3,
                                                                            "renamed_categoryId": 2
                                                                        },
                                                                        {
                                                                            "_id": 6,
                                                                            "renamed_index": 6,
                                                                            "renamed_parentId": 4,
                                                                            "renamed_categoryId": 2
                                                                        },
                                                                        {
                                                                            "_id": 7,
                                                                            "renamed_index": 7,
                                                                            "renamed_parentId": 6,
                                                                            "renamed_categoryId": 2
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
                    		"renamed_index": 1,
                    		"renamed_parentId": null,
                    		"renamed_categoryId": 1,
                    		"children": [
                    			{
                    				"_id": 5,
                    				"renamed_index": 5,
                    				"renamed_parentId": 1,
                    				"renamed_categoryId": 1
                    			},
                    			{
                    				"_id": 2,
                    				"renamed_index": 2,
                    				"renamed_parentId": 1,
                    				"renamed_categoryId": 1
                    			},
                    			{
                    				"_id": 3,
                    				"renamed_index": 3,
                    				"renamed_parentId": 2,
                    				"renamed_categoryId": 2
                    			},
                    			{
                    				"_id": 4,
                    				"renamed_index": 4,
                    				"renamed_parentId": 3,
                    				"renamed_categoryId": 2
                    			},
                    			{
                    				"_id": 6,
                    				"renamed_index": 6,
                    				"renamed_parentId": 4,
                    				"renamed_categoryId": 2
                    			},
                    			{
                    				"_id": 7,
                    				"renamed_index": 7,
                    				"renamed_parentId": 6,
                    				"renamed_categoryId": 2
                    			}
                    		]
                    	}
                    ]""",
            JSONCompareMode.STRICT_ORDER),
        // Expand level handle by $lookup stage and order by index with asc order
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=treeType2s($orderby=index asc)",
            """
                                                    [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
                                                     "treeType2s": [
                                                     { "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1, "renamed_treeType1Id": 1 },
                                                     { "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1, "renamed_treeType1Id": 1 },
                                                     { "_id": 3, "renamed_index": 3, "renamed_parentId": 2, "renamed_categoryId": 2, "renamed_treeType1Id": 1 }
                                                     ]
                                                    }]
                                                    """,
            JSONCompareMode.STRICT_ORDER),
        // Expand level handle by $lookup stage and order by index with desc order
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=treeType2s($orderby=index desc)",
            """
                                                    [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
                                                     "treeType2s": [
                                                     { "_id": 3, "renamed_index": 3, "renamed_parentId": 2, "renamed_categoryId": 2, "renamed_treeType1Id": 1 },
                                                     { "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1, "renamed_treeType1Id": 1 },
                                                     { "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1, "renamed_treeType1Id": 1 }
                                                     ]
                                                    }]
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
                            			"renamed_index": 10,
                            			"renamed_parentId": null,
                            			"renamed_categoryId": 1,
                            			"children": [
                            				{
                            					"_id": 11,
                            					"renamed_index": 11,
                            					"renamed_parentId": 10,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 20,
                            					"renamed_index": 20,
                            					"renamed_parentId": 13,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 23,
                            					"renamed_index": 23,
                            					"renamed_parentId": 14,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 19,
                            					"renamed_index": 19,
                            					"renamed_parentId": 12,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 12,
                            					"renamed_index": 12,
                            					"renamed_parentId": 10,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 16,
                            					"renamed_index": 16,
                            					"renamed_parentId": 11,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 24,
                            					"renamed_index": 24,
                            					"renamed_parentId": 14,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 18,
                            					"renamed_index": 18,
                            					"renamed_parentId": 12,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 13,
                            					"renamed_index": 13,
                            					"renamed_parentId": 10,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 21,
                            					"renamed_index": 21,
                            					"renamed_parentId": 14,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 17,
                            					"renamed_index": 17,
                            					"renamed_parentId": 12,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 15,
                            					"renamed_index": 15,
                            					"renamed_parentId": 11,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 22,
                            					"renamed_index": 22,
                            					"renamed_parentId": 14,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 14,
                            					"renamed_index": 14,
                            					"renamed_parentId": 11,
                            					"renamed_categoryId": 1
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
                            			"renamed_index": 10,
                            			"renamed_parentId": null,
                            			"renamed_categoryId": 1,
                            			"children": [
                            				{
                            					"_id": 11,
                            					"renamed_index": 11,
                            					"renamed_parentId": 10,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 12,
                            					"renamed_index": 12,
                            					"renamed_parentId": 10,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 14,
                            					"renamed_index": 14,
                            					"renamed_parentId": 11,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 15,
                            					"renamed_index": 15,
                            					"renamed_parentId": 11,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 17,
                            					"renamed_index": 17,
                            					"renamed_parentId": 12,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 18,
                            					"renamed_index": 18,
                            					"renamed_parentId": 12,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 21,
                            					"renamed_index": 21,
                            					"renamed_parentId": 14,
                            					"renamed_categoryId": 1
                            				},
                            				{
                            					"_id": 22,
                            					"renamed_index": 22,
                            					"renamed_parentId": 14,
                            					"renamed_categoryId": 1
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
                                                "renamed_index": 10,
                                                "renamed_parentId": null,
                                                "renamed_categoryId": 1,
                                                "children": [
                                                    {
                                                        "_id": 13,
                                                        "renamed_index": 13,
                                                        "renamed_parentId": 10,
                                                        "renamed_categoryId": 1
                                                    },
                                                    {
                                                        "_id": 12,
                                                        "renamed_index": 12,
                                                        "renamed_parentId": 10,
                                                        "renamed_categoryId": 1
                                                    },
                                                    {
                                                        "_id": 20,
                                                        "renamed_index": 20,
                                                        "renamed_parentId": 13,
                                                        "renamed_categoryId": 1
                                                    },
                                                    {
                                                        "_id": 19,
                                                        "renamed_index": 19,
                                                        "renamed_parentId": 12,
                                                        "renamed_categoryId": 1
                                                    },
                                                    {
                                                        "_id": 18,
                                                        "renamed_index": 18,
                                                        "renamed_parentId": 12,
                                                        "renamed_categoryId": 1
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
                                                "renamed_index": 10,
                                                "renamed_parentId": null,
                                                "renamed_categoryId": 1,
                                                "children": [
                                                    {
                                                        "_id": 12,
                                                        "renamed_index": 12,
                                                        "renamed_parentId": 10,
                                                        "renamed_categoryId": 1
                                                    },
                                                    {
                                                        "_id": 13,
                                                        "renamed_index": 13,
                                                        "renamed_parentId": 10,
                                                        "renamed_categoryId": 1
                                                    },{
                                                        "_id": 18,
                                                        "renamed_index": 18,
                                                        "renamed_parentId": 12,
                                                        "renamed_categoryId": 1
                                                    },
                                                    {
                                                        "_id": 19,
                                                        "renamed_index": 19,
                                                        "renamed_parentId": 12,
                                                        "renamed_categoryId": 1
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
                                                        "renamed_index": 10,
                                                        "renamed_parentId": null,
                                                        "renamed_categoryId": 1,
                                                        "children": [
                                                            {
                                                                "renamed_index": 12
                                                            },
                                                            {
                                                                "renamed_index": 13
                                                            },{
                                                                "renamed_index": 18
                                                            },
                                                            {
                                                                "renamed_index": 19
                                                            }
                                                        ]
                                                    }
                                                ]
                                            """,
            JSONCompareMode.STRICT),
        // Testing skip and orderby for 1st level only query that use under hood $lookup
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=treeType2s($skip=1;$orderby=index asc)",
            """
                                                    [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
                                                     "treeType2s": [
                                                     { "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1, "renamed_treeType1Id": 1 },
                                                     { "_id": 3, "renamed_index": 3, "renamed_parentId": 2, "renamed_categoryId": 2, "renamed_treeType1Id": 1 }
                                                     ]
                                                    }]
                                                    """,
            JSONCompareMode.LENIENT),
        // Testing skip, index and orderby for 1st level only query that use under hood $lookup
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=treeType2s($skip=1;$top=1;$orderby=index asc;$select=index)",
            """
                                                    [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
                                                     "treeType2s": [
                                                     { "renamed_index": 2}
                                                     ]
                                                    }]
                                                    """,
            JSONCompareMode.LENIENT),
        // Nested level 1 with root level 1
        // TODO Required register empty array/object level and cleaning nested fields if level is
        // level (more nested)
        // TODO than first empty level set. It will going to required configuration set up
        //        Arguments.of(
        //            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
        //            Set.of(1),
        //            "$expand=children($expand=treeType2s)",
        //            """
        //                                            [{ "_id": 1, "renamed_index": 1,
        // "renamed_parentId": null,
        // "renamed_categoryId": 1,
        //                                             "children": [
        //                                                { "_id": 2, "renamed_index": 2,
        // "renamed_parentId": 1,
        // "renamed_categoryId": 1,
        //                                                    "treeType2s": [
        //
        // {"_id":4,"renamed_index":4,"renamed_categoryId":1},
        //
        // {"_id":5,"renamed_index":5,"renamed_parentId":4,"renamed_categoryId":1,"renamed_treeType1Id":2},
        //
        // {"_id":6,"renamed_index":6,"renamed_parentId":5,"renamed_categoryId":2,"renamed_treeType1Id":2}
        //                                                    ]
        //                                                },
        //                                                { "_id": 5, "renamed_index": 5,
        // "renamed_parentId": 1,
        // "renamed_categoryId": 1,
        //                                                    "treeType2s": []
        //                                                 }
        //                                             ]
        //                                            }]
        //                                            """,
        //            JSONCompareMode.LENIENT),
        // Nested level 1 with root level 1
        // Important! By default, empty arrays and object that being expanded are removed
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($expand=treeType2s)",
            """
                                                    [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
                                                     "children": [
                                                        { "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1,
                                                            "treeType2s": [
                                                                {"_id":4,"renamed_index":4,"renamed_categoryId":1},
                                                                {"_id":5,"renamed_index":5,"renamed_parentId":4,"renamed_categoryId":1,"renamed_treeType1Id":2},
                                                                {"_id":6,"renamed_index":6,"renamed_parentId":5,"renamed_categoryId":2,"renamed_treeType1Id":2}
                                                            ]
                                                        },
                                                        { "_id": 5, "renamed_index": 5, "renamed_parentId": 1, "renamed_categoryId": 1
                                                         }
                                                     ]
                                                    }]
                                                    """,
            JSONCompareMode.LENIENT),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(2, 5, 3, 4, 6, 7),
            "$expand=treeType2s",
            """
                                                            [ { "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1,
                                                                    "treeType2s": [
                                                                        {"_id":4,"renamed_index":4,"renamed_categoryId":1},
                                                                        {"_id":5,"renamed_index":5,"renamed_parentId":4,"renamed_categoryId":1,"renamed_treeType1Id":2},
                                                                        {"_id":6,"renamed_index":6,"renamed_parentId":5,"renamed_categoryId":2,"renamed_treeType1Id":2}
                                                                    ]
                                                                },
                                                                    { "_id": 5, "renamed_index": 5, "renamed_parentId": 1, "renamed_categoryId": 1
                                                                     },
                                                                 { "_id": 3, "renamed_index": 3, "renamed_parentId": 2, "renamed_categoryId": 2 },
                                                                 { "_id": 4, "renamed_index": 4, "renamed_parentId": 3, "renamed_categoryId": 2 },
                                                                 { "_id": 6, "renamed_index": 6, "renamed_parentId": 4, "renamed_categoryId": 2 },
                                                                 { "_id": 7, "renamed_index": 7, "renamed_parentId": 6, "renamed_categoryId": 2 }
                                                             ]
                                                            """,
            JSONCompareMode.LENIENT),
        // TODO $expand=children($level=max;$expand=treeType2s)
        // TODO Fix assertions for the treeType2s
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=max;$expand=treeType2s)",
            """
                                                            [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
                                                             "children": [
                                                                { "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1,
                                                                    "treeType2s": [
                                                                        {"_id":4,"renamed_index":4,"renamed_categoryId":1},
                                                                        {"_id":5,"renamed_index":5,"renamed_parentId":4,"renamed_categoryId":1,"renamed_treeType1Id":2},
                                                                        {"_id":6,"renamed_index":6,"renamed_parentId":5,"renamed_categoryId":2,"renamed_treeType1Id":2}
                                                                    ]
                                                                },
                                                                    { "_id": 5, "renamed_index": 5, "renamed_parentId": 1, "renamed_categoryId": 1
                                                                     },
                                                                 { "_id": 3, "renamed_index": 3, "renamed_parentId": 2, "renamed_categoryId": 2 },
                                                                 { "_id": 4, "renamed_index": 4, "renamed_parentId": 3, "renamed_categoryId": 2 },
                                                                 { "_id": 6, "renamed_index": 6, "renamed_parentId": 4, "renamed_categoryId": 2 },
                                                                 { "_id": 7, "renamed_index": 7, "renamed_parentId": 6, "renamed_categoryId": 2 }
                                                             ]
                                                            }]
                                                            """,
            JSONCompareMode.LENIENT),
        // TODO $expand=children($level=max;$expand=treeType2s($level=max))
        Arguments.of(
            TREETYPE2_MONGO_COLLECTION_USAGE_INFO,
            Set.of(4, 5, 6),
            "$expand=treeType1($expand=treeType2s($select=_id,index,treeType1Id);$select=_id,index)",
            """
                            [
                            		{
                            			"_id": 4,
                            			"renamed_index": 4,
                            			"renamed_parentId": null,
                            			"renamed_categoryId": 1,
                            			"renamed_treeType1Id": 2,
                            			"treeType1": {
                            				"_id": 2,
                            				"renamed_index": 2,
                            				"treeType2s": [
                            					{
                            						"_id": 4,
                            						"renamed_index": 4,
                            						"renamed_treeType1Id": 2
                            					},
                            					{
                            						"_id": 5,
                            						"renamed_index": 5,
                            						"renamed_treeType1Id": 2
                            					},
                            					{
                            						"_id": 6,
                            						"renamed_index": 6,
                            						"renamed_treeType1Id": 2
                            					}
                            				]
                            			}
                            		},
                            		{
                            			"_id": 5,
                            			"renamed_index": 5,
                            			"renamed_parentId": 4,
                            			"renamed_categoryId": 1,
                            			"renamed_treeType1Id": 2,
                            			"treeType1": {
                            				"_id": 2,
                            				"renamed_index": 2,
                            				"treeType2s": [
                            					{
                            						"_id": 4,
                            						"renamed_index": 4,
                            						"renamed_treeType1Id": 2
                            					},
                            					{
                            						"_id": 5,
                            						"renamed_index": 5,
                            						"renamed_treeType1Id": 2
                            					},
                            					{
                            						"_id": 6,
                            						"renamed_index": 6,
                            						"renamed_treeType1Id": 2
                            					}
                            				]
                            			}
                            		},
                            		{
                            			"_id": 6,
                            			"renamed_index": 6,
                            			"renamed_parentId": 5,
                            			"renamed_categoryId": 2,
                            			"renamed_treeType1Id": 2,
                            			"treeType1": {
                            				"_id": 2,
                            				"renamed_index": 2,
                            				"treeType2s": [
                            					{
                            						"_id": 4,
                            						"renamed_index": 4,
                            						"renamed_treeType1Id": 2
                            					},
                            					{
                            						"_id": 5,
                            						"renamed_index": 5,
                            						"renamed_treeType1Id": 2
                            					},
                            					{
                            						"_id": 6,
                            						"renamed_index": 6,
                            						"renamed_treeType1Id": 2
                            					}
                            				]
                            			}
                            		}
                            	]
                            """,
            JSONCompareMode.LENIENT),
        // Nested level 1 with root level 1
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($expand=treeType2s($expand=treeType1($expand=treeType2s($select=_id,index,treeType1Id);$select=_id,index)))",
            """
                                                    [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
                                                     "children": [
                                                        { "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1,
                                                            "treeType2s": [
                                                                {
                                                                    "_id": 4,
                                                                    "renamed_index": 4,
                                                                    "renamed_parentId": null,
                                                                    "renamed_categoryId": 1,
                                                                    "renamed_treeType1Id": 2,
                                                                    "treeType1": {
                                                                        "_id": 2,
                                                                        "renamed_index": 2,
                                                                        "treeType2s": [
                                                                            {
                                                                                "_id": 4,
                                                                                "renamed_index": 4,
                                                                                "renamed_treeType1Id": 2
                                                                            },
                                                                            {
                                                                                "_id": 5,
                                                                                "renamed_index": 5,
                                                                                "renamed_treeType1Id": 2
                                                                            },
                                                                            {
                                                                                "_id": 6,
                                                                                "renamed_index": 6,
                                                                                "renamed_treeType1Id": 2
                                                                            }
                                                                        ]
                                                                    }
                                                                },
                                                                {
                                                                    "_id": 5,
                                                                    "renamed_index": 5,
                                                                    "renamed_parentId": 4,
                                                                    "renamed_categoryId": 1,
                                                                    "renamed_treeType1Id": 2,
                                                                    "treeType1": {
                                                                        "_id": 2,
                                                                        "renamed_index": 2,
                                                                        "treeType2s": [
                                                                            {
                                                                                "_id": 4,
                                                                                "renamed_index": 4,
                                                                                "renamed_treeType1Id": 2
                                                                            },
                                                                            {
                                                                                "_id": 5,
                                                                                "renamed_index": 5,
                                                                                "renamed_treeType1Id": 2
                                                                            },
                                                                            {
                                                                                "_id": 6,
                                                                                "renamed_index": 6,
                                                                                "renamed_treeType1Id": 2
                                                                            }
                                                                        ]
                                                                    }
                                                                },
                                                                {
                                                                    "_id": 6,
                                                                    "renamed_index": 6,
                                                                    "renamed_parentId": 5,
                                                                    "renamed_categoryId": 2,
                                                                    "renamed_treeType1Id": 2,
                                                                    "treeType1": {
                                                                        "_id": 2,
                                                                        "renamed_index": 2,
                                                                        "treeType2s": [
                                                                            {
                                                                                "_id": 4,
                                                                                "renamed_index": 4,
                                                                                "renamed_treeType1Id": 2
                                                                            },
                                                                            {
                                                                                "_id": 5,
                                                                                "renamed_index": 5,
                                                                                "renamed_treeType1Id": 2
                                                                            },
                                                                            {
                                                                                "_id": 6,
                                                                                "renamed_index": 6,
                                                                                "renamed_treeType1Id": 2
                                                                            }
                                                                        ]
                                                                    }
                                                                }
                                                            ]
                                                        },
                                                        { "_id": 5, "renamed_index": 5, "renamed_parentId": 1, "renamed_categoryId": 1,
                                                            "treeType2s": []
                                                         }
                                                     ]
                                                    }]
                                                    """,
            JSONCompareMode.LENIENT)
        // TODO Nested level 1 with root level 1 and graph
        ,
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(1),
            "$expand=children($levels=max;$expand=treeType2s($expand=children($levels=max)))",
            """
                                                                    [
                                                                        {
                                                                        "_id": 1,
                                                                        "renamed_index": 1,
                                                                        "renamed_parentId": null,
                                                                        "renamed_categoryId": 1,
                                                                        "children": [
                                                                            {
                                                                                "_id": 7,
                                                                                "renamed_index": 7,
                                                                                "renamed_parentId": 6,
                                                                                "renamed_categoryId": 2,
                                                                                "treeType2s": []
                                                                            },
                                                                            {
                                                                                "_id": 3,
                                                                                "renamed_index": 3,
                                                                                "renamed_parentId": 2,
                                                                                "renamed_categoryId": 2,
                                                                                "treeType2s": []
                                                                            },
                                                                            {
                                                                                "_id": 2,
                                                                                "renamed_index": 2,
                                                                                "renamed_parentId": 1,
                                                                                "renamed_categoryId": 1,
                                                                                "treeType2s": [
                                                                                    {
                                                                                        "_id": 4,
                                                                                        "renamed_index": 4,
                                                                                        "renamed_parentId": null,
                                                                                        "renamed_categoryId": 1,
                                                                                        "renamed_treeType1Id": 2,
                                                                                        "children": [
                                                                                            {
                                                                                                "_id": 5,
                                                                                                "renamed_index": 5,
                                                                                                "renamed_parentId": 4,
                                                                                                "renamed_categoryId": 1,
                                                                                                "renamed_treeType1Id": 2
                                                                                            },
                                                                                            {
                                                                                                "_id": 6,
                                                                                                "renamed_index": 6,
                                                                                                "renamed_parentId": 5,
                                                                                                "renamed_categoryId": 2,
                                                                                                "renamed_treeType1Id": 2
                                                                                            }
                                                                                        ]
                                                                                    },
                                                                                    {
                                                                                        "_id": 5,
                                                                                        "renamed_index": 5,
                                                                                        "renamed_parentId": 4,
                                                                                        "renamed_categoryId": 1,
                                                                                        "renamed_treeType1Id": 2,
                                                                                        "children": [
                                                                                            {
                                                                                                "_id": 6,
                                                                                                "renamed_index": 6,
                                                                                                "renamed_parentId": 5,
                                                                                                "renamed_categoryId": 2,
                                                                                                "renamed_treeType1Id": 2
                                                                                            }
                                                                                        ]
                                                                                    },
                                                                                    {
                                                                                        "_id": 6,
                                                                                        "renamed_index": 6,
                                                                                        "renamed_parentId": 5,
                                                                                        "renamed_categoryId": 2,
                                                                                        "renamed_treeType1Id": 2,
                                                                                        "children": []
                                                                                    }
                                                                                ]
                                                                            },
                                                                            {
                                                                                "_id": 5,
                                                                                "renamed_index": 5,
                                                                                "renamed_parentId": 1,
                                                                                "renamed_categoryId": 1,
                                                                                "treeType2s": []
                                                                            },
                                                                            {
                                                                                "_id": 4,
                                                                                "renamed_index": 4,
                                                                                "renamed_parentId": 3,
                                                                                "renamed_categoryId": 2,
                                                                                "treeType2s": []
                                                                            },
                                                                            {
                                                                                "_id": 6,
                                                                                "renamed_index": 6,
                                                                                "renamed_parentId": 4,
                                                                                "renamed_categoryId": 2,
                                                                                "treeType2s": []
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
                                                                                            "renamed_index": 5,
                                                                                            "renamed_parentId": 1,
                                                                                            "renamed_categoryId": 1,
                                                                                            "parent": [
                                                                                                {
                                                                                                    "_id": 1,
                                                                                                    "renamed_index": 1,
                                                                                                    "renamed_parentId": null,
                                                                                                    "renamed_categoryId": 1,
                                                                                                    "children": [
                                                                                                        {
                                                                                                            "_id": 3,
                                                                                                            "renamed_index": 3,
                                                                                                            "renamed_parentId": 2,
                                                                                                            "renamed_categoryId": 2
                                                                                                        },
                                                                                                        {
                                                                                                            "_id": 2,
                                                                                                            "renamed_index": 2,
                                                                                                            "renamed_parentId": 1,
                                                                                                            "renamed_categoryId": 1
                                                                                                        },
                                                                                                        {
                                                                                                            "_id": 5,
                                                                                                            "renamed_index": 5,
                                                                                                            "renamed_parentId": 1,
                                                                                                            "renamed_categoryId": 1
                                                                                                        }
                                                                                                    ]
                                                                                                }
                                                                                            ]
                                                                                        },
                                                                                        {
                                                                                            "_id": 7,
                                                                                            "renamed_index": 7,
                                                                                            "renamed_parentId": 6,
                                                                                            "renamed_categoryId": 2,
                                                                                            "parent": [
                                                                                                {
                                                                                                    "_id": 6,
                                                                                                    "renamed_index": 6,
                                                                                                    "renamed_parentId": 4,
                                                                                                    "renamed_categoryId": 2,
                                                                                                    "children": [
                                                                                                        {
                                                                                                            "_id": 7,
                                                                                                            "renamed_index": 7,
                                                                                                            "renamed_parentId": 6,
                                                                                                            "renamed_categoryId": 2
                                                                                                        },
                                                                                                        {
                                                                                                            "_id": 8,
                                                                                                            "renamed_index": 8,
                                                                                                            "renamed_parentId": 7,
                                                                                                            "renamed_categoryId": 2
                                                                                                        }
                                                                                                    ]
                                                                                                },
                                                                                                {
                                                                                                    "_id": 4,
                                                                                                    "renamed_index": 4,
                                                                                                    "renamed_parentId": 3,
                                                                                                    "renamed_categoryId": 2,
                                                                                                    "children": [
                                                                                                        {
                                                                                                            "_id": 6,
                                                                                                            "renamed_index": 6,
                                                                                                            "renamed_parentId": 4,
                                                                                                            "renamed_categoryId": 2
                                                                                                        },
                                                                                                        {
                                                                                                            "_id": 7,
                                                                                                            "renamed_index": 7,
                                                                                                            "renamed_parentId": 6,
                                                                                                            "renamed_categoryId": 2
                                                                                                        }
                                                                                                    ]
                                                                                                }
                                                                                            ]
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
                                                                                                    "renamed_index": 5,
                                                                                                    "renamed_parentId": 1,
                                                                                                    "renamed_categoryId": 1,
                                                                                                    "parent": [
                                                                                                        {
                                                                                                            "_id": 1,
                                                                                                            "renamed_index": 1,
                                                                                                            "renamed_parentId": null,
                                                                                                            "renamed_categoryId": 1,
                                                                                                            "children": [
                                                                                                                {
                                                                                                                    "_id": 3,
                                                                                                                    "renamed_index": 3,
                                                                                                                    "renamed_parentId": 2,
                                                                                                                    "renamed_categoryId": 2
                                                                                                                },
                                                                                                                {
                                                                                                                    "_id": 5,
                                                                                                                    "renamed_index": 5,
                                                                                                                    "renamed_parentId": 1,
                                                                                                                    "renamed_categoryId": 1
                                                                                                                },
                                                                                                                {
                                                                                                                    "_id": 2,
                                                                                                                    "renamed_index": 2,
                                                                                                                    "renamed_parentId": 1,
                                                                                                                    "renamed_categoryId": 1
                                                                                                                }
                                                                                                            ],
                                                                                                            "treeType2s": [
                                                                                                                {
                                                                                                                    "_id": 1,
                                                                                                                    "renamed_index": 1,
                                                                                                                    "renamed_parentId": null,
                                                                                                                    "renamed_categoryId": 1,
                                                                                                                    "renamed_treeType1Id": 1
                                                                                                                },
                                                                                                                {
                                                                                                                    "_id": 2,
                                                                                                                    "renamed_index": 2,
                                                                                                                    "renamed_parentId": 1,
                                                                                                                    "renamed_categoryId": 1,
                                                                                                                    "renamed_treeType1Id": 1
                                                                                                                },
                                                                                                                {
                                                                                                                    "_id": 3,
                                                                                                                    "renamed_index": 3,
                                                                                                                    "renamed_parentId": 2,
                                                                                                                    "renamed_categoryId": 2,
                                                                                                                    "renamed_treeType1Id": 1
                                                                                                                }
                                                                                                            ]
                                                                                                        }
                                                                                                    ]
                                                                                                },
                                                                                                {
                                                                                                    "_id": 7,
                                                                                                    "renamed_index": 7,
                                                                                                    "renamed_parentId": 6,
                                                                                                    "renamed_categoryId": 2,
                                                                                                    "parent": [
                                                                                                        {
                                                                                                            "_id": 6,
                                                                                                            "renamed_index": 6,
                                                                                                            "renamed_parentId": 4,
                                                                                                            "renamed_categoryId": 2,
                                                                                                            "children": [
                                                                                                                {
                                                                                                                    "_id": 8,
                                                                                                                    "renamed_index": 8,
                                                                                                                    "renamed_parentId": 7,
                                                                                                                    "renamed_categoryId": 2
                                                                                                                },
                                                                                                                {
                                                                                                                    "_id": 7,
                                                                                                                    "renamed_index": 7,
                                                                                                                    "renamed_parentId": 6,
                                                                                                                    "renamed_categoryId": 2
                                                                                                                }
                                                                                                            ]
                                                                                                        },
                                                                                                        {
                                                                                                            "_id": 4,
                                                                                                            "renamed_index": 4,
                                                                                                            "renamed_parentId": 3,
                                                                                                            "renamed_categoryId": 2,
                                                                                                            "children": [
                                                                                                                {
                                                                                                                    "_id": 6,
                                                                                                                    "renamed_index": 6,
                                                                                                                    "renamed_parentId": 4,
                                                                                                                    "renamed_categoryId": 2
                                                                                                                },
                                                                                                                {
                                                                                                                    "_id": 7,
                                                                                                                    "renamed_index": 7,
                                                                                                                    "renamed_parentId": 6,
                                                                                                                    "renamed_categoryId": 2
                                                                                                                }
                                                                                                            ]
                                                                                                        }
                                                                                                    ]
                                                                                                }
                                                                                            ]
                                                                                            """,
            JSONCompareMode.LENIENT),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(5, 7),
            "$expand=parent($levels=2;$expand=children($levels=2),treeType2s($select=_id,index,treeType1Id;$orderby=index asc);$select=_id,index;$orderby=index asc)&$orderby=index desc",
            """
                    [
                    	{
                    		"_id": 7,
                    		"renamed_index": 7,
                    		"renamed_parentId": 6,
                    		"renamed_categoryId": 2,
                    		"parent": [
                    			{
                    				"renamed_index": 4,
                    				"_id": 4,
                    				"children": [
                    					{
                    						"_id": 6,
                    						"renamed_index": 6,
                    						"renamed_parentId": 4,
                    						"renamed_categoryId": 2
                    					},
                    					{
                    						"_id": 7,
                    						"renamed_index": 7,
                    						"renamed_parentId": 6,
                    						"renamed_categoryId": 2
                    					}
                    				]
                    			},
                    			{
                    				"renamed_index": 6,
                    				"_id": 6,
                    				"children": [
                    					{
                    						"_id": 7,
                    						"renamed_index": 7,
                    						"renamed_parentId": 6,
                    						"renamed_categoryId": 2
                    					},
                    					{
                    						"_id": 8,
                    						"renamed_index": 8,
                    						"renamed_parentId": 7,
                    						"renamed_categoryId": 2
                    					}
                    				]
                    			}
                    		]
                    	},
                    	{
                    		"_id": 5,
                    		"renamed_index": 5,
                    		"renamed_parentId": 1,
                    		"renamed_categoryId": 1,
                    		"parent": [
                    			{
                    				"renamed_index": 1,
                    				"_id": 1,
                    				"children": [
                    					{
                    						"_id": 5,
                    						"renamed_index": 5,
                    						"renamed_parentId": 1,
                    						"renamed_categoryId": 1
                    					},
                    					{
                    						"_id": 3,
                    						"renamed_index": 3,
                    						"renamed_parentId": 2,
                    						"renamed_categoryId": 2
                    					},
                    					{
                    						"_id": 2,
                    						"renamed_index": 2,
                    						"renamed_parentId": 1,
                    						"renamed_categoryId": 1
                    					}
                    				],
                    				"treeType2s": [
                    					{
                    						"_id": 1,
                    						"renamed_index": 1,
                    						"renamed_treeType1Id": 1
                    					},
                    					{
                    						"_id": 2,
                    						"renamed_index": 2,
                    						"renamed_treeType1Id": 1
                    					},
                    					{
                    						"_id": 3,
                    						"renamed_index": 3,
                    						"renamed_treeType1Id": 1
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
            Set.of(5),
            "$expand=parent($levels=2;$expand=children($levels=2;$orderby=index desc),treeType2s($select=_id,index,treeType1Id;$orderby=index asc);$select=_id,index;$orderby=index asc)&$orderby=index desc",
            """
                    [
                    	{
                    		"_id": 5,
                    		"renamed_index": 5,
                    		"renamed_parentId": 1,
                    		"renamed_categoryId": 1,
                    		"parent": [
                    			{
                    				"renamed_index": 1,
                    				"_id": 1,
                    				"children": [
                    					{
                    						"_id": 5,
                    						"renamed_index": 5,
                    						"renamed_parentId": 1,
                    						"renamed_categoryId": 1
                    					},
                    					{
                    						"_id": 2,
                    						"renamed_index": 2,
                    						"renamed_parentId": 1,
                    						"renamed_categoryId": 1
                    					},
                    					{
                    						"_id": 3,
                    						"renamed_index": 3,
                    						"renamed_parentId": 2,
                    						"renamed_categoryId": 2
                    					}
                    				],
                    				"treeType2s": [
                    					{
                    						"_id": 1,
                    						"renamed_index": 1,
                    						"renamed_treeType1Id": 1
                    					},
                    					{
                    						"_id": 2,
                    						"renamed_index": 2,
                    						"renamed_treeType1Id": 1
                    					},
                    					{
                    						"_id": 3,
                    						"renamed_index": 3,
                    						"renamed_treeType1Id": 1
                    					}
                    				]
                    			}
                    		]
                    	}
                    ]""",
            JSONCompareMode.STRICT_ORDER),
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            Set.of(7),
            "$expand=parent($levels=2;$expand=children($levels=2;$orderby=index desc),treeType2s($select=_id,index,treeType1Id;$orderby=index asc);$select=_id,index;$orderby=index asc)&$orderby=index desc",
            """
                    [
                    	{
                    		"_id": 7,
                    		"renamed_index": 7,
                    		"renamed_parentId": 6,
                    		"renamed_categoryId": 2,
                    		"parent": [
                    			{
                    				"renamed_index": 6,
                    				"_id": 6,
                    				"children": [
                    					{
                    						"_id": 7,
                    						"renamed_index": 7,
                    						"renamed_parentId": 6,
                    						"renamed_categoryId": 2
                    					},
                    					{
                    						"_id": 8,
                    						"renamed_index": 8,
                    						"renamed_parentId": 7,
                    						"renamed_categoryId": 2
                    					}
                    				]
                    			},
                    			{
                    				"renamed_index": 4,
                    				"_id": 4,
                    				"children": [
                    					{
                    						"_id": 6,
                    						"renamed_index": 6,
                    						"renamed_parentId": 4,
                    						"renamed_categoryId": 2
                    					},
                    					{
                    						"_id": 7,
                    						"renamed_index": 7,
                    						"renamed_parentId": 6,
                    						"renamed_categoryId": 2
                    					}
                    				]
                    			}
                    		]
                    	}
                    ]""",
            JSONCompareMode.STRICT_ORDER));
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
    MongoDatabase database = mongoClient.getDatabase("testdb_mapping");
    MongoCollection<Document> collection = database.getCollection(rootMongoCollection.getKey());
    Edm edm = loadEmdProvider("edm/tree_types.xml");
    UriInfo uriInfo =
        new Parser(edm, OData.newInstance())
            .parseUri(rootMongoCollection.getValue(), expandPart, null, null);
    ODataExpandToMongoAggregationPipelineParser tested =
        new ODataExpandToMongoAggregationPipelineParser();

    String rootEdmEntityTypeName =
        ((org.apache.olingo.server.api.uri.UriResourceEntitySet)
                uriInfo.getUriResourceParts().get(0))
            .getEntityType()
            .getFullQualifiedName()
            .getFullQualifiedNameAsString();

    // WHEN
    ExpandOperatorResult result =
        tested.parse(
            uriInfo.getExpandOption(),
            createParserContextBuilder().withRootEdmEntityTypeName(rootEdmEntityTypeName).build());
    List<Bson> pipeline = new ArrayList<>();
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

  private ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder
      createParserContextBuilder() {
    java.util.Map<String, EdmMongoContextFacade> edmTypeMapping = new java.util.HashMap<>();
    com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContextBuilder builder =
        new com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContextBuilder();

    com.github.starnowski.jamolingo.core.mapping.ODataMongoMappingFactory factory =
        new com.github.starnowski.jamolingo.core.mapping.ODataMongoMappingFactory();
    Edm edm = null;
    try {
      edm = loadEmdProvider("edm/tree_types.xml");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    com.github.starnowski.jamolingo.core.mapping.ODataMongoMapping odataMapping =
        factory.build(edm.getSchema("MyService"));

    com.github.starnowski.jamolingo.core.mapping.EntityMapping category =
        odataMapping.getEntities().get("Category");
    category.getProperties().get("name").setMongoPath("renamed_name");
    edmTypeMapping.put(
        "MyService.Category",
        com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade.builder()
            .withEntityPropertiesMongoPathContext(builder.build(category))
            .build());

    com.github.starnowski.jamolingo.core.mapping.EntityMapping t1 =
        odataMapping.getEntities().get("TreeType1");
    t1.getProperties().get("index").setMongoPath("renamed_index");
    t1.getProperties().get("parentId").setMongoPath("renamed_parentId");
    t1.getProperties().get("categoryId").setMongoPath("renamed_categoryId");
    edmTypeMapping.put(
        "MyService.TreeType1",
        com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade.builder()
            .withEntityPropertiesMongoPathContext(builder.build(t1))
            .build());

    com.github.starnowski.jamolingo.core.mapping.EntityMapping t2 =
        odataMapping.getEntities().get("TreeType2");
    t2.getProperties().get("index").setMongoPath("renamed_index");
    t2.getProperties().get("parentId").setMongoPath("renamed_parentId");
    t2.getProperties().get("categoryId").setMongoPath("renamed_categoryId");
    t2.getProperties().get("treeType1Id").setMongoPath("renamed_treeType1Id");
    edmTypeMapping.put(
        "MyService.TreeType2",
        com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade.builder()
            .withEntityPropertiesMongoPathContext(builder.build(t2))
            .build());

    com.github.starnowski.jamolingo.core.mapping.EntityMapping t3 =
        odataMapping.getEntities().get("TreeType3");
    t3.getProperties().get("index").setMongoPath("renamed_index");
    t3.getProperties().get("parentId").setMongoPath("renamed_parentId");
    t3.getProperties().get("categoryId").setMongoPath("renamed_categoryId");
    t3.getProperties().get("treeType2Id").setMongoPath("renamed_treeType2Id");
    edmTypeMapping.put(
        "MyService.TreeType3",
        com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade.builder()
            .withEntityPropertiesMongoPathContext(builder.build(t3))
            .build());

    com.github.starnowski.jamolingo.core.mapping.EntityMapping t4 =
        odataMapping.getEntities().get("TreeType4");
    t4.getProperties().get("index").setMongoPath("renamed_index");
    t4.getProperties().get("parentId").setMongoPath("renamed_parentId");
    t4.getProperties().get("categoryId").setMongoPath("renamed_categoryId");
    t4.getProperties().get("treeType3Id").setMongoPath("renamed_treeType3Id");
    edmTypeMapping.put(
        "MyService.TreeType4",
        com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade.builder()
            .withEntityPropertiesMongoPathContext(builder.build(t4))
            .build());

    return ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.builder()
        .withEdmTypeMapping(edmTypeMapping);
  }

  private Document wrapDocumentsList(List<Document> docs) {
    return new Document("value", docs);
  }

  private static Stream<Arguments> provideRawQueryData() {
    return Stream.of(
        // Nested level 1 with root level 1
        Arguments.of(
            TREETYPE1_MONGO_COLLECTION_USAGE_INFO,
            """
                                {
                                	"value": [
                                		{
                                			"$match": {
                                				"_id": {
                                					"$in": [
                                						1
                                					]
                                				}
                                			}
                                		},
                                		{
                                			"$lookup": {
                                				"from": "MyService.TreeType1",
                                				"localField": "_id",
                                				"foreignField": "renamed_parentId",
                                				"as": "children"
                                			}
                                		},
                                		{
                                			"$unwind": {
                                				"path": "$children",
                                				"preserveNullAndEmptyArrays": true
                                			}
                                		},
                                		{
                                			"$lookup": {
                                				"from": "MyService.TreeType2",
                                				"localField": "children._id",
                                				"foreignField": "renamed_treeType1Id",
                                				"as": "children.treeType2s"
                                			}
                                		},
                                		{
                                			"$unwind": {
                                				"path": "$children.treeType2s",
                                				"preserveNullAndEmptyArrays": true
                                			}
                                		},
                                		{
                                			"$lookup": {
                                				"from": "MyService.TreeType1",
                                				"localField": "children.treeType2s.renamed_treeType1Id",
                                				"foreignField": "_id",
                                				"pipeline": [
                                					{
                                						"$project": {
                                							"renamed_index": 1,
                                							"_id": 1
                                						}
                                					}
                                				],
                                				"as": "children.treeType2s.treeType1"
                                			}
                                		},
                                		{
                                			"$unwind": {
                                				"path": "$children.treeType2s.treeType1",
                                				"preserveNullAndEmptyArrays": true
                                			}
                                		},
                                		{
                                			"$lookup": {
                                				"from": "MyService.TreeType2",
                                				"localField": "children.treeType2s.treeType1._id",
                                				"foreignField": "renamed_treeType1Id",
                                				"pipeline": [
                                					{
                                						"$project": {
                                							"_id": 1,
                                							"renamed_index": 1,
                                							"renamed_treeType1Id": 1
                                						}
                                					}
                                				],
                                				"as": "children.treeType2s.treeType1.treeType2s"
                                			}
                                		},
                                		{
                                            "$addFields": {
                                              "children.treeType2s.treeType1.treeType2s": {
                                                "$cond": {
                                                  "if": {
                                                    "$eq": [{ "$size": "$children.treeType2s.treeType1.treeType2s" }, 0]
                                                  },
                                                  "then": "$$REMOVE",
                                                  "else": "$children.treeType2s.treeType1.treeType2s"
                                                }
                                              }
                                            }
                                         },
                                         {
                                              "$addFields": {
                                                "children.treeType2s.treeType1": {
                                                  "$cond": {
                                                    "if": {
                                                      "$eq": [
                                                        { "$size": { "$objectToArray": { "$ifNull": ["$children.treeType2s.treeType1", {}] } } },
                                                        0
                                                      ]
                                                    },
                                                    "then": "$$REMOVE",
                                                    "else": "$children.treeType2s.treeType1"
                                                  }
                                                }
                                              }
                                            },
                                        {
                                              "$addFields": {
                                                "children.treeType2s": {
                                                  "$cond": {
                                                    "if": {
                                                      "$eq": [
                                                        { "$size": { "$objectToArray": { "$ifNull": ["$children.treeType2s", {}] } } },
                                                        0
                                                      ]
                                                    },
                                                    "then": "$$REMOVE",
                                                    "else": "$children.treeType2s"
                                                  }
                                                }
                                              }
                                            },
                                		{
                                			"$group": {
                                				"_id": {
                                					"id_0": "$children._id",
                                					"id_1": "$_id"
                                				},
                                				"tmp_array": {
                                					"$push": "$children.treeType2s"
                                				},
                                				"tmp_root": {
                                					"$first": "$$ROOT"
                                				}
                                			}
                                		},
                                		{
                                			"$set": {
                                				"tmp_root.children.treeType2s": "$tmp_array"
                                			}
                                		},
                                		{
                                			"$replaceRoot": {
                                				"newRoot": "$tmp_root"
                                			}
                                		},
                                		{
                                			"$group": {
                                				"_id": {
                                					"id_0": "$_id"
                                				},
                                				"tmp_array": {
                                					"$push": "$children"
                                				},
                                				"tmp_root": {
                                					"$first": "$$ROOT"
                                				}
                                			}
                                		},
                                		{
                                			"$set": {
                                				"tmp_root.children": "$tmp_array"
                                			}
                                		},
                                		{
                                			"$replaceRoot": {
                                				"newRoot": "$tmp_root"
                                			}
                                		}
                                	]
                                }
                        """,
            """
                                                                [{ "_id": 1, "renamed_index": 1, "renamed_parentId": null, "renamed_categoryId": 1,
                                                                 "children": [
                                                                    { "_id": 2, "renamed_index": 2, "renamed_parentId": 1, "renamed_categoryId": 1,
                                                                        "treeType2s": [
                                                                            {
                                                                                "_id": 4,
                                                                                "renamed_index": 4,
                                                                                "renamed_parentId": null,
                                                                                "renamed_categoryId": 1,
                                                                                "renamed_treeType1Id": 2,
                                                                                "treeType1": {
                                                                                    "_id": 2,
                                                                                    "renamed_index": 2,
                                                                                    "treeType2s": [
                                                                                        {
                                                                                            "_id": 4,
                                                                                            "renamed_index": 4,
                                                                                            "renamed_treeType1Id": 2
                                                                                        },
                                                                                        {
                                                                                            "_id": 5,
                                                                                            "renamed_index": 5,
                                                                                            "renamed_treeType1Id": 2
                                                                                        },
                                                                                        {
                                                                                            "_id": 6,
                                                                                            "renamed_index": 6,
                                                                                            "renamed_treeType1Id": 2
                                                                                        }
                                                                                    ]
                                                                                }
                                                                            },
                                                                            {
                                                                                "_id": 5,
                                                                                "renamed_index": 5,
                                                                                "renamed_parentId": 4,
                                                                                "renamed_categoryId": 1,
                                                                                "renamed_treeType1Id": 2,
                                                                                "treeType1": {
                                                                                    "_id": 2,
                                                                                    "renamed_index": 2,
                                                                                    "treeType2s": [
                                                                                        {
                                                                                            "_id": 4,
                                                                                            "renamed_index": 4,
                                                                                            "renamed_treeType1Id": 2
                                                                                        },
                                                                                        {
                                                                                            "_id": 5,
                                                                                            "renamed_index": 5,
                                                                                            "renamed_treeType1Id": 2
                                                                                        },
                                                                                        {
                                                                                            "_id": 6,
                                                                                            "renamed_index": 6,
                                                                                            "renamed_treeType1Id": 2
                                                                                        }
                                                                                    ]
                                                                                }
                                                                            },
                                                                            {
                                                                                "_id": 6,
                                                                                "renamed_index": 6,
                                                                                "renamed_parentId": 5,
                                                                                "renamed_categoryId": 2,
                                                                                "renamed_treeType1Id": 2,
                                                                                "treeType1": {
                                                                                    "_id": 2,
                                                                                    "renamed_index": 2,
                                                                                    "treeType2s": [
                                                                                        {
                                                                                            "_id": 4,
                                                                                            "renamed_index": 4,
                                                                                            "renamed_treeType1Id": 2
                                                                                        },
                                                                                        {
                                                                                            "_id": 5,
                                                                                            "renamed_index": 5,
                                                                                            "renamed_treeType1Id": 2
                                                                                        },
                                                                                        {
                                                                                            "_id": 6,
                                                                                            "renamed_index": 6,
                                                                                            "renamed_treeType1Id": 2
                                                                                        }
                                                                                    ]
                                                                                }
                                                                            }
                                                                        ]
                                                                    },
                                                                    { "_id": 5, "renamed_index": 5, "renamed_parentId": 1, "renamed_categoryId": 1,
                                                                        "treeType2s": []
                                                                     }
                                                                 ]
                                                                }]
                                                                """,
            JSONCompareMode.LENIENT));
  }

  @ParameterizedTest
  @MethodSource("provideRawQueryData")
  public void shouldReturnExpectedDocumentsForRawQuery(
      KeyValue<String, String> rootMongoCollection,
      String queryObjectString,
      String expectedJson,
      JSONCompareMode jsonCompareMode)
      throws JSONException {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("testdb_mapping");
    MongoCollection<Document> collection = database.getCollection(rootMongoCollection.getKey());

    // WHEN
    List<Bson> pipeline = new ArrayList<>();
    Document pipelineDocument = Document.parse(queryObjectString);
    pipeline.addAll(pipelineDocument.get("value", List.class));
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
}
