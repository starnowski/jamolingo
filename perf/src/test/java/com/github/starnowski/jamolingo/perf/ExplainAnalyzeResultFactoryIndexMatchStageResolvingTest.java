package com.github.starnowski.jamolingo.perf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@QuarkusTestResource(EmbeddedMongoResource.class)
class ExplainAnalyzeResultFactoryIndexMatchStageResolvingTest {

  private static final String TEST_DATABASE = "test_db";
  @Inject MongoClient mongoClient;

  private static final List<Document> DEFAULT_INDEXES =
      List.of(
          new Document("plainString", 1),
          new Document("password", 1),
          new Document("nestedObject.tokens", 1),
          new Document("nestedObject.numbers", 1));

  public static Stream<Arguments> provideShouldResolveCorrectIndexValueAndReturnCorrectData() {

    return Stream.of(

        // Test case 1: example1.json (tokens="first example" AND numbers in (5, 27))

        // doc1 matches (tokens=first, numbers has 10 which is >5 and <27).

        // doc2 fails tokens.

        // doc3 fails numbers (2 is not > 5).

        Arguments.of(
            DEFAULT_INDEXES,
            "pipelines/example1.json",
            "FETCH + IXSCAN",
            "results/example1_result.json"),

        // Test case 2: example2.json (tokens="first example"), projects only tokens

        Arguments.of(
            DEFAULT_INDEXES,
            "pipelines/example2.json",
            "IXSCAN", // Covered query likely? or just IXSCAN
            "results/example2_result.json"),

        // Test case 3: startswith pattern
        Arguments.of(
            DEFAULT_INDEXES,
            "pipelines/startswith_query.json",
            "FETCH + IXSCAN",
            "results/startswith_query_result.json"));
  }

  @ParameterizedTest
  @MethodSource("provideShouldResolveCorrectIndexValueAndReturnCorrectData")
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc1.json"),
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc2.json"),
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc3.json")
      })
  public void shouldResolveCorrectIndexValueAndReturnCorrectDataForSimpleExactMath(
      List<Document> indexes,
      String pipelineFilePath,
      String expectedIndexValue,
      String expectedResultsFilePath)
      throws IOException {
    shouldResolveCorrectIndexValueAndReturnCorrectData(
        indexes, pipelineFilePath, expectedIndexValue, expectedResultsFilePath);
  }

  @Test
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc1.json"),
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc2.json"),
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc3.json")
      })
  public void shouldResolveCorrectIndexValueAndReturnCorrectDataForOrQuery() throws IOException {
    shouldResolveCorrectIndexValueAndReturnCorrectData(
        DEFAULT_INDEXES,
        "pipelines/or_query.json",
        "FETCH + IXSCAN",
        "results/or_query_result.json");
  }

  @Test
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc1.json"),
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc2.json"),
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc3.json")
      })
  public void shouldResolveCorrectIndexValueAndReturnCorrectDataForOrQueryWithThreeBranches()
      throws IOException {
    shouldResolveCorrectIndexValueAndReturnCorrectData(
        DEFAULT_INDEXES,
        "pipelines/or_query_three_branches.json",
        "FETCH + IXSCAN",
        "results/or_query_three_branches_result.json");
  }

  @Test
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc1.json"),
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc2.json"),
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc3.json")
      })
  public void shouldResolveCorrectIndexValueAndReturnCorrectDataForOrQueryWithSameField()
      throws IOException {
    shouldResolveCorrectIndexValueAndReturnCorrectData(
        DEFAULT_INDEXES,
        "pipelines/or_query_same_field.json",
        "FETCH + IXSCAN",
        "results/or_query_same_field_result.json");
  }

  @Test
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc1.json"),
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc2.json"),
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc3.json")
      })
  public void shouldResolveCorrectIndexValueAndReturnCorrectDataForOrQueryWithPartialIndex()
      throws IOException {
    // Only index plainString, so "nestedObject.numbers" is not indexed.
    // This might result in COLLSCAN if MongoDB decides so.
    shouldResolveCorrectIndexValueAndReturnCorrectData(
        List.of(new Document("plainString", 1)),
        "pipelines/or_query.json",
        "COLLSCAN", // Likely COLLSCAN because one branch is not indexed
        "results/or_query_result.json");
  }

  @Test
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc1.json"),
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc2.json"),
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc3.json")
      })
  public void shouldResolveCorrectIndexValueAndReturnCorrectDataForOrQueryWithNestedAnd()
      throws IOException {
    shouldResolveCorrectIndexValueAndReturnCorrectData(
        DEFAULT_INDEXES,
        "pipelines/or_query_nested_and.json",
        "FETCH + IXSCAN",
        "results/or_query_result.json");
  }

  @Test
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc1.json"),
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc_wildcard.json")
      })
  public void shouldResolveCorrectIndexValueForWildcardIndex() throws IOException {
    shouldResolveCorrectIndexValueAndReturnCorrectData(
        List.of(new Document("wildcardObj.$**", 1)),
        "pipelines/wildcard_query.json",
        "FETCH + IXSCAN",
        "results/wildcard_query_result.json");
  }

  @Test
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc1.json"),
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc_wildcard.json")
      })
  public void shouldResolveCorrectIndexValueForWildcardCompoundQuery() throws IOException {
    shouldResolveCorrectIndexValueAndReturnCorrectData(
        List.of(new Document("wildcardObj.$**", 1)),
        "pipelines/wildcard_compound_query.json",
        "FETCH + IXSCAN",
        "results/wildcard_query_result.json");
  }

  @Test
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc1.json"),
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc_wildcard_deep.json")
      })
  public void shouldResolveCorrectIndexValueForWildcardDeepQuery() throws IOException {
    shouldResolveCorrectIndexValueAndReturnCorrectData(
        List.of(new Document("wildcardObj.$**", 1)),
        "pipelines/wildcard_deep_query.json",
        "FETCH + IXSCAN",
        "results/wildcard_deep_query_result.json");
  }

  @Test
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc1.json"),
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/doc_wildcard_array.json")
      })
  public void shouldResolveCorrectIndexValueForWildcardArrayQuery() throws IOException {
    shouldResolveCorrectIndexValueAndReturnCorrectData(
        List.of(new Document("wildcardObj.$**", 1)),
        "pipelines/wildcard_array_query.json",
        "FETCH + IXSCAN",
        "results/wildcard_array_query_result.json");
  }

  private void shouldResolveCorrectIndexValueAndReturnCorrectData(
      List<Document> indexes,
      String pipelineFilePath,
      String expectedIndexValue,
      String expectedResultsFilePath)
      throws IOException {

    // GIVEN

    createIndexes(indexes);

    List<Document> pipeline = preparePipeline(pipelineFilePath);

    List<Document> expectedResults = prepareExpectedResults(expectedResultsFilePath);

    // WHEN

    // 1. Get Actual Data

    List<Document> actualResults = new ArrayList<>();

    getCollection().aggregate(pipeline).into(actualResults);

    // 2. Get Explain Plan

    Document explain = getCollection().aggregate(pipeline).explain();

    ExplainAnalyzeResultFactory tested = new ExplainAnalyzeResultFactory();

    ExplainAnalyzeResult result = tested.build(explain);

    // TODO Resolve match stage that use index
    List<Bson> indexMatchStages = result.getIndexMatchStages();
    if (!"COLLSCAN".equals(expectedIndexValue)) {
      Assertions.assertFalse(indexMatchStages.isEmpty());
    }
    List<Bson> enhancedPipeline = new ArrayList<>(indexMatchStages);
    enhancedPipeline.add(new Document("$set", new Document("no_such_field_test", true)));
    enhancedPipeline.addAll(pipeline);
    enhancedPipeline.add(new Document("$unset", "no_such_field_test"));
    // resolve index for enhanced pipeline
    String enhancedPipelineIndex =
        resolveIndexStatus(getCollection().aggregate(enhancedPipeline).explain());
    List<Document> actualResultsForEnhancedPipelineIndex = new ArrayList<>();
    getCollection().aggregate(enhancedPipeline).into(actualResultsForEnhancedPipelineIndex);
    // THEN

    // Verify Data

    assertEquals(expectedResults.size(), actualResults.size());
    assertEquals(expectedResults, actualResults);

    // Verify Data for enhanced pipeline

    assertEquals(expectedResults.size(), actualResultsForEnhancedPipelineIndex.size());
    assertEquals(expectedResults, actualResultsForEnhancedPipelineIndex);

    // Verify Index

    Assertions.assertEquals(expectedIndexValue, result.getIndexValue().getValue());

    if (!"COLLSCAN".equals(expectedIndexValue)) {
      Assertions.assertTrue(
          "FETCH + IXSCAN".endsWith(enhancedPipelineIndex)
              || "IXSCAN".equals(enhancedPipelineIndex));
    }
  }

  private String resolveIndexStatus(Document explain) {
    ExplainAnalyzeResultFactory tested = new ExplainAnalyzeResultFactory();

    ExplainAnalyzeResult result = tested.build(explain);
    return result.getIndexValue().getValue();
  }

  private List<Document> prepareExpectedResults(String filePath) throws IOException {

    String json =
        Files.readString(
            Paths.get(
                new File(
                        Objects.requireNonNull(getClass().getClassLoader().getResource(filePath))
                            .getFile())
                    .getPath()));

    Document jsonDocument = Document.parse(json);

    return (List<Document>) jsonDocument.get("value");
  }

  private List<Document> preparePipeline(String filePath) throws IOException {
    String pipeline =
        Files.readString(
            Paths.get(
                new File(
                        Objects.requireNonNull(getClass().getClassLoader().getResource(filePath))
                            .getFile())
                    .getPath()));
    Document pipelineDocument = Document.parse(pipeline);
    return (List<Document>) pipelineDocument.get("pipeline");
  }

  private MongoCollection getCollection() {
    return mongoClient.getDatabase(TEST_DATABASE).getCollection("docs");
  }

  private void createIndexes(List<Document> indexes) {
    MongoCollection<Document> col = getCollection();
    col.dropIndexes();
    indexes.forEach(col::createIndex);
  }
}
