package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.apply.BottomTop;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.bson.Document;
import org.bson.conversions.Bson;

/** BottomTopItemParser type. */
public class BottomTopItemParser implements ApplyItemParser {

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
    if (!(applyItem instanceof BottomTop)) {
      throw new IllegalArgumentException("Expected BottomTop item");
    }
    BottomTop bottomTop = (BottomTop) applyItem;

    BottomTop.Method method = bottomTop.getMethod();
    if (method != BottomTop.Method.TOP_COUNT
        && method != BottomTop.Method.BOTTOM_COUNT
        && method != BottomTop.Method.TOP_SUM
        && method != BottomTop.Method.BOTTOM_SUM
        && method != BottomTop.Method.TOP_PERCENT
        && method != BottomTop.Method.BOTTOM_PERCENT) {
      throw new UnsupportedOperationException("Unsupported BottomTop method: " + method);
    }

    Expression value = bottomTop.getValue();
    if (!(value instanceof Member)) {
      throw new IllegalArgumentException("Expected Member for BottomTop value");
    }
    Member member = (Member) value;
    String edmPath = member.getResourcePath().getUriResourceParts().get(0).getSegmentValue();
    String mongoPath = edmMongoContextFacade.resolveMongoPathForEDMPath(edmPath).getMongoPath();

    Expression numberExpr = bottomTop.getNumber();
    if (!(numberExpr instanceof Literal)) {
      throw new IllegalArgumentException("Expected Literal for BottomTop number");
    }
    double number = Double.parseDouble(((Literal) numberExpr).getText());

    String rootMongoPath =
        ApplyGraphLookupHelper.resolveRootMongoPath(edmMongoContextFacade, context);
    boolean graphLookupMode =
        ApplyGraphLookupHelper.isGraphLookupUnwindMode(edmMongoContextFacade, context);

    List<Bson> stages = new ArrayList<>();

    if (method == BottomTop.Method.TOP_COUNT || method == BottomTop.Method.BOTTOM_COUNT) {
      int sortOrder = method == BottomTop.Method.TOP_COUNT ? -1 : 1;
      stages.add(new Document("$sort", new Document(mongoPath, sortOrder)));
      if (graphLookupMode) {
        stages.addAll(
            ApplyGraphLookupHelper.buildSlicedGroupStages(
                context,
                rootMongoPath,
                new Document("$slice", Arrays.asList("$__jamolingo_docs", (int) number))));
      } else {
        stages.add(new Document("$limit", (int) number));
      }
    } else if (method == BottomTop.Method.TOP_SUM || method == BottomTop.Method.BOTTOM_SUM) {
      int sortOrder = method == BottomTop.Method.TOP_SUM ? -1 : 1;
      // Pre-sort before window function
      stages.add(new Document("$sort", new Document(mongoPath, sortOrder)));

      Document windowFields = new Document();
      if (graphLookupMode) {
        windowFields.append(
            "partitionBy", ApplyGraphLookupHelper.buildGroupIdDocument(context, rootMongoPath));
      }
      windowFields
          .append("sortBy", new Document(mongoPath, sortOrder))
          .append(
              "output",
              new Document(
                  "__jamolingo_cumsum",
                  new Document("$sum", "$" + mongoPath)
                      .append(
                          "window",
                          new Document("documents", Arrays.asList("unbounded", "current")))));

      stages.add(new Document("$setWindowFields", windowFields));
      Document matchCond = new Document("__jamolingo_cumsum", new Document("$lte", number));
      if (graphLookupMode) {
        stages.add(
            new Document(
                "$match",
                new Document(
                    "$or",
                    Arrays.asList(
                        new Document(
                            rootMongoPath + "." + context.getDepthVariable(),
                            new Document("$exists", false)),
                        matchCond))));
      } else {
        stages.add(new Document("$match", matchCond));
      }
      stages.add(new Document("$unset", "__jamolingo_cumsum"));
    } else { // TOP_PERCENT or BOTTOM_PERCENT
      int sortOrder = method == BottomTop.Method.TOP_PERCENT ? -1 : 1;
      // Pre-sort before window function
      stages.add(new Document("$sort", new Document(mongoPath, sortOrder)));

      Document windowFields = new Document();
      if (graphLookupMode) {
        windowFields.append(
            "partitionBy", ApplyGraphLookupHelper.buildGroupIdDocument(context, rootMongoPath));
      }
      windowFields
          .append("sortBy", new Document(mongoPath, sortOrder))
          .append(
              "output",
              new Document(
                      "__jamolingo_totalsum",
                      new Document("$sum", "$" + mongoPath)
                          .append(
                              "window",
                              new Document("documents", Arrays.asList("unbounded", "unbounded"))))
                  .append(
                      "__jamolingo_cumsum",
                      new Document("$sum", "$" + mongoPath)
                          .append(
                              "window",
                              new Document("documents", Arrays.asList("unbounded", "current")))));

      stages.add(new Document("$setWindowFields", windowFields));
      Document exprMatchCond =
          new Document(
              "$expr",
              new Document(
                  "$lte",
                  Arrays.asList(
                      "$__jamolingo_cumsum",
                      new Document(
                          "$multiply", Arrays.asList("$__jamolingo_totalsum", number / 100.0)))));
      if (graphLookupMode) {
        stages.add(
            new Document(
                "$match",
                new Document(
                    "$or",
                    Arrays.asList(
                        new Document(
                            rootMongoPath + "." + context.getDepthVariable(),
                            new Document("$exists", false)),
                        exprMatchCond))));
      } else {
        stages.add(new Document("$match", exprMatchCond));
      }
      stages.add(
          new Document("$unset", Arrays.asList("__jamolingo_totalsum", "__jamolingo_cumsum")));
    }

    String usedProp = ApplyGraphLookupHelper.stripRootMongoPath(mongoPath, rootMongoPath);
    return DefaultApplyOperatorResult.builder()
        .withStageObjects(stages)
        .withUsedMongoDocumentProperties(Collections.singletonList(usedProp))
        .withDocumentShapeRedefined(false)
        .build();
  }
}
