package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.operators.top.OdataTopToMongoLimitParser;
import com.github.starnowski.jamolingo.core.operators.top.TopOperatorResult;
import java.util.Arrays;
import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.apply.Top;
import org.bson.Document;
import org.bson.conversions.Bson;

/** TopItemParser type. */
public class TopItemParser implements ApplyItemParser {

  private final OdataTopToMongoLimitParser topParser = new OdataTopToMongoLimitParser();

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
    if (!(applyItem instanceof Top)) {
      throw new IllegalArgumentException("Expected Top item");
    }
    Top topItem = (Top) applyItem;

    if (ApplyGraphLookupHelper.isGraphLookupUnwindMode(edmMongoContextFacade, context)) {
      String rootMongoPath =
          ApplyGraphLookupHelper.resolveRootMongoPath(edmMongoContextFacade, context);
      int limit = topItem.getTopOption().getValue();
      List<Bson> stages =
          ApplyGraphLookupHelper.buildSlicedGroupStages(
              context,
              rootMongoPath,
              new Document("$slice", Arrays.asList("$__jamolingo_docs", limit)));
      return DefaultApplyOperatorResult.builder()
          .withStageObjects(stages)
          .withDocumentShapeRedefined(false)
          .build();
    }

    TopOperatorResult result = topParser.parse(topItem.getTopOption());

    return DefaultApplyOperatorResult.builder()
        .withStageObjects(result.getStageObjects())
        .withUsedMongoDocumentProperties(result.getUsedMongoDocumentProperties())
        .withWrittenMongoDocumentProperties(result.getWrittenMongoDocumentProperties())
        .withAddedMongoDocumentProperties(result.getAddedMongoDocumentProperties())
        .withRemovedMongoDocumentProperties(result.getRemovedMongoDocumentProperties())
        .withDocumentShapeRedefined(result.isDocumentShapeRedefined())
        .build();
  }
}
