package com.github.starnowski.jamolingo.compat.driver.operators.filter;

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
public class FilterOperatorAnyLambdaTest extends AbstractFilterOperatorTest {

  private static final Set<String> ALL_PLAIN_STRING_VALUE =
      Set.of(
          "eOMtThyhVNLWUZNRcBaQKxI",
          "Some text",
          "Poem",
          "Mario",
          "Oleksa",
          "example1",
          "example2",
          "only_id_and_plainString");

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
            bsonFilePath = "bson/filter/example2_7.json"),
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
    return Stream.of(
        Arguments.of("tags/any(t:t eq 'word wide web')", Set.of("eOMtThyhVNLWUZNRcBaQKxI")),
        Arguments.of("tags/any(t:startswith(t,'star'))", Set.of("Mario", "Oleksa")),
        Arguments.of(
            "tags/any(t:contains(t,'spider'))", Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text")),
        Arguments.of(
            "numericArray/any(n:n gt 25)", Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Mario", "Oleksa")),
        Arguments.of("numericArray/any(n:n lt 10)", Set.of("Some text", "Poem")),
        Arguments.of("tags/any(t:contains(tolower(t),'star'))", Set.of("Mario", "Oleksa")),
        Arguments.of("tags/any(t:endswith(toupper(t),'TRAP'))", Set.of("eOMtThyhVNLWUZNRcBaQKxI")),
        Arguments.of("tags/any(t:length(t) eq 8)", Set.of("Some text", "Poem", "Oleksa")),
        Arguments.of("numericArray/any(n:n add 2 gt 100)", Set.of("Mario")),
        Arguments.of(
            List.of("tags/any(t:t ne 'no such text' and t ne 'no such word')"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem", "Mario", "Oleksa")),
        Arguments.of(
            List.of("tags/any(t:startswith(t,'star') and t ne 'starlord')"),
            Set.of("Mario", "Oleksa")),
        Arguments.of(
            List.of("tags/any(t:startswith(t,'star') or t ne 'starlord')"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem", "Mario", "Oleksa")),
        Arguments.of(
            List.of("tags/any(t:startswith(t,'star ') or t eq 'starlord')"),
            Set.of("Mario", "Oleksa")),
        Arguments.of(
            List.of("tags/any(t:startswith(t,'starlord') or t in ('star trek', 'star wars'))"),
            Set.of("Mario", "Oleksa")),
        Arguments.of(
            List.of(
                "tags/any(t:contains(t,'starlord') or contains(t,'trek') or contains(t,'wars'))"),
            Set.of("Mario", "Oleksa")),
        Arguments.of(List.of("tags/any(t:contains(t,'starlord'))"), Set.of("Oleksa")),
        Arguments.of(
            List.of("tags/any(t:endswith(t,'web') or endswith(t,'trap'))"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text")),
        Arguments.of(
            List.of("tags/any(t:length(t) eq 9)"), Set.of("Some text", "Poem", "Mario", "Oleksa")),
        Arguments.of(
            List.of("numericArray/any(n:n gt 5)"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Mario", "Oleksa")),
        Arguments.of(
            List.of("numericArray/any(n:n gt floor(5.05))"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Mario", "Oleksa")),
        Arguments.of(
            List.of("numericArray/any(n:n add 2 gt round(n))"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem", "Mario", "Oleksa")),
        Arguments.of(
            List.of("numericArray/any(n:n eq 10 or n eq 20 or n eq 30)"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI")),
        // Additional tests from user list
        Arguments.of(
            "nestedObject/tokens/any(t:t eq 'first example') and nestedObject/numbers/any(t:t gt 5 and t lt 27)",
            Set.of("example1")),
        Arguments.of(
            "nestedObject/tokens/any(t:t ne 'no such text')", Set.of("example1", "example2")),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and t eq 'spiderweb')", Set.of("Some text")),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and t ne 'spiderweb' or endswith(t,'web') and t ne 'spiderweb' or contains(t,'wide') and t ne 'word wide')",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI")),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and t ne 'spiderweb' or endswith(t,'web') and t ne 'spiderweb')",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI")),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and t ne 'spiderweb' or endswith(t,'web') and t ne 'spiderwebgg' or contains(t,'wide') and t ne 'word wide')",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text")),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and t ne 'spiderweb' or startswith(t,'spider') and t ne 'spider' or contains(t,'wide') and t ne 'word wide')",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text")),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and t ne 'spiderweb')",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI")));
  }

  private static Stream<Arguments> provideShouldReturnExpectedProjectedDocumentForComplexList() {
    return Stream.of(
        Arguments.of("complexList/any(c:c/someString eq 'Apple')", Set.of("Doc1", "Doc4")),
        Arguments.of(
            "complexList/any(c:c/someNumber gt 35)", Set.of("Doc2", "Doc3", "Doc4", "Doc6")),
        Arguments.of(
            "complexList/any(c:c/someString eq 'Banana' or c/someString eq 'Cherry')",
            Set.of("Doc2", "Doc3")),
        Arguments.of(
            "complexList/any(c:c/nestedComplexArray/any(n:n/stringVal eq 'val1'))",
            Set.of("Doc1", "Doc2", "Doc4")),
        Arguments.of(
            "complexList/any(c:startswith(c/someString,'Ap'))", Set.of("Doc1", "Doc4", "Doc5")),
        Arguments.of("complexList/any(c:contains(c/someString,'ana'))", Set.of("Doc2")),
        Arguments.of("complexList/any(c:endswith(c/someString,'erry'))", Set.of("Doc3", "Doc4")),
        Arguments.of(
            "complexList/any(c:contains(c/someString,'e'))",
            Set.of("Doc1", "Doc3", "Doc4", "Doc6")),
        Arguments.of("complexList/any(c:c/someString eq 'Application')", Set.of("Doc1", "Doc5")),
        // Missing complex numeric tests
        Arguments.of(
            "complexList/any(c:c/someNumber gt 5)",
            Set.of("Doc1", "Doc2", "Doc3", "Doc4", "Doc5", "Doc6")),
        Arguments.of(
            "complexList/any(c:c/someNumber gt 25)", Set.of("Doc2", "Doc3", "Doc4", "Doc6")),
        Arguments.of("complexList/any(c:c/someNumber lt 25)", Set.of("Doc1", "Doc4", "Doc5")),
        Arguments.of(
            "complexList/any(c:c/someNumber eq 10 or c/someNumber eq 20)",
            Set.of("Doc1", "Doc4", "Doc5")),
        Arguments.of(
            "complexList/any(c:c/someNumber add 5 gt 20)",
            Set.of("Doc1", "Doc2", "Doc3", "Doc4", "Doc5", "Doc6")),
        Arguments.of(
            "complexList/any(c:c/someNumber gt floor(5.05))",
            Set.of("Doc1", "Doc2", "Doc3", "Doc4", "Doc5", "Doc6")),
        Arguments.of(
            "complexList/any(c:c/someNumber add 2 gt round(c/someNumber))",
            Set.of("Doc1", "Doc2", "Doc3", "Doc4", "Doc5", "Doc6")),
        Arguments.of("complexList/any(c:c/someNumber eq 20)", Set.of("Doc1", "Doc5")),
        // Missing nested complex tests
        Arguments.of(
            "complexList/any(c:c/nestedComplexArray/any(n:startswith(n/stringVal,'val')))",
            Set.of("Doc1", "Doc2", "Doc4")),
        Arguments.of(
            "complexList/any(c:c/nestedComplexArray/any(n:contains(n/stringVal,'match')))",
            Set.of("Doc5", "Doc6")),
        Arguments.of(
            "complexList/any(c:c/nestedComplexArray/any(n:n/stringVal eq 'val1' or n/stringVal eq 'test1'))",
            Set.of("Doc1", "Doc2", "Doc3", "Doc4")),
        Arguments.of(
            "complexList/any(c:c/nestedComplexArray/any(n:n/stringVal eq 'val1' or n/stringVal eq 'test1') and c/someNumber ge 20)",
            Set.of("Doc2", "Doc3", "Doc4")),
        Arguments.of(
            "complexList/any(c:c/nestedComplexArray/any(n:n/numberVal gt 70))", Set.of("Doc6")),
        Arguments.of(
            "complexList/any(c:c/nestedComplexArray/any(n:n/numberVal eq 71) and c/nestedComplexArray/any(n:n/numberVal eq 72))",
            Set.of("Doc6")),
        Arguments.of("complexList/any(c:c/nestedComplexArray/$count ge 2)", Set.of("Doc6")),
        Arguments.of(
            "complexList/any(c:c/primitiveStringList/any(n:startswith(n,'item11')))",
            Set.of("Doc6")),
        Arguments.of("complexList/any(c:c/primitiveNumberList/any(n:n gt 10))", Set.of("Doc6")),
        // Concat test
        Arguments.of(
            Arrays.asList("complexList/any(c:c/someNumber gt 5)", "plainString eq 'Doc1'"),
            Set.of("Doc1")));
  }
}
