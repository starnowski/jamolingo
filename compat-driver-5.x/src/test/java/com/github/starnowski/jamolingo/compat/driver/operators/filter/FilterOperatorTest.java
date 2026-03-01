package com.github.starnowski.jamolingo.compat.driver.operators.filter;

import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Set;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
public class FilterOperatorTest extends AbstractFilterOperatorTest {

  private static final Set<String> ALL_PLAIN_STRINGS =
      Set.of(
          "eOMtThyhVNLWUZNRcBaQKxI",
          "Some text",
          "Poem",
          "Mario",
          "Oleksa",
          "example1",
          "example2");

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
            bsonFilePath = "bson/filter/example2_3.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_4.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_5.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_6.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_7.json")
      })
  public void shouldReturnExpectedDocuments(
      String filter, Set<String> expectedPlainStrings, String expectedIndex)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException {
    shouldReturnExpectedDocumentsBasedOnFilterOperator(filter, expectedPlainStrings);
  }

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
            bsonFilePath = "bson/filter/example2_3.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_4.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_5.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_6.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_7.json")
      })
  public void shouldUsedExpectedIndexesBasedOnFilterOperator(
      String filter, Set<String> expectedPlainStrings, String expectedIndex)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException {
    shouldUsedExpectedIndexesBasedOnFilterOperator(filter, expectedIndex);
  }

  private static Stream<Arguments> provideShouldReturnExpectedProjectedDocument() {
    return Stream.of(
        Arguments.of(
            "plainString eq 'eOMtThyhVNLWUZNRcBaQKxI'",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "tolower(plainString) eq 'eomtthyhvnlwuznrcbaqkxi'",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "COLLSCAN"),
        Arguments.of(
            "tolower(plainString) eq tolower('eOMtThyhVNLWUZNRcBaQKxI')",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "COLLSCAN"),
        Arguments.of(
            "toupper(plainString) eq 'EOMTTHYHVNLWUZNRCBAQKXI'",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "COLLSCAN"),
        Arguments.of("plainString eq 'Some text'", Set.of("Some text"), "FETCH + IXSCAN"),
        Arguments.of(
            "plainString in ('Some text', 'no such text')", Set.of("Some text"), "FETCH + IXSCAN"),
        Arguments.of("startswith(plainString,'So')", Set.of("Some text"), "FETCH + IXSCAN"),
        Arguments.of(
            "startswith(plainString,'So') and plainString eq 'Some text'",
            Set.of("Some text"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "startswith(plainString,'Some t') and smallInteger eq -1188957731",
            Set.of("Some text"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "startswith(plainString,'Po') or smallInteger eq -113",
            Set.of("Poem"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "timestamp ge 2024-07-20T10:00:00.00Z and timestamp le 2024-07-20T20:00:00.00Z",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "plainString eq 'eOMtThyhVNLWUZNRcBaQKxI' and uuidProp eq b921f1dd-3cbc-0495-fdab-8cd14d33f0aa",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "uuidProp eq b921f1dd-3cbc-0495-fdab-8cd14d33f0aa",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Poem", "Some text"),
            "FETCH + IXSCAN"),
        Arguments.of("toupper(plainString) eq 'POEM'", Set.of("Poem"), "COLLSCAN"),
        Arguments.of("tolower(plainString) eq 'poem'", Set.of("Poem"), "COLLSCAN"),
        Arguments.of("tags/any(t:t in ('developer', 'LLM'))", Set.of("Poem"), "FETCH + IXSCAN"),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and t ne 'spiderweb' or startswith(t,'spider') and t ne 'spider' or contains(t,'wide') and t ne 'word wide')",
            Set.of("Some text", "eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and t ne 'spiderweb' or endswith(t,'web') and t ne 'spiderwebgg' or contains(t,'wide') and t ne 'word wide')",
            Set.of("Some text", "eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "plainString eq 'eOMtThyhVNLWUZNRcBaQKxI' and password eq 'password1'",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "plainString eq 'eOMtThyhVNLWUZNRcBaQKxI' or password eq 'password1'",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "tags/any(t:t eq 'developer') or tags/any(t:t eq 'LLM')",
            Set.of("Poem"),
            "FETCH + IXSCAN"),
        Arguments.of("tags/any(t:startswith(t,'dev'))", Set.of("Poem"), "FETCH + IXSCAN"),
        Arguments.of(
            "tags/any(t:startswith(t,'dev') and length(t) eq 9)", Set.of("Poem"), "COLLSCAN"),
        Arguments.of("tags/any(t:length(t) eq 13)", Set.of("eOMtThyhVNLWUZNRcBaQKxI"), "COLLSCAN"),
        Arguments.of("tags/any(t:tolower(t) eq 'developer')", Set.of("Poem"), "COLLSCAN"),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and endswith(t, 'web'))",
            Set.of("Some text"),
            "COLLSCAN"),
        Arguments.of(
            "year(birthDate) eq 2024",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of(
            "month(birthDate) eq 6",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of(
            "day(birthDate) eq 18",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of(
            "hour(timestamp) eq 10",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of(
            "ceiling(floatValue) eq 1",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of(
            "tags/$count ge 2",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem", "Mario", "Oleksa"),
            "COLLSCAN"),
        Arguments.of("tags/$count ge 3", Set.of("Poem", "Oleksa"), "COLLSCAN"),
        Arguments.of("trim('   Poem   ') eq 'Poem'", ALL_PLAIN_STRINGS, "COLLSCAN"),
        Arguments.of(
            "round(floatValue) eq 1",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of(
            "minute(timestamp) eq 15",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of(
            "second(timestamp) eq 26",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of(
            "floor(floatValue) eq 0",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of("length(plainString) eq 4", Set.of("Poem"), "COLLSCAN"),
        Arguments.of(
            "nestedObject/tokens/any(t:t ne 'no such text')",
            Set.of("example1", "example2"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "tags/all(t:contains(t,'starlord') or contains(t,'trek') or contains(t,'wars'))",
            Set.of("Mario", "Oleksa", "example1", "example2"),
            "COLLSCAN"),
        Arguments.of(
            "tags/all(t:contains(t,'starlord') or contains(t,'trek') or contains(t,'wars')) and tags/any()",
            Set.of("Mario", "Oleksa"),
            "COLLSCAN"));
  }
}
