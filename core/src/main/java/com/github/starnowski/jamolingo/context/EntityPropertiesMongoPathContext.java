package com.github.starnowski.jamolingo.context;

import java.util.Map;

public interface EntityPropertiesMongoPathContext {

  String resolveMongoPathForEDMPath(String edmPath);

  String resolveMongoPathForEDMPath(String edmPath, EdmPathContextSearch edmPathContextSearch);

  Map<String, MongoPathEntry> getEdmToMongoPath();

  static class EntityPropertiesMongoPathContextException extends RuntimeException {
    public EntityPropertiesMongoPathContextException() {}

    public EntityPropertiesMongoPathContextException(String message) {
      super(message);
    }

    public EntityPropertiesMongoPathContextException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  static class InvalidEDMPathException extends EntityPropertiesMongoPathContextException {

    public InvalidEDMPathException(String message) {
      super(message);
    }
  }

  static class ExceededCircularReferenceDepthException
      extends EntityPropertiesMongoPathContextException {

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

  static class MongoPathMaxDepthException extends EntityPropertiesMongoPathContextException {

    public MongoPathMaxDepthException(String message) {
      super(message);
    }
  }

  static class InvalidAnchorPathException extends EntityPropertiesMongoPathContextException {
    public InvalidAnchorPathException(String message) {
      super(message);
    }
  }

  static class ExceededTotalCircularReferenceLimitException
      extends EntityPropertiesMongoPathContextException {
    public ExceededTotalCircularReferenceLimitException(String message) {
      super(message);
    }
  }
}
