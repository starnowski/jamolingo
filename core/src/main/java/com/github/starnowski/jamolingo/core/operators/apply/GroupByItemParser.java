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
    for (GroupByItem item : groupBy.getGroupByItems()) {
      StringBuilder pathBuilder = new StringBuilder();
      for (int i = 0; i < item.getPath().size(); i++) {
        if (i > 0) pathBuilder.append("/");
        pathBuilder.append(item.getPath().get(i).getSegmentValue());
      }
      String edmPath = pathBuilder.toString();
      String mongoPath = edmMongoContextFacade.resolveMongoPathForEDMPath(edmPath).getMongoPath();
      idDoc.put(mongoPath, "$" + mongoPath);
    }

    Document groupStage = new Document("$group", new Document("_id", idDoc));

    List<Bson> stages = new ArrayList<>();
    stages.add(groupStage);

    if (groupBy.getApplyOption() != null) {
      ApplyOperatorResult innerResult =
          applyParser.parse(groupBy.getApplyOption(), edmMongoContextFacade);
      stages.addAll(innerResult.getStageObjects());
    }

    return DefaultApplyOperatorResult.builder().withStageObjects(stages).build();
  }
}
