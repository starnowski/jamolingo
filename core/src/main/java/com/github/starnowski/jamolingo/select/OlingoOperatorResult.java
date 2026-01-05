package com.github.starnowski.jamolingo.select;

import java.util.List;
import org.bson.conversions.Bson;

public interface OlingoOperatorResult {

  /**
   * Returns a list of Bson stages objects that represent operations to be performed. The stages are
   * in the order they should be applied.
   *
   * @return A list of Bson stage objects.
   */
  List<Bson> getStagesObjects();

  /**
   * Returns a list of MongoDB document properties (in dot notation) that are used as input for the
   * operator.
   *
   * @return A list of used MongoDB document properties.
   */
  List<String> getUsedMongoDocumentProperties();

  /**
   * Returns a list of MongoDB document properties (in dot notation) that are produced or modified
   * by the operator.
   *
   * @return A list of produced MongoDB document properties.
   */
  List<String> getProducedMongoDocumentProperties();

  // TODO Add methods that returns properties that are added, remove, transformed new output
}
