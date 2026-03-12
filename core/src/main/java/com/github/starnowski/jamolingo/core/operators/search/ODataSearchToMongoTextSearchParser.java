package com.github.starnowski.jamolingo.core.operators.search;

import org.apache.olingo.server.api.uri.queryoption.SearchOption;

public interface ODataSearchToMongoTextSearchParser<
    T extends ODataSearchToMongoTextSearchOptions, R extends SearchOperatorResult> {

  R parse(SearchOption searchOption);

  R parse(SearchOption searchOption, T options);
}
