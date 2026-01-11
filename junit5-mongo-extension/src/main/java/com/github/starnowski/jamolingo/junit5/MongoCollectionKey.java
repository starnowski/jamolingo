package com.github.starnowski.jamolingo.junit5;

import java.util.Objects;

/**
 * Represents a unique identifier for a MongoDB collection, consisting of the database name and the
 * collection name.
 */
public class MongoCollectionKey {
  private final String database;
  private final String collection;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    MongoCollectionKey that = (MongoCollectionKey) o;
    return Objects.equals(database, that.database) && Objects.equals(collection, that.collection);
  }

  @Override
  public int hashCode() {
    return Objects.hash(database, collection);
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
   * Constructs a new MongoCollectionKey.
   *
   * @param database the database name
   * @param collection the collection name
   */
  public MongoCollectionKey(String database, String collection) {
    this.database = database;
    this.collection = collection;
  }
}
