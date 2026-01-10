package com.github.starnowski.jamolingo.junit5;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Sorts;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTest
@QuarkusTestResource(EmbeddedMongoResource.class)
class MongoDatabaseSetupExtensionTest {

  @Inject
  private MongoClient mongoClient;

  public static Stream<Arguments> provideShouldClearCollections() {
    return Stream.of(
            Arguments.of("clearFirstDatabaseFirstCollection",
                    new HashSet<>(Arrays.asList(new MongoCollectionKey("first", "first"))),
                    new HashSet<>(Arrays.asList(
                            new MongoCollectionKey("first", "second"),
                            new MongoCollectionKey("second", "first"),
                            new MongoCollectionKey("second", "second")
                    ))

            )
    );
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

  private void insertDocuments(MongoClient client) {
    client.getDatabase("first").getCollection("first").insertOne(new Document("key", "value"));
    client.getDatabase("first").getCollection("second").insertOne(new Document("key", "value"));
    client.getDatabase("second").getCollection("first").insertOne(new Document("key", "value"));
    client.getDatabase("second").getCollection("second").insertOne(new Document("key", "value"));
  }

  private long countDocuments(MongoClient client, String database, String collection) {
    return client.getDatabase(database).getCollection(collection).countDocuments();
  }

  @ParameterizedTest
  @MethodSource("provideShouldClearCollections")
  public void shouldClearCollections(String testMethod, Set<MongoCollectionKey> expectedEmptyCollections, Set<MongoCollectionKey> expectedNoneEmptyCollections) throws IllegalAccessException, NoSuchMethodException {
      // GIVEN
    MongoDatabaseSetupExtension tested = new MongoDatabaseSetupExtension();
    ExtensionContext extensionContext = Mockito.mock(ExtensionContext.class);
    Mockito.when(extensionContext.getTestMethod()).thenReturn(Optional.of(this.getClass().getDeclaredMethod(testMethod)));

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    for (MongoCollectionKey collection : expectedEmptyCollections) {
      Assertions.assertEquals(0, countDocuments(mongoClient, collection.getDatabase(), collection.getCollection()));
    }
    for (MongoCollectionKey collection : expectedNoneEmptyCollections) {
      Assertions.assertEquals(1, countDocuments(mongoClient, collection.getDatabase(), collection.getCollection()));
    }
  }
}
