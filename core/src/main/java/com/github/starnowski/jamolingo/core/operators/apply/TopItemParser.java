package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.operators.top.OdataTopToMongoLimitParser;
import com.github.starnowski.jamolingo.core.operators.top.TopOperatorResult;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.apply.Top;

public class TopItemParser implements ApplyItemParser {

  private final OdataTopToMongoLimitParser topParser = new OdataTopToMongoLimitParser();

  @Override
  public ApplyOperatorResult parse(
      ApplyItem applyItem, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    if (!(applyItem instanceof Top)) {
      throw new IllegalArgumentException("Expected Top item");
    }
    Top topItem = (Top) applyItem;

    TopOperatorResult result = topParser.parse(topItem.getTopOption());

    return DefaultApplyOperatorResult.builder().withStageObjects(result.getStageObjects()).build();
  }
}
