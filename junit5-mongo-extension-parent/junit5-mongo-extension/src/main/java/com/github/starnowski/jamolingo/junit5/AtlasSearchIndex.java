package com.github.starnowski.jamolingo.junit5;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines an Atlas Search Index to be created and synchronized on a MongoDB collection during test
 * setup.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AtlasSearchIndex {

  /**
   * The name of the Atlas Search Index. Defaults to "default".
   *
   * @return the search index name
   */
  String name() default "default";

  /**
   * The name of the database where the search index should be created.
   *
   * @return the database name
   */
  String database();

  /**
   * The name of the collection where the search index should be created.
   *
   * @return the collection name
   */
  String collection();

  /**
   * The classpath relative path to the file containing the BSON/JSON search index definition.
   *
   * @return the file path relative to the classpath
   */
  String bsonFilePath();

  /**
   * The name of the document property present in the search index to be used for synchronization
   * validation.
   *
   * @return the validation property name
   */
  String validationProperty() default "";

  /**
   * The array of validation strategy classes to execute in order to verify that the search index is
   * synchronized.
   *
   * @return an array of validation strategy classes
   */
  Class<? extends AtlasSearchIndexSyncValidationStrategy>[] validationStrategies() default {};
}
