package com.github.starnowski.jamolingo.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ODataMongoMapping {

  /** Key = EDM EntityType name (e.g. "Customer") */
  @JsonProperty("entities")
  private Map<String, EntityMapping> entities;

  public Map<String, EntityMapping> getEntities() {
    return entities;
  }

  public void setEntities(Map<String, EntityMapping> entities) {
    this.entities = entities;
  }
}
