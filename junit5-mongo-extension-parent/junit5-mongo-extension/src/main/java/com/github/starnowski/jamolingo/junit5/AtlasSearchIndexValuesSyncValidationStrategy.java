package com.github.starnowski.jamolingo.junit5;

import static com.github.starnowski.jamolingo.junit5.Awaiter.await;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.mongodb.client.MongoCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.bson.Document;

/**
 * Validation strategy that verifies for every validation property and every document in the MongoDB
 * collection containing that property that an Atlas Search query for that property value returns a
 * result containing the document's identifier.
 */
public class AtlasSearchIndexValuesSyncValidationStrategy
    implements AtlasSearchIndexSyncValidationStrategy {

  private final long maxWaitDuration;
  private final TimeUnit maxWaitUnit;
  private final long pollInterval;
  private final TimeUnit pollIntervalUnit;

  /**
   * Constructs a new AtlasSearchIndexValuesSyncValidationStrategy with default wait settings (at
   * most 30 seconds, polling every 1 second).
   */
  public AtlasSearchIndexValuesSyncValidationStrategy() {
    this(30, SECONDS, 1, SECONDS);
  }

  /**
   * Constructs a new AtlasSearchIndexValuesSyncValidationStrategy with custom wait and polling
   * settings.
   *
   * @param maxWaitDuration the maximum duration to wait
   * @param maxWaitUnit the time unit for the maximum wait duration
   * @param pollInterval the interval between polls
   * @param pollIntervalUnit the time unit for the polling interval
   */
  public AtlasSearchIndexValuesSyncValidationStrategy(
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

    Map<String, List<Document>> documentsByProperty = new LinkedHashMap<>();
    for (String validationProperty : validationProperties) {
      List<Document> documentsWithProperty = new ArrayList<>();
      collection
          .find(new Document(validationProperty, new Document("$exists", true)))
          .into(documentsWithProperty);
      documentsByProperty.put(validationProperty, documentsWithProperty);
    }

    return await()
        .atMost(effectiveConfig.getMaxWaitDuration(), effectiveConfig.getMaxWaitUnit())
        .pollInterval(effectiveConfig.getPollInterval(), effectiveConfig.getPollIntervalUnit())
        .until(
            () -> {
              for (Map.Entry<String, List<Document>> entry : documentsByProperty.entrySet()) {
                String validationProperty = entry.getKey();
                List<Document> documentsWithProperty = entry.getValue();

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
                                  new Document("query", propertyValue)
                                      .append("path", validationProperty)));

                  List<Document> searchResults = new ArrayList<>();
                  collection.aggregate(List.of(searchStage)).into(searchResults);

                  boolean found =
                      searchResults.stream()
                          .anyMatch(resultDoc -> Objects.equals(expectedId, resultDoc.get("_id")));
                  if (!found) {
                    return false;
                  }
                }
              }
              return true;
            });
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
