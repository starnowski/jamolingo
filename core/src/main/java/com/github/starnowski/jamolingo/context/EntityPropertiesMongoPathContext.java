package com.github.starnowski.jamolingo.context;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

// TODO Add interface
public class EntityPropertiesMongoPathContext {
  public EntityPropertiesMongoPathContext(Map<String, MongoPathEntry> edmToMongoPath) {
    this.edmToMongoPath = Collections.unmodifiableMap(edmToMongoPath);
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
