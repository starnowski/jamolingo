package com.github.starnowski.jamolingo.core.operators.filter;

import static com.github.starnowski.jamolingo.core.operators.filter.MongoFilterVisitor.CUSTOM_LITERAL_VALUE_PROPERTY;

import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.bson.Document;
import org.bson.conversions.Bson;

public interface LiteralToBsonConverter {

  Bson convert(Literal literal);

  default Document literal(Object value) {
    return new Document(CUSTOM_LITERAL_VALUE_PROPERTY, value);
  }
}
