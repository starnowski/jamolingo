package com.github.starnowski.jamolingo;

import com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContextBuilder;
import com.github.starnowski.jamolingo.core.mapping.ODataMongoMappingFactory;
import com.github.starnowski.jamolingo.core.operators.filter.FilterOperatorResult;
import com.github.starnowski.jamolingo.core.operators.filter.ODataFilterToMongoMatchParser;
import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@QuarkusTestResource(EmbeddedMongoResource.class)
class FilterOperatorTest extends AbstractItTest {

  @Inject MongoClient mongoClient;

  @ParameterizedTest
  @MethodSource("provideShouldReturnExpectedProjectedDocument")
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
            bsonFilePath = "bson/filter/example2_3.json")
      })
  public void shouldReturnExpectedDocuments(String filter, Set<String> expectedPlainStrings)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          IOException,
          JSONException,
          ExpressionVisitException,
          ODataApplicationException {
    // plainString
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("testdb");
    MongoCollection<Document> collection = database.getCollection("Items");

    Edm edm = loadEmdProvider("edm/edm6_filter_main.xml");
    ODataMongoMappingFactory factory = new ODataMongoMappingFactory();
    var odataMapping = factory.build(edm.getSchema("MyService"));
    var entityMapping = odataMapping.getEntities().get("Example2");
    EntityPropertiesMongoPathContextBuilder entityPropertiesMongoPathContextBuilder =
        new EntityPropertiesMongoPathContextBuilder();
    var context = entityPropertiesMongoPathContextBuilder.build(entityMapping);

    UriInfo uriInfo =
        new Parser(edm, OData.newInstance()).parseUri("examples2", "$filter=" + filter, null, null);
    ODataFilterToMongoMatchParser tested = new ODataFilterToMongoMatchParser();

    // WHEN
    FilterOperatorResult result = tested.parse(uriInfo.getFilterOption(), edm);
    List<Bson> pipeline = new ArrayList<>(result.getStageObjects());

    List<Document> results = new ArrayList<>();
    collection.aggregate(pipeline).into(results);

    // THEN
    Assertions.assertEquals(expectedPlainStrings.size(), results.size());
    Set<String> actual =
        results.stream()
            .map(d -> d.get("plainString"))
            .filter(Objects::nonNull)
            .map(s -> (String) s)
            .collect(Collectors.toSet());
    Assertions.assertEquals(expectedPlainStrings, actual);
  }

  private static Stream<Arguments> provideShouldReturnExpectedProjectedDocument() {
    return Stream.of(
        Arguments.of("plainString eq 'eOMtThyhVNLWUZNRcBaQKxI'", Set.of("eOMtThyhVNLWUZNRcBaQKxI")),
        Arguments.of(
            "tolower(plainString) eq 'eomtthyhvnlwuznrcbaqkxi'", Set.of("eOMtThyhVNLWUZNRcBaQKxI")),
        Arguments.of(
            "tolower(plainString) eq tolower('eOMtThyhVNLWUZNRcBaQKxI')",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI")),
        Arguments.of(
            "toupper(plainString) eq 'EOMTTHYHVNLWUZNRCBAQKXI'", Set.of("eOMtThyhVNLWUZNRcBaQKxI")),
        Arguments.of("plainString eq 'Some text'", Set.of("Some text")),
        Arguments.of("plainString in ('Some text', 'no such text')", Set.of("Some text")),
        Arguments.of("startswith(plainString,'So')", Set.of("Some text")),
        Arguments.of(
            "startswith(plainString,'So') and plainString eq 'Some text'", Set.of("Some text")));
  }
}
