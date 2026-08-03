package com.github.starnowski.jamolingo.junit5;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to configure MongoDB data setup for a test method.
 *
 * <p>This annotation triggers a MongoDB data loader extension to load the specified documents into
 * the MongoDB database before the test method is executed.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoSetup {

  /**
   * The list of {@link MongoDocument} configurations defining the data to be loaded.
   *
   * @return an array of {@link MongoDocument}
   */
  MongoDocument[] mongoDocuments();

  /**
   * Flag to indicate if documents should be inserted in batch mode.
   *
   * @return true if batch insertion is enabled, false otherwise
   */
  boolean batchInsertToCollection() default false;
}
