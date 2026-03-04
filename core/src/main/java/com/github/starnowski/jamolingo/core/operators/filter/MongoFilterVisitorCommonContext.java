package com.github.starnowski.jamolingo.core.operators.filter;

/** Interface for providing common context for the MongoFilterVisitor. */
public interface MongoFilterVisitorCommonContext {

  /**
   * Returns the converter for translating OData literals into BSON documents.
   *
   * @return the literal to BSON converter
   */
  LiteralToBsonConverter literalToBsonConverter();

  /**
   * Returns the converter for translating OData values into BSON-compatible objects.
   *
   * @return the OData to BSON converter
   */
  ODataToBsonConverter oDataToBsonConverter();
}
