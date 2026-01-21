package com.github.starnowski.jamolingo.core.operators.skip;

import com.github.starnowski.jamolingo.core.operators.OlingoOperatorResult;

/** Represents the result of processing an OData $skip system query option. */
public interface SkipOperatorResult extends OlingoOperatorResult {

  /**
   * Returns the value specified for the $skip option.
   *
   * @return the skip value
   */
  int getSkipValue();
}
