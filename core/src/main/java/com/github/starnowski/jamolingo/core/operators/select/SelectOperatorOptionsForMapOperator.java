package com.github.starnowski.jamolingo.core.operators.select;

import java.util.Set;

/** Options for the select operator when used within a $map aggregation stage. */
public interface SelectOperatorOptionsForMapOperator {

  /**
   * Returns the set of selected fields.
   *
   * @return set of selected fields
   */
  Set<String> getSelectedFields();

  /**
   * Returns true if all fields are selected (wildcard).
   *
   * @return true if wildcard is used
   */
  boolean isWildCard();

  /**
   * Returns the set of fields that are arrays.
   *
   * @return set of array fields
   */
  Set<String> getArrayFields();
}
