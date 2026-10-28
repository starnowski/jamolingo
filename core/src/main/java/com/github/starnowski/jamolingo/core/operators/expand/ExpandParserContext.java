package com.github.starnowski.jamolingo.core.operators.expand;

import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchParser;
import java.util.Map;
import java.util.Set;

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

  /**
   * Returns the root EDM mongo context facade.
   *
   * @return the root EDM mongo context facade
   */
  default EdmMongoContextFacade getRootEdmMongoContextFacade() {
    return null;
  }

  /**
   * Determines if missing navigation properties should be globally ignored.
   *
   * @return true if they should be ignored, false otherwise
   */
  default boolean isIgnoreGloballyMissingNavigationProperty() {
    return false;
  }

  /**
   * Returns a set of EDM paths for which missing navigation properties should be ignored.
   *
   * @return set of EDM paths to ignore
   */
  default Set<String> getIgnoreForSpecificExpandElementMissingNavigationProperty() {
    return java.util.Collections.emptySet();
  }

  /**
   * Determines if missing properties should be globally ignored.
   *
   * @return true if they should be ignored, false otherwise
   */
  default boolean isIgnoreGloballyMissingProperty() {
    return false;
  }

  /**
   * Returns a set of EDM paths for which missing properties should be ignored.
   *
   * @return set of EDM paths to ignore
   */
  default Set<String> getIgnoreForSpecificExpandElementMissingProperty() {
    return java.util.Collections.emptySet();
  }

  /**
   * Returns the global ODataSearchToMongoAtlasSearchParser used across expand levels and entities.
   *
   * @return the global search parser, or null if not configured
   */
  default ODataSearchToMongoAtlasSearchParser getGlobalSearchParser() {
    return null;
  }

  /**
   * Returns mapping between specific navigation property EDM paths and their dedicated
   * ODataSearchToMongoAtlasSearchParser instances.
   *
   * @return mapping of navigation property EDM paths to search parsers
   */
  default Map<String, ODataSearchToMongoAtlasSearchParser> getNavigationPropertySearchParsers() {
    return java.util.Collections.emptyMap();
  }

  /**
   * Returns mapping between EDM entity type keys and their dedicated
   * ODataSearchToMongoAtlasSearchParser instances.
   *
   * @return mapping of EDM entity type keys to search parsers
   */
  default Map<KeyValue<String, String>, ODataSearchToMongoAtlasSearchParser>
      getEdmTablesToSearchParsers() {
    return java.util.Collections.emptyMap();
  }
}
