package com.github.starnowski.jamolingo.core.operators.orderby;

import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Parses OData $orderby system query options and translates them into MongoDB $sort aggregation
 * stages.
 */
public class OdataOrderByToMongoSortParser {

  /**
   * Parses the given OrderByOption using a default context facade.
   *
   * @param orderByOption the OData orderby option to parse
   * @return the result of the parsing containing the MongoDB sort stage
   */
  public OrderByOperatorResult parse(OrderByOption orderByOption) {
    return parse(
        orderByOption,
        DefaultEdmMongoContextFacade.builder().build());
  }

  public OrderByOperatorResult parse(
      OrderByOption orderByOption, EdmMongoContextFacade edmMongoContextFacade) {
    return parse(
        orderByOption,
        edmMongoContextFacade,
        DefaultOdataOrderByToMongoSortParserContext.builder().build());
  }

  public OrderByOperatorResult parse(
      OrderByOption orderByOption,
      EdmMongoContextFacade edmMongoContextFacade,
      OdataOrderByToMongoSortParserContext context) {
    Map<String, Object> sortFields = new LinkedHashMap<>();

    if (context != null && context.getPrependedSortProperties() != null) {
      for (SortProperty sortProp : context.getPrependedSortProperties()) {
        sortFields.put(sortProp.getPropertyName(), sortProp.isDescending() ? -1 : 1);
      }
    }

    if (orderByOption != null
        && orderByOption.getOrders() != null
        && !orderByOption.getOrders().isEmpty()) {
      for (OrderByItem item : orderByOption.getOrders()) {
        Expression expression = item.getExpression();
        if (!(expression instanceof Member)) {
          throw new IllegalArgumentException(
              "Only Member expressions are supported in $orderby, found: "
                  + expression.getClass().getSimpleName());
        }
        Member member = (Member) expression;
        String mongoPath =
            edmMongoContextFacade
                .resolveMongoPathForEDMPath(member.getResourcePath())
                .getMongoPath();
        sortFields.put(mongoPath, item.isDescending() ? -1 : 1);
      }
    }

    if (sortFields.isEmpty()) {
      return new DefaultOrderByOperatorResult(
          Collections.emptyList(), false, Collections.emptyList());
    }

    return new DefaultOrderByOperatorResult(
        Collections.singletonList(new Document("$sort", new Document(sortFields))),
        true,
        new ArrayList<>(sortFields.keySet()));
  }

  private static class DefaultOrderByOperatorResult implements OrderByOperatorResult {

    private final List<Bson> stageObjects;
    private final boolean present;
    private final List<String> usedMongoDocumentProperties;

    private DefaultOrderByOperatorResult(
        List<Bson> stageObjects, boolean present, List<String> usedMongoDocumentProperties) {
      this.stageObjects = stageObjects;
      this.present = present;
      this.usedMongoDocumentProperties = usedMongoDocumentProperties;
    }

    @Override
    public List<Bson> getStageObjects() {
      return stageObjects;
    }

    @Override
    public List<String> getUsedMongoDocumentProperties() {
      return usedMongoDocumentProperties;
    }

    @Override
    public List<String> getWrittenMongoDocumentProperties() {
      return Collections.emptyList();
    }

    @Override
    public List<String> getAddedMongoDocumentProperties() {
      return Collections.emptyList();
    }

    @Override
    public List<String> getRemovedMongoDocumentProperties() {
      return Collections.emptyList();
    }

    @Override
    public boolean isDocumentShapeRedefined() {
      return false;
    }
  }
}
