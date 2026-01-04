package com.github.starnowski.jamolingo.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyMapping {

  /** Marks OData key property */
  @JsonProperty("key")
  private Boolean key;

  /** MongoDB absolute path (dot notation) */
  @JsonProperty("mongoPath")
  private String mongoPath;

  /** MongoDB field name override (used with relativeTo) */
  @JsonProperty("mongoName")
  private String mongoName;

  /** Base path for nested properties (complex types) */
  @JsonProperty("relativeTo")
  private String relativeTo;

  /** Mongo type hint (ObjectId, Decimal128, Date, etc.) */
  @JsonProperty("type")
  private String type;

  @JsonProperty("circularReferenceMapping")
  private CircularReferenceMapping circularReferenceMapping;

  public Integer getFlattenedLevelUp() {
    return flattenedLevelUp;
  }

  public void setCircularReferenceMapping(CircularReferenceMapping circularReferenceMapping) {
    this.circularReferenceMapping = circularReferenceMapping;
  }

  public CircularReferenceMapping getCircularReferenceMapping() {
    return circularReferenceMapping;
  }

  public PropertyMapping withCircularReferenceMapping(
      CircularReferenceMapping circularReferenceMapping) {
    this.circularReferenceMapping = circularReferenceMapping;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    PropertyMapping that = (PropertyMapping) o;
    return Objects.equals(key, that.key)
        && Objects.equals(mongoPath, that.mongoPath)
        && Objects.equals(mongoName, that.mongoName)
        && Objects.equals(relativeTo, that.relativeTo)
        && Objects.equals(type, that.type)
        && Objects.equals(circularReferenceMapping, that.circularReferenceMapping)
        && Objects.equals(flattenedLevelUp, that.flattenedLevelUp)
        && Objects.equals(ignore, that.ignore)
        && Objects.equals(computed, that.computed)
        && Objects.equals(properties, that.properties);
  }

  @Override
  public String toString() {
    return "PropertyMapping{"
        + "key="
        + key
        + ", mongoPath='"
        + mongoPath
        + '\''
        + ", mongoName='"
        + mongoName
        + '\''
        + ", relativeTo='"
        + relativeTo
        + '\''
        + ", type='"
        + type
        + '\''
        + ", circularReferenceMapping="
        + circularReferenceMapping
        + ", flattenedLevelUp="
        + flattenedLevelUp
        + ", ignore="
        + ignore
        + ", computed="
        + computed
        + ", properties="
        + properties
        + '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        key,
        mongoPath,
        mongoName,
        relativeTo,
        type,
        circularReferenceMapping,
        flattenedLevelUp,
        ignore,
        computed,
        properties);
  }

  public PropertyMapping withFlattenedLevelUp(Integer flatterLevelUp) {
    this.flattenedLevelUp = flatterLevelUp;
    return this;
  }

  public void setFlattenedLevelUp(Integer flattenedLevelUp) {
    this.flattenedLevelUp = flattenedLevelUp;
  }

  @JsonProperty("flattenedLevelUp")
  private Integer flattenedLevelUp;

  public PropertyMapping withKey(Boolean key) {
    this.key = key;
    return this;
  }

  public PropertyMapping withMongoPath(String mongoPath) {
    this.mongoPath = mongoPath;
    return this;
  }

  public PropertyMapping withMongoName(String mongoName) {
    this.mongoName = mongoName;
    return this;
  }

  public PropertyMapping withRelativeTo(String relativeTo) {
    this.relativeTo = relativeTo;
    return this;
  }

  public PropertyMapping withType(String type) {
    this.type = type;
    return this;
  }

  public PropertyMapping withIgnore(Boolean ignore) {
    this.ignore = ignore;
    return this;
  }

  public PropertyMapping withComputed(Boolean computed) {
    this.computed = computed;
    return this;
  }

  public PropertyMapping withProperties(Map<String, PropertyMapping> properties) {
    this.properties = properties;
    return this;
  }

  /** Ignore this property entirely */
  @JsonProperty("ignore")
  private Boolean ignore;

  /** Read-only or computed field */
  @JsonProperty("computed")
  private Boolean computed;

  /** Nested properties for complex types */
  @JsonProperty("properties")
  private Map<String, PropertyMapping> properties;

  public Boolean getKey() {
    return key;
  }

  public void setKey(Boolean key) {
    this.key = key;
  }

  public String getMongoPath() {
    return mongoPath;
  }

  public void setMongoPath(String mongoPath) {
    this.mongoPath = mongoPath;
  }

  public String getMongoName() {
    return mongoName;
  }

  public void setMongoName(String mongoName) {
    this.mongoName = mongoName;
  }

  public String getRelativeTo() {
    return relativeTo;
  }

  public void setRelativeTo(String relativeTo) {
    this.relativeTo = relativeTo;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Boolean getIgnore() {
    return ignore;
  }

  public void setIgnore(Boolean ignore) {
    this.ignore = ignore;
  }

  public Boolean getComputed() {
    return computed;
  }

  public void setComputed(Boolean computed) {
    this.computed = computed;
  }

  public Map<String, PropertyMapping> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, PropertyMapping> properties) {
    this.properties = properties;
  }
}
