package com.github.starnowski.jamolingo.junit5;

import com.mongodb.client.MongoClient;
import java.util.Objects;

/** Context holding parameters required to validate if an Atlas Search Index is synchronized. */
public class AtlasSearchIndexValidationContext {

  private final MongoClient mongoClient;
  private final String database;
  private final String collection;
  private final String indexName;
  private final String validationProperty;

  /**
   * Constructs a new AtlasSearchIndexValidationContext.
   *
   * @param mongoClient the MongoDB client
   * @param database the database name
   * @param collection the collection name
   * @param indexName the Atlas Search index name
   * @param validationProperty the document property name used for validation
   */
  public AtlasSearchIndexValidationContext(
      MongoClient mongoClient,
      String database,
      String collection,
      String indexName,
      String validationProperty) {
    this.mongoClient = mongoClient;
    this.database = database;
    this.collection = collection;
    this.indexName = indexName;
    this.validationProperty = validationProperty;
  }

  /**
   * Returns the MongoDB client.
   *
   * @return the MongoDB client
   */
  public MongoClient getMongoClient() {
    return mongoClient;
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

  /**
   * Returns the document property name used for validation.
   *
   * @return the validation property name
   */
  public String getValidationProperty() {
    return validationProperty;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AtlasSearchIndexValidationContext that = (AtlasSearchIndexValidationContext) o;
    return Objects.equals(mongoClient, that.mongoClient)
        && Objects.equals(database, that.database)
        && Objects.equals(collection, that.collection)
        && Objects.equals(indexName, that.indexName)
        && Objects.equals(validationProperty, that.validationProperty);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mongoClient, database, collection, indexName, validationProperty);
  }

  @Override
  public String toString() {
    return "AtlasSearchIndexValidationContext{"
        + "mongoClient="
        + mongoClient
        + ", database='"
        + database
        + '\''
        + ", collection='"
        + collection
        + '\''
        + ", indexName='"
        + indexName
        + '\''
        + ", validationProperty='"
        + validationProperty
        + '\''
        + '}';
  }

  /**
   * Creates a new builder instance for AtlasSearchIndexValidationContext.
   *
   * @return a new Builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link AtlasSearchIndexValidationContext}. */
  public static class Builder {
    private MongoClient mongoClient;
    private String database;
    private String collection;
    private String indexName;
    private String validationProperty;

    /**
     * Copies properties from an existing {@link AtlasSearchIndexValidationContext}.
     *
     * @param context the existing context to copy from
     * @return this builder instance
     */
    public Builder withAtlasSearchIndexValidationContext(
        AtlasSearchIndexValidationContext context) {
      this.mongoClient = context.mongoClient;
      this.database = context.database;
      this.collection = context.collection;
      this.indexName = context.indexName;
      this.validationProperty = context.validationProperty;
      return this;
    }

    /**
     * Sets the MongoDB client.
     *
     * @param mongoClient the MongoDB client
     * @return this builder instance
     */
    public Builder withMongoClient(MongoClient mongoClient) {
      this.mongoClient = mongoClient;
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
     * Sets the document property name used for validation.
     *
     * @param validationProperty the validation property name
     * @return this builder instance
     */
    public Builder withValidationProperty(String validationProperty) {
      this.validationProperty = validationProperty;
      return this;
    }

    /**
     * Builds a new {@link AtlasSearchIndexValidationContext} instance.
     *
     * @return a new AtlasSearchIndexValidationContext
     */
    public AtlasSearchIndexValidationContext build() {
      return new AtlasSearchIndexValidationContext(
          mongoClient, database, collection, indexName, validationProperty);
    }
  }
}
