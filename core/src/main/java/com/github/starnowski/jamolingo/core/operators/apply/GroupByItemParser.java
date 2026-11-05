package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.apply.GroupBy;
import org.apache.olingo.server.api.uri.queryoption.apply.GroupByItem;
import org.bson.Document;
import org.bson.conversions.Bson;

/** GroupByItemParser type. */
public class GroupByItemParser implements ApplyItemParser {

  private final ODataApplyToMongoAggregationPipelineParser applyParser;

  /** GroupByItemParser constructor. */
  public GroupByItemParser(ODataApplyToMongoAggregationPipelineParser applyParser) {
    this.applyParser = applyParser;
  }

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
    if (!(applyItem instanceof GroupBy)) {
      throw new IllegalArgumentException("Expected GroupBy item");
    }
    GroupBy groupBy = (GroupBy) applyItem;

    String rootMongoPath =
        ApplyGraphLookupHelper.resolveRootMongoPath(edmMongoContextFacade, context);
    boolean graphLookupMode =
        ApplyGraphLookupHelper.isGraphLookupUnwindMode(edmMongoContextFacade, context);

    Document idDoc =
        graphLookupMode
            ? ApplyGraphLookupHelper.buildGroupIdDocument(context, rootMongoPath)
            : new Document();
    List<String> groupedMongoPaths = new ArrayList<>();
    List<String> groupedRelativePaths = new ArrayList<>();
    Document childResultDoc = new Document();
    int groupPropIndex = 0;

    for (GroupByItem item : groupBy.getGroupByItems()) {
      StringBuilder pathBuilder = new StringBuilder();
      for (int i = 0; i < item.getPath().size(); i++) {
        if (i > 0) pathBuilder.append("/");
        pathBuilder.append(item.getPath().get(i).getSegmentValue());
      }
      String edmPath = pathBuilder.toString();
      String mongoPath = edmMongoContextFacade.resolveMongoPathForEDMPath(edmPath).getMongoPath();
      String relativePath = ApplyGraphLookupHelper.stripRootMongoPath(mongoPath, rootMongoPath);
      if (graphLookupMode) {
        String groupKey = "group_" + (groupPropIndex++);
        idDoc.put(groupKey, "$" + mongoPath);
        ApplyGraphLookupHelper.putNestedProperty(childResultDoc, relativePath, "$_id." + groupKey);
      } else {
        idDoc.put(mongoPath, "$" + mongoPath);
      }
      groupedMongoPaths.add(mongoPath);
      groupedRelativePaths.add(relativePath);
    }

    Document groupStageDoc = new Document("_id", idDoc);
    if (graphLookupMode) {
      groupStageDoc.put("tmp_root", new Document("$first", "$$ROOT"));
    }
    Document projectStageDoc = new Document("_id", 0);
    if (!graphLookupMode) {
      for (String mongoPath : groupedMongoPaths) {
        projectStageDoc.put(mongoPath, "$_id." + mongoPath);
      }
    }

    List<String> usedProperties = new ArrayList<>(groupedRelativePaths);
    List<String> addedProperties = new ArrayList<>();

    List<ApplyItem> remainingItems = new ArrayList<>();
    if (groupBy.getApplyOption() != null) {
      for (ApplyItem item : groupBy.getApplyOption().getApplyItems()) {
        if (item instanceof org.apache.olingo.server.api.uri.queryoption.apply.Aggregate) {
          org.apache.olingo.server.api.uri.queryoption.apply.Aggregate aggregate =
              (org.apache.olingo.server.api.uri.queryoption.apply.Aggregate) item;
          for (org.apache.olingo.server.api.uri.queryoption.apply.AggregateExpression expr :
              aggregate.getExpressions()) {
            String alias = expr.getAlias();
            String path = null;
            if (expr.getPath() != null && !expr.getPath().isEmpty()) {
              path = expr.getPath().get(0).getSegmentValue();
            } else if (expr.getExpression()
                instanceof org.apache.olingo.server.api.uri.queryoption.expression.Member) {
              org.apache.olingo.server.api.uri.queryoption.expression.Member member =
                  (org.apache.olingo.server.api.uri.queryoption.expression.Member)
                      expr.getExpression();
              path = member.getResourcePath().getUriResourceParts().get(0).getSegmentValue();
            } else {
              throw new IllegalArgumentException("Cannot extract path from AggregateExpression");
            }
            if (expr.getStandardMethod() == null && "$count".equals(path)) {
              groupStageDoc.put(alias, new Document("$sum", 1));
              projectStageDoc.put(alias, 1);
              childResultDoc.put(alias, "$" + alias);
              addedProperties.add(alias);
            } else {
              String mongoPath =
                  edmMongoContextFacade.resolveMongoPathForEDMPath(path).getMongoPath();
              String method = expr.getStandardMethod().name().toLowerCase();
              if ("average".equals(method)) {
                method = "avg";
              }
              usedProperties.add(
                  ApplyGraphLookupHelper.stripRootMongoPath(mongoPath, rootMongoPath));
              addedProperties.add(alias);
              if ("count_distinct".equals(method)) {
                String distinctArrayField = alias + "_distinctArray";
                groupStageDoc.put(distinctArrayField, new Document("$addToSet", "$" + mongoPath));
                Document sizeExpr = new Document("$size", "$" + distinctArrayField);
                projectStageDoc.put(alias, sizeExpr);
                childResultDoc.put(alias, sizeExpr);
              } else {
                String mongoOperator = "$" + method;
                groupStageDoc.put(alias, new Document(mongoOperator, "$" + mongoPath));
                projectStageDoc.put(alias, 1);
                childResultDoc.put(alias, "$" + alias);
              }
            }
          }
        } else {
          remainingItems.add(item);
        }
      }
    }

    List<Bson> stages = new ArrayList<>();
    stages.add(new Document("$group", groupStageDoc));
    if (graphLookupMode) {
      if (context.getMongoConnectTo() != null
          && !childResultDoc.containsKey(context.getMongoConnectTo())) {
        childResultDoc.put(context.getMongoConnectTo(), "$_id.parent_id");
      }
      childResultDoc.put(context.getDepthVariable(), "$_id.depth");
      stages.add(
          new Document(
              "$set",
              new Document(
                  "tmp_root." + rootMongoPath,
                  new Document(
                      "$cond",
                      new Document(
                              "if",
                              new Document(
                                  "$eq",
                                  Arrays.asList(
                                      new Document("$ifNull", Arrays.asList("$_id.depth", null)),
                                      null)))
                          .append("then", new Document())
                          .append("else", childResultDoc)))));
      stages.add(new Document("$replaceRoot", new Document("newRoot", "$tmp_root")));
    } else {
      stages.add(new Document("$project", projectStageDoc));
    }

    List<String> finalUsedProperties = new ArrayList<>(usedProperties);
    List<String> finalWrittenProperties = new ArrayList<>(addedProperties);
    finalWrittenProperties.addAll(groupedRelativePaths);
    List<String> finalAddedProperties = new ArrayList<>(addedProperties);
    List<String> finalRemovedProperties = new ArrayList<>();

    if (!remainingItems.isEmpty()) {
      ApplyOperatorResult innerResult =
          context == null
              ? applyParser.parse(remainingItems, edmMongoContextFacade)
              : applyParser.parse(remainingItems, edmMongoContextFacade, context);
      stages.addAll(innerResult.getStageObjects());
      if (innerResult.getUsedMongoDocumentProperties() != null) {
        finalUsedProperties.addAll(innerResult.getUsedMongoDocumentProperties());
      }
      if (innerResult.getWrittenMongoDocumentProperties() != null) {
        finalWrittenProperties.addAll(innerResult.getWrittenMongoDocumentProperties());
      }
      if (innerResult.getAddedMongoDocumentProperties() != null) {
        finalAddedProperties.addAll(innerResult.getAddedMongoDocumentProperties());
      }
      if (innerResult.getRemovedMongoDocumentProperties() != null) {
        finalRemovedProperties.addAll(innerResult.getRemovedMongoDocumentProperties());
      }
    }

    return DefaultApplyOperatorResult.builder()
        .withStageObjects(stages)
        .withUsedMongoDocumentProperties(finalUsedProperties)
        .withWrittenMongoDocumentProperties(finalWrittenProperties)
        .withAddedMongoDocumentProperties(finalAddedProperties)
        .withRemovedMongoDocumentProperties(finalRemovedProperties)
        .withDocumentShapeRedefined(true)
        .build();
  }
}
