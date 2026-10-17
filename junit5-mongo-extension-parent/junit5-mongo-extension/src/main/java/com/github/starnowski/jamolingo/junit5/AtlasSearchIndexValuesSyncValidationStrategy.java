package com.github.starnowski.jamolingo.junit5;

import com.mongodb.client.MongoCollection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bson.Document;

/**
 * Validation strategy that verifies for every document in the MongoDB collection containing the
 * validation property that an Atlas Search query for that property value returns a result
 * containing the document's identifier.
 */
public class AtlasSearchIndexValuesSyncValidationStrategy
    implements AtlasSearchIndexSyncValidationStrategy {

  /** Constructs a new AtlasSearchIndexValuesSyncValidationStrategy. */
  public AtlasSearchIndexValuesSyncValidationStrategy() {}

  @Override
  public boolean isSync(AtlasSearchIndexValidationContext context) {
    MongoCollection<Document> collection =
        context
            .getMongoClient()
            .getDatabase(context.getDatabase())
            .getCollection(context.getCollection());
    String validationProperty = context.getValidationProperty();

    List<Document> documentsWithProperty = new ArrayList<>();
    collection
        .find(new Document(validationProperty, new Document("$exists", true)))
        .into(documentsWithProperty);

    for (Document doc : documentsWithProperty) {
      Object propertyValue = extractPropertyValue(doc, validationProperty);
      if (propertyValue == null) {
        continue;
      }
      Object expectedId = doc.get("_id");
      Document searchStage =
          new Document(
              "$search",
              new Document("index", context.getIndexName())
                  .append(
                      "text",
                      new Document("query", propertyValue).append("path", validationProperty)));

      List<Document> searchResults = new ArrayList<>();
      collection.aggregate(List.of(searchStage)).into(searchResults);

      boolean found =
          searchResults.stream()
              .anyMatch(resultDoc -> Objects.equals(expectedId, resultDoc.get("_id")));
      if (!found) {
        return false;
      }
    }
    return true;
  }

  private Object extractPropertyValue(Document doc, String validationProperty) {
    if (doc.containsKey(validationProperty)) {
      return doc.get(validationProperty);
    }
    if (validationProperty != null && validationProperty.contains(".")) {
      String[] parts = validationProperty.split("\\.");
      Object current = doc;
      for (String part : parts) {
        if (current instanceof Document currentDoc) {
          current = currentDoc.get(part);
        } else {
          return null;
        }
      }
      return current;
    }
    return null;
  }
}
