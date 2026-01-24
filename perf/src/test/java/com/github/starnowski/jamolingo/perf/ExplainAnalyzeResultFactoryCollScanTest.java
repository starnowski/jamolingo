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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(EmbeddedMongoResource.class)
class ExplainAnalyzeResultFactoryCollScanTest {

  private static final String TEST_DATABASE = "test_db";
  @Inject MongoClient mongoClient;

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
  public void shouldReturnEmptyIndexMatchStagesForSimpleMatchWithNoIndex() throws IOException {
    shouldResolveCorrectIndexValueAndReturnCorrectData(
        Collections.emptyList(), // No indexes -> COLLSCAN
        "pipelines/example1.json",
        "COLLSCAN",
        "results/example1_result.json");
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
  public void shouldReturnEmptyIndexMatchStagesForSortWithNoIndex() throws IOException {
    shouldResolveCorrectIndexValueAndReturnCorrectData(
        Collections.emptyList(), // No indexes -> COLLSCAN
        "pipelines/sort_only_query.json",
        "COLLSCAN", // Will be COLLSCAN + SORT (in memory)
        "results/sort_only_result.json");
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
  public void shouldReturnEmptyIndexMatchStagesForOrQueryWithNoIndex() throws IOException {
    shouldResolveCorrectIndexValueAndReturnCorrectData(
        Collections.emptyList(), // No indexes -> COLLSCAN
        "pipelines/or_query.json",
        "COLLSCAN",
        "results/or_query_result.json");
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

    // Resolve match stage that use index
    List<Bson> indexMatchStages = result.getIndexMatchStages();

    // THE ASSERTION REQUESTED
    Assertions.assertTrue(
        indexMatchStages.isEmpty(), "Index match stages should be empty for COLLSCAN");

    // Verify Data

    assertEquals(expectedResults.size(), actualResults.size());
    assertEquals(expectedResults, actualResults);

    // Verify Index
    // We check if the index value is indeed COLLSCAN
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
    col.dropIndexes();
    indexes.forEach(col::createIndex);
  }
}
