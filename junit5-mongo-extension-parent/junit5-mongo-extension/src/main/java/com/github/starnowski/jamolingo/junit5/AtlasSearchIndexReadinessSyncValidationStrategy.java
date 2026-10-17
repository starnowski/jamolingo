package com.github.starnowski.jamolingo.junit5;

import static com.github.starnowski.jamolingo.junit5.Awaiter.await;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.mongodb.client.MongoCollection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.bson.Document;

/** Validation strategy that checks whether an Atlas Search Index has reached the READY status. */
public class AtlasSearchIndexReadinessSyncValidationStrategy
    implements AtlasSearchIndexSyncValidationStrategy {

  private final long maxWaitDuration;
  private final TimeUnit maxWaitUnit;
  private final long pollInterval;
  private final TimeUnit pollIntervalUnit;

  /**
   * Constructs a new AtlasSearchIndexReadinessSyncValidationStrategy with default wait settings (at
   * most 30 seconds, polling every 1 second).
   */
  public AtlasSearchIndexReadinessSyncValidationStrategy() {
    this(30, SECONDS, 1, SECONDS);
  }

  /**
   * Constructs a new AtlasSearchIndexReadinessSyncValidationStrategy with custom wait and polling
   * settings.
   *
   * @param maxWaitDuration the maximum duration to wait
   * @param maxWaitUnit the time unit for the maximum wait duration
   * @param pollInterval the interval between polls
   * @param pollIntervalUnit the time unit for the polling interval
   */
  public AtlasSearchIndexReadinessSyncValidationStrategy(
      long maxWaitDuration, TimeUnit maxWaitUnit, long pollInterval, TimeUnit pollIntervalUnit) {
    this.maxWaitDuration = maxWaitDuration;
    this.maxWaitUnit = maxWaitUnit;
    this.pollInterval = pollInterval;
    this.pollIntervalUnit = pollIntervalUnit;
  }

  @Override
  public boolean isSync(AtlasSearchIndexValidationContext context) {
    MongoCollection<Document> collection =
        context
            .getMongoClient()
            .getDatabase(context.getDatabase())
            .getCollection(context.getCollection());
    return await()
        .atMost(maxWaitDuration, maxWaitUnit)
        .pollInterval(pollInterval, pollIntervalUnit)
        .until(
            () -> {
              for (Document index : collection.listSearchIndexes()) {
                if (Objects.equals(context.getIndexName(), index.getString("name"))
                    && "READY".equals(index.getString("status"))) {
                  return true;
                }
              }
              return false;
            });
  }
}
