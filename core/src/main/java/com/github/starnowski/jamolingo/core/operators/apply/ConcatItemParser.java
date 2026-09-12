package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.ApplyOption;
import org.apache.olingo.server.api.uri.queryoption.apply.Concat;
import org.bson.Document;
import org.bson.conversions.Bson;

/** ConcatItemParser type. */
public class ConcatItemParser implements ApplyItemParser {

  private final ODataApplyToMongoAggregationPipelineParser applyParser;

  /** ConcatItemParser constructor. */
  public ConcatItemParser(ODataApplyToMongoAggregationPipelineParser applyParser) {
    this.applyParser = applyParser;
  }

  @Override
  public ApplyOperatorResult parse(
      ApplyItem applyItem, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    if (!(applyItem instanceof Concat)) {
      throw new IllegalArgumentException("Expected Concat item");
    }
    Concat concat = (Concat) applyItem;

    Document facetDoc = new Document();
    List<String> facetFields = new ArrayList<>();

    java.util.Set<String> usedProperties = new java.util.LinkedHashSet<>();
    java.util.Set<String> writtenProperties = new java.util.LinkedHashSet<>();
    java.util.Set<String> addedProperties = new java.util.LinkedHashSet<>();
    java.util.Set<String> removedProperties = new java.util.LinkedHashSet<>();
    boolean shapeRedefined = false;

    int index = 0;
    for (ApplyOption applyOption : concat.getApplyOptions()) {
      String facetKey = "concat_" + index++;
      ApplyOperatorResult innerResult =
          applyParser.parse(applyOption.getApplyItems(), edmMongoContextFacade);
      facetDoc.put(facetKey, innerResult.getStageObjects());
      facetFields.add("$" + facetKey);
      
      if (innerResult.getUsedMongoDocumentProperties() != null) {
        usedProperties.addAll(innerResult.getUsedMongoDocumentProperties());
      }
      if (innerResult.getWrittenMongoDocumentProperties() != null) {
        writtenProperties.addAll(innerResult.getWrittenMongoDocumentProperties());
      }
      if (innerResult.getAddedMongoDocumentProperties() != null) {
        addedProperties.addAll(innerResult.getAddedMongoDocumentProperties());
      }
      if (innerResult.getRemovedMongoDocumentProperties() != null) {
        removedProperties.addAll(innerResult.getRemovedMongoDocumentProperties());
      }
      shapeRedefined = shapeRedefined || innerResult.isDocumentShapeRedefined();
    }

    Document facetStage = new Document("$facet", facetDoc);

    Document projectStage =
        new Document(
            "$project",
            new Document("_combinedResult", new Document("$concatArrays", facetFields)));

    Document unwindStage = new Document("$unwind", "$_combinedResult");

    Document replaceRootStage =
        new Document("$replaceRoot", new Document("newRoot", "$_combinedResult"));

    List<Bson> stages = Arrays.asList(facetStage, projectStage, unwindStage, replaceRootStage);

    return DefaultApplyOperatorResult.builder()
        .withStageObjects(stages)
        .withUsedMongoDocumentProperties(new ArrayList<>(usedProperties))
        .withWrittenMongoDocumentProperties(new ArrayList<>(writtenProperties))
        .withAddedMongoDocumentProperties(new ArrayList<>(addedProperties))
        .withRemovedMongoDocumentProperties(new ArrayList<>(removedProperties))
        .withDocumentShapeRedefined(shapeRedefined)
        .build();
  }
}
