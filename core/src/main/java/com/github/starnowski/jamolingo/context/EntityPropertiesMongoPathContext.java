package com.github.starnowski.jamolingo.context;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static com.github.starnowski.jamolingo.context.Constants.ODATA_PATH_SEPARATOR_CHARACTER;

// TODO Add interface
public class EntityPropertiesMongoPathContext {
  public EntityPropertiesMongoPathContext(Map<String, MongoPathEntry> edmToMongoPath) {
    this.edmToMongoPath = Collections.unmodifiableMap(edmToMongoPath);
  }

  public String resolveMongoPathForEDMPath(String edmPath) {
    if (edmPath == null) {
      return null;
    }
    MongoPathEntry entry = this.edmToMongoPath.get(edmPath);
    if (entry == null) {
      String result = tryToResolveCircularReferencesMongoPath(edmPath);
      if (result == null) {
        throw new InvalidEDMPath("No '%s' EDM path found".formatted(edmPath));
      }
      return result;
    } else {
      return entry.getMongoPath();
    }
  }

  private String tryToResolveCircularReferencesMongoPath(String edmPath) {
    String longestMatchingEDMPath = edmToMongoPath.keySet().stream().filter(edmPath::startsWith).max((s1, s2) -> {
        if (s1.length() > s2.length()) {
            return 1;
        } else if (s1.length() < s2.length()) {
            return -1;
        }
        return 0;
    }).orElse(null);
    if (longestMatchingEDMPath == null) {
      return null;
    }
    // resolve type
    // get type mongoPath
    MongoPathEntry baseEDMProperty = this.edmToMongoPath.get(longestMatchingEDMPath);
    //TODO Check if baseEDMProperty has recurence type
    String baseMongoPath = baseEDMProperty.getMongoPath();
    // TODO in recurence
    // TODO Remove longestMatchingEDMPath from edmPath -> tmpEDMPath
    String tmpEDMPath = edmPath.substring(longestMatchingEDMPath.length());
    MongoPathEntry circuralReferencyType = this.edmToMongoPath.get(baseEDMProperty.getCircularReferenceMapping().getAnchorEdmPath());
    // TODO Concat type EDMPath and tmpEDMPath -> tmpEDMPath
    tmpEDMPath = circuralReferencyType.getEdmPath() + tmpEDMPath;
    // TODO Check if edmToMongoPath has tmpEDMPath
    if (this.edmToMongoPath.containsKey(tmpEDMPath)) {
      // TODO If yes then
      StringBuilder resultBuilder = new StringBuilder(baseMongoPath);
      MongoPathEntry lastElement = this.edmToMongoPath.get(tmpEDMPath);
      resultBuilder.append(lastElement.getMongoPath().substring(circuralReferencyType.getMongoPath().length()));
      return resultBuilder.toString();
    } else {
      String childMongoPath = tryToResolveCircularReferencesMongoPath(tmpEDMPath);
      if (childMongoPath == null){
        return null;
      }
      childMongoPath = childMongoPath.substring(circuralReferencyType.getMongoPath().length());
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

  public static class InvalidEDMPath extends RuntimeException{

    public InvalidEDMPath(String message) {
      super(message);
    }
  }
}
