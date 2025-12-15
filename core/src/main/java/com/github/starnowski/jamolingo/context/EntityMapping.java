package com.github.starnowski.jamolingo.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityMapping {

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
