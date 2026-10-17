package com.github.starnowski.jamolingo.junit5;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

class AtlasSearchIndexIntegrationTest {

  private static final String DATABASE_NAME = "testdb_search";
  private static final String COLLECTION_NAME = "items";
  private static final String KEYWORD_INDEX_PATH = "bson/search/keyword_lowercase_index.json";
  private static final String NGRAM_INDEX_PATH = "bson/search/custom_ngram_index.json";
  private static final String DOC1_PATH = "bson/search/doc1.json";
  private static final String DOC2_PATH = "bson/search/doc2.json";
  private static final String DOC3_WITHOUT_FIELD1_PATH = "bson/search/doc3_without_field1.json";

  private static GenericContainer<?> mongoAtlasContainer;
  private static MongoClient mongoClient;

  @BeforeAll
  static void startContainer() {
    mongoAtlasContainer =
        new GenericContainer<>("mongodb/mongodb-atlas-local:7.0.11")
            .withPrivilegedMode(true)
            .withCreateContainerCmdModifier(
                cmd -> {
                  cmd.getHostConfig().withMemory(4 * 1024 * 1024 * 1024L);
                  cmd.getHostConfig().withShmSize(2 * 1024 * 1024 * 1024L);
                })
            .withExposedPorts(27017, 27027)
            .withEnv("MONGOT_LOG_FILE", "/dev/stdout")
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)));

    mongoAtlasContainer.start();
    Wait.forLogMessage(".*Starting TCP server.*", 2)
        .withStartupTimeout(Duration.ofSeconds(30))
        .waitUntilReady(mongoAtlasContainer);
    Wait.forLogMessage(".*Starting message server.*", 2)
        .withStartupTimeout(Duration.ofSeconds(30))
        .waitUntilReady(mongoAtlasContainer);

    String connectionString =
        String.format(
            "mongodb://%s:%d/?directConnection=true",
            mongoAtlasContainer.getHost(), mongoAtlasContainer.getMappedPort(27017));
    mongoClient = MongoClients.create(connectionString);
  }

  @AfterAll
  static void stopContainer() {
    if (mongoClient != null) {
      mongoClient.close();
    }
    if (mongoAtlasContainer != null) {
      mongoAtlasContainer.stop();
    }
  }

  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = DATABASE_NAME,
            collection = COLLECTION_NAME,
            bsonFilePath = DOC1_PATH),
        @MongoDocument(
            database = DATABASE_NAME,
            collection = COLLECTION_NAME,
            bsonFilePath = DOC2_PATH),
        @MongoDocument(
            database = DATABASE_NAME,
            collection = COLLECTION_NAME,
            bsonFilePath = DOC3_WITHOUT_FIELD1_PATH)
      },
      atlasSearchIndexes = {
        @AtlasSearchIndex(
            name = "search_idx",
            database = DATABASE_NAME,
            collection = COLLECTION_NAME,
            bsonFilePath = KEYWORD_INDEX_PATH,
            validationProperty = "field1",
            validationStrategies = {
              AtlasSearchIndexReadinessSyncValidationStrategy.class,
              AtlasSearchIndexDocumentCountSyncValidationStrategy.class,
              AtlasSearchIndexValuesSyncValidationStrategy.class
            })
      })
  private void methodWithKeywordSearchIndex() {}

  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = DATABASE_NAME,
            collection = COLLECTION_NAME,
            bsonFilePath = DOC1_PATH),
        @MongoDocument(
            database = DATABASE_NAME,
            collection = COLLECTION_NAME,
            bsonFilePath = DOC2_PATH),
        @MongoDocument(
            database = DATABASE_NAME,
            collection = COLLECTION_NAME,
            bsonFilePath = DOC3_WITHOUT_FIELD1_PATH)
      },
      atlasSearchIndexes = {
        @AtlasSearchIndex(
            name = "search_idx",
            database = DATABASE_NAME,
            collection = COLLECTION_NAME,
            bsonFilePath = NGRAM_INDEX_PATH,
            validationProperty = "field1",
            validationStrategies = {
              AtlasSearchIndexReadinessSyncValidationStrategy.class,
              AtlasSearchIndexDocumentCountSyncValidationStrategy.class,
              AtlasSearchIndexValuesSyncValidationStrategy.class
            })
      })
  private void methodWithNgramSearchIndex() {}

  @Test
  void shouldValidateStrategiesUsingAwaitility() throws IOException {
    // GIVEN
    String collectionName = "direct_strategy_items";
    String indexName = "direct_keyword_idx";
    MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
    database.getCollection(collectionName).drop();
    database.createCollection(collectionName);
    MongoCollection<Document> collection = database.getCollection(collectionName);

    String indexDef = readResource(KEYWORD_INDEX_PATH);
    collection.createSearchIndex(indexName, Document.parse(indexDef));

    collection.insertMany(
        List.of(
            Document.parse(readResource(DOC1_PATH)),
            Document.parse(readResource(DOC2_PATH)),
            Document.parse(readResource(DOC3_WITHOUT_FIELD1_PATH))));

    AtlasSearchIndexValidationContext context =
        AtlasSearchIndexValidationContext.builder()
            .withMongoClient(mongoClient)
            .withDatabase(DATABASE_NAME)
            .withCollection(collectionName)
            .withIndexName(indexName)
            .withValidationProperty("field1")
            .build();

    AtlasSearchIndexReadinessSyncValidationStrategy readinessStrategy =
        new AtlasSearchIndexReadinessSyncValidationStrategy(100, MILLISECONDS, 50, MILLISECONDS);
    AtlasSearchIndexDocumentCountSyncValidationStrategy countStrategy =
        new AtlasSearchIndexDocumentCountSyncValidationStrategy(
            100, MILLISECONDS, 50, MILLISECONDS);
    AtlasSearchIndexValuesSyncValidationStrategy valuesStrategy =
        new AtlasSearchIndexValuesSyncValidationStrategy(100, MILLISECONDS, 50, MILLISECONDS);

    // WHEN & THEN
    await()
        .atMost(30, SECONDS)
        .pollInterval(1, SECONDS)
        .until(() -> readinessStrategy.isSync(context));
    await().atMost(30, SECONDS).pollInterval(1, SECONDS).until(() -> countStrategy.isSync(context));
    await()
        .atMost(30, SECONDS)
        .pollInterval(1, SECONDS)
        .until(() -> valuesStrategy.isSync(context));
  }

  @Test
  void shouldCreateCacheAndRecreateAtlasSearchIndexViaMongoSetupAnnotation() throws Exception {
    // GIVEN
    Map<AtlasSearchIndexKey, CachedAtlasSearchIndexDefinition> cache = new HashMap<>();
    AbstractMongoDataLoaderExtension extension =
        new AbstractMongoDataLoaderExtension(cache) {
          @Override
          protected MongoClient resolveMongoClient() {
            return mongoClient;
          }
        };
    MongoCollection<Document> collection =
        mongoClient.getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME);
    AtlasSearchIndexKey key = new AtlasSearchIndexKey(DATABASE_NAME, COLLECTION_NAME, "search_idx");

    // WHEN 1: First run with keyword_lowercase_index.json
    extension.beforeEach(mockExtensionContext("methodWithKeywordSearchIndex"));

    // THEN 1: Index is cached and keyword search works immediately
    CachedAtlasSearchIndexDefinition firstCached = cache.get(key);
    assertNotNull(firstCached);
    assertEquals(readResource(KEYWORD_INDEX_PATH), firstCached.getDefValue());

    List<Document> keywordResults = new ArrayList<>();
    collection
        .aggregate(
            List.of(
                new Document(
                    "$search",
                    new Document("index", "search_idx")
                        .append(
                            "text", new Document("query", "START123").append("path", "field1")))))
        .into(keywordResults);
    assertEquals(1, keywordResults.size());
    assertEquals("doc_2", keywordResults.get(0).getString("_id"));

    // WHEN 2: Second run with same keyword_lowercase_index.json (cached index is reused)
    extension.beforeEach(mockExtensionContext("methodWithKeywordSearchIndex"));
    assertEquals(firstCached, cache.get(key));

    // WHEN 3: Run with custom_ngram_index.json on same index name (index is recreated and cache
    // updated)
    extension.beforeEach(mockExtensionContext("methodWithNgramSearchIndex"));

    // THEN 3: Cache is updated to custom_ngram_index.json and n-gram substring search works
    // immediately
    CachedAtlasSearchIndexDefinition secondCached = cache.get(key);
    assertNotNull(secondCached);
    assertEquals(readResource(NGRAM_INDEX_PATH), secondCached.getDefValue());

    List<Document> ngramResults = new ArrayList<>();
    collection
        .aggregate(
            List.of(
                new Document(
                    "$search",
                    new Document("index", "search_idx")
                        .append("text", new Document("query", "art12").append("path", "field1")))))
        .into(ngramResults);
    Set<String> matchingIds =
        ngramResults.stream().map(d -> d.getString("_id")).collect(Collectors.toSet());
    assertTrue(matchingIds.contains("doc_2"));
  }

  private ExtensionContext mockExtensionContext(String methodName) throws NoSuchMethodException {
    ExtensionContext extensionContext = mock(ExtensionContext.class);
    when(extensionContext.getTestMethod())
        .thenReturn(Optional.of(this.getClass().getDeclaredMethod(methodName)));
    return extensionContext;
  }

  private String readResource(String path) throws IOException {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
      if (is == null) {
        throw new IllegalArgumentException("Resource not found: " + path);
      }
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
