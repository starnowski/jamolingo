package com.github.starnowski.jamolingo.core.operators.filter;

public interface MongoFilterVisitorCommonContext {

  LiteralToBsonConverter literalToBsonConverter();

  ODataToBsonConverter oDataToBsonConverter();
}
