package com.github.starnowski.jamolingo.core.operators.orderby;

import java.util.List;

public interface OdataOrderByToMongoSortParserContext {

  List<SortProperty> getPrependedSortProperties();
}
