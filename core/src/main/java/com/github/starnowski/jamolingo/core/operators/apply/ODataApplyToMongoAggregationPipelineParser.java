package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import java.util.Collections;
import org.apache.olingo.server.api.uri.queryoption.ApplyOption;

public class ODataApplyToMongoAggregationPipelineParser {

  public ApplyOperatorResult parse(
      ApplyOption applyOption, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    if (applyOption == null
        || applyOption.getApplyItems() == null
        || applyOption.getApplyItems().isEmpty()) {
      return DefaultApplyOperatorResult.builder().withStageObjects(Collections.emptyList()).build();
    }

    // Foundation for iterating over ApplyItems
    // For now, return an empty result
    return DefaultApplyOperatorResult.builder().withStageObjects(Collections.emptyList()).build();
  }
}
