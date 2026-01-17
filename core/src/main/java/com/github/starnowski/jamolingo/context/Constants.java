package com.github.starnowski.jamolingo.context;

/** Constants used throughout the context package. */
public class Constants {

  /**
   * Defines the character used to separate segments in an OData path. For example, in
   * "Addresses/City", the "/" is the path separator.
   */
  public static final String ODATA_PATH_SEPARATOR_CHARACTER = "/";

  /**
   * MongoDB supports no more than 100 levels of nesting for BSON documents. Each object or array
   * adds a level.
   * <a href="https://www.mongodb.com/docs/manual/reference/limits/?atlas-class=general&atlas-provider=aws#mongodb-limit-Nested-Depth-for-BSON-Documents">...</a>
   */
  public static final int MONGO_HARDCODED_BSON_DOCUMENT_NESTING_LIMIT = 100;
}
