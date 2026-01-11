package com.github.starnowski.jamolingo.junit5;

import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.arc.Arc;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bson.Document;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class QuarkusMongoDataLoaderExtension implements BeforeEachCallback {

  @Override
  public void beforeEach(ExtensionContext context) throws IllegalAccessException {
    MongoSetup annotation =
        context.getTestMethod().stream()
            .map(t -> t.getAnnotation(MongoSetup.class))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    if (annotation != null) {
      // Get test instance (the test class object)
      Object testInstance = context.getRequiredTestInstance();
      MongoClient mongoClient = Arc.container().instance(MongoClient.class).get();

      if (mongoClient == null) {
        throw new IllegalStateException(
            "No MongoClient field found in test class: " + testInstance.getClass().getName());
      }

      Map<String, Set<String>> collections = new HashMap<>();
      Arrays.stream(annotation.mongoDocuments())
          .forEach(
              an -> {
                collections.merge(
                    an.database(),
                    Set.of(an.collection()),
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

      Arrays.stream(annotation.mongoDocuments())
          .filter(an -> !an.bsonFilePath().trim().isEmpty())
          .forEach(
              an -> {
                MongoCollection<Document> collection =
                    mongoCollectionMap.get(new MongoCollectionKey(an.database(), an.collection()));
                try {
                  String bson =
                      Files.readString(
                          Paths.get(
                              new File(
                                      getClass()
                                          .getClassLoader()
                                          .getResource(an.bsonFilePath())
                                          .getFile())
                                  .getPath()));
                  collection.insertOne(Document.parse(bson));
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              });
    }
  }
}
