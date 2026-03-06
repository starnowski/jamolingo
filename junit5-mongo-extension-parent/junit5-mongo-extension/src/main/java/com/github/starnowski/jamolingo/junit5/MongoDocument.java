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
   * <p>If this property is left as an empty string (the default), the collection will be erased.
   * Note that the collection is erased only once per test setup if multiple {@link MongoDocument}
   * annotations are provided for the same collection. If at least one {@link MongoDocument} for a
   * specific collection contains a file path, the collection will be cleared and then populated
   * with data from all provided files.
   *
   * @return the file path relative to the classpath
   */
  String bsonFilePath() default "";
}
