package com.github.starnowski.jamolingo.core.operators.filter;

import java.util.Collections;
import java.util.List;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.bson.Document;
import org.bson.conversions.Bson;

public class ODataFilterToMongoMatchParser {

  public FilterOperatorResult parse(FilterOption filter, Edm edm)
      throws ODataApplicationException, ExpressionVisitException {
    if (filter == null) return new DefaultFilterOperatorResult();
    Expression expr = filter.getExpression();
    Bson result =
        MongoFilterVisitor.unwrapWrapperIfNeeded(expr.accept(new MongoFilterVisitor(edm)));
    return new DefaultFilterOperatorResult(
        List.of(new Document("$match", new Document("$and", List.of(result)))));
  }

  private static class DefaultFilterOperatorResult implements FilterOperatorResult {

    private final List<Bson> stageObjects;

    public DefaultFilterOperatorResult() {
      this(Collections.emptyList());
    }

    public DefaultFilterOperatorResult(List<Bson> stageObjects) {
      this.stageObjects = stageObjects;
    }

    @Override
    public List<Bson> getStageObjects() {
      return stageObjects;
    }

    @Override
    public List<String> getUsedMongoDocumentProperties() {
      // TODO
      return List.of();
    }

    @Override
    public List<String> getWrittenMongoDocumentProperties() {
      return List.of();
    }

    @Override
    public List<String> getAddedMongoDocumentProperties() {
      return List.of();
    }

    @Override
    public List<String> getRemovedMongoDocumentProperties() {
      return List.of();
    }

    @Override
    public boolean isDocumentShapeRedefined() {
      return false;
    }
  }
}
