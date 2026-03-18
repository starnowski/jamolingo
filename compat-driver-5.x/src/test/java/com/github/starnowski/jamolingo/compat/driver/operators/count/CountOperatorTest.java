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
import java.util.stream.IntStream;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
@QuarkusTestResource(value = EmbeddedMongoResource.class, restrictToAnnotatedClass = true)
public class CountOperatorTest extends AbstractItTest {

  @Inject MongoClient mongoClient;

  @ParameterizedTest
  @CsvSource(
      value = {"1, null, count", "2, null, count", "3, null, count"},
      nullValues = "null")
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "")
      })
  public void shouldReturnCountWithDefaultFieldName(
      int expectedCount, String customFieldName, String resultFieldName) throws Exception {
    testCount(expectedCount, customFieldName, resultFieldName);
  }

  @ParameterizedTest
  @CsvSource(value = {"4, total, total"})
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "")
      })
  public void shouldReturnCountWithCustomFieldName(
      int expectedCount, String customFieldName, String resultFieldName) throws Exception {
    testCount(expectedCount, customFieldName, resultFieldName);
  }

  private void testCount(int expectedCount, String customFieldName, String resultFieldName)
      throws Exception {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("testdb");
    MongoCollection<Document> collection = database.getCollection("Items");
    List<Document> documents =
        IntStream.rangeClosed(1, expectedCount)
            .mapToObj(i -> new Document().append("index", i))
            .toList();
    collection.insertMany(documents);
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
