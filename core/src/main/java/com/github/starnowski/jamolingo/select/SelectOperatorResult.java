package com.github.starnowski.jamolingo.select;

import java.util.Set;
import org.bson.conversions.Bson;

public interface SelectOperatorResult {

  Set<String> getSelectedFields();

  boolean isWildCard();

  Bson getProjectObject();

  Bson getStageObject();

  // TODO Create AbstractOlingoOperatorResult
  // TODO getStagesObjects() - list of stages in order (even if result has one)
  // TODO Used MongoDB document properties with Dot annotation
  // TODO Produced MongoDB document properties
  //  $expand - property that contains fetched (joined) documents
  //  $apply - transformation
}
