package com.github.starnowski.jamolingo.core.operators.select;

import com.github.starnowski.jamolingo.core.operators.OlingoOperatorResult;

import java.util.Set;

public interface SelectOperatorOptionsForMapOperator extends OlingoOperatorResult {

  Set<String> getSelectedFields();

  boolean isWildCard();

  Set<String> getArrayFields();
}
