package com.github.starnowski.jamolingo.core.operators;

import java.util.Objects;

/**
 * Represents metadata about an OData navigation property referenced by an operator during query
 * processing.
 */
public class UsedNavigationProperty {

  private final String edmPath;
  private final String mongoPath;
  private final String edmPropertyName;
  private final String edmEntityFullName;
  private final boolean collection;
  private final String localKeyProperty;
  private final String foreignKeyProperty;

  /**
   * Constructs a new UsedNavigationProperty.
   *
   * @param edmPath the EDM path to the navigation property using slash separator
   * @param mongoPath the resolved MongoDB document path using dot notation
   * @param edmPropertyName the name of the navigation property segment
   * @param edmEntityFullName the full qualified name of the target EDM entity type
   * @param collection true if the navigation property is collection-valued, false otherwise
   * @param localKeyProperty the local referential constraint property name
   * @param foreignKeyProperty the foreign referential constraint property name
   */
  public UsedNavigationProperty(
      String edmPath,
      String mongoPath,
      String edmPropertyName,
      String edmEntityFullName,
      boolean collection,
      String localKeyProperty,
      String foreignKeyProperty) {
    this.edmPath = edmPath;
    this.mongoPath = mongoPath;
    this.edmPropertyName = edmPropertyName;
    this.edmEntityFullName = edmEntityFullName;
    this.collection = collection;
    this.localKeyProperty = localKeyProperty;
    this.foreignKeyProperty = foreignKeyProperty;
  }

  /**
   * Returns the EDM path to the navigation property using slash separator.
   *
   * @return the EDM path
   */
  public String getEdmPath() {
    return edmPath;
  }

  /**
   * Returns the resolved MongoDB document path using dot notation.
   *
   * @return the MongoDB path
   */
  public String getMongoPath() {
    return mongoPath;
  }

  /**
   * Returns the name of the navigation property segment.
   *
   * @return the EDM navigation property name
   */
  public String getEdmPropertyName() {
    return edmPropertyName;
  }

  /**
   * Returns the full qualified name of the target EDM entity type.
   *
   * @return the target EDM entity full name
   */
  public String getEdmEntityFullName() {
    return edmEntityFullName;
  }

  /**
   * Indicates whether the navigation property is collection-valued.
   *
   * @return true if collection-valued, false otherwise
   */
  public boolean isCollection() {
    return collection;
  }

  /**
   * Returns the local referential constraint property name.
   *
   * @return the local key property name
   */
  public String getLocalKeyProperty() {
    return localKeyProperty;
  }

  /**
   * Returns the foreign referential constraint property name.
   *
   * @return the foreign key property name
   */
  public String getForeignKeyProperty() {
    return foreignKeyProperty;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UsedNavigationProperty that = (UsedNavigationProperty) o;
    return collection == that.collection
        && Objects.equals(edmPath, that.edmPath)
        && Objects.equals(mongoPath, that.mongoPath)
        && Objects.equals(edmPropertyName, that.edmPropertyName)
        && Objects.equals(edmEntityFullName, that.edmEntityFullName)
        && Objects.equals(localKeyProperty, that.localKeyProperty)
        && Objects.equals(foreignKeyProperty, that.foreignKeyProperty);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        edmPath,
        mongoPath,
        edmPropertyName,
        edmEntityFullName,
        collection,
        localKeyProperty,
        foreignKeyProperty);
  }

  @Override
  public String toString() {
    return "UsedNavigationProperty{"
        + "edmPath='"
        + edmPath
        + '\''
        + ", mongoPath='"
        + mongoPath
        + '\''
        + ", edmPropertyName='"
        + edmPropertyName
        + '\''
        + ", edmEntityFullName='"
        + edmEntityFullName
        + '\''
        + ", collection="
        + collection
        + ", localKeyProperty='"
        + localKeyProperty
        + '\''
        + ", foreignKeyProperty='"
        + foreignKeyProperty
        + '\''
        + '}';
  }

  /**
   * Creates a new builder for UsedNavigationProperty.
   *
   * @return a new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for UsedNavigationProperty. */
  public static class Builder {
    private String edmPath;
    private String mongoPath;
    private String edmPropertyName;
    private String edmEntityFullName;
    private boolean collection;
    private String localKeyProperty;
    private String foreignKeyProperty;

    /**
     * Copies all properties from an existing UsedNavigationProperty instance.
     *
     * @param usedNavigationProperty the instance to copy from
     * @return this builder
     */
    public Builder withUsedNavigationProperty(UsedNavigationProperty usedNavigationProperty) {
      this.edmPath = usedNavigationProperty.edmPath;
      this.mongoPath = usedNavigationProperty.mongoPath;
      this.edmPropertyName = usedNavigationProperty.edmPropertyName;
      this.edmEntityFullName = usedNavigationProperty.edmEntityFullName;
      this.collection = usedNavigationProperty.collection;
      this.localKeyProperty = usedNavigationProperty.localKeyProperty;
      this.foreignKeyProperty = usedNavigationProperty.foreignKeyProperty;
      return this;
    }

    /**
     * Sets the EDM path to the navigation property.
     *
     * @param edmPath the EDM path
     * @return this builder
     */
    public Builder withEdmPath(String edmPath) {
      this.edmPath = edmPath;
      return this;
    }

    /**
     * Sets the resolved MongoDB document path.
     *
     * @param mongoPath the MongoDB path
     * @return this builder
     */
    public Builder withMongoPath(String mongoPath) {
      this.mongoPath = mongoPath;
      return this;
    }

    /**
     * Sets the name of the navigation property segment.
     *
     * @param edmPropertyName the EDM navigation property name
     * @return this builder
     */
    public Builder withEdmPropertyName(String edmPropertyName) {
      this.edmPropertyName = edmPropertyName;
      return this;
    }

    /**
     * Sets the full qualified name of the target EDM entity type.
     *
     * @param edmEntityFullName the target EDM entity full name
     * @return this builder
     */
    public Builder withEdmEntityFullName(String edmEntityFullName) {
      this.edmEntityFullName = edmEntityFullName;
      return this;
    }

    /**
     * Sets whether the navigation property is collection-valued.
     *
     * @param collection true if collection-valued, false otherwise
     * @return this builder
     */
    public Builder withCollection(boolean collection) {
      this.collection = collection;
      return this;
    }

    /**
     * Sets the local referential constraint property name.
     *
     * @param localKeyProperty the local key property name
     * @return this builder
     */
    public Builder withLocalKeyProperty(String localKeyProperty) {
      this.localKeyProperty = localKeyProperty;
      return this;
    }

    /**
     * Sets the foreign referential constraint property name.
     *
     * @param foreignKeyProperty the foreign key property name
     * @return this builder
     */
    public Builder withForeignKeyProperty(String foreignKeyProperty) {
      this.foreignKeyProperty = foreignKeyProperty;
      return this;
    }

    /**
     * Builds a new UsedNavigationProperty instance.
     *
     * @return the created UsedNavigationProperty
     */
    public UsedNavigationProperty build() {
      return new UsedNavigationProperty(
          edmPath,
          mongoPath,
          edmPropertyName,
          edmEntityFullName,
          collection,
          localKeyProperty,
          foreignKeyProperty);
    }
  }
}
