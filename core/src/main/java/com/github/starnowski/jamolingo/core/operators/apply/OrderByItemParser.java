package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.operators.orderby.OdataOrderByToMongoSortParser;
import com.github.starnowski.jamolingo.core.operators.orderby.OrderByOperatorResult;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.apply.OrderBy;

public class OrderByItemParser implements ApplyItemParser {

  private final OdataOrderByToMongoSortParser orderByParser = new OdataOrderByToMongoSortParser();

  @Override
  public ApplyOperatorResult parse(
      ApplyItem applyItem, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    if (!(applyItem instanceof OrderBy)) {
      throw new IllegalArgumentException("Expected OrderBy item");
    }
    OrderBy orderByItem = (OrderBy) applyItem;

    // The OdataOrderByToMongoSortParser requires EdmMongoContextFacade, but we only have
    // EdmPropertyMongoPathResolver
    // However, if we assume edmMongoContextFacade implements EdmMongoContextFacade or we can pass
    // it
    // Wait, let's check what EdmPropertyMongoPathResolver actually is.
    // Usually edmMongoContextFacade is actually an instance of EdmMongoContextFacade.
    if (!(edmMongoContextFacade instanceof EdmMongoContextFacade)) {
      throw new IllegalArgumentException(
          "Expected edmMongoContextFacade to be an instance of EdmMongoContextFacade");
    }

    OrderByOperatorResult result =
        orderByParser.parse(
            orderByItem.getOrderByOption(), (EdmMongoContextFacade) edmMongoContextFacade);

    return DefaultApplyOperatorResult.builder().withStageObjects(result.getStageObjects()).build();
  }
}
