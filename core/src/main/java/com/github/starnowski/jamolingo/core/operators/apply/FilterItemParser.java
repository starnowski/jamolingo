package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.operators.filter.FilterOperatorResult;
import com.github.starnowski.jamolingo.core.operators.filter.ODataFilterToMongoMatchParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.apply.Filter;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.bson.Document;
import org.bson.conversions.Bson;

/** FilterItemParser type. */
public class FilterItemParser implements ApplyItemParser {

  private final ODataFilterToMongoMatchParser filterParser = new ODataFilterToMongoMatchParser();

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
    if (!(applyItem instanceof Filter)) {
      throw new IllegalArgumentException("Expected Filter item");
    }
    Filter filterItem = (Filter) applyItem;

    try {
      FilterOperatorResult result =
          filterParser.parse(filterItem.getFilterOption(), edmMongoContextFacade);
      String rootMongoPath =
          ApplyGraphLookupHelper.resolveRootMongoPath(edmMongoContextFacade, context);
      boolean graphLookupMode =
          ApplyGraphLookupHelper.isGraphLookupUnwindMode(edmMongoContextFacade, context);

      List<Bson> stages = result.getStageObjects();
      if (graphLookupMode && stages != null && !stages.isEmpty()) {
        List<Bson> wrappedStages = new ArrayList<>(stages.size());
        String depthFieldPath = rootMongoPath + "." + context.getDepthVariable();
        for (Bson stage : stages) {
          if (stage instanceof Document && ((Document) stage).containsKey("$match")) {
            Object matchCondition = ((Document) stage).get("$match");
            wrappedStages.add(
                new Document(
                    "$match",
                    new Document(
                        "$or",
                        Arrays.asList(
                            new Document(depthFieldPath, new Document("$exists", false)),
                            new Document(
                                "$and",
                                Arrays.asList(
                                    new Document(depthFieldPath, new Document("$exists", true)),
                                    matchCondition))))));
          } else {
            wrappedStages.add(stage);
          }
        }
        stages = wrappedStages;
      }

      List<String> usedProperties =
          result.getUsedMongoDocumentProperties() != null
              ? result.getUsedMongoDocumentProperties().stream()
                  .map(p -> ApplyGraphLookupHelper.stripRootMongoPath(p, rootMongoPath))
                  .collect(Collectors.toList())
              : null;

      return DefaultApplyOperatorResult.builder()
          .withStageObjects(stages)
          .withUsedMongoDocumentProperties(usedProperties)
          .withWrittenMongoDocumentProperties(result.getWrittenMongoDocumentProperties())
          .withAddedMongoDocumentProperties(result.getAddedMongoDocumentProperties())
          .withRemovedMongoDocumentProperties(result.getRemovedMongoDocumentProperties())
          .withDocumentShapeRedefined(result.isDocumentShapeRedefined())
          .build();
    } catch (ODataApplicationException | ExpressionVisitException e) {
      throw new RuntimeException("Failed to parse filter item", e);
    }
  }
}
