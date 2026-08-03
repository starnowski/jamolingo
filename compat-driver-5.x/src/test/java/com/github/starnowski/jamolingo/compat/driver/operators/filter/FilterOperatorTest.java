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
  public void shouldReturnExpectedDocumentsForQueryObject(
      String filter, Set<String> expectedPlainStrings, String expectedIndex)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException {
    shouldReturnExpectedDocumentsBasedOnQueryObjectForFilterOperator(filter, expectedPlainStrings);
  }

  private static Stream<Arguments> provideShouldReturnExpectedProjectedDocument() {
    return FilterTestsCasesAggregator
        .provideShouldReturnExpectedProjectedDocumentForBasicFiltering();
  }

  /**
   * String comparison is case-sensitive, case-insensitive comparison can be achieved in combination
   * with tolower or toupper.
   * https://docs.oasis-open.org/odata/odata/v4.01/os/part2-url-conventions/odata-v4.01-os-part2-url-conventions.html#sec_contains
   * https://docs.oasis-open.org/odata/odata/v4.01/os/part2-url-conventions/odata-v4.01-os-part2-url-conventions.html#sec_endswith
   * https://docs.oasis-open.org/odata/odata/v4.01/os/part2-url-conventions/odata-v4.01-os-part2-url-conventions.html#sec_startswith
   *
   * @param filter
   * @param expectedPlainStrings
   * @throws UriValidationException
   * @throws UriParserException
   * @throws XMLStreamException
   * @throws ExpressionVisitException
   * @throws ODataApplicationException
   */
  @ParameterizedTest
  @MethodSource("provideCaseSensitivityTests")
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/case_sensitivity/case_sensitivity_1.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/case_sensitivity/case_sensitivity_2.json")
      })
  public void shouldCorrectCheckCaseSensitivityForStringFunctions(
      String filter, Set<String> expectedPlainStrings)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException {
    shouldReturnExpectedDocumentsBasedOnFilterOperator(filter, expectedPlainStrings);
  }

  private static Stream<Arguments> provideCaseSensitivityTests() {
    return Stream.of(
        // contains

        Arguments.of("contains(password, 'Secret')", Set.of("doc1")),
        Arguments.of("contains(password, 'secret')", Set.of("doc2")),
        Arguments.of("contains(password, 'WORD')", Set.of()),
        // startswith
        Arguments.of("startswith(password, 'My')", Set.of("doc1")),
        Arguments.of("startswith(password, 'my')", Set.of("doc2")),
        // endswith
        Arguments.of("endswith(password, '123')", Set.of("doc1", "doc2")),
        Arguments.of("endswith(password, 'Secret123')", Set.of("doc1")),
        Arguments.of("endswith(password, 'secret123')", Set.of("doc2")));
  }
}
