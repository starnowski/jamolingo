package com.github.starnowski.jamolingo.core.operators.search;

import org.apache.olingo.server.api.uri.queryoption.SearchOption;

public interface ODataSearchToMongoTextSearchParser<T extends ODataSearchToMongoTextSearchOptions> {

    SearchOperatorResult parse(
            SearchOption searchOption);

    SearchOperatorResult parse(
            SearchOption searchOption, T options);
}
