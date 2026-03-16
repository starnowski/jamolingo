package com.github.starnowski.jamolingo.core.operators.search;

import org.apache.olingo.server.api.uri.queryoption.search.SearchExpression;
import org.bson.conversions.Bson;

/** Factory interface for building MongoDB Bson documents from OData search expressions. */
public interface SearchDocumentFactory {

  /**
   * Builds a Bson document representing the search stage based on the provided search expression
   * and options.
   *
   * @param searchExpression the OData search expression
   * @param options the search options
   * @return the Bson document representing the search operation
   */
  Bson build(SearchExpression searchExpression, ODataSearchToMongoAtlasSearchOptions options);
}
