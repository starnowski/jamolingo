package com.github.starnowski.jamolingo.junit5;

import java.util.Objects;

/** Holds cached definition and database state values for a created Atlas Search Index. */
public class CachedAtlasSearchIndexDefinition {

  private final String defValue;
  private final String stateValue;

  /**
   * Constructs a new CachedAtlasSearchIndexDefinition.
   *
   * @param defValue the string content of the index definition file used for creation
   * @param stateValue the JSON representation of the index definition returned by the MongoDB
   *     client
   */
  public CachedAtlasSearchIndexDefinition(String defValue, String stateValue) {
    this.defValue = defValue;
    this.stateValue = stateValue;
  }

  /**
   * Returns the string content of the index definition file used for creation.
   *
   * @return the definition value
   */
  public String getDefValue() {
    return defValue;
  }

  /**
   * Returns the JSON representation of the index definition returned by the MongoDB client.
   *
   * @return the state value
   */
  public String getStateValue() {
    return stateValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CachedAtlasSearchIndexDefinition that = (CachedAtlasSearchIndexDefinition) o;
    return Objects.equals(defValue, that.defValue) && Objects.equals(stateValue, that.stateValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(defValue, stateValue);
  }

  @Override
  public String toString() {
    return "CachedAtlasSearchIndexDefinition{"
        + "defValue='"
        + defValue
        + '\''
        + ", stateValue='"
        + stateValue
        + '\''
        + '}';
  }

  /**
   * Creates a new builder instance for CachedAtlasSearchIndexDefinition.
   *
   * @return a new Builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link CachedAtlasSearchIndexDefinition}. */
  public static class Builder {
    private String defValue;
    private String stateValue;

    /**
     * Copies properties from an existing {@link CachedAtlasSearchIndexDefinition}.
     *
     * @param cachedAtlasSearchIndexDefinition the existing instance to copy from
     * @return this builder instance
     */
    public Builder withCachedAtlasSearchIndexDefinition(
        CachedAtlasSearchIndexDefinition cachedAtlasSearchIndexDefinition) {
      this.defValue = cachedAtlasSearchIndexDefinition.defValue;
      this.stateValue = cachedAtlasSearchIndexDefinition.stateValue;
      return this;
    }

    /**
     * Sets the string content of the index definition file used for creation.
     *
     * @param defValue the definition value
     * @return this builder instance
     */
    public Builder withDefValue(String defValue) {
      this.defValue = defValue;
      return this;
    }

    /**
     * Sets the JSON representation of the index definition returned by the MongoDB client.
     *
     * @param stateValue the state value
     * @return this builder instance
     */
    public Builder withStateValue(String stateValue) {
      this.stateValue = stateValue;
      return this;
    }

    /**
     * Builds a new {@link CachedAtlasSearchIndexDefinition} instance.
     *
     * @return a new CachedAtlasSearchIndexDefinition
     */
    public CachedAtlasSearchIndexDefinition build() {
      return new CachedAtlasSearchIndexDefinition(defValue, stateValue);
    }
  }
}
