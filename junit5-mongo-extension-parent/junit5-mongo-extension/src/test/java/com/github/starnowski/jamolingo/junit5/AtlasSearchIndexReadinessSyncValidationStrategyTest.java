package com.github.starnowski.jamolingo.junit5;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mongodb.client.ListSearchIndexesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AtlasSearchIndexReadinessSyncValidationStrategyTest {

  private MongoClient mongoClient;
  private MongoDatabase mongoDatabase;
  private MongoCollection<Document> mongoCollection;
  private AtlasSearchIndexValidationContext context;

  @BeforeEach
  void setUp() {
    mongoClient = mock(MongoClient.class);
    mongoDatabase = mock(MongoDatabase.class);
    mongoCollection = mock(MongoCollection.class);
    when(mongoClient.getDatabase("testdb")).thenReturn(mongoDatabase);
    when(mongoDatabase.getCollection("items")).thenReturn(mongoCollection);

    context =
        AtlasSearchIndexValidationContext.builder()
            .withMongoClient(mongoClient)
            .withDatabase("testdb")
            .withCollection("items")
            .withIndexName("my_index")
            .withValidationProperty("field1")
            .build();
  }

  @Test
  void shouldReturnTrueWhenIndexStatusIsReady() {
    // GIVEN
    ListSearchIndexesIterable<Document> iterable =
        mockSearchIndexesIterable(
            List.of(
                new Document("name", "other_index").append("status", "BUILDING"),
                new Document("name", "my_index").append("status", "READY")));
    when(mongoCollection.listSearchIndexes()).thenReturn(iterable);
    AtlasSearchIndexReadinessSyncValidationStrategy tested =
        new AtlasSearchIndexReadinessSyncValidationStrategy(50, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(context);

    // THEN
    assertTrue(result);
  }

  @Test
  void shouldReturnTrueWhenIndexBecomesReadyOnSubsequentPoll() {
    // GIVEN
    ListSearchIndexesIterable<Document> buildingIterable =
        mockSearchIndexesIterable(
            List.of(new Document("name", "my_index").append("status", "BUILDING")));
    ListSearchIndexesIterable<Document> readyIterable =
        mockSearchIndexesIterable(
            List.of(new Document("name", "my_index").append("status", "READY")));
    when(mongoCollection.listSearchIndexes()).thenReturn(buildingIterable, readyIterable);
    AtlasSearchIndexReadinessSyncValidationStrategy tested =
        new AtlasSearchIndexReadinessSyncValidationStrategy(200, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(context);

    // THEN
    assertTrue(result);
  }

  @ParameterizedTest
  @ValueSource(strings = {"BUILDING", "PENDING", "FAILED"})
  void shouldReturnFalseWhenIndexStatusIsNotReady(String status) {
    // GIVEN
    ListSearchIndexesIterable<Document> iterable =
        mockSearchIndexesIterable(
            List.of(new Document("name", "my_index").append("status", status)));
    when(mongoCollection.listSearchIndexes()).thenReturn(iterable);
    AtlasSearchIndexReadinessSyncValidationStrategy tested =
        new AtlasSearchIndexReadinessSyncValidationStrategy(30, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(context);

    // THEN
    assertFalse(result);
  }

  @Test
  void shouldReturnFalseWhenIndexIsMissing() {
    // GIVEN
    ListSearchIndexesIterable<Document> iterable =
        mockSearchIndexesIterable(
            List.of(new Document("name", "another_index").append("status", "READY")));
    when(mongoCollection.listSearchIndexes()).thenReturn(iterable);
    AtlasSearchIndexReadinessSyncValidationStrategy tested =
        new AtlasSearchIndexReadinessSyncValidationStrategy(30, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(context);

    // THEN
    assertFalse(result);
  }

  @Test
  void shouldUseProvidedValidationConfigWhenCheckingReadiness() {
    // GIVEN
    ListSearchIndexesIterable<Document> buildingIterable =
        mockSearchIndexesIterable(
            List.of(new Document("name", "my_index").append("status", "BUILDING")));
    ListSearchIndexesIterable<Document> readyIterable =
        mockSearchIndexesIterable(
            List.of(new Document("name", "my_index").append("status", "READY")));
    when(mongoCollection.listSearchIndexes()).thenReturn(buildingIterable, readyIterable);
    AtlasSearchIndexReadinessSyncValidationStrategy tested =
        new AtlasSearchIndexReadinessSyncValidationStrategy();
    AtlasSearchIndexValidationConfig config =
        AtlasSearchIndexValidationConfig.builder()
            .withMaxWaitDuration(200)
            .withMaxWaitUnit(MILLISECONDS)
            .withPollInterval(10)
            .withPollIntervalUnit(MILLISECONDS)
            .build();

    // WHEN
    boolean result = tested.isSync(context, config);

    // THEN
    assertTrue(result);
  }

  @SuppressWarnings("unchecked")
  private ListSearchIndexesIterable<Document> mockSearchIndexesIterable(List<Document> documents) {
    ListSearchIndexesIterable<Document> iterable = mock(ListSearchIndexesIterable.class);
    when(iterable.iterator())
        .thenAnswer(
            invocation -> {
              var iter = documents.iterator();
              MongoCursor<Document> cursor = mock(MongoCursor.class);
              when(cursor.hasNext()).thenAnswer(inv -> iter.hasNext());
              when(cursor.next()).thenAnswer(inv -> iter.next());
              return cursor;
            });
    return iterable;
  }
}
