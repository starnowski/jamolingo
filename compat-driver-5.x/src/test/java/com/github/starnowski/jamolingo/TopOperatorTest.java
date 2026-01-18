package com.github.starnowski.jamolingo;

import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import com.github.starnowski.jamolingo.top.OdataTopToMongoLimitParser;
import com.github.starnowski.jamolingo.top.TopOperatorResult;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@QuarkusTestResource(EmbeddedMongoResource.class)
public class TopOperatorTest extends AbstractItTest {

  @Inject MongoClient mongoClient;

  @ParameterizedTest
  @MethodSource("provideShouldReturnExpectedNumberOfDocuments")
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/edm1.json"),
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/edm3.json"),
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/edm4.json")
      })
  public void shouldReturnExpectedNumberOfDocuments(int topValue) throws Exception {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("testdb");
    MongoCollection<Document> collection = database.getCollection("Items");
    Edm edm = loadEmdProvider("edm/edm1.xml");

    UriInfo uriInfo =
        new Parser(edm, OData.newInstance()).parseUri("Items", "$top=" + topValue, null, null);
    OdataTopToMongoLimitParser parser = new OdataTopToMongoLimitParser();

    // WHEN
    TopOperatorResult result = parser.parse(uriInfo.getTopOption());
    List<Bson> pipeline = new ArrayList<>(result.getStageObjects());

    List<Document> results = new ArrayList<>();
    collection.aggregate(pipeline).into(results);

    // THEN
    Assertions.assertEquals(topValue, results.size());
  }

  private static Stream<Arguments> provideShouldReturnExpectedNumberOfDocuments() {
    return Stream.of(Arguments.of(0), Arguments.of(1), Arguments.of(2), Arguments.of(3));
  }
}
