package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.operators.skip.OdataSkipToMongoSkipParser;
import com.github.starnowski.jamolingo.core.operators.skip.SkipOperatorResult;
import java.util.Arrays;
import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.apply.Skip;
import org.bson.Document;
import org.bson.conversions.Bson;

/** SkipItemParser type. */
public class SkipItemParser implements ApplyItemParser {

  private final OdataSkipToMongoSkipParser skipParser = new OdataSkipToMongoSkipParser();

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
    if (!(applyItem instanceof Skip)) {
      throw new IllegalArgumentException("Expected Skip item");
    }
    Skip skipItem = (Skip) applyItem;

    if (ApplyGraphLookupHelper.isGraphLookupUnwindMode(edmMongoContextFacade, context)) {
      String rootMongoPath =
          ApplyGraphLookupHelper.resolveRootMongoPath(edmMongoContextFacade, context);
      int skip = skipItem.getSkipOption().getValue();
      List<Bson> stages =
          ApplyGraphLookupHelper.buildSlicedGroupStages(
              context,
              rootMongoPath,
              new Document(
                  "$slice",
                  Arrays.asList(
                      "$__jamolingo_docs", skip, new Document("$size", "$__jamolingo_docs"))));
      return DefaultApplyOperatorResult.builder()
          .withStageObjects(stages)
          .withDocumentShapeRedefined(false)
          .build();
    }

    SkipOperatorResult result = skipParser.parse(skipItem.getSkipOption());

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
