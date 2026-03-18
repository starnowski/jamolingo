package com.github.starnowski.jamolingo.perf;

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
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.github.starnowski.jamolingo.perf.EmbeddedMongoResource.TEST_DATABASE;

@QuarkusTest
@QuarkusTestResource(value = MongoAtlasResource.class, restrictToAnnotatedClass = true)
@ExtendWith(QuarkusMongoDataLoaderExtension.class)
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
class ExplainAnalyzeResultFactoryISearchIndexResolvingTest {

  private static final String TEST_DATABASE = "test_db";
  @Inject MongoClient mongoClient;

  private static final List<Document> DEFAULT_INDEXES =
      List.of(
          new Document("plainString", 1),
          new Document("password", 1),
          new Document("nestedObject.tokens", 1),
          new Document("nestedObject.numbers", 1));

  public static Stream<Arguments> provideShouldResolveCorrectIndexValue() {
    return Stream.of(
        Arguments.of(DEFAULT_INDEXES, "pipelines/example1.json", "FETCH + IXSCAN", false),
        Arguments.of(DEFAULT_INDEXES, "pipelines/example2.json", "IXSCAN", false),
        Arguments.of(DEFAULT_INDEXES, "pipelines/example3.json", "COLLSCAN", false),
        Arguments.of(List.of(), "pipelines/example1.json", "COLLSCAN", false),
        Arguments.of(List.of(), "pipelines/example2.json", "COLLSCAN", false),
        Arguments.of(
            List.of(new Document("plainString", 1)), "pipelines/example1.json", "COLLSCAN", false),
        Arguments.of(
            List.of(new Document("password", 1)), "pipelines/example2.json", "COLLSCAN", false),
        Arguments.of(
            List.of(new Document("nestedObject.tokens", 1)),
            "pipelines/example2.json",
            "IXSCAN",
            false),
        Arguments.of(
            List.of(new Document("password", 1)),
            "pipelines/example_covered_password.json",
            "IXSCAN",
            false),
        Arguments.of(
            List.of(new Document("plainString", 1)),
            "pipelines/example_covered_plainString.json",
            "IXSCAN",
            false),
        Arguments.of(
            List.of(new Document("plainString", 1), new Document("password", 1)),
            "pipelines/example_covered_plainString.json",
            "IXSCAN",
            false),
        Arguments.of(
            DEFAULT_INDEXES, "pipelines/example_merge_indexes.json", "FETCH + IXSCAN", false),
        Arguments.of(List.of(), "pipelines/search_example.json", "SEARCH", true));
  }

  @ParameterizedTest
  @MethodSource("provideShouldResolveCorrectIndexValue")
  public void shouldResolveCorrectIndexValue(
      List<Document> indexes,
      String pipelineFilePath,
      String expectedIndexValue,
      boolean searchIndex)
      throws IOException, InterruptedException {
    // GIVEN
    if (searchIndex) {
      ensureSearchIndex(getCollection());
    } else {
      createIndexes(indexes);
    }
    List<Document> pipeline = preparePipeline(pipelineFilePath);
    // Run explain on the aggregation
    Document explain = getCollection().aggregate(pipeline).explain();
    ExplainAnalyzeResultFactory tested = new ExplainAnalyzeResultFactory();

    // WHEN
    ExplainAnalyzeResult result = tested.build(explain);

    // THEN
    Assertions.assertNotNull(result);
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

  @AfterEach
  public void dropIndexes() {
    getCollection().dropIndexes();
    try {
      getCollection().dropSearchIndex("atlas_search_index");
    } catch (Exception e) {
      // Ignore
    }
  }
}
