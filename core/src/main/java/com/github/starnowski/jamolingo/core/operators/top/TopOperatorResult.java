package com.github.starnowski.jamolingo.core.operators.top;

import com.github.starnowski.jamolingo.core.operators.OlingoOperatorResult;

/** Represents the result of processing an OData $top system query option. */
public interface TopOperatorResult extends OlingoOperatorResult {

  /**
   * Returns the value specified for the $top option.
   *
   * @return the top value
   */
  int getTopValue();
}
