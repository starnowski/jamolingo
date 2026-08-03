package com.github.starnowski.jamolingo.core.operators.filter;

import org.bson.conversions.Bson;

/** Represents result of OData filter parsing to MongoDB query object. */
public interface FilterOperatorQueryObjectResult {

  /**
   * Returns MongoDB query object.
   *
   * @return query object
   */
  Bson getQueryObject();

  /**
   * Returns true if aggregation pipeline is required to execute the query.
   *
   * @return true if aggregation pipeline is required
   */
  boolean isAggregationPipelineRequired();

  /**
   * Returns cause of failure if parsing failed.
   *
   * @return cause of failure
   */
  Throwable getCause();
}
