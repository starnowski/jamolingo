package com.github.starnowski.jamolingo.core.operators.filter;

public interface ODataToBsonConverter {

  Object toBsonValue(String value, String edmType);
}
