package com.github.starnowski.jamolingo;

import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import com.github.starnowski.jamolingo.orderby.OdataOrderByToMongoSortParser;
import com.github.starnowski.jamolingo.orderby.OrderByOperatorResult;
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
public class OrderByOperatorTest extends AbstractItTest {

  @Inject MongoClient mongoClient;

  @ParameterizedTest
  @MethodSource("provideShouldReturnSortedDocuments")
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/edm1.json"), // plainString: "example1"
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/edm3.json"), // plainString: "Sample String"
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/edm5.json")  // plainString: "another example"
      })
  public void shouldReturnSortedDocuments(String orderByClause, String[] expectedOrder)
      throws Exception {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("testdb");
    MongoCollection<Document> collection = database.getCollection("Items");
    Edm edm = loadEmdProvider("edm/edm1.xml");

    UriInfo uriInfo =
        new Parser(edm, OData.newInstance()).parseUri("Items", "$orderby=" + orderByClause, null, null);
    OdataOrderByToMongoSortParser parser = new OdataOrderByToMongoSortParser();

    // WHEN
    OrderByOperatorResult result = parser.parse(uriInfo.getOrderByOption());
    List<Bson> pipeline = new ArrayList<>(result.getStageObjects());

    List<Document> results = new ArrayList<>();
    collection.aggregate(pipeline).into(results);

    // THEN
    Assertions.assertEquals(expectedOrder.length, results.size());
    for (int i = 0; i < expectedOrder.length; i++) {
        Assertions.assertEquals(expectedOrder[i], results.get(i).getString("plainString"));
    }
  }

  private static Stream<Arguments> provideShouldReturnSortedDocuments() {
    return Stream.of(
        Arguments.of("plainString asc", new String[]{"Sample String", "another example", "example1"}), // Default mongo sort order might be case sensitive? "Sample String" (S) < "another example" (a) < "example1" (e) ? No, 'S' is 83, 'a' is 97. So "Sample String" < "another example"
        // Wait, "another example" starts with 'a', "example1" starts with 'e', "Sample String" starts with 'S'.
        // ASCII: 'S' (83) < 'a' (97) < 'e' (101).
        // So ascending: "Sample String", "another example", "example1"
        
        Arguments.of("plainString desc", new String[]{"example1", "another example", "Sample String"})
    );
  }
}
