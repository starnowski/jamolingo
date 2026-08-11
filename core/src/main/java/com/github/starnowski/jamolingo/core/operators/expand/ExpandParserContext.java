package com.github.starnowski.jamolingo.core.operators.expand;

import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import java.util.Map;

/** Context for parsing OData $expand system query option. */
public interface ExpandParserContext {

  /** Default maximum level of recursion for $expand. */
  Integer DEFAULT_MAX_LEVEL = 5;

  /**
   * Returns mapping between EDM type names and their Mongo path resolvers.
   *
   * @return mapping of EDM type names to resolvers
   */
  Map<String, EdmMongoContextFacade> getEDMTypeMapping();

  /**
   * Returns mapping between EDM entity sets and their MongoDB collection names.
   *
   * @return mapping of EDM entity sets to collection names
   */
  Map<KeyValue<String, String>, String> getEDMTablesToMongoDBCollections();

  /**
   * Returns the maximum level of recursion for $expand.
   *
   * @return maximum expansion level
   */
  Integer getMaxLevel();

  /**
   * Determines if the $lookup stage should be used to handle $level greater than 1. By default,
   * returns false because by default the $graphLookup stage handles such cases.
   *
   * @return true if the $lookup stage should be used, false otherwise
   */
  default boolean isUseLookupForLevelGreaterThanOne() {
    return false;
  }

  /**
   * Determines if the join keys used for the $graphLookup stage should be propagated to the
   * transformation result when tree structures are built.
   *
   * @return true if the join keys should be propagated, false otherwise
   */
  default boolean propagateGraphLookUpJoinKeys() {
    return false;
  }

  /**
   * Determines if an exception should be thrown when the requested expand level is higher than the
   * allowed maximal level. By default, returns false.
   *
   * @return true if an exception should be thrown, false otherwise
   */
  default boolean isThrowExceptionOnExpandLevelsExceeded() {
    return false;
  }

  /**
   * Returns the maximum number of nested expand operations that can be used. If null, no limit is
   * applied.
   *
   * @return maximum nested expand operations limit, or null if no limit
   */
  default Integer getMaxAllowedNestedExpandLevel() {
    return null;
  }

  /**
   * Returns the root EDM entity type name.
   *
   * @return the root EDM entity type name
   */
  default String getRootEdmEntityTypeName() {
    return null;
  }
}
