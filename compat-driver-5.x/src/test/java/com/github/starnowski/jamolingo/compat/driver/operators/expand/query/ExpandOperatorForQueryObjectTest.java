package com.github.starnowski.jamolingo.compat.driver.operators.expand.query;

import static com.github.starnowski.jamolingo.compat.driver.operators.filter.FilterTestsCasesAggregator.provideShouldReturnExpectedProjectedDocumentForComplexListForAllLambda;

import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.compat.driver.operators.filter.FilterTestsCasesAggregator;
import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
public class ExpandOperatorForQueryObjectTest extends AbstractExpandOperatorForQueryObjectTest {

  private static final Set<String> ALL_PLAIN_STRINGS =
      Set.of(
          "eOMtThyhVNLWUZNRcBaQKxI",
          "Some text",
          "Poem",
          "Mario",
          "Oleksa",
          "example1",
          "example2");
  public static final Map<@NotNull KeyValue<String, String>, @NotNull KeyValue<String, String>>
      EDM_TABLES_TO_MONGO_DB_COLLECTIONS =
          Map.of(
              new KeyValue<>("MyService", "Category"),
              new KeyValue<>("testdb", "categories"),
              new KeyValue<>("MyService", "Example2"),
              new KeyValue<>("testdb", "examples"));
  public static final int ROOT_DOCUMENT_ID = 100;

  private static Stream<Arguments> provideShouldReturnExpectedProjectedDocument() {
    return FilterTestsCasesAggregator
        .provideShouldReturnExpectedProjectedDocumentForBasicFiltering();
  }

  public static Stream<Arguments> provideShouldReturnExpectedProjectedDocumentForAllLambda() {
    return FilterTestsCasesAggregator.provideShouldReturnExpectedProjectedDocumentForAllLambda();
  }

  private static Stream<Arguments> provideShouldReturnExpectedProjectedDocumentForComplexList() {
    return provideShouldReturnExpectedProjectedDocumentForComplexListForAllLambda();
  }

  @ParameterizedTest
  @MethodSource("provideShouldReturnExpectedProjectedDocument")
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = "testdb",
            collection = "categories",
            bsonFilePath = "bson/expand/query/category_1.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_1.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_2.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_3.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_4.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_5.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_6.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_7.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_root.json")
      })
  public void shouldReturnExpectedDocumentsForQueryObject(
      String filter, Set<String> expectedPlainStrings)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException {
    shouldReturnExpectedDocumentsBasedOnQueryObjectForFilterOperator(
        filter, expectedPlainStrings, EDM_TABLES_TO_MONGO_DB_COLLECTIONS, ROOT_DOCUMENT_ID);
  }

  @ParameterizedTest
  @MethodSource("provideShouldReturnExpectedProjectedDocumentForAllLambda")
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_1.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_2.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_3.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_4.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_5.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_6.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_7.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_only_id.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_root.json")
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
    shouldReturnExpectedDocumentsBasedOnQueryObjectForFilterOperator(
        filterString, expectedPlainStrings, EDM_TABLES_TO_MONGO_DB_COLLECTIONS, ROOT_DOCUMENT_ID);
  }

  @ParameterizedTest
  @MethodSource("provideShouldReturnExpectedProjectedDocumentForComplexList")
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_complex_1.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_complex_2.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_complex_3.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_complex_4.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_complex_5.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_complex_6.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_only_id.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_root.json")
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
    shouldReturnExpectedDocumentsBasedOnQueryObjectForFilterOperator(
        filterString, expectedPlainStrings, EDM_TABLES_TO_MONGO_DB_COLLECTIONS, ROOT_DOCUMENT_ID);
  }
}
