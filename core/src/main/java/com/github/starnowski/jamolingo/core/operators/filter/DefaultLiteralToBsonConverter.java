package com.github.starnowski.jamolingo.core.operators.filter;

import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.bson.conversions.Bson;

/** Default implementation of the LiteralToBsonConverter. */
public class DefaultLiteralToBsonConverter implements LiteralToBsonConverter {
  @Override
  public Bson convert(Literal literal) {
    String text = literal.getText();
    if ("null".equals(text)) {
      return literal(null);
    }
    if (text.startsWith("'") && text.endsWith("'")) {
      return literal(text.substring(1, text.length() - 1)); // placeholder, field comes later
    }
    try {
      return literal(Integer.parseInt(text));
    } catch (NumberFormatException e) {
      try {
        return literal(Double.parseDouble(text));
      } catch (NumberFormatException ignored) {
      }
    }
    return literal(text);
  }
}
