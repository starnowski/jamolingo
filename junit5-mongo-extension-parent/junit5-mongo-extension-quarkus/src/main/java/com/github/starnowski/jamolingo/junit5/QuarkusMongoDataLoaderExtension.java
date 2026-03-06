package com.github.starnowski.jamolingo.junit5;

import com.mongodb.client.MongoClient;
import io.quarkus.arc.Arc;

/**
 * A JUnit 5 extension that loads data into MongoDB collections before each test method execution in
 * a Quarkus environment.
 *
 * <p>This extension looks for the {@link MongoSetup} annotation on the test method. If present, it
 * retrieves the {@link MongoClient} from the Quarkus {@link Arc} container, clears the specified
 * collections, and inserts the documents defined in the annotation.
 */
public class QuarkusMongoDataLoaderExtension extends AbstractMongoDataLoaderExtension {

  @Override
  protected MongoClient resolveMongoClient() {
    if (Arc.container() == null) {
      return null;
    }
    try {
      return Arc.container().instance(MongoClient.class).get();
    } catch (Exception e) {
      return null;
    }
  }
}
