package com.github.starnowski.jamolingo.junit5;

import java.util.Objects;

/**
 * Tuple key representing a unique identifier for an Atlas Search Index, consisting of the database
 * name, the collection name, and the index name.
 */
public class AtlasSearchIndexKey {

  private final String database;
  private final String collection;
  private final String indexName;

  /**
   * Constructs a new AtlasSearchIndexKey.
   *
   * @param database the database name
   * @param collection the collection name
   * @param indexName the Atlas Search index name
   */
  public AtlasSearchIndexKey(String database, String collection, String indexName) {
    this.database = database;
    this.collection = collection;
    this.indexName = indexName;
  }

  /**
   * Returns the database name.
   *
   * @return the database name
   */
  public String getDatabase() {
    return database;
  }

  /**
   * Returns the collection name.
   *
   * @return the collection name
   */
  public String getCollection() {
    return collection;
  }

  /**
   * Returns the Atlas Search index name.
   *
   * @return the index name
   */
  public String getIndexName() {
    return indexName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AtlasSearchIndexKey that = (AtlasSearchIndexKey) o;
    return Objects.equals(database, that.database)
        && Objects.equals(collection, that.collection)
        && Objects.equals(indexName, that.indexName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(database, collection, indexName);
  }

  @Override
  public String toString() {
    return "AtlasSearchIndexKey{"
        + "database='"
        + database
        + '\''
        + ", collection='"
        + collection
        + '\''
        + ", indexName='"
        + indexName
        + '\''
        + '}';
  }

  /**
   * Creates a new builder instance for AtlasSearchIndexKey.
   *
   * @return a new Builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link AtlasSearchIndexKey}. */
  public static class Builder {
    private String database;
    private String collection;
    private String indexName;

    /**
     * Copies properties from an existing {@link AtlasSearchIndexKey}.
     *
     * @param atlasSearchIndexKey the existing key to copy from
     * @return this builder instance
     */
    public Builder withAtlasSearchIndexKey(AtlasSearchIndexKey atlasSearchIndexKey) {
      this.database = atlasSearchIndexKey.database;
      this.collection = atlasSearchIndexKey.collection;
      this.indexName = atlasSearchIndexKey.indexName;
      return this;
    }

    /**
     * Sets the database name.
     *
     * @param database the database name
     * @return this builder instance
     */
    public Builder withDatabase(String database) {
      this.database = database;
      return this;
    }

    /**
     * Sets the collection name.
     *
     * @param collection the collection name
     * @return this builder instance
     */
    public Builder withCollection(String collection) {
      this.collection = collection;
      return this;
    }

    /**
     * Sets the Atlas Search index name.
     *
     * @param indexName the index name
     * @return this builder instance
     */
    public Builder withIndexName(String indexName) {
      this.indexName = indexName;
      return this;
    }

    /**
     * Builds a new {@link AtlasSearchIndexKey} instance.
     *
     * @return a new AtlasSearchIndexKey
     */
    public AtlasSearchIndexKey build() {
      return new AtlasSearchIndexKey(database, collection, indexName);
    }
  }
}
