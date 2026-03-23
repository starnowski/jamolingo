package com.github.starnowski.jamolingo.core.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/** Represents the mapping configuration for an OData navigation property. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NavigationMapping {

  /** Target MongoDB database name */
  @JsonProperty("database")
  private String database;

  /** Target MongoDB collection name */
  @JsonProperty("collection")
  private String collection;

  /** Field in the current collection used for join */
  @JsonProperty("localField")
  private String localField;

  /** Field in the target collection used for join */
  @JsonProperty("foreignField")
  private String foreignField;

  /**
   * Returns target MongoDB database name.
   *
   * @return database name
   */
  public String getDatabase() {
    return database;
  }

  /**
   * Sets target MongoDB database name.
   *
   * @param database database name
   */
  public void setDatabase(String database) {
    this.database = database;
  }

  /**
   * Sets target MongoDB database name.
   *
   * @param database database name
   * @return current instance
   */
  public NavigationMapping withDatabase(String database) {
    this.database = database;
    return this;
  }

  /**
   * Returns target MongoDB collection name.
   *
   * @return collection name
   */
  public String getCollection() {
    return collection;
  }

  /**
   * Sets target MongoDB collection name.
   *
   * @param collection collection name
   */
  public void setCollection(String collection) {
    this.collection = collection;
  }

  /**
   * Sets target MongoDB collection name.
   *
   * @param collection collection name
   * @return current instance
   */
  public NavigationMapping withCollection(String collection) {
    this.collection = collection;
    return this;
  }

  /**
   * Returns field in the current collection used for join.
   *
   * @return local field
   */
  public String getLocalField() {
    return localField;
  }

  /**
   * Sets field in the current collection used for join.
   *
   * @param localField local field
   */
  public void setLocalField(String localField) {
    this.localField = localField;
  }

  /**
   * Sets field in the current collection used for join.
   *
   * @param localField local field
   * @return current instance
   */
  public NavigationMapping withLocalField(String localField) {
    this.localField = localField;
    return this;
  }

  /**
   * Returns field in the target collection used for join.
   *
   * @return foreign field
   */
  public String getForeignField() {
    return foreignField;
  }

  /**
   * Sets field in the target collection used for join.
   *
   * @param foreignField foreign field
   */
  public void setForeignField(String foreignField) {
    this.foreignField = foreignField;
  }

  /**
   * Sets field in the target collection used for join.
   *
   * @param foreignField foreign field
   * @return current instance
   */
  public NavigationMapping withForeignField(String foreignField) {
    this.foreignField = foreignField;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NavigationMapping that = (NavigationMapping) o;
    return Objects.equals(database, that.database)
        && Objects.equals(collection, that.collection)
        && Objects.equals(localField, that.localField)
        && Objects.equals(foreignField, that.foreignField);
  }

  @Override
  public int hashCode() {
    return Objects.hash(database, collection, localField, foreignField);
  }

  @Override
  public String toString() {
    return "NavigationMapping{"
        + "database='"
        + database
        + '\''
        + ", collection='"
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

  /**
   * Returns new builder instance.
   *
   * @return builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for NavigationMapping. */
  public static class Builder {
    private String database;
    private String collection;
    private String localField;
    private String foreignField;

    /**
     * Sets target MongoDB database name.
     *
     * @param database database name
     * @return builder
     */
    public Builder withDatabase(String database) {
      this.database = database;
      return this;
    }

    /**
     * Sets target MongoDB collection name.
     *
     * @param collection collection name
     * @return builder
     */
    public Builder withCollection(String collection) {
      this.collection = collection;
      return this;
    }

    /**
     * Sets field in the current collection used for join.
     *
     * @param localField local field
     * @return builder
     */
    public Builder withLocalField(String localField) {
      this.localField = localField;
      return this;
    }

    /**
     * Sets field in the target collection used for join.
     *
     * @param foreignField foreign field
     * @return builder
     */
    public Builder withForeignField(String foreignField) {
      this.foreignField = foreignField;
      return this;
    }

    /**
     * Builds NavigationMapping instance.
     *
     * @return navigation mapping
     */
    public NavigationMapping build() {
      NavigationMapping navigationMapping = new NavigationMapping();
      navigationMapping.setDatabase(database);
      navigationMapping.setCollection(collection);
      navigationMapping.setLocalField(localField);
      navigationMapping.setForeignField(foreignField);
      return navigationMapping;
    }
  }
}
