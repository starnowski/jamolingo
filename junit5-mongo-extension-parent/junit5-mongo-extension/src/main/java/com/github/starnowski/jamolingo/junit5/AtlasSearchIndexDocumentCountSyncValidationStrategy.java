package com.github.starnowski.jamolingo.junit5;

import com.mongodb.client.MongoCollection;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

/**
 * Validation strategy that checks whether the number of documents in the Atlas Search Index
 * containing the validation property matches the number of documents in the MongoDB collection
 * containing that property.
 */
public class AtlasSearchIndexDocumentCountSyncValidationStrategy
    implements AtlasSearchIndexSyncValidationStrategy {

  /** Constructs a new AtlasSearchIndexDocumentCountSyncValidationStrategy. */
  public AtlasSearchIndexDocumentCountSyncValidationStrategy() {}

  @Override
  public boolean isSync(AtlasSearchIndexValidationContext context) {
    MongoCollection<Document> collection =
        context
            .getMongoClient()
            .getDatabase(context.getDatabase())
            .getCollection(context.getCollection());
    String validationProperty = context.getValidationProperty();
    long collectionCount =
        collection.countDocuments(new Document(validationProperty, new Document("$exists", true)));

    Document searchMetaStage =
        new Document(
            "$searchMeta",
            new Document("index", context.getIndexName())
                .append("exists", new Document("path", validationProperty))
                .append("count", new Document("type", "total")));

    List<Document> results = new ArrayList<>();
    collection.aggregate(List.of(searchMetaStage)).into(results);

    if (!results.isEmpty()) {
      Document countDoc = results.get(0).get("count", Document.class);
      if (countDoc != null && countDoc.get("total") instanceof Number total) {
        return total.longValue() == collectionCount;
      }
      return false;
    }
    return collectionCount == 0;
  }
}
