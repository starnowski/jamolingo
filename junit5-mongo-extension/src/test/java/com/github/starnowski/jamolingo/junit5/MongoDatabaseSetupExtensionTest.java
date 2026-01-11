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
class MongoDatabaseSetupExtensionTest {

  private static final String EXAMPLE_1_FILE_PATH = "bson/example1.json";
  private static final String EXAMPLE_2_FILE_PATH = "bson/example2.json";
  private static final String EXAMPLE_3_FILE_PATH = "bson/example3.json";
  private static final String EXAMPLE_4_FILE_PATH = "bson/example4.json";
  private static final String EXAMPLE_5_FILE_PATH = "bson/example5.json";
  private static final String EXAMPLE_6_FILE_PATH = "bson/example6.json";
  private static final String EXAMPLE_7_FILE_PATH = "bson/example7.json";
  private static final String EXAMPLE_8_FILE_PATH = "bson/example8.json";

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
    MongoDatabaseSetupExtension tested = new MongoDatabaseSetupExtension();
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
}
