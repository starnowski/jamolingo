package com.github.starnowski.jamolingo.core.operators.filter;

import org.bson.conversions.Bson;

public interface FilterOperatorQueryObjectResult {

  Bson getQueryObject();

  boolean isAggregationPipelineRequired();

  Throwable getCause();
}
