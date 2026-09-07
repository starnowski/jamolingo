package com.github.starnowski.jamolingo.core.operators.apply;

public interface ApplySearchToMongoPipelineParser {
  ApplyOperatorResult parse(SearchApplyItemContext context);
}
