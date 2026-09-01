package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import java.util.ArrayList;
import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.apply.GroupBy;
import org.apache.olingo.server.api.uri.queryoption.apply.GroupByItem;
import org.bson.Document;
import org.bson.conversions.Bson;

public class GroupByItemParser implements ApplyItemParser {

  private final ODataApplyToMongoAggregationPipelineParser applyParser;

  public GroupByItemParser(ODataApplyToMongoAggregationPipelineParser applyParser) {
    this.applyParser = applyParser;
  }

  @Override
  public ApplyOperatorResult parse(
      ApplyItem applyItem, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    if (!(applyItem instanceof GroupBy)) {
      throw new IllegalArgumentException("Expected GroupBy item");
    }
    GroupBy groupBy = (GroupBy) applyItem;

    Document idDoc = new Document();
    List<String> groupedMongoPaths = new ArrayList<>();
    for (GroupByItem item : groupBy.getGroupByItems()) {
      StringBuilder pathBuilder = new StringBuilder();
      for (int i = 0; i < item.getPath().size(); i++) {
        if (i > 0) pathBuilder.append("/");
        pathBuilder.append(item.getPath().get(i).getSegmentValue());
      }
      String edmPath = pathBuilder.toString();
      String mongoPath = edmMongoContextFacade.resolveMongoPathForEDMPath(edmPath).getMongoPath();
      idDoc.put(mongoPath, "$" + mongoPath);
      groupedMongoPaths.add(mongoPath);
    }

    Document groupStageDoc = new Document("_id", idDoc);
    Document projectStageDoc = new Document("_id", 0);
    for (String mongoPath : groupedMongoPaths) {
      projectStageDoc.put(mongoPath, "$_id." + mongoPath);
    }

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
            } else {
              String mongoPath =
                  edmMongoContextFacade.resolveMongoPathForEDMPath(path).getMongoPath();
              String method = expr.getStandardMethod().name().toLowerCase();
              if ("average".equals(method)) {
                method = "avg";
              }
              if ("count_distinct".equals(method)) {
                String distinctArrayField = alias + "_distinctArray";
                groupStageDoc.put(distinctArrayField, new Document("$addToSet", "$" + mongoPath));
                projectStageDoc.put(alias, new Document("$size", "$" + distinctArrayField));
              } else {
                String mongoOperator = "$" + method;
                groupStageDoc.put(alias, new Document(mongoOperator, "$" + mongoPath));
                projectStageDoc.put(alias, 1);
              }
            }
          }
        } else {
          remainingItems.add(item);
        }
      }
    }

    Document groupStage = new Document("$group", groupStageDoc);
    Document projectStage = new Document("$project", projectStageDoc);

    List<Bson> stages = new ArrayList<>();
    stages.add(groupStage);
    stages.add(projectStage);

    if (!remainingItems.isEmpty()) {
      ApplyOperatorResult innerResult = applyParser.parse(remainingItems, edmMongoContextFacade);
      stages.addAll(innerResult.getStageObjects());
    }

    return DefaultApplyOperatorResult.builder().withStageObjects(stages).build();
  }
}
