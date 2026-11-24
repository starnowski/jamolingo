package com.github.starnowski.jamolingo.core.utils;

import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import java.util.Collections;
import java.util.Map;

/**
 * Context providing EDM-to-MongoDB type and collection mappings for generating lookup stages from
 * used navigation properties.
 */
public interface NavigationPropertyLookupContext {

  /**
   * Returns the root EDM entity type full name.
   *
   * @return the root EDM entity type full name, or null if not configured
   */
  default String getRootEdmEntityTypeName() {
    return null;
  }

  /**
   * Returns the root EDM mongo context facade used to resolve properties on the root entity.
   *
   * @return the root EDM mongo context facade, or null if not configured
   */
  default EdmMongoContextFacade getRootEdmMongoContextFacade() {
    return null;
  }

  /**
   * Returns the mapping between EDM entity full type names and their Mongo path resolvers.
   *
   * @return mapping of EDM type names to resolvers
   */
  default Map<String, EdmMongoContextFacade> getEdmTypeMapping() {
    return Collections.emptyMap();
  }

  /**
   * Returns the mapping between EDM entity type keys (namespace and entity type name) and their
   * MongoDB collection names.
   *
   * @return mapping of EDM entity type keys to MongoDB collection names
   */
  default Map<KeyValue<String, String>, String> getEdmTablesToMongoDBCollections() {
    return Collections.emptyMap();
  }
}
