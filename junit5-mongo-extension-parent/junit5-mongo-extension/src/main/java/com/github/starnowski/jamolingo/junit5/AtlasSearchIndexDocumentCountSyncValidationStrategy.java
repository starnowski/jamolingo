package com.github.starnowski.jamolingo.junit5;

import static com.github.starnowski.jamolingo.junit5.Awaiter.await;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.mongodb.client.MongoCollection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bson.Document;

/**
 * Validation strategy that checks whether the number of documents in the Atlas Search Index
 * containing the validation property matches the number of documents in the MongoDB collection
 * containing that property.
 */
public class AtlasSearchIndexDocumentCountSyncValidationStrategy
    implements AtlasSearchIndexSyncValidationStrategy {

  private final long maxWaitDuration;
  private final TimeUnit maxWaitUnit;
  private final long pollInterval;
  private final TimeUnit pollIntervalUnit;

  /**
   * Constructs a new AtlasSearchIndexDocumentCountSyncValidationStrategy with default wait settings
   * (at most 30 seconds, polling every 1 second).
   */
  public AtlasSearchIndexDocumentCountSyncValidationStrategy() {
    this(30, SECONDS, 1, SECONDS);
  }

  /**
   * Constructs a new AtlasSearchIndexDocumentCountSyncValidationStrategy with custom wait and
   * polling settings.
   *
   * @param maxWaitDuration the maximum duration to wait
   * @param maxWaitUnit the time unit for the maximum wait duration
   * @param pollInterval the interval between polls
   * @param pollIntervalUnit the time unit for the polling interval
   */
  public AtlasSearchIndexDocumentCountSyncValidationStrategy(
      long maxWaitDuration, TimeUnit maxWaitUnit, long pollInterval, TimeUnit pollIntervalUnit) {
    this.maxWaitDuration = maxWaitDuration;
    this.maxWaitUnit = maxWaitUnit;
    this.pollInterval = pollInterval;
    this.pollIntervalUnit = pollIntervalUnit;
  }

  @Override
  public boolean isSync(AtlasSearchIndexValidationContext context) {
    return isSync(
        context,
        new AtlasSearchIndexValidationConfig(
            maxWaitDuration, maxWaitUnit, pollInterval, pollIntervalUnit));
  }

  @Override
  public boolean isSync(
      AtlasSearchIndexValidationContext context, AtlasSearchIndexValidationConfig config) {
    AtlasSearchIndexValidationConfig effectiveConfig =
        config != null
            ? config
            : new AtlasSearchIndexValidationConfig(
                maxWaitDuration, maxWaitUnit, pollInterval, pollIntervalUnit);
    MongoCollection<Document> collection =
        context
            .getMongoClient()
            .getDatabase(context.getDatabase())
            .getCollection(context.getCollection());
    String validationProperty = context.getValidationProperty();
    long collectionCount =
        collection.countDocuments(new Document(validationProperty, new Document("$exists", true)));

    return await()
        .atMost(effectiveConfig.getMaxWaitDuration(), effectiveConfig.getMaxWaitUnit())
        .pollInterval(effectiveConfig.getPollInterval(), effectiveConfig.getPollIntervalUnit())
        .until(
            () -> {
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
            });
  }
}
