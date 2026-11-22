package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.operators.NavigationPropertyExtractor;
import com.github.starnowski.jamolingo.core.operators.UsedNavigationProperty;
import com.github.starnowski.jamolingo.core.operators.filter.DefaultMongoFilterVisitorCommonContext;
import com.github.starnowski.jamolingo.core.operators.filter.MongoFilterVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceCount;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.apply.Aggregate;
import org.apache.olingo.server.api.uri.queryoption.apply.AggregateExpression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.bson.Document;
import org.bson.conversions.Bson;

/** AggregateItemParser type. */
public class AggregateItemParser implements ApplyItemParser {
  @Override
  public ApplyOperatorResult parse(
      ApplyItem applyItem, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    if (!(applyItem instanceof Aggregate)) {
      throw new IllegalArgumentException("Expected Aggregate item");
    }
    Aggregate aggregate = (Aggregate) applyItem;

    Document groupStageDoc = new Document("_id", null);
    Document projectStageDoc = new Document("_id", 0);

    List<String> usedProperties = new ArrayList<>();
    List<String> addedProperties = new ArrayList<>();
    Set<UsedNavigationProperty> usedNavigationProperties = new LinkedHashSet<>();

    for (AggregateExpression expr : aggregate.getExpressions()) {
      processAggregateExpression(
          expr,
          null,
          edmMongoContextFacade,
          groupStageDoc,
          projectStageDoc,
          usedProperties,
          addedProperties,
          usedNavigationProperties);
    }

    Document groupStage = new Document("$group", groupStageDoc);
    Document projectStage = new Document("$project", projectStageDoc);

    List<Bson> stages = new ArrayList<>();
    stages.add(groupStage);
    stages.add(projectStage);

    return DefaultApplyOperatorResult.builder()
        .withStageObjects(stages)
        .withUsedMongoDocumentProperties(usedProperties)
        .withWrittenMongoDocumentProperties(addedProperties)
        .withAddedMongoDocumentProperties(addedProperties)
        .withDocumentShapeRedefined(true)
        .withUsedNavigationProperties(new ArrayList<>(usedNavigationProperties))
        .build();
  }

  static void processAggregateExpression(
      AggregateExpression expr,
      String baseEdmPathPrefix,
      EdmPropertyMongoPathResolver edmMongoContextFacade,
      Document groupStageDoc,
      Document projectStageDoc,
      List<String> usedProperties,
      List<String> addedProperties,
      Set<UsedNavigationProperty> usedNavigationProperties) {
    if (expr.getFrom() != null && !expr.getFrom().isEmpty()) {
      for (AggregateExpression fromExpr : expr.getFrom()) {
        if (fromExpr.getExpression() instanceof Member) {
          Member fromMember = (Member) fromExpr.getExpression();
          usedNavigationProperties.addAll(
              NavigationPropertyExtractor.extract(
                  fromMember.getResourcePath(), baseEdmPathPrefix, edmMongoContextFacade));
          String fromRelPath =
              NavigationPropertyExtractor.extractPath(fromMember.getResourcePath(), "/");
          String fullFromPath = joinEdmPath(baseEdmPathPrefix, fromRelPath);
          if (!fullFromPath.isEmpty()) {
            usedProperties.add(
                edmMongoContextFacade.resolveMongoPathForEDMPath(fullFromPath).getMongoPath());
          }
        }
      }
    }

    if (expr.getInlineAggregateExpression() != null) {
      usedNavigationProperties.addAll(
          NavigationPropertyExtractor.extract(
              expr.getPath(), baseEdmPathPrefix, edmMongoContextFacade));
      String prefixSegment = NavigationPropertyExtractor.extractPath(expr.getPath(), "/");
      String nextPrefix = joinEdmPath(baseEdmPathPrefix, prefixSegment);
      processAggregateExpression(
          expr.getInlineAggregateExpression(),
          nextPrefix,
          edmMongoContextFacade,
          groupStageDoc,
          projectStageDoc,
          usedProperties,
          addedProperties,
          usedNavigationProperties);
      return;
    }

    String alias = expr.getAlias();
    if (expr.getPath() != null && !expr.getPath().isEmpty()) {
      usedNavigationProperties.addAll(
          NavigationPropertyExtractor.extract(
              expr.getPath(), baseEdmPathPrefix, edmMongoContextFacade));
      UriResource lastResource = expr.getPath().get(expr.getPath().size() - 1);
      boolean endsWithCount =
          lastResource instanceof UriResourceCount
              || "$count".equals(lastResource.getSegmentValue());
      String relPath = NavigationPropertyExtractor.extractPath(expr.getPath(), "/");
      String fullEdmPath = joinEdmPath(baseEdmPathPrefix, relPath);

      if (expr.getStandardMethod() == null && endsWithCount) {
        if (fullEdmPath.isEmpty() || "$count".equals(fullEdmPath)) {
          groupStageDoc.put(alias, new Document("$sum", 1));
          projectStageDoc.put(alias, 1);
          addedProperties.add(alias);
        } else {
          String mongoPath =
              edmMongoContextFacade.resolveMongoPathForEDMPath(fullEdmPath).getMongoPath();
          usedProperties.add(mongoPath);
          addedProperties.add(alias);
          groupStageDoc.put(
              alias,
              new Document(
                  "$sum",
                  new Document(
                      "$size",
                      new Document("$ifNull", Arrays.asList("$" + mongoPath, List.of())))));
          projectStageDoc.put(alias, 1);
        }
      } else {
        String mongoPath =
            edmMongoContextFacade.resolveMongoPathForEDMPath(fullEdmPath).getMongoPath();
        applyStandardMethod(
            expr.getStandardMethod(),
            mongoPath,
            "$" + mongoPath,
            alias,
            groupStageDoc,
            projectStageDoc,
            usedProperties,
            addedProperties);
      }
    } else if (expr.getExpression() instanceof Member) {
      Member member = (Member) expr.getExpression();
      usedNavigationProperties.addAll(
          NavigationPropertyExtractor.extract(
              member.getResourcePath(), baseEdmPathPrefix, edmMongoContextFacade));
      List<UriResource> parts =
          member.getResourcePath() != null ? member.getResourcePath().getUriResourceParts() : null;
      UriResource lastPart = parts != null && !parts.isEmpty() ? parts.get(parts.size() - 1) : null;
      boolean endsWithCount =
          lastPart instanceof UriResourceCount
              || (lastPart != null && "$count".equals(lastPart.getSegmentValue()));
      String relPath = NavigationPropertyExtractor.extractPath(member.getResourcePath(), "/");
      String fullEdmPath = joinEdmPath(baseEdmPathPrefix, relPath);

      if (expr.getStandardMethod() == null
          && endsWithCount
          && (fullEdmPath.isEmpty() || "$count".equals(fullEdmPath))) {
        groupStageDoc.put(alias, new Document("$sum", 1));
        projectStageDoc.put(alias, 1);
        addedProperties.add(alias);
      } else {
        String mongoPath =
            edmMongoContextFacade.resolveMongoPathForEDMPath(fullEdmPath).getMongoPath();
        Object operand =
            endsWithCount
                ? new Document(
                    "$size", new Document("$ifNull", Arrays.asList("$" + mongoPath, List.of())))
                : "$" + mongoPath;
        applyStandardMethod(
            expr.getStandardMethod(),
            mongoPath,
            operand,
            alias,
            groupStageDoc,
            projectStageDoc,
            usedProperties,
            addedProperties);
      }
    } else if (expr.getExpression() != null) {
      MongoFilterVisitor visitor =
          new MongoFilterVisitor(
              edmMongoContextFacade,
              MongoFilterVisitor.MongoFilterVisitorContext.builder()
                  .isExprMode(true)
                  .isRootContext(true)
                  .build(),
              DefaultMongoFilterVisitorCommonContext.builder().build());
      try {
        Bson parsedExpr =
            MongoFilterVisitor.unwrapWrapperIfNeeded(expr.getExpression().accept(visitor));
        usedProperties.addAll(visitor.getUsedMongoDBProperties());
        usedNavigationProperties.addAll(visitor.getUsedNavigationProperties());
        applyStandardMethodForOperand(
            expr.getStandardMethod(),
            parsedExpr,
            alias,
            groupStageDoc,
            projectStageDoc,
            addedProperties);
      } catch (ExpressionVisitException | ODataApplicationException e) {
        throw new RuntimeException("Failed to parse aggregate expression", e);
      }
    } else {
      throw new IllegalArgumentException("Cannot extract path from AggregateExpression");
    }
  }

  private static void applyStandardMethod(
      AggregateExpression.StandardMethod standardMethod,
      String mongoPath,
      Object mongoOperand,
      String alias,
      Document groupStageDoc,
      Document projectStageDoc,
      List<String> usedProperties,
      List<String> addedProperties) {
    usedProperties.add(mongoPath);
    applyStandardMethodForOperand(
        standardMethod, mongoOperand, alias, groupStageDoc, projectStageDoc, addedProperties);
  }

  private static void applyStandardMethodForOperand(
      AggregateExpression.StandardMethod standardMethod,
      Object mongoOperand,
      String alias,
      Document groupStageDoc,
      Document projectStageDoc,
      List<String> addedProperties) {
    String method = standardMethod.name().toLowerCase();
    if ("average".equals(method)) {
      method = "avg";
    }
    addedProperties.add(alias);
    if ("count_distinct".equals(method)) {
      String distinctArrayField = alias + "_distinctArray";
      groupStageDoc.put(distinctArrayField, new Document("$addToSet", mongoOperand));
      projectStageDoc.put(alias, new Document("$size", "$" + distinctArrayField));
    } else {
      String mongoOperator = "$" + method;
      groupStageDoc.put(alias, new Document(mongoOperator, mongoOperand));
      projectStageDoc.put(alias, 1);
    }
  }

  private static String joinEdmPath(String prefix, String segment) {
    if (prefix == null || prefix.isEmpty()) {
      return segment != null ? segment : "";
    }
    if (segment == null || segment.isEmpty()) {
      return prefix;
    }
    return prefix + "/" + segment;
  }
}
