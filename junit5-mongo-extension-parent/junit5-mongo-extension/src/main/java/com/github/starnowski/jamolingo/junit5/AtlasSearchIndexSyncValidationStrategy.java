package com.github.starnowski.jamolingo.junit5;

/**
 * Strategy interface for validating whether an Atlas Search Index is synchronized with the target
 * MongoDB collection.
 */
public interface AtlasSearchIndexSyncValidationStrategy {

  /**
   * Checks whether the Atlas Search Index is synchronized according to this validation strategy.
   *
   * @param context the validation context containing the MongoDB client, database, collection,
   *     index name, and validation property
   * @return true if the index is synchronized, false otherwise
   */
  boolean isSync(AtlasSearchIndexValidationContext context);
}
