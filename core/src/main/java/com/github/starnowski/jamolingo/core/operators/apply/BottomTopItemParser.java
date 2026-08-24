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
    if (method != BottomTop.Method.TOP_COUNT && method != BottomTop.Method.BOTTOM_COUNT) {
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
    int number = Integer.parseInt(((Literal) numberExpr).getText());

    List<Bson> stages = new ArrayList<>();
    // For topcount, sort descending (-1)
    // For bottomcount, sort ascending (1)
    int sortOrder = method == BottomTop.Method.TOP_COUNT ? -1 : 1;
    stages.add(new Document("$sort", new Document(mongoPath, sortOrder)));
    stages.add(new Document("$limit", number));

    return DefaultApplyOperatorResult.builder().withStageObjects(stages).build();
  }
}
