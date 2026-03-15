package com.github.starnowski.jamolingo.core.operators.search;

import org.apache.olingo.server.api.uri.queryoption.search.SearchExpression;
import org.bson.conversions.Bson;

public interface SearchDocumentFactory {

  Bson build(SearchExpression searchExpression, ODataSearchToMongoAtlasSearchOptions options);
}
