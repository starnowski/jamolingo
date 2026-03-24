package com.github.starnowski.jamolingo.compat.driver.operators.filter;

import static com.github.starnowski.jamolingo.compat.driver.operators.filter.FilterTestsCasesAggregator.provideShouldReturnExpectedProjectedDocumentForAllLambda;
import static com.github.starnowski.jamolingo.compat.driver.operators.filter.FilterTestsCasesAggregator.provideShouldReturnExpectedProjectedDocumentForComplexListForAllLambda;

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
  public void shouldReturnExpectedDocuments(
      Object filter, Set<String> expectedPlainStrings, String expectedIndex)
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
  public void shouldReturnExpectedDocumentsForQueryObject(
      Object filter, Set<String> expectedPlainStrings, String expectedIndex)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException {
    String filterString =
        filter instanceof String ? (String) filter : String.join(" and ", (List<String>) filter);
    shouldReturnExpectedDocumentsBasedOnQueryObjectForFilterOperator(
        filterString, expectedPlainStrings);
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
            bsonFilePath = "bson/filter/example2_7.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/filter/example2_only_id.json")
      })
  public void shouldUsedExpectedIndexesBasedOnFilterOperator(
      Object filter, Set<String> expectedPlainStrings, String expectedIndex)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException {
    String filterString =
        filter instanceof String ? (String) filter : String.join(" and ", (List<String>) filter);
    shouldUsedExpectedIndexesBasedOnFilterOperator(filterString, expectedIndex);
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
      Object filter, Set<String> expectedPlainStrings, String expectedIndex)
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
  public void shouldUsedExpectedIndexesBasedOnFilterOperatorForComplexList(
      Object filter, Set<String> expectedPlainStrings, String expectedIndex)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException {
    String filterString =
        filter instanceof String ? (String) filter : String.join(" and ", (List<String>) filter);
    shouldUsedExpectedIndexesBasedOnFilterOperator(filterString, expectedIndex);
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
  public void shouldReturnExpectedDocumentsForComplexListForQueryObject(
      Object filter, Set<String> expectedPlainStrings, String expectedIndex)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException {
    String filterString =
        filter instanceof String ? (String) filter : String.join(" and ", (List<String>) filter);
    shouldReturnExpectedDocumentsBasedOnQueryObjectForFilterOperator(
        filterString, expectedPlainStrings);
  }

  private static Stream<Arguments> provideShouldReturnExpectedProjectedDocument() {
    return provideShouldReturnExpectedProjectedDocumentForAllLambda();
  }

  private static Stream<Arguments> provideShouldReturnExpectedProjectedDocumentForComplexList() {
    return provideShouldReturnExpectedProjectedDocumentForComplexListForAllLambda();
  }
}
