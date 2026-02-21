package com.github.starnowski.jamolingo;

import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import io.quarkus.test.junit.QuarkusTest;

import java.util.List;
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

  private static final Set<String> ALL_PLAIN_STRINGS = Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem", "Mario", "Oleksa");

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
                      bsonFilePath = "bson/filter/example2_5.json")
      })
  public void shouldReturnExpectedDocuments(String filter, Set<String> expectedPlainStrings)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException {
    shouldReturnExpectedDocumentsBasedOnFilterOperator(filter, expectedPlainStrings);
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
            "startswith(plainString,'So') and plainString eq 'Some text'", Set.of("Some text")),
        Arguments.of(
            "startswith(plainString,'Some t') and smallInteger eq -1188957731",
            Set.of("Some text")),
        Arguments.of("startswith(plainString,'Po') or smallInteger eq -113", Set.of("Poem")),
        Arguments.of(
            "timestamp ge 2024-07-20T10:00:00.00Z and timestamp le 2024-07-20T20:00:00.00Z",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI")),
        Arguments.of(
            "plainString eq 'eOMtThyhVNLWUZNRcBaQKxI' and uuidProp eq b921f1dd-3cbc-0495-fdab-8cd14d33f0aa",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI")),
        Arguments.of(
            "plainString eq 'eOMtThyhVNLWUZNRcBaQKxI' and password eq 'password1'",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI")),
        Arguments.of(
            "plainString eq 'eOMtThyhVNLWUZNRcBaQKxI' or password eq 'password1'",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI")),
        Arguments.of("tags/any(t:t eq 'developer') or tags/any(t:t eq 'LLM')", Set.of("Poem")),
        Arguments.of("tags/any(t:startswith(t,'dev'))", Set.of("Poem")),
        Arguments.of("tags/any(t:startswith(t,'dev') and length(t) eq 9)", Set.of("Poem")),
        Arguments.of("tags/any(t:length(t) eq 13)", Set.of("eOMtThyhVNLWUZNRcBaQKxI")),
        Arguments.of("tags/any(t:tolower(t) eq 'developer')", Set.of("Poem")),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and endswith(t, 'web'))", Set.of("Some text")),
        Arguments.of(
            "year(birthDate) eq 2024", Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem")),
        Arguments.of(
            "month(birthDate) eq 6", Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem")),
        Arguments.of(
            "day(birthDate) eq 18", Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem")),
        Arguments.of(
            "hour(timestamp) eq 10", Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem")),
        Arguments.of(
            "ceiling(floatValue) eq 1", Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem")),
        Arguments.of("tags/$count ge 2", Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem")),
        Arguments.of("tags/$count ge 3", Set.of("Poem")),
        Arguments.of(
            "trim('   Poem   ') eq 'Poem'", ALL_PLAIN_STRINGS),
        Arguments.of(
            "round(floatValue) eq 1", Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem")),
        Arguments.of(
            "minute(timestamp) eq 15", Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem")),
        Arguments.of(
            "second(timestamp) eq 26", Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem")),
        Arguments.of("floor(floatValue) eq 0", Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem")),
        Arguments.of("length(plainString) eq 4", Set.of("Poem")),
            Arguments.of("nestedObject/tokens/any(t:t ne 'no such text')", ALL_PLAIN_STRINGS),
            Arguments.of(
                    "tags/all(t:contains(t,'starlord') or contains(t,'trek') or contains(t,'wars'))",
                    Set.of(
                            "Mario", "Oleksa"))
    );
  }
}
