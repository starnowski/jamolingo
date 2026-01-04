package com.github.starnowski.jamolingo.context;

import static com.github.starnowski.jamolingo.context.Constants.ODATA_PATH_SEPARATOR_CHARACTER;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// TODO Add interface
public class EntityPropertiesMongoPathContext {

  private static final String MONGO_PATH_MAX_DEPTH_EXCEPTION_MESSAGE_PATTERN =
      "Mongo path '%s' for '%s' edm path exceeded max depth %s";

  private static final String MONGO_EXCEEDED_CIRCULAR_REFERENCE_DEPTH_EXCEPTION_MESSAGE_PATTERN =
  "Circular edm path '%s' exceeded max depth %s in main edm path '%s'";

  public EntityPropertiesMongoPathContext(Map<String, MongoPathEntry> edmToMongoPath) {
    this.edmToMongoPath = Collections.unmodifiableMap(edmToMongoPath);
  }

  public String resolveMongoPathForEDMPath(String edmPath) {
    return resolveMongoPathForEDMPath(edmPath, DefaultEdmPathContextSearch.builder().build());
  }

  public String resolveMongoPathForEDMPath(
      String edmPath, EdmPathContextSearch edmPathContextSearch) {
    if (edmPath == null) {
      return null;
    }
    MongoPathEntry entry = this.edmToMongoPath.get(edmPath);
    if (entry == null) {
      String result = null;
      try {
        result =
            tryToResolveCircularReferencesMongoPath(
                edmPath, EdmPathSearchState.builder(edmPathContextSearch).build());
      } catch (InternalMongoPathMaxDepthException e) {
        throw new MongoPathMaxDepthException(
            MONGO_PATH_MAX_DEPTH_EXCEPTION_MESSAGE_PATTERN.formatted(
                e.getMongoPath(), edmPath, edmPathContextSearch.getMongoPathMaxDepth()));
      } catch (InternalMaxCircularLimitPerEdmPathException e) {
          throw new ExceededCircularReferenceDepthException(MONGO_EXCEEDED_CIRCULAR_REFERENCE_DEPTH_EXCEPTION_MESSAGE_PATTERN.formatted(
                  e.getEdmPath(), e.getCircularLimit(), edmPath), e.getEdmPath(), e.getCircularLimit());
      }
        if (result == null) {
        throw new InvalidEDMPathException("No '%s' EDM path found".formatted(edmPath));
      }
      return result;
    } else {
      String mongoPath = entry.getMongoPath();
      if (edmPathContextSearch.getMongoPathMaxDepth() == null) {
        return mongoPath;
      } else if (mongoPath.split("\\.").length > edmPathContextSearch.getMongoPathMaxDepth()) {
        throw new MongoPathMaxDepthException(
            MONGO_PATH_MAX_DEPTH_EXCEPTION_MESSAGE_PATTERN.formatted(
                mongoPath, edmPath, edmPathContextSearch.getMongoPathMaxDepth()));
      } else {
        return mongoPath;
      }
    }
  }

  private String tryToResolveCircularReferencesMongoPath(
      String edmPath, EdmPathSearchState edmPathSearchState)
          throws InternalMongoPathMaxDepthException, InternalMaxCircularLimitPerEdmPathException {
    String longestMatchingEDMPath =
        edmToMongoPath.keySet().stream()
            .filter(edmPath::startsWith)
            .max(
                (s1, s2) -> {
                  if (s1.length() > s2.length()) {
                    return 1;
                  } else if (s1.length() < s2.length()) {
                    return -1;
                  }
                  return 0;
                })
            .orElse(null);
    if (longestMatchingEDMPath == null) {
      return null;
    }
    if (!edmPath
        .substring(longestMatchingEDMPath.length())
        .startsWith(ODATA_PATH_SEPARATOR_CHARACTER)) {
      /*
       * Although the chosen edm part is a prefix for edmPath, the other part does not start with '/' character
       * which means that chosen part does not match because it is not correct parent.
       */
      return null;
    }
    // resolve type
    // get type mongoPath
    MongoPathEntry baseEDMProperty = this.edmToMongoPath.get(longestMatchingEDMPath);
    // Check if baseEDMProperty has recurrence type
    if (baseEDMProperty.getCircularReferenceMapping() == null
        || baseEDMProperty.getCircularReferenceMapping().getAnchorEdmPath() == null) {
      return null;
    }
    String baseMongoPath = baseEDMProperty.getMongoPath();
    edmPathSearchState.validateCurrentWithAppliedPath(baseMongoPath);
    edmPathSearchState.validateMaxCircularLimitPerEdmPathWithAdditionalEdmPath(baseEDMProperty);
    // Remove longestMatchingEDMPath from edmPath -> tmpEDMPath
    String tmpEDMPath = edmPath.substring(longestMatchingEDMPath.length());
    MongoPathEntry circumferentialType =
        this.edmToMongoPath.get(baseEDMProperty.getCircularReferenceMapping().getAnchorEdmPath());
    //TODO
    // Concat type EDMPath and tmpEDMPath -> tmpEDMPath
    tmpEDMPath = circumferentialType.getEdmPath() + tmpEDMPath;
    // Check if edmToMongoPath has tmpEDMPath
    if (this.edmToMongoPath.containsKey(tmpEDMPath)) {
      StringBuilder resultBuilder = new StringBuilder(baseMongoPath);
      MongoPathEntry lastElement = this.edmToMongoPath.get(tmpEDMPath);
      resultBuilder.append(
          lastElement.getMongoPath().substring(circumferentialType.getMongoPath().length()));
      return resultBuilder.toString();
    } else {
      String childMongoPath =
          tryToResolveCircularReferencesMongoPath(
              tmpEDMPath,
              EdmPathSearchState.builder(edmPathSearchState.edmPathContextSearch)
                  .withCurrentMongoPath(
                      edmPathSearchState.currentMongoPath + "." + baseMongoPath.split("\\.").length)
                      .withEdmPathCircularReferenceCount(edmPathSearchState.edmPathCircularReferenceCount)
                      .increaseEdmPathCircularReferenceCountForEdmPath(baseEDMProperty.getEdmPath())
                  .build());
      if (childMongoPath == null) {
        return null;
      }
      childMongoPath = childMongoPath.substring(circumferentialType.getMongoPath().length());
      return baseMongoPath + childMongoPath;
    }
  }

  public Map<String, MongoPathEntry> getEdmToMongoPath() {
    return edmToMongoPath;
  }

  @Override
  public String toString() {
    return "EntityPropertiesMongoPathContext{" + "edmToMongoPath=" + edmToMongoPath + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    EntityPropertiesMongoPathContext that = (EntityPropertiesMongoPathContext) o;
    return Objects.equals(edmToMongoPath, that.edmToMongoPath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(edmToMongoPath);
  }

  private final Map<String, MongoPathEntry> edmToMongoPath;

  public static class EntityPropertiesMongoPathContextException extends RuntimeException {
    public EntityPropertiesMongoPathContextException() {}

    public EntityPropertiesMongoPathContextException(String message) {
      super(message);
    }

    public EntityPropertiesMongoPathContextException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static class InvalidEDMPathException extends EntityPropertiesMongoPathContextException {

    public InvalidEDMPathException(String message) {
      super(message);
    }
  }

  public static class ExceededCircularReferenceDepthException
      extends EntityPropertiesMongoPathContextException {

    private final String edmPathReference;

    public int getExceededLimit() {
      return exceededLimit;
    }

    public String getEdmPathReference() {
      return edmPathReference;
    }

    private final int exceededLimit;

    public ExceededCircularReferenceDepthException(String message, String edmPathReference, int exceededLimit) {
      super(message);
        this.edmPathReference = edmPathReference;
        this.exceededLimit = exceededLimit;
    }
  }

  public static class MongoPathMaxDepthException extends EntityPropertiesMongoPathContextException {

    public MongoPathMaxDepthException(String message) {
      super(message);
    }
  }

  private static class InternalMongoPathMaxDepthException extends Exception {

    private final String mongoPath;

    public String getMongoPath() {
      return mongoPath;
    }

    private InternalMongoPathMaxDepthException(String mongoPath) {
      this.mongoPath = mongoPath;
    }
  }

  private static class InternalMaxCircularLimitPerEdmPathException extends Exception {

    private final String edmPath;
    private final int circularLimit;

    public int getCircularLimit() {
      return circularLimit;
    }

    public String getEdmPath() {
      return edmPath;
    }

    private InternalMaxCircularLimitPerEdmPathException(String edmPath, int circularLimit) {
          this.edmPath = edmPath;
        this.circularLimit = circularLimit;
    }
  }

  private static class EdmPathSearchState {
    private final EdmPathContextSearch edmPathContextSearch;
    private final String currentMongoPath;
    private final Map<String, Integer> edmPathCircularReferenceCount;

    public EdmPathSearchState(EdmPathContextSearch edmPathContextSearch, String currentMongoPath, Map<String, Integer> edmPathCircularReferenceCount) {
      this.edmPathContextSearch = edmPathContextSearch;
      this.currentMongoPath = currentMongoPath;
        this.edmPathCircularReferenceCount = edmPathCircularReferenceCount == null ? Collections.emptyMap() : Collections.unmodifiableMap(edmPathCircularReferenceCount);
    }

    public void validateCurrentWithAppliedPath(String nextPartOfPath)
        throws InternalMongoPathMaxDepthException {
      String path =
          currentMongoPath == null ? nextPartOfPath : currentMongoPath + "." + nextPartOfPath;
      if (edmPathContextSearch.getMongoPathMaxDepth() != null
          && (path.split("\\.").length > edmPathContextSearch.getMongoPathMaxDepth())) {
        throw new InternalMongoPathMaxDepthException(path);
      }
    }

    public void validateMaxCircularLimitPerEdmPathWithAdditionalEdmPath(MongoPathEntry mongoPathEntry) throws InternalMaxCircularLimitPerEdmPathException {
      if (edmPathContextSearch.getMaxCircularLimitPerEdmPath() == null) {
        return;
      }
      Integer currentCount = edmPathCircularReferenceCount.get(mongoPathEntry.getEdmPath());
      if ((currentCount == null ? 0 : currentCount) + 1 > edmPathContextSearch.getMaxCircularLimitPerEdmPath()){
        throw new InternalMaxCircularLimitPerEdmPathException(mongoPathEntry.getEdmPath(), edmPathContextSearch.getMaxCircularLimitPerEdmPath());
      }
    }

    public static EdmPathSearchState.Builder builder(EdmPathContextSearch edmPathContextSearch) {
      return new EdmPathSearchState.Builder(edmPathContextSearch);
    }

    public static class Builder {
      private EdmPathContextSearch edmPathContextSearch;
      private String currentMongoPath;
      private Map<String, Integer> edmPathCircularReferenceCount;

      public Builder withEdmPathCircularReferenceCount(Map<String, Integer> edmPathCircularReferenceCount) {
        this.edmPathCircularReferenceCount = edmPathCircularReferenceCount == null ? null : new HashMap<>(edmPathCircularReferenceCount);
        return this;
      }

      private Builder(EdmPathContextSearch edmPathContextSearch) {
        this.edmPathContextSearch = edmPathContextSearch;
      }

      public Builder withCurrentMongoPath(String currentMongoPath) {
        this.currentMongoPath = currentMongoPath;
        return this;
      }

      public Builder increaseEdmPathCircularReferenceCountForEdmPath(String edmPath) {
        if (edmPathCircularReferenceCount == null) {
          edmPathCircularReferenceCount = new HashMap<>();
        }
        edmPathCircularReferenceCount.merge(edmPath, 1, Integer::sum
        );
        return this;
      }


      public EdmPathSearchState build() {
        return new EdmPathSearchState(edmPathContextSearch, currentMongoPath, edmPathCircularReferenceCount);
      }
    }
  }
}
