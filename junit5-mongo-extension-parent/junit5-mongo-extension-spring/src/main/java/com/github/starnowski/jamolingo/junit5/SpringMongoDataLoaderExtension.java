package com.github.starnowski.jamolingo.junit5;

import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * A JUnit 5 extension that loads data into MongoDB collections before each test method execution in
 * a Spring application.
 *
 * <p>This extension looks for the {@link MongoSetup} annotation on the test method or test class.
 * If present, it retrieves the {@link MongoClient} from the Spring {@link ApplicationContext},
 * clears the specified collections, and inserts the documents defined in the annotation.
 */
public class SpringMongoDataLoaderExtension extends AbstractMongoDataLoaderExtension {

  private ExtensionContext lastContext;

  @Override
  public void beforeEach(ExtensionContext context) throws IllegalAccessException {
    this.lastContext = context;
    super.beforeEach(context);
  }

  @Override
  protected MongoClient resolveMongoClient() {
    if (lastContext == null) {
      return null;
    }
    ApplicationContext applicationContext = SpringExtension.getApplicationContext(lastContext);
    return applicationContext.getBean(MongoClient.class);
  }
}
