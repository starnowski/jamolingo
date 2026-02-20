package com.github.starnowski.jamolingo;

import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Arrays;
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
public class AllLambdaFilterOperatorTest extends AbstractFilterOperatorTest {

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
  public void shouldReturnExpectedDocuments(Object filter, Set<String> expectedPlainStrings)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException {
    String filterString =
        filter instanceof String ? (String) filter : String.join(" and ", (List<String>) filter);
    shouldReturnExpectedDocumentsBasedOnFilterOperator(filterString, expectedPlainStrings);
  }

  private static Stream<Arguments> provideShouldReturnExpectedProjectedDocument() {
    return Stream.of(
        Arguments.of("numericArray/all(n:n gt 5)", Set.of("eOMtThyhVNLWUZNRcBaQKxI")),
        Arguments.of(
            "numericArray/all(n:n ge 1)", Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem")),
        Arguments.of(
            "tags/all(t:length(t) gt 2)", Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem")),
        Arguments.of("tags/all(t:length(t) gt 3)", Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text")),
        Arguments.of(
            Arrays.asList("numericArray/all(n:n gt 5)", "tags/all(t:length(t) gt 10)"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI")),
        Arguments.of(
            Arrays.asList("tags/all(t:length(t) gt 3)", "plainString eq 'Some text'"),
            Set.of("Some text")));
  }
}
