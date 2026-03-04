package com.github.starnowski.jamolingo.core.operators.filter;

import static com.github.starnowski.jamolingo.core.operators.filter.MongoFilterVisitor.CUSTOM_LITERAL_VALUE_PROPERTY;

import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.bson.Document;
import org.bson.conversions.Bson;

/** Interface for converting OData literals into BSON documents. */
public interface LiteralToBsonConverter {

  /**
   * Converts the given OData literal into a BSON representation.
   *
   * @param literal the OData literal
   * @return the BSON representation
   */
  Bson convert(Literal literal);

  /**
   * Wraps the given value into a BSON document suitable for OData literal representation.
   *
   * @param value the value to wrap
   * @return the BSON document
   */
  default Document literal(Object value) {
    return new Document(CUSTOM_LITERAL_VALUE_PROPERTY, value);
  }
}
