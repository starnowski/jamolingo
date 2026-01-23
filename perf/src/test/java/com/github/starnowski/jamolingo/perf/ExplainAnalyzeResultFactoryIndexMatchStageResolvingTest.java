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
import org.junit.jupiter.api.Assertions;
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
            "results/example2_result.json"));
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

    // THEN

    // Verify Data

    assertEquals(expectedResults.size(), actualResults.size());

    assertEquals(expectedResults, actualResults);

    // Verify Index

    Assertions.assertEquals(expectedIndexValue, result.getIndexValue().getValue());
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
    indexes.forEach(col::createIndex);
  }
}
