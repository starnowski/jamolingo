package com.github.starnowski.jamolingo.junit5;

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.client.MongoClient;
import org.bson.Document;

class MongoDatabaseSetupExtensionTest {

  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "first", collection = "first", bsonFilePath = "")
      })
  private void clearFirstDatabaseFirstCollection() {}

  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "first", collection = "first", bsonFilePath = "")
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
}
