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
    // Shared data
    Document doc1 =
        new Document("_id", "doc1")
            .append("plainString", "abc")
            .append(
                "nestedObject",
                new Document("tokens", "first example").append("numbers", List.of(10, 20)));

    return Stream.of(
        // Test case 1: example1.json (tokens="first example" AND numbers in (5, 27))
        // doc1 matches (tokens=first, numbers has 10 which is >5 and <27).
        // doc2 fails tokens.
        // doc3 fails numbers (2 is not > 5).
        Arguments.of(DEFAULT_INDEXES, "pipelines/example1.json", "FETCH + IXSCAN", List.of(doc1)),
        // Test case 2: example2.json (tokens="first example"), projects only tokens
        Arguments.of(
            DEFAULT_INDEXES,
            "pipelines/example2.json",
            "IXSCAN", // Covered query likely? or just IXSCAN
            List.of(
                new Document("nestedObject", new Document("tokens", "first example")),
                new Document("nestedObject", new Document("tokens", "first example")))));
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
  public void shouldResolveCorrectIndexValueAndReturnCorrectData(
      List<Document> indexes,
      String pipelineFilePath,
      String expectedIndexValue,
      List<Document> expectedResults)
      throws IOException {
    // GIVEN
    createIndexes(indexes);
    List<Document> pipeline = preparePipeline(pipelineFilePath);

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
