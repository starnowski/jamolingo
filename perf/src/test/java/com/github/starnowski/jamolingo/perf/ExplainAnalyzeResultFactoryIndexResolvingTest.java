package com.github.starnowski.jamolingo.perf;

import static org.junit.jupiter.api.Assertions.*;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@QuarkusTestResource(EmbeddedMongoResource.class)
class ExplainAnalyzeResultFactoryIndexResolvingTest {

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
        Arguments.of(DEFAULT_INDEXES, "pipelines/example1.json", "FETCH + IXSCAN"),
        Arguments.of(DEFAULT_INDEXES, "pipelines/example2.json", "IXSCAN"),
        Arguments.of(DEFAULT_INDEXES, "pipelines/example3.json", "COLLSCAN"));
  }

  @ParameterizedTest
  @MethodSource("provideShouldResolveCorrectIndexValue")
  public void shouldResolveCorrectIndexValue(
      List<Document> indexes, String pipelineFilePath, String expectedIndexValue)
      throws IOException {
    // GIVEN
    createIndexes(indexes);
    List<Document> pipeline = preparePipeline(pipelineFilePath);
    // Run explain on the aggregation
    Document explain = getCollection().aggregate(pipeline).explain();
    deleteIndexes(indexes);
    ExplainAnalyzeResultFactory tested = new ExplainAnalyzeResultFactory();

    // WHEN
    ExplainAnalyzeResult result = tested.build(explain);

    // THEN
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
    //        col.createIndex(new Document("plainString", 1));
    //        col.createIndex(new Document("password", 1));
    //        col.createIndex(new Document("nestedObject.tokens", 1));
    //        col.createIndex(new Document("nestedObject.numbers", 1));
    indexes.forEach(col::createIndex);
  }

  private void deleteIndexes(List<Document> indexes) {
    MongoCollection<Document> col = getCollection();
    //        col.createIndex(new Document("plainString", 1));
    //        col.createIndex(new Document("password", 1));
    //        col.createIndex(new Document("nestedObject.tokens", 1));
    //        col.createIndex(new Document("nestedObject.numbers", 1));
    indexes.forEach(col::dropIndex);
  }
}
