package com.github.starnowski.jamolingo.junit5;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.Collection;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AtlasSearchIndexValuesSyncValidationStrategyTest {

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
    AtlasSearchIndexValuesSyncValidationStrategy tested =
        new AtlasSearchIndexValuesSyncValidationStrategy(30, MILLISECONDS, 10, MILLISECONDS);

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
    AtlasSearchIndexValuesSyncValidationStrategy tested =
        new AtlasSearchIndexValuesSyncValidationStrategy(30, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(contextWithEmptyProperties);

    // THEN
    assertTrue(result);
    verifyNoInteractions(mongoClient, mongoDatabase, mongoCollection);
  }

  @Test
  void shouldReturnTrueWhenAllDocumentsWithValidationPropertyAreFoundInSearchResults() {
    // GIVEN
    Document doc1 = new Document("_id", "doc_1").append("field1", "val1");
    Document doc2 = new Document("_id", "doc_2").append("field1", "val2");
    Document docWithNull = new Document("_id", "doc_3").append("field1", null);

    FindIterable<Document> findIterable = mockFindIterable(List.of(doc1, doc2, docWithNull));
    when(mongoCollection.find(new Document("field1", new Document("$exists", true))))
        .thenReturn(findIterable);

    Document searchStage1 =
        new Document(
            "$search",
            new Document("index", "my_index")
                .append("text", new Document("query", "val1").append("path", "field1")));
    Document searchStage2 =
        new Document(
            "$search",
            new Document("index", "my_index")
                .append("text", new Document("query", "val2").append("path", "field1")));

    AggregateIterable<Document> searchResult1 =
        mockAggregateIterable(List.of(new Document("_id", "other"), new Document("_id", "doc_1")));
    AggregateIterable<Document> searchResult2 =
        mockAggregateIterable(List.of(new Document("_id", "doc_2")));

    when(mongoCollection.aggregate(List.of(searchStage1))).thenReturn(searchResult1);
    when(mongoCollection.aggregate(List.of(searchStage2))).thenReturn(searchResult2);

    AtlasSearchIndexValuesSyncValidationStrategy tested =
        new AtlasSearchIndexValuesSyncValidationStrategy(50, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(context);

    // THEN
    assertTrue(result);
  }

  @Test
  void shouldReturnTrueWhenAllDocumentsForMultipleValidationPropertiesAreFoundInSearchResults() {
    // GIVEN
    AtlasSearchIndexValidationContext multiPropertyContext =
        AtlasSearchIndexValidationContext.builder()
            .withMongoClient(mongoClient)
            .withDatabase("testdb")
            .withCollection("items")
            .withIndexName("my_index")
            .withValidationProperty("field1", "field2")
            .build();

    Document doc1 = new Document("_id", "doc_1").append("field1", "val1").append("field2", "abc");
    Document doc2 = new Document("_id", "doc_2").append("field2", "def");

    FindIterable<Document> findField1 = mockFindIterable(List.of(doc1));
    FindIterable<Document> findField2 = mockFindIterable(List.of(doc1, doc2));
    when(mongoCollection.find(new Document("field1", new Document("$exists", true))))
        .thenReturn(findField1);
    when(mongoCollection.find(new Document("field2", new Document("$exists", true))))
        .thenReturn(findField2);

    Document searchStageField1Doc1 =
        new Document(
            "$search",
            new Document("index", "my_index")
                .append("text", new Document("query", "val1").append("path", "field1")));
    Document searchStageField2Doc1 =
        new Document(
            "$search",
            new Document("index", "my_index")
                .append("text", new Document("query", "abc").append("path", "field2")));
    Document searchStageField2Doc2 =
        new Document(
            "$search",
            new Document("index", "my_index")
                .append("text", new Document("query", "def").append("path", "field2")));

    AggregateIterable<Document> resultField1Doc1 =
        mockAggregateIterable(List.of(new Document("_id", "doc_1")));
    AggregateIterable<Document> resultField2Doc1 =
        mockAggregateIterable(List.of(new Document("_id", "doc_1")));
    AggregateIterable<Document> resultField2Doc2 =
        mockAggregateIterable(List.of(new Document("_id", "doc_2")));

    when(mongoCollection.aggregate(List.of(searchStageField1Doc1))).thenReturn(resultField1Doc1);
    when(mongoCollection.aggregate(List.of(searchStageField2Doc1))).thenReturn(resultField2Doc1);
    when(mongoCollection.aggregate(List.of(searchStageField2Doc2))).thenReturn(resultField2Doc2);

    AtlasSearchIndexValuesSyncValidationStrategy tested =
        new AtlasSearchIndexValuesSyncValidationStrategy(50, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(multiPropertyContext);

    // THEN
    assertTrue(result);
  }

  @Test
  void shouldReturnFalseWhenAnyValidationPropertyDocumentIsMissingInMultiPropertyArray() {
    // GIVEN
    AtlasSearchIndexValidationContext multiPropertyContext =
        AtlasSearchIndexValidationContext.builder()
            .withMongoClient(mongoClient)
            .withDatabase("testdb")
            .withCollection("items")
            .withIndexName("my_index")
            .withValidationProperty("field1", "field2")
            .build();

    Document doc1 = new Document("_id", "doc_1").append("field1", "val1").append("field2", "abc");

    FindIterable<Document> findField1 = mockFindIterable(List.of(doc1));
    FindIterable<Document> findField2 = mockFindIterable(List.of(doc1));
    when(mongoCollection.find(new Document("field1", new Document("$exists", true))))
        .thenReturn(findField1);
    when(mongoCollection.find(new Document("field2", new Document("$exists", true))))
        .thenReturn(findField2);

    Document searchStageField1Doc1 =
        new Document(
            "$search",
            new Document("index", "my_index")
                .append("text", new Document("query", "val1").append("path", "field1")));
    Document searchStageField2Doc1 =
        new Document(
            "$search",
            new Document("index", "my_index")
                .append("text", new Document("query", "abc").append("path", "field2")));

    AggregateIterable<Document> resultField1Doc1 =
        mockAggregateIterable(List.of(new Document("_id", "doc_1")));
    AggregateIterable<Document> resultField2Doc1Mismatch =
        mockAggregateIterable(List.of(new Document("_id", "other_doc")));

    when(mongoCollection.aggregate(List.of(searchStageField1Doc1))).thenReturn(resultField1Doc1);
    when(mongoCollection.aggregate(List.of(searchStageField2Doc1)))
        .thenReturn(resultField2Doc1Mismatch);

    AtlasSearchIndexValuesSyncValidationStrategy tested =
        new AtlasSearchIndexValuesSyncValidationStrategy(30, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(multiPropertyContext);

    // THEN
    assertFalse(result);
  }

  @Test
  void shouldReturnTrueWhenDocumentAppearsInSearchResultsOnSubsequentPoll() {
    // GIVEN
    Document doc1 = new Document("_id", "doc_1").append("field1", "val1");
    FindIterable<Document> findIterable = mockFindIterable(List.of(doc1));
    when(mongoCollection.find(new Document("field1", new Document("$exists", true))))
        .thenReturn(findIterable);

    Document searchStage1 =
        new Document(
            "$search",
            new Document("index", "my_index")
                .append("text", new Document("query", "val1").append("path", "field1")));

    AggregateIterable<Document> emptyResult = mockAggregateIterable(List.of());
    AggregateIterable<Document> foundResult =
        mockAggregateIterable(List.of(new Document("_id", "doc_1")));
    when(mongoCollection.aggregate(List.of(searchStage1))).thenReturn(emptyResult, foundResult);

    AtlasSearchIndexValuesSyncValidationStrategy tested =
        new AtlasSearchIndexValuesSyncValidationStrategy(200, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(context);

    // THEN
    assertTrue(result);
  }

  @Test
  void shouldReturnFalseWhenExpectedDocumentIdIsMissingFromSearchResults() {
    // GIVEN
    Document doc1 = new Document("_id", "doc_1").append("field1", "val1");
    Document doc2 = new Document("_id", "doc_2").append("field1", "val2");
    FindIterable<Document> findIterable = mockFindIterable(List.of(doc1, doc2));
    when(mongoCollection.find(new Document("field1", new Document("$exists", true))))
        .thenReturn(findIterable);

    Document searchStage1 =
        new Document(
            "$search",
            new Document("index", "my_index")
                .append("text", new Document("query", "val1").append("path", "field1")));
    Document searchStage2 =
        new Document(
            "$search",
            new Document("index", "my_index")
                .append("text", new Document("query", "val2").append("path", "field1")));

    AggregateIterable<Document> searchResult1 =
        mockAggregateIterable(List.of(new Document("_id", "doc_1")));
    AggregateIterable<Document> searchResult2 =
        mockAggregateIterable(List.of(new Document("_id", "wrong_id")));

    when(mongoCollection.aggregate(List.of(searchStage1))).thenReturn(searchResult1);
    when(mongoCollection.aggregate(List.of(searchStage2))).thenReturn(searchResult2);

    AtlasSearchIndexValuesSyncValidationStrategy tested =
        new AtlasSearchIndexValuesSyncValidationStrategy(30, MILLISECONDS, 10, MILLISECONDS);

    // WHEN
    boolean result = tested.isSync(context);

    // THEN
    assertFalse(result);
  }

  @Test
  void shouldUseProvidedValidationConfigWhenCheckingValues() {
    // GIVEN
    Document doc1 = new Document("_id", "doc_1").append("field1", "val1");
    FindIterable<Document> findIterable = mockFindIterable(List.of(doc1));
    when(mongoCollection.find(new Document("field1", new Document("$exists", true))))
        .thenReturn(findIterable);

    Document searchStage1 =
        new Document(
            "$search",
            new Document("index", "my_index")
                .append("text", new Document("query", "val1").append("path", "field1")));

    AggregateIterable<Document> emptyResult = mockAggregateIterable(List.of());
    AggregateIterable<Document> foundResult =
        mockAggregateIterable(List.of(new Document("_id", "doc_1")));
    when(mongoCollection.aggregate(List.of(searchStage1))).thenReturn(emptyResult, foundResult);

    AtlasSearchIndexValuesSyncValidationStrategy tested =
        new AtlasSearchIndexValuesSyncValidationStrategy();
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
  private FindIterable<Document> mockFindIterable(List<Document> documents) {
    FindIterable<Document> iterable = mock(FindIterable.class);
    when(iterable.into(any()))
        .thenAnswer(
            invocation -> {
              Collection<Document> target = invocation.getArgument(0);
              target.addAll(documents);
              return target;
            });
    return iterable;
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
