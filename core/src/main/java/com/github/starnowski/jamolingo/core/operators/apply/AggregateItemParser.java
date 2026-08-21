package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;

public class AggregateItemParser implements ApplyItemParser {
  @Override
  public ApplyOperatorResult parse(
      ApplyItem applyItem, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    if (!(applyItem instanceof org.apache.olingo.server.api.uri.queryoption.apply.Aggregate)) {
      throw new IllegalArgumentException("Expected Aggregate item");
    }
    org.apache.olingo.server.api.uri.queryoption.apply.Aggregate aggregate =
        (org.apache.olingo.server.api.uri.queryoption.apply.Aggregate) applyItem;

    org.bson.Document groupStageDoc = new org.bson.Document("_id", null);
    org.bson.Document projectStageDoc = new org.bson.Document("_id", 0);

    for (org.apache.olingo.server.api.uri.queryoption.apply.AggregateExpression expr :
        aggregate.getExpressions()) {
      String alias = expr.getAlias();
      String path = null;
      if (expr.getPath() != null && !expr.getPath().isEmpty()) {
        path = expr.getPath().get(0).getSegmentValue();
      } else if (expr.getExpression()
          instanceof org.apache.olingo.server.api.uri.queryoption.expression.Member) {
        org.apache.olingo.server.api.uri.queryoption.expression.Member member =
            (org.apache.olingo.server.api.uri.queryoption.expression.Member) expr.getExpression();
        path = member.getResourcePath().getUriResourceParts().get(0).getSegmentValue();
      } else {
        throw new IllegalArgumentException("Cannot extract path from AggregateExpression");
      }
      String mongoPath = edmMongoContextFacade.resolveMongoPathForEDMPath(path).getMongoPath();
      String method = expr.getStandardMethod().name().toLowerCase();

      String mongoOperator = "$" + method;
      groupStageDoc.put(alias, new org.bson.Document(mongoOperator, "$" + mongoPath));
      projectStageDoc.put(alias, 1);
    }

    org.bson.Document groupStage = new org.bson.Document("$group", groupStageDoc);
    org.bson.Document projectStage = new org.bson.Document("$project", projectStageDoc);

    java.util.List<org.bson.conversions.Bson> stages = new java.util.ArrayList<>();
    stages.add(groupStage);
    stages.add(projectStage);

    return DefaultApplyOperatorResult.builder().withStageObjects(stages).build();
  }
}
