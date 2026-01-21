package com.github.starnowski.jamolingo.context;

import static com.github.starnowski.jamolingo.core.utils.Constants.ODATA_PATH_SEPARATOR_CHARACTER;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Default implementation of {@link EntityPropertiesMongoPathContext}. */
public class DefaultEntityPropertiesMongoPathContext implements EntityPropertiesMongoPathContext {

  private static final String MONGO_PATH_MAX_DEPTH_EXCEPTION_MESSAGE_PATTERN =
      "Mongo path '%s' for '%s' edm path exceeded max depth %s";

  private static final String MONGO_EXCEEDED_CIRCULAR_REFERENCE_DEPTH_EXCEPTION_MESSAGE_PATTERN =
      "Circular edm path '%s' exceeded max depth %s in main edm path '%s'";

  /**
   * Constructs a new DefaultEntityPropertiesMongoPathContext.
   *
   * @param edmToMongoPath a map of EDM paths to Mongo paths
   */
  public DefaultEntityPropertiesMongoPathContext(Map<String, MongoPathEntry> edmToMongoPath) {
    this.edmToMongoPath = Collections.unmodifiableMap(edmToMongoPath);
  }

  @Override
  public MongoPathResolution resolveMongoPathForEDMPath(String edmPath) {
    return resolveMongoPathForEDMPath(edmPath, DefaultEdmPathContextSearch.builder().build());
  }

  @Override
  public MongoPathResolution resolveMongoPathForEDMPath(
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
        throw new EntityPropertiesMongoPathContext.MongoPathMaxDepthException(
            MONGO_PATH_MAX_DEPTH_EXCEPTION_MESSAGE_PATTERN.formatted(
                e.getMongoPath(), edmPath, edmPathContextSearch.getMongoPathMaxDepth()));
      } catch (InternalMaxCircularLimitPerEdmPathException e) {
        throw new EntityPropertiesMongoPathContext.ExceededCircularReferenceDepthException(
            MONGO_EXCEEDED_CIRCULAR_REFERENCE_DEPTH_EXCEPTION_MESSAGE_PATTERN.formatted(
                e.getEdmPath(), e.getCircularLimit(), edmPath),
            e.getEdmPath(),
            e.getCircularLimit());
      } catch (InternalInvalidAnchorPathException e) {
        throw new EntityPropertiesMongoPathContext.InvalidAnchorPathException(
            String.format(
                "The anchor path '%s' defined in the circular reference mapping for '%s' is not a valid EDM path.",
                e.getAnchorPath(), e.getEdmPath()));
      } catch (InternalExceededTotalCircularReferenceLimitException e) {
        throw new EntityPropertiesMongoPathContext.ExceededTotalCircularReferenceLimitException(
            String.format("Total circular reference limit of %d exceeded.", e.getCircularLimit()));
      }
      if (result == null) {
        throw new EntityPropertiesMongoPathContext.InvalidEDMPathException(
            "No '%s' EDM path found".formatted(edmPath));
      }
      return new DefaultMongoPathResolution(result);
    } else {
      String mongoPath = entry.getMongoPath();
      if (edmPathContextSearch.getMongoPathMaxDepth() == null) {
        return new DefaultMongoPathResolution(mongoPath);
      } else if (mongoPath.split("\\.").length > edmPathContextSearch.getMongoPathMaxDepth()) {
        throw new EntityPropertiesMongoPathContext.MongoPathMaxDepthException(
            MONGO_PATH_MAX_DEPTH_EXCEPTION_MESSAGE_PATTERN.formatted(
                mongoPath, edmPath, edmPathContextSearch.getMongoPathMaxDepth()));
      } else {
        return new DefaultMongoPathResolution(mongoPath);
      }
    }
  }

  private String tryToResolveCircularReferencesMongoPath(
      String edmPath, EdmPathSearchState edmPathSearchState)
      throws InternalMongoPathMaxDepthException,
          InternalMaxCircularLimitPerEdmPathException,
          InternalInvalidAnchorPathException,
          InternalExceededTotalCircularReferenceLimitException {
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
    String anchorPath = baseEDMProperty.getCircularReferenceMapping().getAnchorEdmPath();
    MongoPathEntry circumferentialType = this.edmToMongoPath.get(anchorPath);
    if (circumferentialType == null) {
      throw new InternalInvalidAnchorPathException(anchorPath, longestMatchingEDMPath);
    }
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
                  .withEdmPathCircularReferenceCount(
                      edmPathSearchState.edmPathCircularReferenceCount)
                  .increaseEdmPathCircularReferenceCountForEdmPath(baseEDMProperty.getEdmPath())
                  .build());
      if (childMongoPath == null) {
        return null;
      }
      childMongoPath = childMongoPath.substring(circumferentialType.getMongoPath().length());
      return baseMongoPath + childMongoPath;
    }
  }

  @Override
  public Map<String, MongoPathEntry> getEdmToMongoPath() {
    return edmToMongoPath;
  }

  @Override
  public String toString() {
    return "DefaultEntityPropertiesMongoPathContext{" + "edmToMongoPath=" + edmToMongoPath + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DefaultEntityPropertiesMongoPathContext that = (DefaultEntityPropertiesMongoPathContext) o;
    return Objects.equals(edmToMongoPath, that.edmToMongoPath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(edmToMongoPath);
  }

  private final Map<String, MongoPathEntry> edmToMongoPath;

  private static class DefaultMongoPathResolution implements MongoPathResolution {

    private final String mongoPath;

    private DefaultMongoPathResolution(String mongoPath) {
      this.mongoPath = mongoPath;
    }

    @Override
    public String getMongoPath() {
      return mongoPath;
    }

    @Override
    public String toString() {
      return "DefaultMongoPathResolution{" + "mongoPath='" + mongoPath + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DefaultMongoPathResolution that = (DefaultMongoPathResolution) o;
      return Objects.equals(mongoPath, that.mongoPath);
    }

    @Override
    public int hashCode() {
      return Objects.hash(mongoPath);
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

  private static class InternalInvalidAnchorPathException extends Exception {

    private final String anchorPath;
    private final String edmPath;

    public String getAnchorPath() {
      return anchorPath;
    }

    public String getEdmPath() {
      return edmPath;
    }

    private InternalInvalidAnchorPathException(String anchorPath, String edmPath) {
      this.anchorPath = anchorPath;
      this.edmPath = edmPath;
    }
  }

  private static class InternalExceededTotalCircularReferenceLimitException extends Exception {
    private final int circularLimit;

    public int getCircularLimit() {
      return circularLimit;
    }

    private InternalExceededTotalCircularReferenceLimitException(int circularLimit) {
      this.circularLimit = circularLimit;
    }
  }

  private static class EdmPathSearchState {

    private final EdmPathContextSearch edmPathContextSearch;

    private final String currentMongoPath;

    private final Map<String, Integer> edmPathCircularReferenceCount;

    public EdmPathSearchState(
        EdmPathContextSearch edmPathContextSearch,
        String currentMongoPath,
        Map<String, Integer> edmPathCircularReferenceCount) {

      this.edmPathContextSearch = edmPathContextSearch;

      this.currentMongoPath = currentMongoPath;

      this.edmPathCircularReferenceCount =
          edmPathCircularReferenceCount == null
              ? Collections.emptyMap()
              : Collections.unmodifiableMap(edmPathCircularReferenceCount);
    }

    @Override
    public boolean equals(Object o) {

      if (this == o) return true;

      if (o == null || getClass() != o.getClass()) return false;

      EdmPathSearchState that = (EdmPathSearchState) o;

      return Objects.equals(edmPathContextSearch, that.edmPathContextSearch)
          && Objects.equals(currentMongoPath, that.currentMongoPath)
          && Objects.equals(edmPathCircularReferenceCount, that.edmPathCircularReferenceCount);
    }

    @Override
    public int hashCode() {

      return Objects.hash(edmPathContextSearch, currentMongoPath, edmPathCircularReferenceCount);
    }

    @Override
    public String toString() {

      return "EdmPathSearchState{"
          + "edmPathContextSearch="
          + edmPathContextSearch
          + ", currentMongoPath='"
          + currentMongoPath
          + '\''
          + ", edmPathCircularReferenceCount="
          + edmPathCircularReferenceCount
          + '}';
    }

    private int getTotalEdmPathCircularReferenceCount() {

      return edmPathCircularReferenceCount.values().stream().mapToInt(Integer::intValue).sum();
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

    public void validateMaxCircularLimitPerEdmPathWithAdditionalEdmPath(
        MongoPathEntry mongoPathEntry)
        throws InternalMaxCircularLimitPerEdmPathException,
            InternalExceededTotalCircularReferenceLimitException {

      Integer effectiveMaxCircularLimitPerEdmPath =
          mongoPathEntry.getMaxCircularLimitPerEdmPath() != null
              ? mongoPathEntry.getMaxCircularLimitPerEdmPath()
              : edmPathContextSearch.getMaxCircularLimitPerEdmPath();

      if (effectiveMaxCircularLimitPerEdmPath != null) {

        Integer currentCount = edmPathCircularReferenceCount.get(mongoPathEntry.getEdmPath());

        if ((currentCount == null ? 0 : currentCount) + 1 > effectiveMaxCircularLimitPerEdmPath) {

          throw new InternalMaxCircularLimitPerEdmPathException(
              mongoPathEntry.getEdmPath(), effectiveMaxCircularLimitPerEdmPath);
        }
      }

      if (edmPathContextSearch.getMaxCircularLimitForAllEdmPaths() != null) {

        if (getTotalEdmPathCircularReferenceCount() + 1
            > edmPathContextSearch.getMaxCircularLimitForAllEdmPaths()) {

          throw new InternalExceededTotalCircularReferenceLimitException(
              edmPathContextSearch.getMaxCircularLimitForAllEdmPaths());
        }
      }
    }

    public static EdmPathSearchStateBuilder builder(EdmPathContextSearch edmPathContextSearch) {

      return new EdmPathSearchStateBuilder(edmPathContextSearch);
    }

    public static class EdmPathSearchStateBuilder {

      private EdmPathContextSearch edmPathContextSearch;

      private String currentMongoPath;

      private Map<String, Integer> edmPathCircularReferenceCount;

      public EdmPathSearchStateBuilder withEdmPathCircularReferenceCount(
          Map<String, Integer> edmPathCircularReferenceCount) {

        this.edmPathCircularReferenceCount =
            edmPathCircularReferenceCount == null
                ? null
                : new HashMap<>(edmPathCircularReferenceCount);

        return this;
      }

      private EdmPathSearchStateBuilder(EdmPathContextSearch edmPathContextSearch) {

        this.edmPathContextSearch = edmPathContextSearch;
      }

      public EdmPathSearchStateBuilder withCurrentMongoPath(String currentMongoPath) {

        this.currentMongoPath = currentMongoPath;

        return this;
      }

      public EdmPathSearchStateBuilder increaseEdmPathCircularReferenceCountForEdmPath(
          String edmPath) {

        if (edmPathCircularReferenceCount == null) {

          edmPathCircularReferenceCount = new HashMap<>();
        }

        edmPathCircularReferenceCount.merge(edmPath, 1, Integer::sum);

        return this;
      }

      public EdmPathSearchState build() {

        return new EdmPathSearchState(
            edmPathContextSearch, currentMongoPath, edmPathCircularReferenceCount);
      }
    }
  }
}
