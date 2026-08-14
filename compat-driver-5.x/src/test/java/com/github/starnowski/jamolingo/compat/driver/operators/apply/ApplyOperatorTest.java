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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
      String applyQuery, Set<String> expectedPlainStrings) throws Exception {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("testdb");
    MongoCollection<Document> collection = database.getCollection("Items");
    Edm edm = loadEmdProvider("edm/edm6_filter_main.xml");

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
    collection.aggregate(pipeline).into(results);

    // THEN
    Assertions.assertEquals(expectedPlainStrings.size(), results.size());
    Set<String> actual =
        results.stream()
            .map(
                d -> {
                  if (d.containsKey("plainString")) {
                    return d.getString("plainString");
                  }
                  if (d.containsKey("_id") && d.get("_id") instanceof Document) {
                    Document idDoc = (Document) d.get("_id");
                    if (idDoc.containsKey("plainString")) {
                      return idDoc.getString("plainString");
                    }
                  }
                  return null;
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    Assertions.assertEquals(expectedPlainStrings, actual);
  }

  private static Stream<Arguments> provideApplyTestCases() {
    return Stream.of(
        Arguments.of("filter(plainString eq 'eOMtThyhVNLWUZNRcBaQKxI')", Set.of("eOMtThyhVNLWUZNRcBaQKxI")),
        Arguments.of("filter(plainString eq 'Some text')", Set.of("Some text")),
        Arguments.of("identity", Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Mario", "Poem")),
        Arguments.of("groupby((plainString))", Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Mario", "Poem")),
        Arguments.of("filter(plainString eq 'Mario')/groupby((plainString))", Set.of("Mario"))
    );
  }
}
