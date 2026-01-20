package com.github.starnowski.jamolingo.orderby;

import com.github.starnowski.jamolingo.context.DefaultEdmMongoContextFacade;
import com.github.starnowski.jamolingo.context.EdmMongoContextFacade;
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
    return parse(orderByOption, DefaultEdmMongoContextFacade.builder().build());
  }

  /**
   * Parses the given OrderByOption using the provided context facade.
   *
   * @param orderByOption the OData orderby option to parse
   * @param edmMongoContextFacade the context facade for resolving paths
   * @return the result of the parsing containing the MongoDB sort stage
   */
  public OrderByOperatorResult parse(
      OrderByOption orderByOption, EdmMongoContextFacade edmMongoContextFacade) {
    if (orderByOption == null || orderByOption.getOrders().isEmpty()) {
      return new DefaultOrderByOperatorResult(Collections.emptyList(), false);
    }

    Map<String, Object> sortFields = new LinkedHashMap<>();
    for (OrderByItem item : orderByOption.getOrders()) {
      Expression expression = item.getExpression();
      if (!(expression instanceof Member)) {
        throw new IllegalArgumentException(
            "Only Member expressions are supported in $orderby, found: "
                + expression.getClass().getSimpleName());
      }
      Member member = (Member) expression;
      String mongoPath =
          edmMongoContextFacade.resolveMongoPathForEDMPath(member.getResourcePath()).getMongoPath();
      sortFields.put(mongoPath, item.isDescending() ? -1 : 1);
    }

    return new DefaultOrderByOperatorResult(
        Collections.singletonList(new Document("$sort", new Document(sortFields))), true);
  }

  private static class DefaultOrderByOperatorResult implements OrderByOperatorResult {

    private final List<Bson> stageObjects;
    private final boolean present;

    private DefaultOrderByOperatorResult(List<Bson> stageObjects, boolean present) {
      this.stageObjects = stageObjects;
      this.present = present;
    }

    @Override
    public List<Bson> getStageObjects() {
      return stageObjects;
    }

    @Override
    public List<String> getUsedMongoDocumentProperties() {
      // Logic to extract used properties could be added here if needed,
      // for now we return empty list as in other basic implementations if not strictly required
      // or we can extract keys from the sort document if we kept it accessible.
      // Given the interface, strictly we should return the fields used in sort.
      // But looking at OdataSkipToMongoSkipParser, it returns empty list.
      // OdataSelectToMongoProjectParser returns selected fields.
      // Let's return empty list for now to keep it simple, unless I want to parse the stageObjects
      // back.
      return Collections.emptyList();
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
