package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.bson.Document;

/** ComputeItemParser type. */
public class ComputeItemParser implements ApplyItemParser {
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
    if (!(applyItem instanceof org.apache.olingo.server.api.uri.queryoption.apply.Compute)) {
      throw new IllegalArgumentException("Expected Compute, got " + applyItem.getClass().getName());
    }
    org.apache.olingo.server.api.uri.queryoption.apply.Compute compute =
        (org.apache.olingo.server.api.uri.queryoption.apply.Compute) applyItem;
    Document addFieldsDoc = new Document();

    String rootMongoPath =
        ApplyGraphLookupHelper.resolveRootMongoPath(edmMongoContextFacade, context);
    boolean graphLookupMode =
        ApplyGraphLookupHelper.isGraphLookupUnwindMode(edmMongoContextFacade, context);

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

    List<String> addedProperties = new ArrayList<>();
    try {
      for (org.apache.olingo.server.api.uri.queryoption.apply.ComputeExpression expr :
          compute.getExpressions()) {
        org.bson.conversions.Bson parsedExpression = expr.getExpression().accept(visitor);
        org.bson.conversions.Bson unwrapped =
            com.github.starnowski.jamolingo.core.operators.filter.MongoFilterVisitor
                .unwrapWrapperIfNeeded(parsedExpression);
        String targetField =
            rootMongoPath != null ? rootMongoPath + "." + expr.getAlias() : expr.getAlias();
        if (graphLookupMode) {
          addFieldsDoc.append(
              targetField,
              new Document(
                  "$cond",
                  new Document(
                          "if",
                          new Document(
                              "$eq",
                              Arrays.asList(
                                  new Document(
                                      "$ifNull",
                                      Arrays.asList(
                                          "$" + rootMongoPath + "." + context.getDepthVariable(),
                                          null)),
                                  null)))
                      .append("then", "$$REMOVE")
                      .append("else", unwrapped)));
        } else {
          addFieldsDoc.append(targetField, unwrapped);
        }
        addedProperties.add(expr.getAlias());
      }
    } catch (org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException
        | org.apache.olingo.server.api.ODataApplicationException e) {
      throw new RuntimeException("Failed to parse compute expression", e);
    }

    List<String> usedProperties =
        visitor.getUsedMongoDBProperties().stream()
            .map(p -> ApplyGraphLookupHelper.stripRootMongoPath(p, rootMongoPath))
            .collect(Collectors.toList());

    return DefaultApplyOperatorResult.builder()
        .withStageObjects(Collections.singletonList(new Document("$addFields", addFieldsDoc)))
        .withUsedMongoDocumentProperties(usedProperties)
        .withWrittenMongoDocumentProperties(addedProperties)
        .withAddedMongoDocumentProperties(addedProperties)
        .withDocumentShapeRedefined(false)
        .build();
  }
}
