package com.github.starnowski.jamolingo.core.operators.select;

import java.util.Set;

/** Context for parsing OData select option to Mongo project stage. */
public interface OdataSelectToMongoProjectParserContext {

  /**
   * Returns the set of additional fields that should be part of the $project stage.
   *
   * @return set of additional fields
   */
  Set<String> getAdditionalFields();
}
