package com.github.starnowski.jamolingo.core.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;

/** Represents the mapping configuration for an OData entity. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityMapping {
  @Override
  public String toString() {
    return "EntityMapping{"
        + "namespace='"
        + namespace
        + '\''
        + ", table='"
        + table
        + '\''
        + ", rootPath='"
        + rootPath
        + '\''
        + ", properties="
        + properties
        + '}';
  }

  /** EDM namespace name */
  @JsonProperty("namespace")
  private String namespace;

  /** EDM table name */
  @JsonProperty("table")
  private String table;

  /** Root path inside Mongo document (optional) Example: "orders[]" */
  @JsonProperty("rootPath")
  private String rootPath;

  /** Scalar & complex properties */
  @JsonProperty("properties")
  private Map<String, PropertyMapping> properties;

  /**
   * Returns the EDM namespace name.
   *
   * @return the EDM namespace name
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * Sets the EDM namespace name.
   *
   * @param namespace the EDM namespace name
   */
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  /**
   * Sets the EDM namespace name.
   *
   * @param namespace EDM namespace name
   * @return this entity mapping
   */
  public EntityMapping withNamespace(String namespace) {
    this.namespace = namespace;
    return this;
  }

  /**
   * Returns the MongoDB collection name.
   *
   * @return the collection name
   */
  public String getTable() {
    return table;
  }

  /**
   * Sets the EDM table name.
   *
   * @param table the EDM table name
   * @return this entity mapping
   */
  public EntityMapping withTable(String table) {
    this.table = table;
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
    return Objects.equals(namespace, that.namespace)
        && Objects.equals(table, that.table)
        && Objects.equals(rootPath, that.rootPath)
        && Objects.equals(properties, that.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(namespace, table, rootPath, properties);
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
   * Sets the EDM table name.
   *
   * @param table the table name
   */
  public void setTable(String table) {
    this.table = table;
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
