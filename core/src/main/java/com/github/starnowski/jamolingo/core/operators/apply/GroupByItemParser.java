package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.operators.NavigationPropertyExtractor;
import com.github.starnowski.jamolingo.core.operators.UsedNavigationProperty;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.apply.Aggregate;
import org.apache.olingo.server.api.uri.queryoption.apply.AggregateExpression;
import org.apache.olingo.server.api.uri.queryoption.apply.GroupBy;
import org.apache.olingo.server.api.uri.queryoption.apply.GroupByItem;
import org.bson.Document;
import org.bson.conversions.Bson;

/** GroupByItemParser type. */
public class GroupByItemParser implements ApplyItemParser {

  private final ODataApplyToMongoAggregationPipelineParser applyParser;

  /** GroupByItemParser constructor. */
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
    Set<UsedNavigationProperty> usedNavigationProperties = new LinkedHashSet<>();

    collectGroupByItems(
        groupBy.getGroupByItems(),
        edmMongoContextFacade,
        idDoc,
        groupedMongoPaths,
        usedNavigationProperties);

    Document groupStageDoc = new Document("_id", idDoc);
    Document projectStageDoc = new Document("_id", 0);
    for (String mongoPath : groupedMongoPaths) {
      projectStageDoc.put(mongoPath, "$_id." + mongoPath);
    }

    List<String> usedProperties = new ArrayList<>(groupedMongoPaths);
    List<String> addedProperties = new ArrayList<>();

    List<ApplyItem> remainingItems = new ArrayList<>();
    if (groupBy.getApplyOption() != null) {
      for (ApplyItem item : groupBy.getApplyOption().getApplyItems()) {
        if (item instanceof Aggregate) {
          Aggregate aggregate = (Aggregate) item;
          for (AggregateExpression expr : aggregate.getExpressions()) {
            AggregateItemParser.processAggregateExpression(
                expr,
                null,
                edmMongoContextFacade,
                groupStageDoc,
                projectStageDoc,
                usedProperties,
                addedProperties,
                usedNavigationProperties);
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

    List<String> finalUsedProperties = new ArrayList<>(usedProperties);
    List<String> finalWrittenProperties = new ArrayList<>(addedProperties);
    finalWrittenProperties.addAll(groupedMongoPaths);
    List<String> finalAddedProperties = new ArrayList<>(addedProperties);
    List<String> finalRemovedProperties = new ArrayList<>();

    if (!remainingItems.isEmpty()) {
      ApplyOperatorResult innerResult = applyParser.parse(remainingItems, edmMongoContextFacade);
      stages.addAll(innerResult.getStageObjects());
      if (innerResult.getUsedMongoDocumentProperties() != null) {
        finalUsedProperties.addAll(innerResult.getUsedMongoDocumentProperties());
      }
      if (innerResult.getWrittenMongoDocumentProperties() != null) {
        finalWrittenProperties.addAll(innerResult.getWrittenMongoDocumentProperties());
      }
      if (innerResult.getAddedMongoDocumentProperties() != null) {
        finalAddedProperties.addAll(innerResult.getAddedMongoDocumentProperties());
      }
      if (innerResult.getRemovedMongoDocumentProperties() != null) {
        finalRemovedProperties.addAll(innerResult.getRemovedMongoDocumentProperties());
      }
      if (innerResult.getUsedNavigationProperties() != null) {
        usedNavigationProperties.addAll(innerResult.getUsedNavigationProperties());
      }
    }

    return DefaultApplyOperatorResult.builder()
        .withStageObjects(stages)
        .withUsedMongoDocumentProperties(finalUsedProperties)
        .withWrittenMongoDocumentProperties(finalWrittenProperties)
        .withAddedMongoDocumentProperties(finalAddedProperties)
        .withRemovedMongoDocumentProperties(finalRemovedProperties)
        .withDocumentShapeRedefined(true)
        .withUsedNavigationProperties(new ArrayList<>(usedNavigationProperties))
        .build();
  }

  private void collectGroupByItems(
      List<GroupByItem> items,
      EdmPropertyMongoPathResolver edmMongoContextFacade,
      Document idDoc,
      List<String> groupedMongoPaths,
      Set<UsedNavigationProperty> usedNavigationProperties) {
    if (items == null) {
      return;
    }
    for (GroupByItem item : items) {
      if (item.getPath() != null && !item.getPath().isEmpty()) {
        String edmPath = NavigationPropertyExtractor.extractPath(item.getPath(), "/");
        usedNavigationProperties.addAll(
            NavigationPropertyExtractor.extract(item.getPath(), edmMongoContextFacade));
        String mongoPath = edmMongoContextFacade.resolveMongoPathForEDMPath(edmPath).getMongoPath();
        idDoc.put(mongoPath, "$" + mongoPath);
        groupedMongoPaths.add(mongoPath);
      }
      if (item.getRollup() != null && !item.getRollup().isEmpty()) {
        collectGroupByItems(
            item.getRollup(),
            edmMongoContextFacade,
            idDoc,
            groupedMongoPaths,
            usedNavigationProperties);
      }
    }
  }
}
