package com.github.starnowski.jamolingo.junit5;

import com.mongodb.client.MongoCollection;
import java.util.Objects;
import org.bson.Document;

/** Validation strategy that checks whether an Atlas Search Index has reached the READY status. */
public class AtlasSearchIndexReadinessSyncValidationStrategy
    implements AtlasSearchIndexSyncValidationStrategy {

  /** Constructs a new AtlasSearchIndexReadinessSyncValidationStrategy. */
  public AtlasSearchIndexReadinessSyncValidationStrategy() {}

  @Override
  public boolean isSync(AtlasSearchIndexValidationContext context) {
    MongoCollection<Document> collection =
        context
            .getMongoClient()
            .getDatabase(context.getDatabase())
            .getCollection(context.getCollection());
    for (Document index : collection.listSearchIndexes()) {
      if (Objects.equals(context.getIndexName(), index.getString("name"))
          && "READY".equals(index.getString("status"))) {
        return true;
      }
    }
    return false;
  }
}
