package com.github.starnowski.jamolingo.core.context;

import com.github.starnowski.jamolingo.context.EdmPathContextSearch;
import com.github.starnowski.jamolingo.context.MongoPathEntry;
import com.github.starnowski.jamolingo.context.MongoPathResolution;

import java.util.Map;

/** Context for resolving EDM property paths to MongoDB field paths. */
public interface EntityPropertiesMongoPathContext {

  /**
   * Resolves the MongoDB path for a given EDM path.
   *
   * @param edmPath the EDM property path (e.g., "Address/City")
   * @return the resolved MongoDB path resolution
   */
  MongoPathResolution resolveMongoPathForEDMPath(String edmPath);

  /**
   * Resolves the MongoDB path for a given EDM path with specific search context.
   *
   * @param edmPath the EDM property path
   * @param edmPathContextSearch the search configuration
   * @return the resolved MongoDB path resolution
   */
  MongoPathResolution resolveMongoPathForEDMPath(
      String edmPath, EdmPathContextSearch edmPathContextSearch);

  /**
   * Returns the mapping of EDM paths to Mongo path entries.
   *
   * @return the mapping map
   */
  Map<String, MongoPathEntry> getEdmToMongoPath();

  /** Base exception for errors occurring within {@link EntityPropertiesMongoPathContext}. */
  class EntityPropertiesMongoPathContextException extends RuntimeException {
    public EntityPropertiesMongoPathContextException() {}

    public EntityPropertiesMongoPathContextException(String message) {
      super(message);
    }

    public EntityPropertiesMongoPathContextException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /** Exception thrown when an invalid EDM path is encountered. */
  class InvalidEDMPathException extends EntityPropertiesMongoPathContextException {

    public InvalidEDMPathException(String message) {
      super(message);
    }
  }

  /** Exception thrown when the circular reference depth limit is exceeded. */
  class ExceededCircularReferenceDepthException extends EntityPropertiesMongoPathContextException {

    private final String edmPathReference;

    public int getExceededLimit() {
      return exceededLimit;
    }

    public String getEdmPathReference() {
      return edmPathReference;
    }

    private final int exceededLimit;

    public ExceededCircularReferenceDepthException(
        String message, String edmPathReference, int exceededLimit) {
      super(message);
      this.edmPathReference = edmPathReference;
      this.exceededLimit = exceededLimit;
    }
  }

  /** Exception thrown when the MongoDB path exceeds the maximum allowed depth. */
  class MongoPathMaxDepthException extends EntityPropertiesMongoPathContextException {

    public MongoPathMaxDepthException(String message) {
      super(message);
    }
  }

  /**
   * Exception thrown when an invalid anchor path is encountered in a circular reference mapping.
   */
  class InvalidAnchorPathException extends EntityPropertiesMongoPathContextException {
    public InvalidAnchorPathException(String message) {
      super(message);
    }
  }

  /** Exception thrown when the total circular reference limit is exceeded. */
  class ExceededTotalCircularReferenceLimitException
      extends EntityPropertiesMongoPathContextException {
    public ExceededTotalCircularReferenceLimitException(String message) {
      super(message);
    }
  }
}
