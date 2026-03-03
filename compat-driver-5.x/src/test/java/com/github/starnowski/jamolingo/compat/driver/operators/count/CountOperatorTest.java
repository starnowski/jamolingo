package com.github.starnowski.jamolingo.compat.driver.operators.count;

import com.github.starnowski.jamolingo.AbstractItTest;
import com.github.starnowski.jamolingo.EmbeddedMongoResource;
import com.github.starnowski.jamolingo.core.operators.count.CountOperatorResult;
import com.github.starnowski.jamolingo.core.operators.count.OdataCountToMongoCountParser;
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
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(EmbeddedMongoResource.class)
public class CountOperatorTest extends AbstractItTest {

  @Inject MongoClient mongoClient;

  @Test
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/item.json")
      })
  public void shouldReturnCountOfOneWithDefaultFieldName() throws Exception {
    testCount(1, null, "count");
  }

  @Test
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/item.json"),
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/item.json")
      })
  public void shouldReturnCountOfTwoWithDefaultFieldName() throws Exception {
    testCount(2, null, "count");
  }

  @Test
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/item.json"),
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/item.json"),
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/item.json")
      })
  public void shouldReturnCountOfThreeWithDefaultFieldName() throws Exception {
    testCount(3, null, "count");
  }

  @Test
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/item.json"),
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/item.json"),
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/item.json"),
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/item.json")
      })
  public void shouldReturnCountOfFourWithCustomFieldName() throws Exception {
    testCount(4, "total", "total");
  }

  private void testCount(int expectedCount, String customFieldName, String resultFieldName)
      throws Exception {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("testdb");
    MongoCollection<Document> collection = database.getCollection("Items");
    Edm edm = loadEmdProvider("edm/edm1.xml");

    UriInfo uriInfo =
        new Parser(edm, OData.newInstance()).parseUri("Items", "$count=true", null, null);
    OdataCountToMongoCountParser parser = new OdataCountToMongoCountParser();

    // WHEN
    CountOperatorResult result =
        customFieldName == null
            ? parser.parse(uriInfo.getCountOption())
            : parser.parse(uriInfo.getCountOption(), customFieldName);
    List<Bson> pipeline = new ArrayList<>(result.getStageObjects());

    List<Document> results = new ArrayList<>();
    collection.aggregate(pipeline).into(results);

    // THEN
    Assertions.assertEquals(1, results.size());
    Assertions.assertEquals(expectedCount, results.get(0).getInteger(resultFieldName));
  }
}
