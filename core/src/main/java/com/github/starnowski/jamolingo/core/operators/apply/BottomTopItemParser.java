package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import java.util.ArrayList;
import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.apply.BottomTop;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.bson.Document;
import org.bson.conversions.Bson;

public class BottomTopItemParser implements ApplyItemParser {

  @Override
  public ApplyOperatorResult parse(
      ApplyItem applyItem, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    if (!(applyItem instanceof BottomTop)) {
      throw new IllegalArgumentException("Expected BottomTop item");
    }
    BottomTop bottomTop = (BottomTop) applyItem;

    BottomTop.Method method = bottomTop.getMethod();
    if (method != BottomTop.Method.TOP_COUNT
        && method != BottomTop.Method.BOTTOM_COUNT
        && method != BottomTop.Method.TOP_SUM
        && method != BottomTop.Method.BOTTOM_SUM) {
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

    List<Bson> stages = new ArrayList<>();

    if (method == BottomTop.Method.TOP_COUNT || method == BottomTop.Method.BOTTOM_COUNT) {
      int sortOrder = method == BottomTop.Method.TOP_COUNT ? -1 : 1;
      stages.add(new Document("$sort", new Document(mongoPath, sortOrder)));
      stages.add(new Document("$limit", (int) number));
    } else { // TOP_SUM or BOTTOM_SUM
      int sortOrder = method == BottomTop.Method.TOP_SUM ? -1 : 1;
      // Pre-sort before window function
      stages.add(new Document("$sort", new Document(mongoPath, sortOrder)));

      Document windowFields =
          new Document("sortBy", new Document(mongoPath, sortOrder))
              .append(
                  "output",
                  new Document(
                      "__jamolingo_cumsum",
                      new Document("$sum", "$" + mongoPath)
                          .append(
                              "window",
                              new Document(
                                  "documents", java.util.Arrays.asList("unbounded", "current")))));

      stages.add(new Document("$setWindowFields", windowFields));
      stages.add(
          new Document("$match", new Document("__jamolingo_cumsum", new Document("$lte", number))));
      stages.add(new Document("$unset", "__jamolingo_cumsum"));
    }

    return DefaultApplyOperatorResult.builder().withStageObjects(stages).build();
  }
}
