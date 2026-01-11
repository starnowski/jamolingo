package com.github.starnowski.jamolingo.junit5;

/** Defines a single document to be inserted into a MongoDB collection during test setup. */
public @interface MongoDocument {
  /**
   * The name of the collection where the document should be inserted.
   *
   * @return the collection name
   */
  String collection();

  /**
   * The name of the database where the document should be inserted.
   *
   * @return the database name
   */
  String database();

  /**
   * The path to the file containing the document data.
   *
   * <p>The file should contain a JSON representation of the document, which may include BSON types
   * (e.g. Extended JSON).
   *
   * @return the file path relative to the classpath
   */
  String bsonFilePath();
}
