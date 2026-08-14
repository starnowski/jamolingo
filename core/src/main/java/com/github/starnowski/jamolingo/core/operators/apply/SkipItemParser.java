package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.operators.skip.OdataSkipToMongoSkipParser;
import com.github.starnowski.jamolingo.core.operators.skip.SkipOperatorResult;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.apply.Skip;

public class SkipItemParser implements ApplyItemParser {

  private final OdataSkipToMongoSkipParser skipParser = new OdataSkipToMongoSkipParser();

  @Override
  public ApplyOperatorResult parse(
      ApplyItem applyItem, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    if (!(applyItem instanceof Skip)) {
      throw new IllegalArgumentException("Expected Skip item");
    }
    Skip skipItem = (Skip) applyItem;

    SkipOperatorResult result = skipParser.parse(skipItem.getSkipOption());

    return DefaultApplyOperatorResult.builder().withStageObjects(result.getStageObjects()).build();
  }
}
