package com.github.starnowski.jamolingo.compat.driver.operators.filter;

import com.github.starnowski.jamolingo.AbstractFilterOperatorTest;
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
    Set<String> allExamplesInResponse =
        Set.of(
            "eOMtThyhVNLWUZNRcBaQKxI",
            "Some text",
            "Poem",
            "Mario",
            "Oleksa",
            "example1",
            "example2",
            "only_id_and_plainString");
    return Stream.of(
        // Basic ALL tests
        Arguments.of(
            "numericArray/all(n:n gt 5)",
            Set.of(
                "eOMtThyhVNLWUZNRcBaQKxI",
                "Mario",
                "Oleksa",
                "example1",
                "example2",
                "only_id_and_plainString")),
        Arguments.of(
            "numericArray/all(n:n ge 1)",
            Set.of(
                "eOMtThyhVNLWUZNRcBaQKxI",
                "Some text",
                "Poem",
                "Mario",
                "Oleksa",
                "example1",
                "example2",
                "only_id_and_plainString")),
        // Tests from GitHub example - Tags
        Arguments.of(
            List.of("tags/all(t:t ne 'no such text' and t ne 'no such word')"),
            allExamplesInResponse),
        Arguments.of(
            List.of("tags/all(t:startswith(t,'star') and t ne 'starlord')"),
            Set.of(
                "Mario",
                "example1",
                "example2",
                "only_id_and_plainString")), // Mario has trek/wars, Oleksa has trek/wars/starlord
        // (starlord fails ne)
        Arguments.of(
            List.of("tags/all(t:startswith(t,'star') or t ne 'starlord')"), allExamplesInResponse),
        Arguments.of(
            List.of("tags/all(t:startswith(t,'star ') or t eq 'starlord')"),
            Set.of("Mario", "Oleksa", "example1", "example2", "only_id_and_plainString")),
        Arguments.of(
            List.of("tags/all(t:startswith(t,'starlord') or t in ('star trek', 'star wars'))"),
            Set.of("Mario", "Oleksa", "example1", "example2", "only_id_and_plainString")),
        Arguments.of(
            List.of("tags/all(t:contains(t,'starlord'))"),
            Set.of("example1", "example2", "only_id_and_plainString")),
        Arguments.of(
            List.of("tags/all(t:endswith(t,'web') or endswith(t,'trap'))"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "example1", "example2", "only_id_and_plainString")),
        Arguments.of(
            List.of("tags/all(t:length(t) eq 9)"),
            Set.of("Mario", "example1", "example2", "only_id_and_plainString")),
        Arguments.of(
            List.of("tags/all(t:contains(tolower(t),'star'))"),
            Set.of("Mario", "Oleksa", "example1", "example2", "only_id_and_plainString")),
        Arguments.of(
            List.of("tags/all(t:contains(tolower(t),tolower('star')))"),
            Set.of("Mario", "Oleksa", "example1", "example2", "only_id_and_plainString")),
        Arguments.of(
            List.of("tags/all(t:startswith(toupper(t),toupper('star')))"),
            Set.of("Mario", "Oleksa", "example1", "example2", "only_id_and_plainString")),
        Arguments.of(List.of("tags/all(t:endswith(tolower(t),tolower(t)))"), allExamplesInResponse),
        Arguments.of(
            List.of("tags/all(t:contains(toupper(t),'STAR'))"),
            Set.of("Mario", "Oleksa", "example1", "example2", "only_id_and_plainString")),
        // Numeric array ALL tests
        Arguments.of(
            List.of("numericArray/all(n:n gt floor(5.05))"),
            Set.of(
                "eOMtThyhVNLWUZNRcBaQKxI",
                "Mario",
                "Oleksa",
                "example1",
                "example2",
                "only_id_and_plainString")),
        Arguments.of(List.of("numericArray/all(n:n add 2 gt round(n))"), allExamplesInResponse),
        Arguments.of(
            List.of("numericArray/all(n:n eq 10 or n eq 20 or n eq 30)"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "example1", "example2", "only_id_and_plainString")));
  }

  private static Stream<Arguments> provideShouldReturnExpectedProjectedDocumentForComplexList() {
    return Stream.of(
        // Complex list tests
        Arguments.of(
            List.of("complexList/all(c:startswith(c/someString,'Ap'))"),
            Set.of("Doc1", "Doc5", "only_id_and_plainString")),
        Arguments.of(
            List.of("complexList/all(c:contains(c/someString,'ana'))"),
            Set.of("Doc2", "only_id_and_plainString")),
        Arguments.of(
            List.of("complexList/all(c:endswith(c/someString,'erry'))"),
            Set.of("Doc3", "only_id_and_plainString")),
        Arguments.of(
            List.of("complexList/all(c:contains(c/someString,'e'))"),
            Set.of("Doc3", "Doc4", "Doc6", "only_id_and_plainString")),
        Arguments.of(
            List.of("complexList/all(c:c/someString eq 'Application')"),
            Set.of("Doc5", "only_id_and_plainString")),
        // Complex numeric tests
        Arguments.of(
            "complexList/all(c:c/someNumber gt 5)",
            Set.of("Doc1", "Doc2", "Doc3", "Doc4", "Doc5", "Doc6", "only_id_and_plainString")),
        Arguments.of(
            "complexList/all(c:c/someNumber gt 25)",
            Set.of("Doc2", "Doc3", "Doc6", "only_id_and_plainString")),
        Arguments.of(
            "complexList/all(c:c/someNumber lt 25)",
            Set.of("Doc1", "Doc5", "only_id_and_plainString")),
        Arguments.of(
            "complexList/all(c:c/someNumber eq 10 or c/someNumber eq 20)",
            Set.of("Doc1", "Doc5", "only_id_and_plainString")),
        Arguments.of(
            "complexList/all(c:c/someNumber add 5 gt 20)",
            Set.of("Doc2", "Doc3", "Doc5", "Doc6", "only_id_and_plainString")),
        Arguments.of(
            "complexList/all(c:c/someNumber gt floor(5.05))",
            Set.of("Doc1", "Doc2", "Doc3", "Doc4", "Doc5", "Doc6", "only_id_and_plainString")),
        Arguments.of(
            "complexList/all(c:c/someNumber add 2 gt round(c/someNumber))",
            Set.of("Doc1", "Doc2", "Doc3", "Doc4", "Doc5", "Doc6", "only_id_and_plainString")),
        Arguments.of(
            "complexList/all(c:c/someNumber eq 20)", Set.of("Doc5", "only_id_and_plainString")),
        // Nested complex array tests
        Arguments.of(
            List.of("complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'val1'))"),
            Set.of("Doc2", "only_id_and_plainString")),
        Arguments.of(
            List.of("complexList/all(c:c/nestedComplexArray/all(n:startswith(n/stringVal,'val')))"),
            Set.of("Doc1", "Doc2", "only_id_and_plainString")),
        Arguments.of(
            List.of("complexList/all(c:c/nestedComplexArray/all(n:contains(n/stringVal,'match')))"),
            Set.of("Doc5", "Doc6", "only_id_and_plainString")),
        Arguments.of(
            List.of(
                "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'val1' or n/stringVal eq 'test1'))"),
            Set.of("Doc2", "Doc4", "only_id_and_plainString")),
        Arguments.of(
            List.of(
                "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'val1' or n/stringVal eq 'test1') and c/someNumber ge 20)"),
            Set.of("Doc2", "only_id_and_plainString")),
        Arguments.of(
            List.of(
                "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'val1' or n/stringVal eq 'test1')) and complexList/any()"),
            Set.of("Doc2", "Doc4")),
        Arguments.of(
            List.of(
                "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'val1') and c/nestedComplexArray/any()) and complexList/any(c:c/nestedComplexArray/any())"),
            Set.of("Doc2")),
        Arguments.of(
            List.of("complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'matchAll'))"),
            Set.of("Doc6", "only_id_and_plainString")),
        Arguments.of(
            List.of("complexList/all(c:c/nestedComplexArray/all(n:n/numberVal gt 70))"),
            Set.of("Doc6", "only_id_and_plainString")),
        Arguments.of(
            List.of(
                "complexList/all(c:c/nestedComplexArray/any(n:n/numberVal eq 71) and c/nestedComplexArray/any(n:n/numberVal eq 72))"),
            Set.of("Doc6", "only_id_and_plainString")),
        Arguments.of(
            List.of("complexList/all(c:c/nestedComplexArray/$count ge 2)"),
            Set.of("Doc6", "only_id_and_plainString")),
        Arguments.of(
            List.of(
                "complexList/all(c:c/nestedComplexArray/$count ge 2)  and complexList/any(c:c/nestedComplexArray/any())"),
            Set.of("Doc6")),
        Arguments.of(
            List.of(
                "complexList/all(c:c/nestedComplexArray/any(n:n/numberVal ge 70 and n/stringVal eq 'matchAll')) and complexList/any(c:c/nestedComplexArray/any())"),
            Set.of("Doc6")),
        Arguments.of(
            List.of("complexList/all(c:c/primitiveStringList/all(n:startswith(n,'item1')))"),
            Set.of("Doc6", "only_id_and_plainString")),
        Arguments.of(
            List.of(
                "complexList/all(c:c/primitiveStringList/all(n:startswith(n,'item1'))) and complexList/any(c:c/primitiveStringList/any())"),
            Set.of("Doc6")),
        Arguments.of(
            List.of("complexList/all(c:c/primitiveNumberList/all(n:n gt 10))"),
            Set.of("Doc6", "only_id_and_plainString")),
        Arguments.of(
            List.of(
                "complexList/all(c:c/primitiveNumberList/all(n:n gt 10)) and complexList/any(c:c/primitiveStringList/any())"),
            Set.of("Doc6")),
        Arguments.of(
            List.of(
                "complexList/all(c:c/nestedComplexArray/all(n:n/numberVal eq c/someNumber))  and complexList/any(c:c/nestedComplexArray/any())"),
            Set.of("Doc1", "Doc2", "Doc3", "Doc4", "Doc5")),
        Arguments.of(
            List.of(
                "complexList/all(c:c/nestedComplexArray/all(n:c/someNumber eq n/numberVal))  and complexList/any(c:c/nestedComplexArray/any())"),
            Set.of("Doc1", "Doc2", "Doc3", "Doc4", "Doc5")),
        Arguments.of(
            List.of(
                "complexList/all(c:not c/nestedComplexArray/all(n:c/someNumber eq n/numberVal))  and complexList/any(c:c/nestedComplexArray/any())"),
            Set.of("Doc6")));
  }
}
