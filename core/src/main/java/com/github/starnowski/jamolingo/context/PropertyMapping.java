package com.github.starnowski.jamolingo.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

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
