package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.bson.Document;
import org.bson.conversions.Bson;

/** AggregateItemParser type. */
public class AggregateItemParser implements ApplyItemParser {
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
    if (!(applyItem instanceof org.apache.olingo.server.api.uri.queryoption.apply.Aggregate)) {
      throw new IllegalArgumentException("Expected Aggregate item");
    }
    org.apache.olingo.server.api.uri.queryoption.apply.Aggregate aggregate =
        (org.apache.olingo.server.api.uri.queryoption.apply.Aggregate) applyItem;

    String rootMongoPath =
        ApplyGraphLookupHelper.resolveRootMongoPath(edmMongoContextFacade, context);
    boolean graphLookupMode =
        ApplyGraphLookupHelper.isGraphLookupUnwindMode(edmMongoContextFacade, context);

    Document groupStageDoc =
        graphLookupMode
            ? new Document(
                    "_id", ApplyGraphLookupHelper.buildGroupIdDocument(context, rootMongoPath))
                .append("tmp_root", new Document("$first", "$$ROOT"))
            : new Document("_id", null);
    Document projectStageDoc = new Document("_id", 0);
    Document childResultDoc = new Document();

    List<String> usedProperties = new ArrayList<>();
    List<String> addedProperties = new ArrayList<>();

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
      if (expr.getStandardMethod() == null && "$count".equals(path)) {
        groupStageDoc.put(alias, new Document("$sum", 1));
        projectStageDoc.put(alias, 1);
        childResultDoc.put(alias, "$" + alias);
        addedProperties.add(alias);
      } else {
        String mongoPath = edmMongoContextFacade.resolveMongoPathForEDMPath(path).getMongoPath();
        String method = expr.getStandardMethod().name().toLowerCase();
        if ("average".equals(method)) {
          method = "avg";
        }
        usedProperties.add(ApplyGraphLookupHelper.stripRootMongoPath(mongoPath, rootMongoPath));
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

    return DefaultApplyOperatorResult.builder()
        .withStageObjects(stages)
        .withUsedMongoDocumentProperties(usedProperties)
        .withWrittenMongoDocumentProperties(addedProperties)
        .withAddedMongoDocumentProperties(addedProperties)
        .withDocumentShapeRedefined(true)
        .build();
  }
}
