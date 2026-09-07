package com.github.starnowski.jamolingo.core.operators.apply;

import org.apache.olingo.server.api.uri.queryoption.apply.Search;

public interface SearchApplyItemContext {
  Search getSearch();

  boolean isFirstApplyItem();
}
