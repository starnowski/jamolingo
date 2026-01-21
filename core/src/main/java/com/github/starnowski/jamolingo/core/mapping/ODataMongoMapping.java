package com.github.starnowski.jamolingo.core.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ODataMongoMapping {
  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ODataMongoMapping that = (ODataMongoMapping) o;
    return Objects.equals(entities, that.entities);
  }

  public ODataMongoMapping withEntities(Map<String, EntityMapping> entities) {
    this.entities = entities;
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(entities);
  }

  /** Key = EDM EntityType name (e.g. "Customer") */
  @JsonProperty("entities")
  private Map<String, EntityMapping> entities;

  public Map<String, EntityMapping> getEntities() {
    return entities;
  }

  public void setEntities(Map<String, EntityMapping> entities) {
    this.entities = entities;
  }

  @Override
  public String toString() {
    return "ODataMongoMapping{" + "entities=" + entities + '}';
  }
}
