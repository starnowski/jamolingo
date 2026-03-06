package com.github.starnowski.jamolingo.junit5;

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.client.MongoClient;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.*;
import java.util.stream.Stream;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTest
@QuarkusTestResource(EmbeddedMongoResource.class)
class QuarkusMongoDataLoaderExtensionTest {

  private static final String EXAMPLE_1_FILE_PATH = "bson/example1.json";
  private static final String EXAMPLE_2_FILE_PATH = "bson/example2.json";
  private static final String EXAMPLE_3_FILE_PATH = "bson/example3.json";
  private static final String EXAMPLE_4_FILE_PATH = "bson/example4.json";
  private static final String EXAMPLE_5_FILE_PATH = "bson/example5.json";
  private static final String EXAMPLE_6_FILE_PATH = "bson/example6.json";
  private static final String EXAMPLE_7_FILE_PATH = "bson/example7.json";
  private static final String EXAMPLE_8_FILE_PATH = "bson/example8.json";
  private static final String EXAMPLE_1 = "example1";
  private static final String EXAMPLE_2 = "example2";
  private static final String EXAMPLE_3 = "example3";
  private static final String EXAMPLE_4 = "example4";
  private static final String EXAMPLE_5 = "example5";
  private static final String EXAMPLE_6 = "example6";
  private static final String EXAMPLE_7 = "example7";
  private static final String EXAMPLE_8 = "example8";

  @Inject private MongoClient mongoClient;

  public static Stream<Arguments> provideShouldClearCollections() {
    return Stream.of(
        Arguments.of(
            "clearFirstDatabaseFirstCollection",
            new HashSet<>(Arrays.asList(new MongoCollectionKey("first", "first"))),
            new HashSet<>(
                Arrays.asList(
                    new MongoCollectionKey("first", "second"),
                    new MongoCollectionKey("second", "first"),
                    new MongoCollectionKey("second", "second")))),
        Arguments.of(
            "clearFirstDatabaseSecondCollection",
            new HashSet<>(Arrays.asList(new MongoCollectionKey("first", "second"))),
            new HashSet<>(
                Arrays.asList(
                    new MongoCollectionKey("first", "first"),
                    new MongoCollectionKey("second", "first"),
                    new MongoCollectionKey("second", "second")))),
        Arguments.of(
            "clearSecondDatabaseFirstCollection",
            new HashSet<>(Arrays.asList(new MongoCollectionKey("second", "first"))),
            new HashSet<>(
                Arrays.asList(
                    new MongoCollectionKey("first", "first"),
                    new MongoCollectionKey("first", "second"),
                    new MongoCollectionKey("second", "second")))),
        Arguments.of(
            "clearSecondDatabaseSecondCollection",
            new HashSet<>(Arrays.asList(new MongoCollectionKey("second", "second"))),
            new HashSet<>(
                Arrays.asList(
                    new MongoCollectionKey("first", "first"),
                    new MongoCollectionKey("first", "second"),
                    new MongoCollectionKey("second", "first")))),
        Arguments.of(
            "clearSecondDatabaseFirstAndSecondCollection",
            new HashSet<>(
                Arrays.asList(
                    new MongoCollectionKey("second", "first"),
                    new MongoCollectionKey("second", "second"))),
            new HashSet<>(
                Arrays.asList(
                    new MongoCollectionKey("first", "first"),
                    new MongoCollectionKey("first", "second")))),
        Arguments.of(
            "clearFirstDatabaseFirstCollectionSecondDatabaseFirstCollection",
            new HashSet<>(
                Arrays.asList(
                    new MongoCollectionKey("first", "first"),
                    new MongoCollectionKey("second", "second"))),
            new HashSet<>(
                Arrays.asList(
                    new MongoCollectionKey("first", "second"),
                    new MongoCollectionKey("second", "first")))));
  }

  public Stream<Arguments> provideShouldAddDocumentsIntoCollections() {
    return Stream.of(
        Arguments.of(
            "insertDataIntoCollectionsCase1",
            Map.of(
                new MongoCollectionKey("first", "first"),
                Set.of(EXAMPLE_1, EXAMPLE_2),
                new MongoCollectionKey("first", "second"),
                Set.of(EXAMPLE_2, EXAMPLE_3, EXAMPLE_4),
                new MongoCollectionKey("second", "first"),
                Set.of(EXAMPLE_3),
                new MongoCollectionKey("second", "second"),
                Set.of(EXAMPLE_5, EXAMPLE_6, EXAMPLE_7))),
        Arguments.of(
            "insertDataIntoCollectionsCase2",
            Map.of(
                new MongoCollectionKey("first", "first"),
                Set.of(EXAMPLE_1, EXAMPLE_8),
                new MongoCollectionKey("second", "second"),
                Set.of(EXAMPLE_5))),
        Arguments.of(
            "insertDataIntoCollectionsCase3",
            Map.of(
                new MongoCollectionKey("first", "second"),
                Set.of(EXAMPLE_2, EXAMPLE_3, EXAMPLE_4, EXAMPLE_5))),
        Arguments.of(
            "insertDataIntoCollectionsCase4",
            Map.of(
                new MongoCollectionKey("second", "first"),
                Set.of(EXAMPLE_6),
                new MongoCollectionKey("second", "second"),
                Set.of(EXAMPLE_7, EXAMPLE_8))),
        Arguments.of(
            "insertDataIntoCollectionsCase5",
            Map.of(
                new MongoCollectionKey("first", "first"),
                Set.of(EXAMPLE_1),
                new MongoCollectionKey("first", "second"),
                Set.of(EXAMPLE_2),
                new MongoCollectionKey("second", "first"),
                Set.of(EXAMPLE_3),
                new MongoCollectionKey("second", "second"),
                Set.of(EXAMPLE_4))));
  }

  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = "first",
            collection = "first",
            bsonFilePath = EXAMPLE_1_FILE_PATH),
        @MongoDocument(
            database = "first",
            collection = "first",
            bsonFilePath = EXAMPLE_2_FILE_PATH),
        @MongoDocument(
            database = "first",
            collection = "second",
            bsonFilePath = EXAMPLE_2_FILE_PATH),
        @MongoDocument(
            database = "first",
            collection = "second",
            bsonFilePath = EXAMPLE_3_FILE_PATH),
        @MongoDocument(
            database = "first",
            collection = "second",
            bsonFilePath = EXAMPLE_4_FILE_PATH),
        @MongoDocument(
            database = "second",
            collection = "first",
            bsonFilePath = EXAMPLE_3_FILE_PATH),
        @MongoDocument(
            database = "second",
            collection = "second",
            bsonFilePath = EXAMPLE_5_FILE_PATH),
        @MongoDocument(
            database = "second",
            collection = "second",
            bsonFilePath = EXAMPLE_6_FILE_PATH),
        @MongoDocument(
            database = "second",
            collection = "second",
            bsonFilePath = EXAMPLE_7_FILE_PATH)
      })
  private void insertDataIntoCollectionsCase1() {}

  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = "first",
            collection = "first",
            bsonFilePath = EXAMPLE_1_FILE_PATH),
        @MongoDocument(
            database = "first",
            collection = "first",
            bsonFilePath = EXAMPLE_8_FILE_PATH),
        @MongoDocument(
            database = "second",
            collection = "second",
            bsonFilePath = EXAMPLE_5_FILE_PATH)
      })
  private void insertDataIntoCollectionsCase2() {}

  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = "first",
            collection = "second",
            bsonFilePath = EXAMPLE_2_FILE_PATH),
        @MongoDocument(
            database = "first",
            collection = "second",
            bsonFilePath = EXAMPLE_3_FILE_PATH),
        @MongoDocument(
            database = "first",
            collection = "second",
            bsonFilePath = EXAMPLE_4_FILE_PATH),
        @MongoDocument(
            database = "first",
            collection = "second",
            bsonFilePath = EXAMPLE_5_FILE_PATH)
      })
  private void insertDataIntoCollectionsCase3() {}

  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = "second",
            collection = "first",
            bsonFilePath = EXAMPLE_6_FILE_PATH),
        @MongoDocument(
            database = "second",
            collection = "second",
            bsonFilePath = EXAMPLE_7_FILE_PATH),
        @MongoDocument(
            database = "second",
            collection = "second",
            bsonFilePath = EXAMPLE_8_FILE_PATH)
      })
  private void insertDataIntoCollectionsCase4() {}

  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = "first",
            collection = "first",
            bsonFilePath = EXAMPLE_1_FILE_PATH),
        @MongoDocument(
            database = "first",
            collection = "second",
            bsonFilePath = EXAMPLE_2_FILE_PATH),
        @MongoDocument(
            database = "second",
            collection = "first",
            bsonFilePath = EXAMPLE_3_FILE_PATH),
        @MongoDocument(
            database = "second",
            collection = "second",
            bsonFilePath = EXAMPLE_4_FILE_PATH)
      })
  private void insertDataIntoCollectionsCase5() {}

  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "first", collection = "first", bsonFilePath = "")
      })
  private void clearFirstDatabaseFirstCollection() {}

  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "first", collection = "second", bsonFilePath = "")
      })
  private void clearFirstDatabaseSecondCollection() {}

  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "second", collection = "first", bsonFilePath = "")
      })
  private void clearSecondDatabaseFirstCollection() {}

  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "second", collection = "second", bsonFilePath = "")
      })
  private void clearSecondDatabaseSecondCollection() {}

  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "second", collection = "first", bsonFilePath = ""),
        @MongoDocument(database = "second", collection = "second", bsonFilePath = "")
      })
  private void clearSecondDatabaseFirstAndSecondCollection() {}

  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "first", collection = "first", bsonFilePath = ""),
        @MongoDocument(database = "second", collection = "second", bsonFilePath = "")
      })
  private void clearFirstDatabaseFirstCollectionSecondDatabaseFirstCollection() {}

  private void insertDocuments(MongoClient client) {
    client.getDatabase("first").getCollection("first").insertOne(new Document("key", "value"));
    client.getDatabase("first").getCollection("second").insertOne(new Document("key", "value"));
    client.getDatabase("second").getCollection("first").insertOne(new Document("key", "value"));
    client.getDatabase("second").getCollection("second").insertOne(new Document("key", "value"));
  }

  private void clearAllCollections(MongoClient client) {
    client.getDatabase("first").getCollection("first").deleteMany(new Document());
    client.getDatabase("first").getCollection("second").deleteMany(new Document());
    client.getDatabase("second").getCollection("first").deleteMany(new Document());
    client.getDatabase("second").getCollection("second").deleteMany(new Document());
  }

  private long countDocuments(MongoClient client, String database, String collection) {
    return client.getDatabase(database).getCollection(collection).countDocuments();
  }

  private void assertDocumentsExist(
      MongoClient client,
      String database,
      String collection,
      String propertyName,
      String... expectedValues) {
    List<String> actualValues = new ArrayList<>();
    client
        .getDatabase(database)
        .getCollection(collection)
        .find()
        .map(document -> document.getString(propertyName))
        .forEach(actualValues::add);

    List<String> expectedValuesList = Arrays.asList(expectedValues);
    Assertions.assertTrue(
        actualValues.containsAll(expectedValuesList),
        String.format(
            "Expected values %s not found in actual values %s for collection %s.%s",
            expectedValuesList, actualValues, database, collection));
  }

  @ParameterizedTest
  @MethodSource("provideShouldClearCollections")
  public void shouldClearCollections(
      String testMethod,
      Set<MongoCollectionKey> expectedEmptyCollections,
      Set<MongoCollectionKey> expectedNoneEmptyCollections)
      throws IllegalAccessException, NoSuchMethodException {
    // GIVEN
    clearAllCollections(mongoClient);
    insertDocuments(mongoClient);
    QuarkusMongoDataLoaderExtension tested = new QuarkusMongoDataLoaderExtension();
    ExtensionContext extensionContext = Mockito.mock(ExtensionContext.class);
    Mockito.when(extensionContext.getTestMethod())
        .thenReturn(Optional.of(this.getClass().getDeclaredMethod(testMethod)));

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    for (MongoCollectionKey collection : expectedEmptyCollections) {
      Assertions.assertEquals(
          0,
          countDocuments(mongoClient, collection.getDatabase(), collection.getCollection()),
          "Collection %s.%s should be empty"
              .formatted(collection.getDatabase(), collection.getCollection()));
    }
    for (MongoCollectionKey collection : expectedNoneEmptyCollections) {
      Assertions.assertEquals(
          1,
          countDocuments(mongoClient, collection.getDatabase(), collection.getCollection()),
          "Collection %s.%s should not be empty"
              .formatted(collection.getDatabase(), collection.getCollection()));
    }
  }

  @ParameterizedTest
  @MethodSource("provideShouldAddDocumentsIntoCollections")
  public void shouldAddDocumentsIntoCollections(
      String testMethod, Map<MongoCollectionKey, Set<String>> expectedCollectionsContent)
      throws IllegalAccessException, NoSuchMethodException {
    // GIVEN
    clearAllCollections(mongoClient);
    QuarkusMongoDataLoaderExtension tested = new QuarkusMongoDataLoaderExtension();
    ExtensionContext extensionContext = Mockito.mock(ExtensionContext.class);
    Mockito.when(extensionContext.getTestMethod())
        .thenReturn(Optional.of(this.getClass().getDeclaredMethod(testMethod)));

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    for (Map.Entry<MongoCollectionKey, Set<String>> entry : expectedCollectionsContent.entrySet()) {
      assertDocumentsExist(
          mongoClient,
          entry.getKey().getDatabase(),
          entry.getKey().getCollection(),
          "stringKey",
          entry.getValue().toArray(new String[0]));
    }
  }
}
