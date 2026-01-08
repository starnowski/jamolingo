package com.github.starnowski.jamolingo.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityMapping {
  @Override
  public String toString() {
    return "EntityMapping{"
        + "collection='"
        + collection
        + '\''
        + ", rootPath='"
        + rootPath
        + '\''
        + ", properties="
        + properties
        + '}';
  }

  /** MongoDB collection name */
  @JsonProperty("collection")
  private String collection;

  /** Root path inside Mongo document (optional) Example: "orders[]" */
  @JsonProperty("rootPath")
  private String rootPath;

  /** Scalar & complex properties */
  @JsonProperty("properties")
  private Map<String, PropertyMapping> properties;

  public String getCollection() {
    return collection;
  }

  public EntityMapping withCollection(String collection) {
    this.collection = collection;
    return this;
  }

  public EntityMapping withRootPath(String rootPath) {
    this.rootPath = rootPath;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    EntityMapping that = (EntityMapping) o;
    return Objects.equals(collection, that.collection)
        && Objects.equals(rootPath, that.rootPath)
        && Objects.equals(properties, that.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(collection, rootPath, properties);
  }

  public EntityMapping withProperties(Map<String, PropertyMapping> properties) {
    this.properties = properties;
    return this;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  public String getRootPath() {
    return rootPath;
  }

  public void setRootPath(String rootPath) {
    this.rootPath = rootPath;
  }

  public Map<String, PropertyMapping> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, PropertyMapping> properties) {
    this.properties = properties;
  }
}
