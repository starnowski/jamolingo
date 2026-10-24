package com.github.starnowski.jamolingo.junit5;

import static com.github.starnowski.jamolingo.junit5.Awaiter.await;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.mongodb.MongoCommandException;
import com.mongodb.WriteConcern;
import com.mongodb.client.ListSearchIndexesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bson.BsonType;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.json.JsonReader;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A JUnit 5 extension that loads data and configures Atlas Search indexes in MongoDB collections
 * before each test method execution.
 *
 * <p>This extension looks for the {@link MongoSetup} annotation on the test method or test class.
 * If present, it retrieves the {@link MongoClient} from the implementation, ensures any configured
 * Atlas Search indexes exist, clears the specified collections, inserts the documents defined in
 * the annotation, and executes any configured Atlas Search index synchronization validation
 * strategies. Files specified in {@link MongoDocument#bsonFilePath()} may contain either a single
 * BSON document or an array of BSON documents.
 */
public abstract class AbstractMongoDataLoaderExtension implements BeforeEachCallback {

  private final Map<AtlasSearchIndexKey, CachedAtlasSearchIndexDefinition> cachedSearchIndexes;
  private final AtlasSearchIndexValidationConfig defaultValidationConfig;

  /** Constructs a new AbstractMongoDataLoaderExtension with an empty search index cache. */
  public AbstractMongoDataLoaderExtension() {
    this(new HashMap<>(), AtlasSearchIndexValidationConfig.builder().build());
  }

  /**
   * Constructs a new AbstractMongoDataLoaderExtension with the provided search index cache map.
   *
   * @param cachedSearchIndexes the map used to cache Atlas Search index definitions
   */
  public AbstractMongoDataLoaderExtension(
      Map<AtlasSearchIndexKey, CachedAtlasSearchIndexDefinition> cachedSearchIndexes) {
    this(cachedSearchIndexes, AtlasSearchIndexValidationConfig.builder().build());
  }

  /**
   * Constructs a new AbstractMongoDataLoaderExtension with the provided search index cache map and
   * default validation configuration.
   *
   * @param cachedSearchIndexes the map used to cache Atlas Search index definitions
   * @param defaultValidationConfig the default validation configuration
   */
  public AbstractMongoDataLoaderExtension(
      Map<AtlasSearchIndexKey, CachedAtlasSearchIndexDefinition> cachedSearchIndexes,
      AtlasSearchIndexValidationConfig defaultValidationConfig) {
    this.cachedSearchIndexes = cachedSearchIndexes;
    this.defaultValidationConfig =
        defaultValidationConfig != null
            ? defaultValidationConfig
            : AtlasSearchIndexValidationConfig.builder().build();
  }

  /**
   * Returns the map of cached Atlas Search index definitions.
   *
   * @return the cache map
   */
  protected Map<AtlasSearchIndexKey, CachedAtlasSearchIndexDefinition> getCachedSearchIndexes() {
    return cachedSearchIndexes;
  }

  /**
   * Returns the default Atlas Search index validation configuration.
   *
   * @return the default validation configuration
   */
  protected AtlasSearchIndexValidationConfig getDefaultValidationConfig() {
    return defaultValidationConfig;
  }

  /**
   * Resolves the {@link MongoClient} to be used for data loading.
   *
   * @return the {@link MongoClient}
   */
  protected abstract MongoClient resolveMongoClient();

  /**
   * Executed before each test method.
   *
   * <p>Checks for the {@link MongoSetup} annotation and performs the data loading logic.
   *
   * @param context the extension context
   * @throws IllegalAccessException if accessing the test instance fails
   */
  @Override
  public void beforeEach(ExtensionContext context) throws IllegalAccessException {
    MongoSetup annotation =
        context.getTestMethod().stream()
            .map(t -> t.getAnnotation(MongoSetup.class))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    if (annotation == null) {
      annotation =
          context.getTestClass().stream()
              .map(t -> t.getAnnotation(MongoSetup.class))
              .filter(Objects::nonNull)
              .findFirst()
              .orElse(null);
    }
    AtlasSearchIndexValidationConfig validationConfig = resolveValidationConfig(context);
    loadData(annotation, validationConfig);
  }

  /**
   * Resolves the {@link AtlasSearchIndexValidationConfig} for the current test execution context.
   *
   * @param context the extension context
   * @return the resolved validation configuration
   */
  protected AtlasSearchIndexValidationConfig resolveValidationConfig(ExtensionContext context) {
    if (context != null) {
      AtlasSearchIndexValidation validationAnnotation =
          context.getTestMethod().stream()
              .map(t -> t.getAnnotation(AtlasSearchIndexValidation.class))
              .filter(Objects::nonNull)
              .findFirst()
              .orElse(null);
      if (validationAnnotation == null) {
        validationAnnotation =
            context.getTestClass().stream()
                .map(t -> t.getAnnotation(AtlasSearchIndexValidation.class))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
      }
      if (validationAnnotation != null) {
        return AtlasSearchIndexValidationConfig.builder()
            .withMaxWaitDuration(validationAnnotation.maxWaitDuration())
            .withMaxWaitUnit(validationAnnotation.maxWaitUnit())
            .withPollInterval(validationAnnotation.pollInterval())
            .withPollIntervalUnit(validationAnnotation.pollIntervalUnit())
            .build();
      }
    }
    return defaultValidationConfig;
  }

  protected void loadData(MongoSetup annotation) {
    loadData(annotation, defaultValidationConfig);
  }

  protected void loadData(
      MongoSetup annotation, AtlasSearchIndexValidationConfig validationConfig) {
    if (annotation != null) {
      MongoClient mongoClient = resolveMongoClient();

      if (mongoClient == null) {
        throw new IllegalStateException("No MongoClient provided by the extension");
      }

      Map<String, Set<String>> collections = new HashMap<>();
      Arrays.stream(annotation.mongoDocuments())
          .forEach(
              an -> {
                collections.merge(
                    an.database(),
                    new HashSet<>(Collections.singletonList(an.collection())),
                    (s1, s2) ->
                        Stream.concat(s1.stream(), s2.stream())
                            .collect(Collectors.toUnmodifiableSet()));
              });
      Map<MongoCollectionKey, MongoCollection> mongoCollectionMap = new HashMap<>();
      collections
          .entrySet()
          .forEach(
              entry -> {
                MongoDatabase database = mongoClient.getDatabase(entry.getKey());
                entry
                    .getValue()
                    .forEach(
                        cName -> {
                          MongoCollection<Document> collection =
                              mongoCollectionMap.computeIfAbsent(
                                  new MongoCollectionKey(entry.getKey(), cName),
                                  (key) -> database.getCollection(key.getCollection()));
                          collection.withWriteConcern(WriteConcern.W1); // no .withJournal(true)
                          collection.deleteMany(new Document()); // clears collection
                        });
              });

      Map<MongoCollectionKey, List<MongoDocument>> groupedDocuments =
          Arrays.stream(annotation.mongoDocuments())
              .filter(an -> !an.bsonFilePath().trim().isEmpty())
              .collect(
                  Collectors.groupingBy(
                      an -> new MongoCollectionKey(an.database(), an.collection()),
                      LinkedHashMap::new,
                      Collectors.toList()));

      if (annotation.batchInsertToCollection()) {
        groupedDocuments.forEach(
            (key, docs) -> {
              MongoCollection<Document> collection = mongoCollectionMap.get(key);
              List<Document> documentsToInsert =
                  docs.stream()
                      .flatMap(an -> parseBsonDocuments(an.bsonFilePath()).stream())
                      .collect(Collectors.toList());
              if (!documentsToInsert.isEmpty()) {
                collection.insertMany(documentsToInsert);
              }
            });
      } else {
        groupedDocuments.forEach(
            (key, docs) -> {
              MongoCollection<Document> collection = mongoCollectionMap.get(key);
              docs.forEach(
                  an -> {
                    List<Document> documents = parseBsonDocuments(an.bsonFilePath());
                    documents.forEach(collection::insertOne);
                  });
            });
      }

      AtlasSearchIndex[] searchIndexes = annotation.atlasSearchIndexes();
      if (searchIndexes != null) {
        for (AtlasSearchIndex searchIndex : searchIndexes) {
          ensureAtlasSearchIndex(mongoClient, searchIndex);
        }
      }

      if (searchIndexes != null) {
        for (AtlasSearchIndex searchIndex : searchIndexes) {
          validateAtlasSearchIndex(mongoClient, searchIndex, validationConfig);
        }
      }
    }
  }

  private void ensureAtlasSearchIndex(MongoClient mongoClient, AtlasSearchIndex searchIndex) {
    String databaseName = searchIndex.database();
    String collectionName = searchIndex.collection();
    String indexName = searchIndex.name();
    String defValue = readResourceAsString(searchIndex.bsonFilePath());

    MongoDatabase database = mongoClient.getDatabase(databaseName);
    MongoCollection<Document> collection = database.getCollection(collectionName);

    String currentStateValue = findSearchIndexStateValue(collection, indexName);
    AtlasSearchIndexKey key = new AtlasSearchIndexKey(databaseName, collectionName, indexName);
    CachedAtlasSearchIndexDefinition cached = cachedSearchIndexes.get(key);

    if (cached != null && Objects.equals(defValue, cached.getDefValue())) {
      if (currentStateValue != null && Objects.equals(cached.getStateValue(), currentStateValue)) {
        return;
      }
    }

    createOrReplaceSearchIndex(database, collection, indexName, Document.parse(defValue));
    String newStateValue = findSearchIndexStateValue(collection, indexName);
    cachedSearchIndexes.put(key, new CachedAtlasSearchIndexDefinition(defValue, newStateValue));
  }

  private void createOrReplaceSearchIndex(
      MongoDatabase database,
      MongoCollection<Document> collection,
      String indexName,
      Document indexDefinition) {
    try {
      collection.createSearchIndex(indexName, indexDefinition);
    } catch (MongoCommandException e) {
      if (e.getErrorCode() == 26) {
        database.createCollection(collection.getNamespace().getCollectionName());
        collection.createSearchIndex(indexName, indexDefinition);
      } else {
        collection.dropSearchIndex(indexName);
        await()
            .atMost(30, SECONDS)
            .pollInterval(500, MILLISECONDS)
            .until(() -> findSearchIndexStateValue(collection, indexName) == null);
        collection.createSearchIndex(indexName, indexDefinition);
      }
    }
  }

  private void validateAtlasSearchIndex(
      MongoClient mongoClient,
      AtlasSearchIndex searchIndex,
      AtlasSearchIndexValidationConfig validationConfig) {
    Class<? extends AtlasSearchIndexSyncValidationStrategy>[] strategyClasses =
        searchIndex.validationStrategies();
    if (strategyClasses != null && strategyClasses.length > 0) {
      AtlasSearchIndexValidationContext validationContext =
          AtlasSearchIndexValidationContext.builder()
              .withMongoClient(mongoClient)
              .withDatabase(searchIndex.database())
              .withCollection(searchIndex.collection())
              .withIndexName(searchIndex.name())
              .withValidationProperty(searchIndex.validationProperty())
              .build();
      AtlasSearchIndexValidationConfig effectiveConfig =
          validationConfig != null ? validationConfig : defaultValidationConfig;

      for (Class<? extends AtlasSearchIndexSyncValidationStrategy> strategyClass :
          strategyClasses) {
        AtlasSearchIndexSyncValidationStrategy strategy =
            instantiateValidationStrategy(strategyClass);
        if (!strategy.isSync(validationContext, effectiveConfig)) {
          throw new IllegalStateException(
              String.format(
                  "Atlas search index '%s' on '%s.%s' failed synchronization validation using strategy %s",
                  searchIndex.name(),
                  searchIndex.database(),
                  searchIndex.collection(),
                  strategyClass.getName()));
        }
      }

      MongoCollection<Document> collection =
          mongoClient.getDatabase(searchIndex.database()).getCollection(searchIndex.collection());
      String latestStateValue = findSearchIndexStateValue(collection, searchIndex.name());
      if (latestStateValue != null) {
        AtlasSearchIndexKey key =
            new AtlasSearchIndexKey(
                searchIndex.database(), searchIndex.collection(), searchIndex.name());
        CachedAtlasSearchIndexDefinition existingCached = cachedSearchIndexes.get(key);
        String defValue =
            existingCached != null
                ? existingCached.getDefValue()
                : readResourceAsString(searchIndex.bsonFilePath());
        cachedSearchIndexes.put(
            key, new CachedAtlasSearchIndexDefinition(defValue, latestStateValue));
      }
    }
  }

  /**
   * Instantiates the given {@link AtlasSearchIndexSyncValidationStrategy} class using its public
   * no-argument constructor.
   *
   * @param strategyClass the strategy class to instantiate
   * @return the instantiated strategy
   */
  protected AtlasSearchIndexSyncValidationStrategy instantiateValidationStrategy(
      Class<? extends AtlasSearchIndexSyncValidationStrategy> strategyClass) {
    try {
      return strategyClass.getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(
          "Failed to instantiate validation strategy: " + strategyClass.getName(), e);
    }
  }

  private String findSearchIndexStateValue(MongoCollection<Document> collection, String indexName) {
    try {
      ListSearchIndexesIterable<Document> indexes = collection.listSearchIndexes();
      if (indexes != null) {
        for (Document index : indexes) {
          if (Objects.equals(indexName, index.getString("name"))) {
            return index.toJson();
          }
        }
      }
    } catch (MongoCommandException e) {
      if (e.getErrorCode() != 26) {
        throw e;
      }
    }
    return null;
  }

  private String readResourceAsString(String bsonFilePath) {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(bsonFilePath)) {
      if (is == null) {
        throw new RuntimeException("Resource not found: " + bsonFilePath);
      }
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read BSON resource: " + bsonFilePath, e);
    }
  }

  private List<Document> parseBsonDocuments(String bsonFilePath) {
    String bson = readResourceAsString(bsonFilePath);
    JsonReader reader = new JsonReader(bson);
    BsonType bsonType = reader.readBsonType();
    if (bsonType == BsonType.ARRAY) {
      reader.readStartArray();
      List<Document> documents = new ArrayList<>();
      DocumentCodec codec = new DocumentCodec();
      DecoderContext decoderContext = DecoderContext.builder().build();
      while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
        documents.add(codec.decode(reader, decoderContext));
      }
      reader.readEndArray();
      return documents;
    } else {
      return Collections.singletonList(Document.parse(bson));
    }
  }

  private Document parseBsonDocument(String bsonFilePath) {
    List<Document> documents = parseBsonDocuments(bsonFilePath);
    return documents.isEmpty() ? null : documents.get(0);
  }
}
