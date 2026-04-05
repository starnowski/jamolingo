package com.github.starnowski.jamolingo.core.operators.select;

import java.util.Set;

public interface SelectOperatorOptionsForMapOperator {

  Set<String> getSelectedFields();

  boolean isWildCard();

  Set<String> getArrayFields();
}
