package com.github.starnowski.jamolingo.compat.driver.operators.filter;

import com.github.starnowski.jamolingo.core.operators.filter.FilterOperatorResult;
import com.github.starnowski.jamolingo.core.operators.filter.ODataFilterToMongoMatchParser;
import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import com.github.starnowski.jamolingo.perf.ExplainAnalyzeResult;
import com.github.starnowski.jamolingo.perf.ExplainAnalyzeResultFactory;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@QuarkusTest
public class FilterOperatorIndexMatchingCheckTest extends AbstractBaseFilterOperatorTest {

  @Inject
  protected MongoClient mongoClient;

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
          ODataApplicationException, IOException {
    shouldResolveCorrectIndexValueAndReturnCorrectData(filter, expectedPlainStrings);
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
          ODataApplicationException, IOException {
    shouldResolveCorrectIndexValueAndReturnCorrectData(filter, expectedPlainStrings);
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
          ODataApplicationException, IOException {
    shouldResolveCorrectIndexValueAndReturnCorrectData(filter, expectedPlainStrings);
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
          ODataApplicationException, IOException {
    shouldResolveCorrectIndexValueAndReturnCorrectData(filter, expectedPlainStrings);
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

  private void shouldResolveCorrectIndexValueAndReturnCorrectData(
          String filter, Set<String> expectedPlainStrings)
          throws IOException, XMLStreamException, UriValidationException, UriParserException, ExpressionVisitException, ODataApplicationException {

    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("testdb");
    MongoCollection<Document> collection = database.getCollection("Items");
    Edm edm = loadEmdProvider("edm/edm6_filter_main.xml");
    UriInfo uriInfo =
            new Parser(edm, OData.newInstance()).parseUri("examples2", "$filter=" + filter, null, null);
    ODataFilterToMongoMatchParser tested = new ODataFilterToMongoMatchParser();

    // WHEN
    FilterOperatorResult result = tested.parse(uriInfo.getFilterOption());
    List<Bson> pipeline = new ArrayList<>(result.getStageObjects());

    // 1. Get Actual Data
    List<Document> results = new ArrayList<>();
    List<Document> expectedResults = collection.aggregate(pipeline).into(results);

    // 2. Get Explain Plan

    Document explain = collection.aggregate(pipeline).explain();

    ExplainAnalyzeResultFactory explainAnalyzeResultFactoryTested = new ExplainAnalyzeResultFactory();

    ExplainAnalyzeResult explainAnalyzeResult = explainAnalyzeResultFactoryTested.build(explain);

    // TODO Resolve match stage that use index
    List<Bson> indexMatchStages = explainAnalyzeResult.getIndexMatchStages();
    System.out.println("Pipeline: " + filter);
      assumeTrue(!"COLLSCAN".equals(explainAnalyzeResult.getIndexValue().getValue()), "Test ignored because it suppose to test only queries without COLLSCAN");
      Assertions.assertFalse(indexMatchStages.isEmpty());
    List<Bson> enhancedPipeline = new ArrayList<>(indexMatchStages);
    enhancedPipeline.add(new Document("$set", new Document("no_such_field_test", true)));
    enhancedPipeline.addAll(pipeline);
    enhancedPipeline.add(new Document("$unset", "no_such_field_test"));
    // resolve index for enhanced pipeline

    ExplainAnalyzeResult enhancedPipelineIndex = explainAnalyzeResultFactoryTested.build(collection.aggregate(enhancedPipeline).explain());
    List<Document> actualResultsForEnhancedPipelineIndex = new ArrayList<>();
    collection.aggregate(enhancedPipeline).into(actualResultsForEnhancedPipelineIndex);
    // THEN

    // Verify Data for enhanced pipeline

    assertEquals(expectedResults.size(), actualResultsForEnhancedPipelineIndex.size());
    assertEquals(expectedResults, actualResultsForEnhancedPipelineIndex);

    // Verify Index

    Assertions.assertEquals(explainAnalyzeResult.getIndexValue().getValue(), enhancedPipelineIndex.getIndexValue().getValue());

      // Verify that raw index documents matches
      Document explainEnhancedPipeline = collection.aggregate(enhancedPipeline).explain();
      Assertions.assertNotNull(enhancedPipelineIndex.getRawIndexDocuments());
      Assertions.assertFalse(enhancedPipelineIndex.getRawIndexDocuments().isEmpty());
      Assertions.assertEquals(
              explainAnalyzeResult.getRawIndexDocuments(), enhancedPipelineIndex.getRawIndexDocuments());
  }
}
