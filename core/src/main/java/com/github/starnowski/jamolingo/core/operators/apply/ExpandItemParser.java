package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.operators.expand.ExpandElement;
import com.github.starnowski.jamolingo.core.operators.expand.ExpandOperatorResult;
import com.github.starnowski.jamolingo.core.operators.expand.ExpandParserContext;
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.apply.Expand;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

/** ExpandItemParser type. */
public class ExpandItemParser implements ApplyItemParser {

  private final ODataExpandToMongoAggregationPipelineParser expandParser;

  /** ExpandItemParser default constructor. */
  public ExpandItemParser() {
    this(new ODataExpandToMongoAggregationPipelineParser());
  }

  /**
   * ExpandItemParser constructor with expand parser.
   *
   * @param expandParser the expand parser
   */
  public ExpandItemParser(ODataExpandToMongoAggregationPipelineParser expandParser) {
    this.expandParser =
        expandParser != null ? expandParser : new ODataExpandToMongoAggregationPipelineParser();
  }

  /**
   * Parses the expand apply item into an ApplyOperatorResult.
   *
   * @param applyItem the expand apply item
   * @param edmMongoContextFacade the resolver for EDM property to MongoDB path
   * @return the apply operator result
   */
  @Override
  public ApplyOperatorResult parse(
      ApplyItem applyItem, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    if (!(applyItem instanceof Expand)) {
      throw new IllegalArgumentException("Expected Expand item");
    }
    Expand expand = (Expand) applyItem;

    ExpandParserContext expandParserContext;
    if (edmMongoContextFacade instanceof ExpandParserContext) {
      expandParserContext = (ExpandParserContext) edmMongoContextFacade;
    } else if (edmMongoContextFacade instanceof EdmMongoContextFacade) {
      expandParserContext =
          ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.builder()
              .withRootEdmMongoContextFacade((EdmMongoContextFacade) edmMongoContextFacade)
              .build();
    } else {
      expandParserContext =
          ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.builder().build();
    }

    try {
      ExpandOperatorResult result =
          expandParser.parse(expand.getExpandOption(), expandParserContext);
      Set<String> usedProperties = new LinkedHashSet<>();
      Set<String> writtenProperties = new LinkedHashSet<>();
      Set<String> addedProperties = new LinkedHashSet<>();

      if (result.getUsedMongoDocumentProperties() != null) {
        usedProperties.addAll(result.getUsedMongoDocumentProperties());
      }
      if (result.getWrittenMongoDocumentProperties() != null) {
        writtenProperties.addAll(result.getWrittenMongoDocumentProperties());
      }
      if (result.getAddedMongoDocumentProperties() != null) {
        addedProperties.addAll(result.getAddedMongoDocumentProperties());
      }

      for (Map.Entry<String, ExpandElement> entry : result.getExpandElements().entrySet()) {
        ExpandElement element = entry.getValue();
        if (element.getLocalKeyProperty() != null) {
          usedProperties.add(element.getLocalKeyProperty());
        }
        writtenProperties.add(entry.getKey());
        addedProperties.add(entry.getKey());
      }

      return DefaultApplyOperatorResult.builder()
          .withStageObjects(result.getStageObjects())
          .withUsedMongoDocumentProperties(new ArrayList<>(usedProperties))
          .withWrittenMongoDocumentProperties(new ArrayList<>(writtenProperties))
          .withAddedMongoDocumentProperties(new ArrayList<>(addedProperties))
          .withRemovedMongoDocumentProperties(result.getRemovedMongoDocumentProperties())
          .withDocumentShapeRedefined(result.isDocumentShapeRedefined())
          .build();
    } catch (ODataApplicationException | ExpressionVisitException e) {
      throw new RuntimeException("Failed to parse expand item", e);
    }
  }
}
