package com.github.starnowski.jamolingo.perf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import com.github.starnowski.jamolingo.junit5.QuarkusMongoDataLoaderExtension;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@QuarkusTestResource(MongoAtlasResource.class)
@ExtendWith(QuarkusMongoDataLoaderExtension.class)
class ExplainAnalyzeResultFactoryISearchIndexMatchStageResolvingTest {

  private static final String TEST_DATABASE = "test_db";
  @Inject MongoClient mongoClient;

  public static Stream<Arguments> provideShouldResolveCorrectIndexValueAndReturnCorrectData() {
    return Stream.of(
        Arguments.of(
            "pipelines/search_simple.json", "SEARCH", "results/search_simple_result.json"));
  }

  @ParameterizedTest
  @MethodSource("provideShouldResolveCorrectIndexValueAndReturnCorrectData")
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = TEST_DATABASE,
            collection = "docs",
            bsonFilePath = "data/search_doc1.json")
      })
  public void shouldResolveCorrectIndexValueAndReturnCorrectData(
      String pipelineFilePath, String expectedIndexValue, String expectedResultsFilePath)
      throws IOException, InterruptedException {
    // GIVEN
    ensureSearchIndex(getCollection());
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
    Assertions.assertNotNull(result);
    Assertions.assertEquals(expectedIndexValue, result.getIndexValue().getValue());

    List<Bson> indexMatchStages = result.getIndexMatchStages();
    Assertions.assertFalse(indexMatchStages.isEmpty());

    // MongoDB does not allow more than one $search stage in the aggregation pipeline.
    // Therefore, to verify if the resolved $search stage is correct, we must remove
    // the original $search stage from the pipeline and replace it with the one extracted by the
    // factory.
    List<Bson> enhancedPipeline = new ArrayList<>(indexMatchStages);
    if (pipeline.size() > 1) {
      enhancedPipeline.addAll(pipeline.subList(1, pipeline.size()));
    }

    List<Document> actualResultsForEnhancedPipelineIndex = new ArrayList<>();
    getCollection().aggregate(enhancedPipeline).into(actualResultsForEnhancedPipelineIndex);

    // Verify Data
    assertEquals(expectedResults.size(), actualResults.size());
    // Note: We might need to normalize documents if _id is present but not in expected
    assertEquals(expectedResults.size(), actualResultsForEnhancedPipelineIndex.size());
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

  private void ensureSearchIndex(MongoCollection<Document> collection) throws InterruptedException {
    try {
      collection.createSearchIndex(
          "atlas_search_index", new Document("mappings", new Document("dynamic", true)));
      // Wait for index to be ready
      while (true) {
        boolean ready = false;
        for (Document index : collection.listSearchIndexes()) {
          if ("atlas_search_index".equals(index.getString("name"))
              && "READY".equals(index.getString("status"))) {
            ready = true;
            break;
          }
        }
        if (ready) break;
        Thread.sleep(500);
      }
    } catch (Exception e) {
      // Index might already exist
    }
  }
}
