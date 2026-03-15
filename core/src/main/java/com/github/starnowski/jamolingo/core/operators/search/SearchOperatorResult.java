package com.github.starnowski.jamolingo.core.operators.search;

import com.github.starnowski.jamolingo.core.operators.OlingoOperatorResult;
import java.util.List;
import org.bson.conversions.Bson;

public interface SearchOperatorResult extends OlingoOperatorResult {

  /**
   * MongoDB aggregation pipeline stages related to search operations.
   *
   * @return list of Bson objects representing the stages
   */
  List<Bson> getSearchStages();

  /**
   * MongoDB aggregation pipeline stages that filter documents based on the score value.
   *
   * @return list of Bson objects representing the stages
   */
  List<Bson> getScoreFilterStages();
}
