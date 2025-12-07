package com.github.starnowski.jamolingo.select;

import org.bson.conversions.Bson;

import java.util.Set;

public interface SelectOperatorResult {

    Set<String> getSelectedFields();

    boolean isWildCard();

    Bson getProjectObject();

    Bson getStageObject();
}
