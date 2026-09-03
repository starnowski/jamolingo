package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;

public class ComputeItemParser implements ApplyItemParser {
  @Override
  public ApplyOperatorResult parse(
      ApplyItem applyItem, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    if (!(applyItem instanceof org.apache.olingo.server.api.uri.queryoption.apply.Compute)) {
      throw new IllegalArgumentException("Expected Compute, got " + applyItem.getClass().getName());
    }
    org.apache.olingo.server.api.uri.queryoption.apply.Compute compute =
        (org.apache.olingo.server.api.uri.queryoption.apply.Compute) applyItem;
    org.bson.Document addFieldsDoc = new org.bson.Document();

    com.github.starnowski.jamolingo.core.operators.filter.MongoFilterVisitor visitor =
        new com.github.starnowski.jamolingo.core.operators.filter.MongoFilterVisitor(
            edmMongoContextFacade,
            com.github.starnowski.jamolingo.core.operators.filter.MongoFilterVisitor
                .MongoFilterVisitorContext.builder()
                .isExprMode(true)
                .isRootContext(true)
                .build(),
            com.github.starnowski.jamolingo.core.operators.filter
                .DefaultMongoFilterVisitorCommonContext.builder()
                .build());

    try {
      for (org.apache.olingo.server.api.uri.queryoption.apply.ComputeExpression expr :
          compute.getExpressions()) {
        org.bson.conversions.Bson parsedExpression = expr.getExpression().accept(visitor);
        org.bson.conversions.Bson unwrapped =
            com.github.starnowski.jamolingo.core.operators.filter.MongoFilterVisitor
                .unwrapWrapperIfNeeded(parsedExpression);
        addFieldsDoc.append(expr.getAlias(), unwrapped);
      }
    } catch (org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException
        | org.apache.olingo.server.api.ODataApplicationException e) {
      throw new RuntimeException("Failed to parse compute expression", e);
    }

    return DefaultApplyOperatorResult.builder()
        .withStageObjects(
            java.util.Collections.singletonList(new org.bson.Document("$addFields", addFieldsDoc)))
        .build();
  }
}
