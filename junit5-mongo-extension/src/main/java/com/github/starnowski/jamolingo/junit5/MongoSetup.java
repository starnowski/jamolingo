package com.github.starnowski.jamolingo.junit5;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Annotation to configure MongoDB data setup for a test method.
 *
 * This annotation triggers the {@link QuarkusMongoDataLoaderExtension} to load the specified
 * documents into the MongoDB database before the test method is executed.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(QuarkusMongoDataLoaderExtension.class)
public @interface MongoSetup {

  /**
   * The list of {@link MongoDocument} configurations defining the data to be loaded.
   *
   * @return an array of {@link MongoDocument}
   */
  MongoDocument[] mongoDocuments();
}
