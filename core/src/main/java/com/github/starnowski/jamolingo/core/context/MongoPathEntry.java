package com.github.starnowski.jamolingo.core.context;

import com.github.starnowski.jamolingo.core.mapping.CircularReferenceMappingRecord;
import java.util.Objects;

public final class MongoPathEntry {

  private final String edmPath;
  private final String mongoPath;
  private final boolean key;
  private final String type;
  private final CircularReferenceMappingRecord circularReferenceMapping;
  private final Integer maxCircularLimitPerEdmPath;

  public CircularReferenceMappingRecord getCircularReferenceMapping() {
    return circularReferenceMapping;
  }

  public Integer getMaxCircularLimitPerEdmPath() {
    return maxCircularLimitPerEdmPath;
  }

  public static class MongoPathEntryBuilder {
    private String edmPath;
    private String mongoPath;
    private boolean key;
    private String type;
    private CircularReferenceMappingRecord circularReferenceMapping;
    private Integer maxCircularLimitPerEdmPath;

    public MongoPathEntryBuilder withEdmPath(String edmPath) {
      this.edmPath = edmPath;
      return this;
    }

    public MongoPathEntryBuilder withMongoPath(String mongoPath) {
      this.mongoPath = mongoPath;
      return this;
    }

    public MongoPathEntryBuilder withKey(boolean key) {
      this.key = key;
      return this;
    }

    public MongoPathEntryBuilder withType(String type) {
      this.type = type;
      return this;
    }

    public MongoPathEntryBuilder withCircularReferenceMapping(
        CircularReferenceMappingRecord circularReferenceMapping) {
      this.circularReferenceMapping = circularReferenceMapping;
      return this;
    }

    public MongoPathEntryBuilder withMaxCircularLimitPerEdmPath(
        Integer maxCircularLimitPerEdmPath) {
      this.maxCircularLimitPerEdmPath = maxCircularLimitPerEdmPath;
      return this;
    }

    public MongoPathEntry build() {
      return new MongoPathEntry(
          edmPath, mongoPath, key, type, circularReferenceMapping, maxCircularLimitPerEdmPath);
    }
  }

  @Override
  public String toString() {
    return "MongoPathEntry{"
        + "edmPath='"
        + edmPath
        + '\''
        + ", mongoPath='"
        + mongoPath
        + '\''
        + ", key="
        + key
        + ", type='"
        + type
        + '\''
        + ", circularReferenceMapping="
        + circularReferenceMapping
        + ", maxCircularLimitPerEdmPath="
        + maxCircularLimitPerEdmPath
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    MongoPathEntry that = (MongoPathEntry) o;
    return key == that.key
        && Objects.equals(edmPath, that.edmPath)
        && Objects.equals(mongoPath, that.mongoPath)
        && Objects.equals(type, that.type)
        && Objects.equals(circularReferenceMapping, that.circularReferenceMapping)
        && Objects.equals(maxCircularLimitPerEdmPath, that.maxCircularLimitPerEdmPath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        edmPath, mongoPath, key, type, circularReferenceMapping, maxCircularLimitPerEdmPath);
  }

  public MongoPathEntry(
      String edmPath,
      String mongoPath,
      boolean key,
      String type,
      CircularReferenceMappingRecord circularReferenceMapping,
      Integer maxCircularLimitPerEdmPath) {

    this.edmPath = edmPath;
    this.mongoPath = mongoPath;
    this.key = key;
    this.type = type;
    this.circularReferenceMapping = circularReferenceMapping;
    this.maxCircularLimitPerEdmPath = maxCircularLimitPerEdmPath;
  }

  public String getEdmPath() {
    return edmPath;
  }

  public String getMongoPath() {
    return mongoPath;
  }

  public boolean isKey() {
    return key;
  }

  public String getType() {
    return type;
  }
}
