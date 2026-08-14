package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.operators.filter.FilterOperatorResult;
import com.github.starnowski.jamolingo.core.operators.filter.ODataFilterToMongoMatchParser;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.apply.Filter;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

public class FilterItemParser implements ApplyItemParser {

  private final ODataFilterToMongoMatchParser filterParser = new ODataFilterToMongoMatchParser();

  @Override
  public ApplyOperatorResult parse(
      ApplyItem applyItem, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    if (!(applyItem instanceof Filter)) {
      throw new IllegalArgumentException("Expected Filter item");
    }
    Filter filterItem = (Filter) applyItem;

    try {
      FilterOperatorResult result =
          filterParser.parse(filterItem.getFilterOption(), edmMongoContextFacade);
      return DefaultApplyOperatorResult.builder()
          .withStageObjects(result.getStageObjects())
          .build();
    } catch (ODataApplicationException | ExpressionVisitException e) {
      throw new RuntimeException("Failed to parse filter item", e);
    }
  }
}
