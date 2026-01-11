package com.github.starnowski.jamolingo.select;

import java.util.Set;
import org.bson.conversions.Bson;

/** Represents the result of processing an OData $select system query option. */
public interface SelectOperatorResult extends OlingoOperatorResult {

  /**
   * Returns the set of fields selected by the client.
   *
   * @return set of selected fields
   */
  Set<String> getSelectedFields();

  /**
   * Indicates if the selection includes all fields (wildcard selection).
   *
   * @return true if all fields are selected, false otherwise
   */
  boolean isWildCard();

  /**
   * Returns the BSON object representing the $project stage of the aggregation pipeline.
   *
   * @return the BSON project object
   */
  Bson getProjectObject();

  /**
   * Returns the complete aggregation stage object including the $project operator.
   *
   * @return the BSON stage object
   */
  Bson getStageObject();
}
