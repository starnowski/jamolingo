package com.github.starnowski.jamolingo.core.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.starnowski.jamolingo.context.PropertyMapping;

import java.util.Map;
import java.util.Objects;

/** Represents the mapping configuration for an OData entity. */
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

  /**
   * Returns the MongoDB collection name.
   *
   * @return the collection name
   */
  public String getCollection() {
    return collection;
  }

  /**
   * Sets the MongoDB collection name.
   *
   * @param collection the collection name
   * @return this entity mapping
   */
  public EntityMapping withCollection(String collection) {
    this.collection = collection;
    return this;
  }

  /**
   * Sets the root path inside the Mongo document.
   *
   * @param rootPath the root path
   * @return this entity mapping
   */
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

  /**
   * Sets the properties mapping.
   *
   * @param properties the properties mapping
   * @return this entity mapping
   */
  public EntityMapping withProperties(Map<String, PropertyMapping> properties) {
    this.properties = properties;
    return this;
  }

  /**
   * Sets the MongoDB collection name.
   *
   * @param collection the collection name
   */
  public void setCollection(String collection) {
    this.collection = collection;
  }

  /**
   * Returns the root path inside the Mongo document.
   *
   * @return the root path
   */
  public String getRootPath() {
    return rootPath;
  }

  /**
   * Sets the root path inside the Mongo document.
   *
   * @param rootPath the root path
   */
  public void setRootPath(String rootPath) {
    this.rootPath = rootPath;
  }

  /**
   * Returns the properties mapping.
   *
   * @return the properties mapping
   */
  public Map<String, PropertyMapping> getProperties() {
    return properties;
  }

  /**
   * Sets the properties mapping.
   *
   * @param properties the properties mapping
   */
  public void setProperties(Map<String, PropertyMapping> properties) {
    this.properties = properties;
  }
}
