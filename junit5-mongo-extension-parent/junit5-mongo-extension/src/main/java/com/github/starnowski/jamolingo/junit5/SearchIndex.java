package com.github.starnowski.jamolingo.junit5;

import com.mongodb.client.model.IndexOptions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.function.Supplier;
import org.bson.conversions.Bson;

/** Defines a MongoDB index to be created on a collection during test setup. */
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchIndex {

  /**
   * The name of the database where the index should be managed.
   *
   * @return the database name
   */
  String database();

  /**
   * The name of the collection where the index should be managed.
   *
   * @return the collection name
   */
  String collection();

  /**
   * The optional field name for a simple single-field index key definition. Takes precedence over
   * {@link #bsonSupplier()} when non-empty.
   *
   * @return the field name, or an empty string by default
   */
  String fieldName() default "";

  /**
   * The index direction or value associated with {@link #fieldName()}. Defaults to 1 (ascending).
   *
   * @return the index value
   */
  int indexValue() default 1;

  /**
   * The optional supplier class providing a custom {@link Bson} index key specification. Ignored if
   * {@link #fieldName()} is non-empty.
   *
   * @return the {@link Bson} supplier class
   */
  Class<? extends Supplier<Bson>> bsonSupplier() default DefaultBsonSupplier.class;

  /**
   * The optional supplier class providing a custom {@link IndexOptions} configuration.
   *
   * @return the {@link IndexOptions} supplier class
   */
  Class<? extends Supplier<IndexOptions>> indexOptionsSupplier() default
      DefaultIndexOptionsSupplier.class;

  /** Default {@link Bson} supplier that returns null when no custom supplier is configured. */
  class DefaultBsonSupplier implements Supplier<Bson> {

    /** Constructs a new DefaultBsonSupplier. */
    public DefaultBsonSupplier() {}

    @Override
    public Bson get() {
      return null;
    }
  }

  /**
   * Default {@link IndexOptions} supplier that returns a new default {@link IndexOptions} instance.
   */
  class DefaultIndexOptionsSupplier implements Supplier<IndexOptions> {

    /** Constructs a new DefaultIndexOptionsSupplier. */
    public DefaultIndexOptionsSupplier() {}

    @Override
    public IndexOptions get() {
      return new IndexOptions();
    }
  }
}
