package com.github.starnowski.jamolingo.core.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/** Represents the mapping configuration for an OData navigation property. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NavigationMapping {

  /** Target MongoDB collection name */
  @JsonProperty("collection")
  private String collection;

  /** Field in the current collection used for join */
  @JsonProperty("localField")
  private String localField;

  /** Field in the target collection used for join */
  @JsonProperty("foreignField")
  private String foreignField;

  public String getCollection() {
    return collection;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  public NavigationMapping withCollection(String collection) {
    this.collection = collection;
    return this;
  }

  public String getLocalField() {
    return localField;
  }

  public void setLocalField(String localField) {
    this.localField = localField;
  }

  public NavigationMapping withLocalField(String localField) {
    this.localField = localField;
    return this;
  }

  public String getForeignField() {
    return foreignField;
  }

  public void setForeignField(String foreignField) {
    this.foreignField = foreignField;
  }

  public NavigationMapping withForeignField(String foreignField) {
    this.foreignField = foreignField;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NavigationMapping that = (NavigationMapping) o;
    return Objects.equals(collection, that.collection)
        && Objects.equals(localField, that.localField)
        && Objects.equals(foreignField, that.foreignField);
  }

  @Override
  public int hashCode() {
    return Objects.hash(collection, localField, foreignField);
  }

  @Override
  public String toString() {
    return "NavigationMapping{"
        + "collection='"
        + collection
        + '\''
        + ", localField='"
        + localField
        + '\''
        + ", foreignField='"
        + foreignField
        + '\''
        + '}';
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String collection;
    private String localField;
    private String foreignField;

    public Builder withCollection(String collection) {
      this.collection = collection;
      return this;
    }

    public Builder withLocalField(String localField) {
      this.localField = localField;
      return this;
    }

    public Builder withForeignField(String foreignField) {
      this.foreignField = foreignField;
      return this;
    }

    public NavigationMapping build() {
      NavigationMapping navigationMapping = new NavigationMapping();
      navigationMapping.setCollection(collection);
      navigationMapping.setLocalField(localField);
      navigationMapping.setForeignField(foreignField);
      return navigationMapping;
    }
  }
}
