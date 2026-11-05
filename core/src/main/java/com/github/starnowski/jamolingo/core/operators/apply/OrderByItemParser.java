package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.operators.orderby.OdataOrderByToMongoSortParser;
import com.github.starnowski.jamolingo.core.operators.orderby.OrderByOperatorResult;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.apply.OrderBy;

/** OrderByItemParser type. */
public class OrderByItemParser implements ApplyItemParser {

  private final OdataOrderByToMongoSortParser orderByParser = new OdataOrderByToMongoSortParser();

  @Override
  public ApplyOperatorResult parse(
      ApplyItem applyItem, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    return parse(applyItem, edmMongoContextFacade, null);
  }

  @Override
  public ApplyOperatorResult parse(
      ApplyItem applyItem,
      EdmPropertyMongoPathResolver edmMongoContextFacade,
      ApplyParserContext context) {
    if (!(applyItem instanceof OrderBy)) {
      throw new IllegalArgumentException("Expected OrderBy item");
    }
    OrderBy orderByItem = (OrderBy) applyItem;

    if (!(edmMongoContextFacade instanceof EdmMongoContextFacade)) {
      throw new IllegalArgumentException(
          "Expected edmMongoContextFacade to be an instance of EdmMongoContextFacade");
    }

    String rootMongoPath =
        ApplyGraphLookupHelper.resolveRootMongoPath(edmMongoContextFacade, context);

    OrderByOperatorResult result =
        orderByParser.parse(
            orderByItem.getOrderByOption(), (EdmMongoContextFacade) edmMongoContextFacade);

    List<String> usedProperties =
        result.getUsedMongoDocumentProperties() != null
            ? result.getUsedMongoDocumentProperties().stream()
                .map(p -> ApplyGraphLookupHelper.stripRootMongoPath(p, rootMongoPath))
                .collect(Collectors.toList())
            : null;

    return DefaultApplyOperatorResult.builder()
        .withStageObjects(result.getStageObjects())
        .withUsedMongoDocumentProperties(usedProperties)
        .withWrittenMongoDocumentProperties(result.getWrittenMongoDocumentProperties())
        .withAddedMongoDocumentProperties(result.getAddedMongoDocumentProperties())
        .withRemovedMongoDocumentProperties(result.getRemovedMongoDocumentProperties())
        .withDocumentShapeRedefined(result.isDocumentShapeRedefined())
        .build();
  }
}
