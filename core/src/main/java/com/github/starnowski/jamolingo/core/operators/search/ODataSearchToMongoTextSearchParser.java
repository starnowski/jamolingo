package com.github.starnowski.jamolingo.core.operators.search;

import org.apache.olingo.server.api.uri.queryoption.SearchOption;

/**
 * Interface for parsing OData search options into MongoDB text search aggregation pipeline stages.
 *
 * @param <T> the type of search options
 * @param <R> the type of search operator result
 */
public interface ODataSearchToMongoTextSearchParser<
    T extends ODataSearchToMongoTextSearchOptions, R extends SearchOperatorResult> {

  /**
   * Parses the given OData search option into a search operator result.
   *
   * @param searchOption the OData search option to parse
   * @return the resulting search operator result
   */
  R parse(SearchOption searchOption);

  /**
   * Parses the given OData search option into a search operator result, applying the provided
   * options.
   *
   * @param searchOption the OData search option to parse
   * @param options the search options to apply
   * @return the resulting search operator result
   */
  R parse(SearchOption searchOption, T options);
}
