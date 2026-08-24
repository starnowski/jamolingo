package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import java.util.Collections;
import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.ApplyOption;

public class ODataApplyToMongoAggregationPipelineParser {

  public ApplyOperatorResult parse(
      ApplyOption applyOption, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    if (applyOption == null
        || applyOption.getApplyItems() == null
        || applyOption.getApplyItems().isEmpty()) {
      return DefaultApplyOperatorResult.builder().withStageObjects(Collections.emptyList()).build();
    }
    return parse(applyOption.getApplyItems(), edmMongoContextFacade);
  }

  public ApplyOperatorResult parse(
      List<ApplyItem> applyItems, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    if (applyItems == null || applyItems.isEmpty()) {
      return DefaultApplyOperatorResult.builder().withStageObjects(Collections.emptyList()).build();
    }

    List<org.bson.conversions.Bson> stages = new java.util.ArrayList<>();
    for (ApplyItem applyItem : applyItems) {
      ApplyItemParser parser = getParser(applyItem);
      if (parser != null) {
        ApplyOperatorResult result = parser.parse(applyItem, edmMongoContextFacade);
        stages.addAll(result.getStageObjects());
      } else {
        throw new UnsupportedOperationException("Unsupported apply item: " + applyItem.getKind());
      }
    }

    return DefaultApplyOperatorResult.builder().withStageObjects(stages).build();
  }

  private ApplyItemParser getParser(ApplyItem applyItem) {
    if (applyItem.getKind() == ApplyItem.Kind.FILTER) {
      return new FilterItemParser();
    } else if (applyItem.getKind() == ApplyItem.Kind.IDENTITY) {
      return new IdentityItemParser();
    } else if (applyItem.getKind() == ApplyItem.Kind.GROUP_BY) {
      return new GroupByItemParser(this);
    } else if (applyItem.getKind() == ApplyItem.Kind.ORDERBY) {
      return new OrderByItemParser();
    } else if (applyItem.getKind() == ApplyItem.Kind.TOP) {
      return new TopItemParser();
    } else if (applyItem.getKind() == ApplyItem.Kind.SKIP) {
      return new SkipItemParser();
    } else if (applyItem.getKind() == ApplyItem.Kind.AGGREGATE) {
      return new AggregateItemParser();
    } else if (applyItem.getKind() == ApplyItem.Kind.COMPUTE) {
      return new ComputeItemParser();
    } else if (applyItem.getKind() == ApplyItem.Kind.CONCAT) {
      return new ConcatItemParser();
    } else if (applyItem.getKind() == ApplyItem.Kind.SEARCH) {
      return new SearchItemParser();
    } else if (applyItem.getKind() == ApplyItem.Kind.BOTTOM_TOP) {
      return new BottomTopItemParser();
    }
    // TODO CUSTOM_FUNCTION
    // EXPAND
    return null;
  }
}
