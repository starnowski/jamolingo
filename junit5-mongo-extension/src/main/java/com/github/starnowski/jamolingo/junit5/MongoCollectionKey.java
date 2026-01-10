package com.github.starnowski.jamolingo.junit5;

import java.util.Objects;

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

  public String getDatabase() {
    return database;
  }

  public String getCollection() {
    return collection;
  }

  public MongoCollectionKey(String database, String collection) {
    this.database = database;
    this.collection = collection;
  }
}
