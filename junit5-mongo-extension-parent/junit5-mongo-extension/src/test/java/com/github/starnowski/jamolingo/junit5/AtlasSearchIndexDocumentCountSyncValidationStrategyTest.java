package com.github.starnowski.jamolingo.junit5;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.Collection;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AtlasSearchIndexDocumentCountSyncValidationStrategyTest {

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
  void shouldSkipValidationAndReturnTrueWhenValidationPropertyIsNull() {
    // GIVEN
    AtlasSearchIndexValidationContext contextWithoutProperties =
        AtlasSearchIndexValidationContext.builder()
            .withMongoClient(mongoClient)
            .withDatabase("testdb")
            .withCollection("items")
            .withIndexName("my_index")
            .withValidationProperty((String[]) null)
            .build();
    AtlasSearchIndexDocumentCountSyncValidationStrategy tested =
        new AtlasSearchIndexDocumentCountSyncValidationStrategy(30, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(contextWithoutProperties);

    // THEN
    assertTrue(result);
    verifyNoInteractions(mongoClient, mongoDatabase, mongoCollection);
  }

  @Test
  void shouldSkipValidationAndReturnTrueWhenValidationPropertyArrayIsEmpty() {
    // GIVEN
    AtlasSearchIndexValidationContext contextWithEmptyProperties =
        AtlasSearchIndexValidationContext.builder()
            .withMongoClient(mongoClient)
            .withDatabase("testdb")
            .withCollection("items")
            .withIndexName("my_index")
            .withValidationProperty()
            .build();
    AtlasSearchIndexDocumentCountSyncValidationStrategy tested =
        new AtlasSearchIndexDocumentCountSyncValidationStrategy(30, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(contextWithEmptyProperties);

    // THEN
    assertTrue(result);
    verifyNoInteractions(mongoClient, mongoDatabase, mongoCollection);
  }

  @Test
  void shouldReturnTrueWhenSearchMetaTotalMatchesCollectionCountForValidationProperty() {
    // GIVEN
    Document expectedCountFilter = new Document("field1", new Document("$exists", true));
    when(mongoCollection.countDocuments(expectedCountFilter)).thenReturn(3L);

    Document expectedSearchMetaStage =
        new Document(
            "$searchMeta",
            new Document("index", "my_index")
                .append("exists", new Document("path", "field1"))
                .append("count", new Document("type", "total")));
    AggregateIterable<Document> aggregateIterable =
        mockAggregateIterable(List.of(new Document("count", new Document("total", 3L))));
    when(mongoCollection.aggregate(List.of(expectedSearchMetaStage))).thenReturn(aggregateIterable);

    AtlasSearchIndexDocumentCountSyncValidationStrategy tested =
        new AtlasSearchIndexDocumentCountSyncValidationStrategy(50, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(context);

    // THEN
    assertTrue(result);
    verify(mongoCollection).countDocuments(expectedCountFilter);
  }

  @Test
  void shouldReturnTrueWhenSearchMetaTotalsMatchCollectionCountsForMultipleValidationProperties() {
    // GIVEN
    AtlasSearchIndexValidationContext multiPropertyContext =
        AtlasSearchIndexValidationContext.builder()
            .withMongoClient(mongoClient)
            .withDatabase("testdb")
            .withCollection("items")
            .withIndexName("my_index")
            .withValidationProperty("field1", "field2")
            .build();

    Document countFilterField1 = new Document("field1", new Document("$exists", true));
    Document countFilterField2 = new Document("field2", new Document("$exists", true));
    when(mongoCollection.countDocuments(countFilterField1)).thenReturn(2L);
    when(mongoCollection.countDocuments(countFilterField2)).thenReturn(3L);

    Document searchMetaStageField1 =
        new Document(
            "$searchMeta",
            new Document("index", "my_index")
                .append("exists", new Document("path", "field1"))
                .append("count", new Document("type", "total")));
    Document searchMetaStageField2 =
        new Document(
            "$searchMeta",
            new Document("index", "my_index")
                .append("exists", new Document("path", "field2"))
                .append("count", new Document("type", "total")));

    AggregateIterable<Document> aggregateField1 =
        mockAggregateIterable(List.of(new Document("count", new Document("total", 2L))));
    AggregateIterable<Document> aggregateField2 =
        mockAggregateIterable(List.of(new Document("count", new Document("total", 3L))));
    when(mongoCollection.aggregate(List.of(searchMetaStageField1))).thenReturn(aggregateField1);
    when(mongoCollection.aggregate(List.of(searchMetaStageField2))).thenReturn(aggregateField2);

    AtlasSearchIndexDocumentCountSyncValidationStrategy tested =
        new AtlasSearchIndexDocumentCountSyncValidationStrategy(50, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(multiPropertyContext);

    // THEN
    assertTrue(result);
    verify(mongoCollection).countDocuments(countFilterField1);
    verify(mongoCollection).countDocuments(countFilterField2);
  }

  @Test
  void shouldReturnFalseWhenAnyValidationPropertyCountDoesNotMatchInMultiPropertyArray() {
    // GIVEN
    AtlasSearchIndexValidationContext multiPropertyContext =
        AtlasSearchIndexValidationContext.builder()
            .withMongoClient(mongoClient)
            .withDatabase("testdb")
            .withCollection("items")
            .withIndexName("my_index")
            .withValidationProperty("field1", "field2")
            .build();

    Document countFilterField1 = new Document("field1", new Document("$exists", true));
    Document countFilterField2 = new Document("field2", new Document("$exists", true));
    when(mongoCollection.countDocuments(countFilterField1)).thenReturn(2L);
    when(mongoCollection.countDocuments(countFilterField2)).thenReturn(3L);

    Document searchMetaStageField1 =
        new Document(
            "$searchMeta",
            new Document("index", "my_index")
                .append("exists", new Document("path", "field1"))
                .append("count", new Document("type", "total")));
    Document searchMetaStageField2 =
        new Document(
            "$searchMeta",
            new Document("index", "my_index")
                .append("exists", new Document("path", "field2"))
                .append("count", new Document("type", "total")));

    AggregateIterable<Document> aggregateField1 =
        mockAggregateIterable(List.of(new Document("count", new Document("total", 2L))));
    AggregateIterable<Document> aggregateField2Mismatch =
        mockAggregateIterable(List.of(new Document("count", new Document("total", 1L))));
    when(mongoCollection.aggregate(List.of(searchMetaStageField1))).thenReturn(aggregateField1);
    when(mongoCollection.aggregate(List.of(searchMetaStageField2)))
        .thenReturn(aggregateField2Mismatch);

    AtlasSearchIndexDocumentCountSyncValidationStrategy tested =
        new AtlasSearchIndexDocumentCountSyncValidationStrategy(30, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(multiPropertyContext);

    // THEN
    assertFalse(result);
  }

  @Test
  void shouldReturnTrueWhenSearchMetaTotalReachesCollectionCountOnSubsequentPoll() {
    // GIVEN
    Document expectedCountFilter = new Document("field1", new Document("$exists", true));
    when(mongoCollection.countDocuments(expectedCountFilter)).thenReturn(2L);

    Document expectedSearchMetaStage =
        new Document(
            "$searchMeta",
            new Document("index", "my_index")
                .append("exists", new Document("path", "field1"))
                .append("count", new Document("type", "total")));
    AggregateIterable<Document> firstPoll =
        mockAggregateIterable(List.of(new Document("count", new Document("total", 1L))));
    AggregateIterable<Document> secondPoll =
        mockAggregateIterable(List.of(new Document("count", new Document("total", 2L))));
    when(mongoCollection.aggregate(List.of(expectedSearchMetaStage)))
        .thenReturn(firstPoll, secondPoll);

    AtlasSearchIndexDocumentCountSyncValidationStrategy tested =
        new AtlasSearchIndexDocumentCountSyncValidationStrategy(
            200, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(context);

    // THEN
    assertTrue(result);
  }

  @Test
  void shouldReturnFalseWhenSearchMetaTotalDoesNotMatchCollectionCount() {
    // GIVEN
    Document expectedCountFilter = new Document("field1", new Document("$exists", true));
    when(mongoCollection.countDocuments(expectedCountFilter)).thenReturn(3L);

    Document expectedSearchMetaStage =
        new Document(
            "$searchMeta",
            new Document("index", "my_index")
                .append("exists", new Document("path", "field1"))
                .append("count", new Document("type", "total")));
    AggregateIterable<Document> aggregateIterable =
        mockAggregateIterable(List.of(new Document("count", new Document("total", 1L))));
    when(mongoCollection.aggregate(List.of(expectedSearchMetaStage))).thenReturn(aggregateIterable);

    AtlasSearchIndexDocumentCountSyncValidationStrategy tested =
        new AtlasSearchIndexDocumentCountSyncValidationStrategy(30, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(context);

    // THEN
    assertFalse(result);
  }

  @Test
  void shouldReturnTrueWhenCollectionCountIsZeroAndSearchMetaReturnsEmpty() {
    // GIVEN
    Document expectedCountFilter = new Document("field1", new Document("$exists", true));
    when(mongoCollection.countDocuments(expectedCountFilter)).thenReturn(0L);

    AggregateIterable<Document> aggregateIterable = mockAggregateIterable(List.of());
    when(mongoCollection.aggregate(any())).thenReturn(aggregateIterable);

    AtlasSearchIndexDocumentCountSyncValidationStrategy tested =
        new AtlasSearchIndexDocumentCountSyncValidationStrategy(30, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(context);

    // THEN
    assertTrue(result);
  }

  @Test
  void shouldUseProvidedValidationConfigWhenCheckingDocumentCount() {
    // GIVEN
    Document expectedCountFilter = new Document("field1", new Document("$exists", true));
    when(mongoCollection.countDocuments(expectedCountFilter)).thenReturn(2L);

    Document expectedSearchMetaStage =
        new Document(
            "$searchMeta",
            new Document("index", "my_index")
                .append("exists", new Document("path", "field1"))
                .append("count", new Document("type", "total")));
    AggregateIterable<Document> firstPoll =
        mockAggregateIterable(List.of(new Document("count", new Document("total", 1L))));
    AggregateIterable<Document> secondPoll =
        mockAggregateIterable(List.of(new Document("count", new Document("total", 2L))));
    when(mongoCollection.aggregate(List.of(expectedSearchMetaStage)))
        .thenReturn(firstPoll, secondPoll);

    AtlasSearchIndexDocumentCountSyncValidationStrategy tested =
        new AtlasSearchIndexDocumentCountSyncValidationStrategy();
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
  private AggregateIterable<Document> mockAggregateIterable(List<Document> documents) {
    AggregateIterable<Document> iterable = mock(AggregateIterable.class);
    when(iterable.into(any()))
        .thenAnswer(
            invocation -> {
              Collection<Document> target = invocation.getArgument(0);
              target.addAll(documents);
              return target;
            });
    return iterable;
  }
}
