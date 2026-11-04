package com.github.starnowski.jamolingo.junit5;

import static com.github.starnowski.jamolingo.junit5.Awaiter.await;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.mongodb.client.MongoCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.bson.Document;

/**
 * Validation strategy that checks whether the number of documents in the Atlas Search Index
 * containing each validation property matches the number of documents in the MongoDB collection
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
    String[] validationPropertiesRaw = context.getValidationProperty();
    if (validationPropertiesRaw == null || validationPropertiesRaw.length == 0) {
      return true;
    }
    List<String> validationProperties =
        Arrays.stream(validationPropertiesRaw)
            .filter(p -> p != null && !p.trim().isEmpty())
            .toList();
    if (validationProperties.isEmpty()) {
      return true;
    }

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

    Map<String, Long> collectionCounts = new LinkedHashMap<>();
    for (String validationProperty : validationProperties) {
      long count =
          collection.countDocuments(
              new Document(validationProperty, new Document("$exists", true)));
      collectionCounts.put(validationProperty, count);
    }

    return await()
        .atMost(effectiveConfig.getMaxWaitDuration(), effectiveConfig.getMaxWaitUnit())
        .pollInterval(effectiveConfig.getPollInterval(), effectiveConfig.getPollIntervalUnit())
        .until(
            () -> {
              for (Map.Entry<String, Long> entry : collectionCounts.entrySet()) {
                String validationProperty = entry.getKey();
                long collectionCount = entry.getValue();

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
                  if (countDoc == null
                      || !(countDoc.get("total") instanceof Number total)
                      || total.longValue() != collectionCount) {
                    return false;
                  }
                } else if (collectionCount != 0) {
                  return false;
                }
              }
              return true;
            });
  }
}
