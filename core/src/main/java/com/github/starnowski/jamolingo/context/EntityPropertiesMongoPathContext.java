package com.github.starnowski.jamolingo.context;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

// TODO Add interface
public class EntityPropertiesMongoPathContext {
  public EntityPropertiesMongoPathContext(Map<String, MongoPathEntry> edmToMongoPath) {
    this.edmToMongoPath = Collections.unmodifiableMap(edmToMongoPath);
  }

  public String resolveMongoPathForEDMPath(String edmPath) {
    //TODO
    MongoPathEntry entry = this.edmToMongoPath.get(edmPath);
    if (entry == null) {
      //TODO
      String longestMatchingEDMPath = edmToMongoPath.keySet().stream().filter(edmPath::startsWith).max((s1, s2) -> {
          if (s1.length() > s2.length()) {
              return 1;
          } else if (s1.length() < s2.length()) {
              return -1;
          }
          return 0;
      }).orElse(null);
      if (longestMatchingEDMPath == null) {
        //TODO exception
      }
      String baseMongoPath = this.edmToMongoPath.get(longestMatchingEDMPath).getMongoPath();
      //TODO resolve type
      //TODO get type mongoPath
      // TODO in recurence
      // TODO Remove longestMatchingEDMPath from edmPath -> tmpEDMPath
      // TODO Concat type EDMPath and tmpEDMPath -> tmpEDMPath
      // TODO Check if edmToMongoPath has tmpEDMPath
      // TODO If yes then
      // TODO end recurence
      return baseMongoPath;
    } else {
      return entry.getMongoPath();
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
}
