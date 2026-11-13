package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser;
import java.util.Collections;
import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.ApplyOption;

/** ODataApplyToMongoAggregationPipelineParser type. */
public class ODataApplyToMongoAggregationPipelineParser {

  private final ApplySearchToMongoPipelineParser applySearchToMongoPipelineParser;
  private final ODataExpandToMongoAggregationPipelineParser expandParser;

  /** ODataApplyToMongoAggregationPipelineParser constructor. */
  public ODataApplyToMongoAggregationPipelineParser() {
    this(null, null);
  }

  /**
   * ODataApplyToMongoAggregationPipelineParser constructor with search delegate parser.
   *
   * @param applySearchToMongoPipelineParser the search delegate parser
   */
  public ODataApplyToMongoAggregationPipelineParser(
      ApplySearchToMongoPipelineParser applySearchToMongoPipelineParser) {
    this(applySearchToMongoPipelineParser, null);
  }

  /**
   * ODataApplyToMongoAggregationPipelineParser constructor with expand parser.
   *
   * @param expandParser the expand parser
   */
  public ODataApplyToMongoAggregationPipelineParser(
      ODataExpandToMongoAggregationPipelineParser expandParser) {
    this(null, expandParser);
  }

  /**
   * ODataApplyToMongoAggregationPipelineParser constructor with search delegate parser and expand
   * parser.
   *
   * @param applySearchToMongoPipelineParser the search delegate parser
   * @param expandParser the expand parser
   */
  public ODataApplyToMongoAggregationPipelineParser(
      ApplySearchToMongoPipelineParser applySearchToMongoPipelineParser,
      ODataExpandToMongoAggregationPipelineParser expandParser) {
    this.applySearchToMongoPipelineParser = applySearchToMongoPipelineParser;
    this.expandParser =
        expandParser != null ? expandParser : new ODataExpandToMongoAggregationPipelineParser();
  }

  /**
   * Parses the apply option into an ApplyOperatorResult containing MongoDB aggregation pipeline
   * stages.
   *
   * @param applyOption the apply query option
   * @param edmMongoContextFacade the resolver for EDM property to MongoDB path
   * @return the apply operator result
   */
  public ApplyOperatorResult parse(
      ApplyOption applyOption, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    if (applyOption == null
        || applyOption.getApplyItems() == null
        || applyOption.getApplyItems().isEmpty()) {
      return DefaultApplyOperatorResult.builder().withStageObjects(Collections.emptyList()).build();
    }
    return parse(applyOption.getApplyItems(), edmMongoContextFacade);
  }

  public ApplyOperatorResult parse(
      List<ApplyItem> applyItems, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    if (applyItems == null || applyItems.isEmpty()) {
      return DefaultApplyOperatorResult.builder().withStageObjects(Collections.emptyList()).build();
    }

    List<org.bson.conversions.Bson> stages = new java.util.ArrayList<>();
    java.util.Set<String> usedProperties = new java.util.LinkedHashSet<>();
    java.util.Set<String> writtenProperties = new java.util.LinkedHashSet<>();
    java.util.Set<String> addedProperties = new java.util.LinkedHashSet<>();
    java.util.Set<String> removedProperties = new java.util.LinkedHashSet<>();
    boolean shapeRedefined = false;

    for (int i = 0; i < applyItems.size(); i++) {
      ApplyItem applyItem = applyItems.get(i);
      ApplyItemParser parser = getParser(applyItem, i == 0);
      if (parser != null) {
        ApplyOperatorResult result = parser.parse(applyItem, edmMongoContextFacade);
        stages.addAll(result.getStageObjects());

        if (result.getUsedMongoDocumentProperties() != null) {
          usedProperties.addAll(result.getUsedMongoDocumentProperties());
        }
        if (result.getWrittenMongoDocumentProperties() != null) {
          writtenProperties.addAll(result.getWrittenMongoDocumentProperties());
        }
        if (result.getAddedMongoDocumentProperties() != null) {
          addedProperties.addAll(result.getAddedMongoDocumentProperties());
        }
        if (result.getRemovedMongoDocumentProperties() != null) {
          removedProperties.addAll(result.getRemovedMongoDocumentProperties());
        }
        shapeRedefined = shapeRedefined || result.isDocumentShapeRedefined();
      } else {
        throw new UnsupportedOperationException("Unsupported apply item: " + applyItem.getKind());
      }
    }

    return DefaultApplyOperatorResult.builder()
        .withStageObjects(stages)
        .withUsedMongoDocumentProperties(new java.util.ArrayList<>(usedProperties))
        .withWrittenMongoDocumentProperties(new java.util.ArrayList<>(writtenProperties))
        .withAddedMongoDocumentProperties(new java.util.ArrayList<>(addedProperties))
        .withRemovedMongoDocumentProperties(new java.util.ArrayList<>(removedProperties))
        .withDocumentShapeRedefined(shapeRedefined)
        .build();
  }

  private ApplyItemParser getParser(ApplyItem applyItem, boolean isFirstApplyItem) {
    if (applyItem.getKind() == ApplyItem.Kind.FILTER) {
      return new FilterItemParser();
    } else if (applyItem.getKind() == ApplyItem.Kind.IDENTITY) {
      return new IdentityItemParser();
    } else if (applyItem.getKind() == ApplyItem.Kind.GROUP_BY) {
      return new GroupByItemParser(this);
    } else if (applyItem.getKind() == ApplyItem.Kind.ORDERBY) {
      return new OrderByItemParser();
    } else if (applyItem.getKind() == ApplyItem.Kind.TOP) {
      return new TopItemParser();
    } else if (applyItem.getKind() == ApplyItem.Kind.SKIP) {
      return new SkipItemParser();
    } else if (applyItem.getKind() == ApplyItem.Kind.AGGREGATE) {
      return new AggregateItemParser();
    } else if (applyItem.getKind() == ApplyItem.Kind.COMPUTE) {
      return new ComputeItemParser();
    } else if (applyItem.getKind() == ApplyItem.Kind.CONCAT) {
      return new ConcatItemParser(this);
    } else if (applyItem.getKind() == ApplyItem.Kind.SEARCH) {
      // TODO Add support when the search is not the first one
      return new SearchItemParser(applySearchToMongoPipelineParser, isFirstApplyItem);
    } else if (applyItem.getKind() == ApplyItem.Kind.BOTTOM_TOP) {
      return new BottomTopItemParser();
    } else if (applyItem.getKind() == ApplyItem.Kind.EXPAND) {
      return new ExpandItemParser(expandParser);
    }
    // TODO CUSTOM_FUNCTION
    return null;
  }
}
