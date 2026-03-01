package com.github.starnowski.jamolingo.compat.driver.operators.filter;

import com.github.starnowski.jamolingo.AbstractItTest;
import com.github.starnowski.jamolingo.EmbeddedMongoResource;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import org.bson.Document;
import org.bson.conversions.Bson;

@QuarkusTest
@QuarkusTestResource(EmbeddedMongoResource.class)
public abstract class AbstractBaseFilterOperatorTest extends AbstractItTest {

  @Inject protected MongoClient mongoClient;

  protected void createIndexesForPropertyInCollection(
      String database, String collectionName, Set<String> properties) {
    properties.forEach(pro -> createIndexForPropertyInCollection(database, collectionName, pro));
  }

  protected void createIndexForPropertyInCollection(
      String database, String collectionName, String property) {
    MongoCollection<Document> col = mongoClient.getDatabase(database).getCollection(collectionName);
    col.createIndex(new Document(property, 1));
  }

  protected void dropIndexesForCollections(String database, Set<String> collectionNames) {
    collectionNames.forEach(col -> dropIndexesForCollection(database, col));
  }

  protected void dropIndexesForCollection(String database, String collectionName) {
    try {

      MongoCollection<Document> col =
          mongoClient.getDatabase(database).getCollection(collectionName);
      col.dropIndexes();
    } catch (Exception exception) {
      // Do nothing
      exception.printStackTrace();
    }
  }

  protected void logTestsObject(String filterString, List<Bson> pipeline) {
    String path =
        Paths.get(new File(getClass().getClassLoader().getResource(".").getFile()).getPath())
            .toAbsolutePath()
            .toString();
    System.out.println("File path is " + path);
    try (FileOutputStream outputStream =
        new FileOutputStream(path + File.separator + "testcases.txt", true)) {
      outputStream.write("<test>".getBytes(StandardCharsets.UTF_8));
      outputStream.write(
          ("<filter>$filter=" + filterString + "</filter>").getBytes(StandardCharsets.UTF_8));
      outputStream.write(
          ("<pipeline>"
                  + pipeline
                      .get(0)
                      .toBsonDocument(Document.class, this.mongoClient.getCodecRegistry())
                      .toJson()
                  + "</pipeline>")
              .getBytes(StandardCharsets.UTF_8));
      outputStream.write("</test>".getBytes(StandardCharsets.UTF_8));
      outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
