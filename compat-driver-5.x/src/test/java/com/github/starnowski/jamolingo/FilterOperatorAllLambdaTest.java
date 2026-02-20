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
public class FilterOperatorAllLambdaTest extends AbstractFilterOperatorTest {

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
            bsonFilePath = "bson/filter/example2_only_id.json")
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

  @ParameterizedTest
  @MethodSource("provideShouldReturnExpectedProjectedDocumentForComplexList")
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_complex_1.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_complex_2.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_complex_3.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_complex_4.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_complex_5.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_complex_6.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_only_id.json")
      })
  public void shouldReturnExpectedDocumentsForComplexList(
      Object filter, Set<String> expectedPlainStrings)
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
    Set<String> allExamplesInResponse =
        Set.of(
            "eOMtThyhVNLWUZNRcBaQKxI",
            "Some text",
            "Poem",
            "Mario",
            "Oleksa",
            "only_id_and_plainString");
    return Stream.of(
        // Basic ALL tests
        Arguments.of(
            "numericArray/all(n:n gt 5)",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Mario", "Oleksa", "only_id_and_plainString")),
        Arguments.of(
            "numericArray/all(n:n ge 1)",
            Set.of(
                "eOMtThyhVNLWUZNRcBaQKxI",
                "Some text",
                "Poem",
                "Mario",
                "Oleksa",
                "only_id_and_plainString")),
        // Tests from GitHub example - Tags
        Arguments.of(
            List.of("tags/all(t:t ne 'no such text' and t ne 'no such word')"),
            allExamplesInResponse),
        Arguments.of(
            List.of("tags/all(t:startswith(t,'star') and t ne 'starlord')"),
            Set.of("Mario", "only_id_and_plainString")),
        Arguments.of(
            List.of("tags/all(t:startswith(t,'star') or t ne 'starlord')"), allExamplesInResponse),
        Arguments.of(
            List.of("tags/all(t:startswith(t,'star ') or t eq 'starlord')"),
            Set.of("Mario", "Oleksa", "only_id_and_plainString")),
        Arguments.of(
            List.of("tags/all(t:startswith(t,'starlord') or t in ('star trek', 'star wars'))"),
            Set.of("Mario", "Oleksa", "only_id_and_plainString")),
        Arguments.of(
            List.of("tags/all(t:contains(t,'starlord'))"), Set.of("only_id_and_plainString")),
        Arguments.of(
            List.of("tags/all(t:length(t) eq 9)"), Set.of("Mario", "only_id_and_plainString")),
        Arguments.of(
            List.of("tags/all(t:contains(tolower(t),'star'))"),
            Set.of("Mario", "Oleksa", "only_id_and_plainString")),
        // Concat test
        Arguments.of(
            Arrays.asList("numericArray/all(n:n gt 5)", "tags/all(t:length(t) gt 1)"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Mario", "Oleksa", "only_id_and_plainString")));
  }

  private static Stream<Arguments> provideShouldReturnExpectedProjectedDocumentForComplexList() {
    return Stream.of(
        // Complex list tests
        Arguments.of(
            List.of("complexList/all(c:startswith(c/someString,'Ap'))"),
            Set.of("Doc1", "Doc5", "only_id_and_plainString")),
        Arguments.of(
            List.of("complexList/all(c:c/someString eq 'Application')"),
            Set.of("Doc5", "only_id_and_plainString")),
        // Nested complex array tests
        Arguments.of(
            List.of("complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'val1'))"),
            Set.of("Doc2", "only_id_and_plainString")),
        Arguments.of(
            List.of("complexList/all(c:c/nestedComplexArray/all(n:n/numberVal gt 70))"),
            Set.of("Doc6", "only_id_and_plainString")),
        Arguments.of(
            List.of("complexList/all(c:c/nestedComplexArray/$count ge 2)"),
            Set.of("Doc6", "only_id_and_plainString")));
  }
}
