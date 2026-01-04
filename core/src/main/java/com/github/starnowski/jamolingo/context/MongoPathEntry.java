package com.github.starnowski.jamolingo.context;

import java.util.Objects;

public final class MongoPathEntry {

  private final String edmPath;
  private final String mongoPath;
  private final boolean key;
  private final String type;
  private final CircularReferenceMapping circularReferenceMapping;

  public CircularReferenceMapping getCircularReferenceMapping() {
    return circularReferenceMapping;
  }

  public static class MongoPathEntryBuilder {
    private String edmPath;
    private String mongoPath;
    private boolean key;

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
        CircularReferenceMapping circularReferenceMapping) {
      this.circularReferenceMapping = circularReferenceMapping;
      return this;
    }

    private String type;
    private CircularReferenceMapping circularReferenceMapping;

    public MongoPathEntry build() {
      return new MongoPathEntry(edmPath, mongoPath, key, type, circularReferenceMapping);
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
        && Objects.equals(circularReferenceMapping, that.circularReferenceMapping);
  }

  @Override
  public int hashCode() {
    return Objects.hash(edmPath, mongoPath, key, type, circularReferenceMapping);
  }

  public MongoPathEntry(
      String edmPath,
      String mongoPath,
      boolean key,
      String type,
      CircularReferenceMapping circularReferenceMapping) {

    this.edmPath = edmPath;
    this.mongoPath = mongoPath;
    this.key = key;
    this.type = type;
    this.circularReferenceMapping = circularReferenceMapping;
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
