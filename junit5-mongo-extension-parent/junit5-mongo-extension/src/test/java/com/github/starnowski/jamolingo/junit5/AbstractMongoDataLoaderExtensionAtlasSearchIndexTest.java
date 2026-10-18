package com.github.starnowski.jamolingo.junit5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mongodb.client.ListSearchIndexesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

class AbstractMongoDataLoaderExtensionAtlasSearchIndexTest {

  private static final String KEYWORD_INDEX_PATH = "bson/search/keyword_lowercase_index.json";
  private static final String NGRAM_INDEX_PATH = "bson/search/custom_ngram_index.json";
  private static final String DOC1_PATH = "bson/search/doc1.json";

  private static final List<String> EXECUTION_LOG = new ArrayList<>();
  private static final List<AtlasSearchIndexValidationConfig> CAPTURED_CONFIGS = new ArrayList<>();

  private MongoClient mongoClient;
  private MongoDatabase mongoDatabase;
  private MongoCollection<Document> mongoCollection;
  private Map<AtlasSearchIndexKey, CachedAtlasSearchIndexDefinition> cache;
  private AbstractMongoDataLoaderExtension tested;

  @BeforeEach
  void setUp() {
    EXECUTION_LOG.clear();
    CAPTURED_CONFIGS.clear();
    mongoClient = mock(MongoClient.class);
    mongoDatabase = mock(MongoDatabase.class);
    mongoCollection = mock(MongoCollection.class);
    when(mongoClient.getDatabase("testdb")).thenReturn(mongoDatabase);
    when(mongoDatabase.getCollection("items")).thenReturn(mongoCollection);
    when(mongoCollection.withWriteConcern(any())).thenReturn(mongoCollection);

    cache = new HashMap<>();
    tested =
        new AbstractMongoDataLoaderExtension(cache) {
          @Override
          protected MongoClient resolveMongoClient() {
            return mongoClient;
          }
        };
  }

  @MongoSetup(
      atlasSearchIndexes = {
        @AtlasSearchIndex(
            name = "idx_test",
            database = "testdb",
            collection = "items",
            bsonFilePath = KEYWORD_INDEX_PATH)
      })
  private void setupWithKeywordIndex() {}

  @MongoSetup(
      atlasSearchIndexes = {
        @AtlasSearchIndex(
            name = "idx_test",
            database = "testdb",
            collection = "items",
            bsonFilePath = NGRAM_INDEX_PATH)
      })
  private void setupWithNgramIndex() {}

  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "testdb", collection = "items", bsonFilePath = DOC1_PATH)
      },
      atlasSearchIndexes = {
        @AtlasSearchIndex(
            name = "idx_test",
            database = "testdb",
            collection = "items",
            bsonFilePath = KEYWORD_INDEX_PATH,
            validationProperty = "field1",
            validationStrategies = {
              FirstRecordingStrategy.class,
              SecondRecordingStrategy.class,
              ThirdRecordingStrategy.class
            })
      })
  private void setupWithOrderedValidationStrategies() {}

  @MongoSetup(
      atlasSearchIndexes = {
        @AtlasSearchIndex(
            name = "idx_test",
            database = "testdb",
            collection = "items",
            bsonFilePath = KEYWORD_INDEX_PATH,
            validationProperty = "field1",
            validationStrategies = {
              FirstRecordingStrategy.class,
              FailingValidationStrategy.class,
              ThirdRecordingStrategy.class
            })
      })
  private void setupWithFailingValidationStrategy() {}

  @AtlasSearchIndexValidation(
      maxWaitDuration = 60L,
      maxWaitUnit = TimeUnit.SECONDS,
      pollInterval = 250L,
      pollIntervalUnit = TimeUnit.MILLISECONDS)
  static class ClassAnnotatedTestSample {

    @MongoSetup(
        atlasSearchIndexes = {
          @AtlasSearchIndex(
              name = "idx_test",
              database = "testdb",
              collection = "items",
              bsonFilePath = KEYWORD_INDEX_PATH,
              validationProperty = "field1",
              validationStrategies = {FirstRecordingStrategy.class})
        })
    void methodInheritingClassValidationConfig() {}

    @AtlasSearchIndexValidation(
        maxWaitDuration = 12L,
        maxWaitUnit = TimeUnit.MINUTES,
        pollInterval = 50L,
        pollIntervalUnit = TimeUnit.MILLISECONDS)
    @MongoSetup(
        atlasSearchIndexes = {
          @AtlasSearchIndex(
              name = "idx_test",
              database = "testdb",
              collection = "items",
              bsonFilePath = KEYWORD_INDEX_PATH,
              validationProperty = "field1",
              validationStrategies = {FirstRecordingStrategy.class})
        })
    void methodOverridingClassValidationConfig() {}
  }

  @Test
  void shouldCreateIndexAndCacheDefinitionOnFirstRun() throws Exception {
    // GIVEN
    Document createdIndexDoc =
        new Document("name", "idx_test")
            .append("status", "READY")
            .append("latestDefinition", Document.parse(readResource(KEYWORD_INDEX_PATH)));
    ListSearchIndexesIterable<Document> emptyIterable = mockSearchIndexesIterable(List.of());
    ListSearchIndexesIterable<Document> afterCreateIterable =
        mockSearchIndexesIterable(List.of(createdIndexDoc));
    when(mongoCollection.listSearchIndexes()).thenReturn(emptyIterable, afterCreateIterable);

    ExtensionContext extensionContext = mockExtensionContext("setupWithKeywordIndex");

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    String expectedDefValue = readResource(KEYWORD_INDEX_PATH);
    verify(mongoCollection, times(1))
        .createSearchIndex(eq("idx_test"), eq(Document.parse(expectedDefValue)));

    AtlasSearchIndexKey expectedKey = new AtlasSearchIndexKey("testdb", "items", "idx_test");
    CachedAtlasSearchIndexDefinition cachedDef = cache.get(expectedKey);
    assertNotNull(cachedDef);
    assertEquals(expectedDefValue, cachedDef.getDefValue());
    assertEquals(createdIndexDoc.toJson(), cachedDef.getStateValue());
  }

  @Test
  void shouldSkipIndexCreationWhenCachedDefValueAndDatabaseStateValueMatch() throws Exception {
    // GIVEN
    String defValue = readResource(KEYWORD_INDEX_PATH);
    Document existingIndexDoc =
        new Document("name", "idx_test")
            .append("status", "READY")
            .append("latestDefinition", Document.parse(defValue));
    String stateValue = existingIndexDoc.toJson();

    AtlasSearchIndexKey key = new AtlasSearchIndexKey("testdb", "items", "idx_test");
    cache.put(key, new CachedAtlasSearchIndexDefinition(defValue, stateValue));

    ListSearchIndexesIterable<Document> existingIterable =
        mockSearchIndexesIterable(List.of(existingIndexDoc));
    when(mongoCollection.listSearchIndexes()).thenReturn(existingIterable);

    ExtensionContext extensionContext = mockExtensionContext("setupWithKeywordIndex");

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    verify(mongoCollection, never()).createSearchIndex(any(), any());
    assertEquals(new CachedAtlasSearchIndexDefinition(defValue, stateValue), cache.get(key));
  }

  @Test
  void shouldRecreateIndexWhenDatabaseStateValueDiffersFromCachedStateValueEvenIfDefValueMatches()
      throws Exception {
    // GIVEN
    String defValue = readResource(KEYWORD_INDEX_PATH);
    Document cachedIndexDoc =
        new Document("name", "idx_test").append("status", "READY").append("version", 1);
    Document currentDbIndexDoc =
        new Document("name", "idx_test").append("status", "READY").append("version", 2);
    Document newlyCreatedIndexDoc =
        new Document("name", "idx_test").append("status", "READY").append("version", 3);

    AtlasSearchIndexKey key = new AtlasSearchIndexKey("testdb", "items", "idx_test");
    cache.put(key, new CachedAtlasSearchIndexDefinition(defValue, cachedIndexDoc.toJson()));

    ListSearchIndexesIterable<Document> beforeCreateIterable =
        mockSearchIndexesIterable(List.of(currentDbIndexDoc));
    ListSearchIndexesIterable<Document> afterCreateIterable =
        mockSearchIndexesIterable(List.of(newlyCreatedIndexDoc));
    when(mongoCollection.listSearchIndexes()).thenReturn(beforeCreateIterable, afterCreateIterable);

    ExtensionContext extensionContext = mockExtensionContext("setupWithKeywordIndex");

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    verify(mongoCollection, times(1))
        .createSearchIndex(eq("idx_test"), eq(Document.parse(defValue)));
    assertEquals(
        new CachedAtlasSearchIndexDefinition(defValue, newlyCreatedIndexDoc.toJson()),
        cache.get(key));
  }

  @Test
  void shouldRecreateIndexAndUpdateCacheWhenDefValueChanges() throws Exception {
    // GIVEN
    String oldDefValue = readResource(KEYWORD_INDEX_PATH);
    String newDefValue = readResource(NGRAM_INDEX_PATH);
    Document oldIndexDoc =
        new Document("name", "idx_test")
            .append("status", "READY")
            .append("latestDefinition", Document.parse(oldDefValue));
    Document newIndexDoc =
        new Document("name", "idx_test")
            .append("status", "READY")
            .append("latestDefinition", Document.parse(newDefValue));

    AtlasSearchIndexKey key = new AtlasSearchIndexKey("testdb", "items", "idx_test");
    cache.put(key, new CachedAtlasSearchIndexDefinition(oldDefValue, oldIndexDoc.toJson()));

    ListSearchIndexesIterable<Document> beforeCreateIterable =
        mockSearchIndexesIterable(List.of(oldIndexDoc));
    ListSearchIndexesIterable<Document> afterCreateIterable =
        mockSearchIndexesIterable(List.of(newIndexDoc));
    when(mongoCollection.listSearchIndexes()).thenReturn(beforeCreateIterable, afterCreateIterable);

    ExtensionContext extensionContext = mockExtensionContext("setupWithNgramIndex");

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    verify(mongoCollection, times(1))
        .createSearchIndex(eq("idx_test"), eq(Document.parse(newDefValue)));
    assertEquals(
        new CachedAtlasSearchIndexDefinition(newDefValue, newIndexDoc.toJson()), cache.get(key));
  }

  @Test
  void shouldExecuteValidationStrategiesInExactDeclaredOrderAfterLoadingDocuments()
      throws Exception {
    // GIVEN
    Document createdIndexDoc = new Document("name", "idx_test").append("status", "READY");
    ListSearchIndexesIterable<Document> emptyIterable = mockSearchIndexesIterable(List.of());
    ListSearchIndexesIterable<Document> afterCreateIterable =
        mockSearchIndexesIterable(List.of(createdIndexDoc));
    when(mongoCollection.listSearchIndexes())
        .thenReturn(emptyIterable, afterCreateIterable, afterCreateIterable);
    when(mongoCollection.insertOne(any()))
        .thenAnswer(
            inv -> {
              EXECUTION_LOG.add("insertOne");
              return null;
            });

    ExtensionContext extensionContext =
        mockExtensionContext("setupWithOrderedValidationStrategies");

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    assertEquals(
        List.of(
            "insertOne",
            "FirstRecordingStrategy",
            "SecondRecordingStrategy",
            "ThirdRecordingStrategy"),
        EXECUTION_LOG);
  }

  @Test
  void shouldThrowExceptionAndStopWhenValidationStrategyReturnsFalse() throws Exception {
    // GIVEN
    Document createdIndexDoc = new Document("name", "idx_test").append("status", "READY");
    ListSearchIndexesIterable<Document> emptyIterable = mockSearchIndexesIterable(List.of());
    ListSearchIndexesIterable<Document> afterCreateIterable =
        mockSearchIndexesIterable(List.of(createdIndexDoc));
    when(mongoCollection.listSearchIndexes()).thenReturn(emptyIterable, afterCreateIterable);

    ExtensionContext extensionContext = mockExtensionContext("setupWithFailingValidationStrategy");

    // WHEN & THEN
    assertThrows(IllegalStateException.class, () -> tested.beforeEach(extensionContext));
    assertEquals(List.of("FirstRecordingStrategy", "FailingValidationStrategy"), EXECUTION_LOG);
  }

  @Test
  void shouldPassDefaultValidationConfigWhenNoAnnotationPresent() throws Exception {
    // GIVEN
    Document createdIndexDoc = new Document("name", "idx_test").append("status", "READY");
    ListSearchIndexesIterable<Document> emptyIterable = mockSearchIndexesIterable(List.of());
    ListSearchIndexesIterable<Document> afterCreateIterable =
        mockSearchIndexesIterable(List.of(createdIndexDoc));
    when(mongoCollection.listSearchIndexes())
        .thenReturn(emptyIterable, afterCreateIterable, afterCreateIterable);

    ExtensionContext extensionContext =
        mockExtensionContext("setupWithOrderedValidationStrategies");

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    assertEquals(1, CAPTURED_CONFIGS.size());
    assertEquals(AtlasSearchIndexValidationConfig.builder().build(), CAPTURED_CONFIGS.get(0));
  }

  @Test
  void shouldPassClassLevelValidationConfigToStrategy() throws Exception {
    // GIVEN
    Document createdIndexDoc = new Document("name", "idx_test").append("status", "READY");
    ListSearchIndexesIterable<Document> emptyIterable = mockSearchIndexesIterable(List.of());
    ListSearchIndexesIterable<Document> afterCreateIterable =
        mockSearchIndexesIterable(List.of(createdIndexDoc));
    when(mongoCollection.listSearchIndexes())
        .thenReturn(emptyIterable, afterCreateIterable, afterCreateIterable);

    ExtensionContext extensionContext =
        mockExtensionContext(
            ClassAnnotatedTestSample.class, "methodInheritingClassValidationConfig");

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    assertEquals(1, CAPTURED_CONFIGS.size());
    AtlasSearchIndexValidationConfig expected =
        AtlasSearchIndexValidationConfig.builder()
            .withMaxWaitDuration(60L)
            .withMaxWaitUnit(TimeUnit.SECONDS)
            .withPollInterval(250L)
            .withPollIntervalUnit(TimeUnit.MILLISECONDS)
            .build();
    assertEquals(expected, CAPTURED_CONFIGS.get(0));
  }

  @Test
  void shouldOverrideClassLevelWithMethodLevelValidationConfig() throws Exception {
    // GIVEN
    Document createdIndexDoc = new Document("name", "idx_test").append("status", "READY");
    ListSearchIndexesIterable<Document> emptyIterable = mockSearchIndexesIterable(List.of());
    ListSearchIndexesIterable<Document> afterCreateIterable =
        mockSearchIndexesIterable(List.of(createdIndexDoc));
    when(mongoCollection.listSearchIndexes())
        .thenReturn(emptyIterable, afterCreateIterable, afterCreateIterable);

    ExtensionContext extensionContext =
        mockExtensionContext(
            ClassAnnotatedTestSample.class, "methodOverridingClassValidationConfig");

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    assertEquals(1, CAPTURED_CONFIGS.size());
    AtlasSearchIndexValidationConfig expected =
        AtlasSearchIndexValidationConfig.builder()
            .withMaxWaitDuration(12L)
            .withMaxWaitUnit(TimeUnit.MINUTES)
            .withPollInterval(50L)
            .withPollIntervalUnit(TimeUnit.MILLISECONDS)
            .build();
    assertEquals(expected, CAPTURED_CONFIGS.get(0));
  }

  @Test
  void shouldUseCustomDefaultValidationConfigWhenNoAnnotationPresent() throws Exception {
    // GIVEN
    AtlasSearchIndexValidationConfig customDefault =
        AtlasSearchIndexValidationConfig.builder()
            .withMaxWaitDuration(45L)
            .withMaxWaitUnit(TimeUnit.SECONDS)
            .withPollInterval(500L)
            .withPollIntervalUnit(TimeUnit.MILLISECONDS)
            .withProperties(Map.of("env", "ci"))
            .build();
    AbstractMongoDataLoaderExtension customTested =
        new AbstractMongoDataLoaderExtension(cache, customDefault) {
          @Override
          protected MongoClient resolveMongoClient() {
            return mongoClient;
          }
        };

    Document createdIndexDoc = new Document("name", "idx_test").append("status", "READY");
    ListSearchIndexesIterable<Document> emptyIterable = mockSearchIndexesIterable(List.of());
    ListSearchIndexesIterable<Document> afterCreateIterable =
        mockSearchIndexesIterable(List.of(createdIndexDoc));
    when(mongoCollection.listSearchIndexes())
        .thenReturn(emptyIterable, afterCreateIterable, afterCreateIterable);

    ExtensionContext extensionContext =
        mockExtensionContext("setupWithOrderedValidationStrategies");

    // WHEN
    customTested.beforeEach(extensionContext);

    // THEN
    assertEquals(1, CAPTURED_CONFIGS.size());
    assertEquals(customDefault, CAPTURED_CONFIGS.get(0));
  }

  private ExtensionContext mockExtensionContext(String methodName) throws NoSuchMethodException {
    return mockExtensionContext(this.getClass(), methodName);
  }

  private ExtensionContext mockExtensionContext(Class<?> testClass, String methodName)
      throws NoSuchMethodException {
    ExtensionContext extensionContext = mock(ExtensionContext.class);
    when(extensionContext.getTestClass()).thenReturn(Optional.of(testClass));
    when(extensionContext.getTestMethod())
        .thenReturn(Optional.of(testClass.getDeclaredMethod(methodName)));
    return extensionContext;
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

  private String readResource(String path) throws IOException {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
      if (is == null) {
        throw new IllegalArgumentException("Resource not found: " + path);
      }
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  public static class FirstRecordingStrategy implements AtlasSearchIndexSyncValidationStrategy {
    public FirstRecordingStrategy() {}

    @Override
    public boolean isSync(
        AtlasSearchIndexValidationContext context, AtlasSearchIndexValidationConfig config) {
      assertEquals("testdb", context.getDatabase());
      assertEquals("items", context.getCollection());
      assertEquals("idx_test", context.getIndexName());
      assertEquals("field1", context.getValidationProperty());
      CAPTURED_CONFIGS.add(config);
      EXECUTION_LOG.add("FirstRecordingStrategy");
      return true;
    }
  }

  public static class SecondRecordingStrategy implements AtlasSearchIndexSyncValidationStrategy {
    public SecondRecordingStrategy() {}

    @Override
    public boolean isSync(
        AtlasSearchIndexValidationContext context, AtlasSearchIndexValidationConfig config) {
      EXECUTION_LOG.add("SecondRecordingStrategy");
      return true;
    }
  }

  public static class ThirdRecordingStrategy implements AtlasSearchIndexSyncValidationStrategy {
    public ThirdRecordingStrategy() {}

    @Override
    public boolean isSync(
        AtlasSearchIndexValidationContext context, AtlasSearchIndexValidationConfig config) {
      EXECUTION_LOG.add("ThirdRecordingStrategy");
      return true;
    }
  }

  public static class FailingValidationStrategy implements AtlasSearchIndexSyncValidationStrategy {
    public FailingValidationStrategy() {}

    @Override
    public boolean isSync(
        AtlasSearchIndexValidationContext context, AtlasSearchIndexValidationConfig config) {
      EXECUTION_LOG.add("FailingValidationStrategy");
      return false;
    }
  }
}
