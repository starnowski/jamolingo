package com.github.starnowski.jamolingo.select;

import java.util.Set;
import org.bson.conversions.Bson;

public interface SelectOperatorResult extends AbstractOlingoOperatorResult {

  Set<String> getSelectedFields();

  boolean isWildCard();

  Bson getProjectObject();

  Bson getStageObject();
}
