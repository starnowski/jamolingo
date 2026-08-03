package com.github.starnowski.jamolingo.core.operators.expand;

import com.github.starnowski.jamolingo.core.operators.OlingoOperatorResult;
import java.util.Map;

/** Represents the result of processing an OData $expand system query option. */
public interface ExpandOperatorResult extends OlingoOperatorResult {
  Map<String, ExpandElement> getExpandElements();
}
