package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContext;
import com.github.starnowski.jamolingo.core.context.MongoPathResolution;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.ApplyOption;

/** ODataApplyToMongoAggregationPipelineParser type. */
public class ODataApplyToMongoAggregationPipelineParser {

  private final ApplySearchToMongoPipelineParser applySearchToMongoPipelineParser;

  /** ODataApplyToMongoAggregationPipelineParser constructor. */
  public ODataApplyToMongoAggregationPipelineParser() {
    this(null);
  }

  public ODataApplyToMongoAggregationPipelineParser(
      ApplySearchToMongoPipelineParser applySearchToMongoPipelineParser) {
    this.applySearchToMongoPipelineParser = applySearchToMongoPipelineParser;
  }

  public ApplyOperatorResult parse(
      ApplyOption applyOption, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    return parse(applyOption, edmMongoContextFacade, null);
  }

  /**
   * Parses the given ApplyOption using the provided path resolver and apply parser context.
   *
   * @param applyOption the OData apply option to parse
   * @param edmMongoContextFacade the path resolver
   * @param context the apply parser context
   * @return the apply operator result
   */
  public ApplyOperatorResult parse(
      ApplyOption applyOption,
      EdmPropertyMongoPathResolver edmMongoContextFacade,
      ApplyParserContext context) {
    if (applyOption == null
        || applyOption.getApplyItems() == null
        || applyOption.getApplyItems().isEmpty()) {
      return DefaultApplyOperatorResult.builder().withStageObjects(Collections.emptyList()).build();
    }
    if (context == null) {
      return parse(applyOption.getApplyItems(), edmMongoContextFacade);
    }
    return parse(applyOption.getApplyItems(), edmMongoContextFacade, context);
  }

  public ApplyOperatorResult parse(
      List<ApplyItem> applyItems, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    return parse(applyItems, edmMongoContextFacade, null);
  }

  /**
   * Parses the given list of ApplyItems using the provided path resolver and apply parser context.
   *
   * @param applyItems the list of apply items to parse
   * @param edmMongoContextFacade the path resolver
   * @param context the apply parser context
   * @return the apply operator result
   */
  public ApplyOperatorResult parse(
      List<ApplyItem> applyItems,
      EdmPropertyMongoPathResolver edmMongoContextFacade,
      ApplyParserContext context) {
    if (applyItems == null || applyItems.isEmpty()) {
      return DefaultApplyOperatorResult.builder().withStageObjects(Collections.emptyList()).build();
    }

    EdmPropertyMongoPathResolver effectiveFacade =
        (context != null && context.getRootMongoPath() != null && edmMongoContextFacade != null)
            ? wrapWithRootMongoPath(edmMongoContextFacade, context.getRootMongoPath())
            : edmMongoContextFacade;

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
        ApplyOperatorResult result = parser.parse(applyItem, effectiveFacade, context);
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
    }
    // TODO CUSTOM_FUNCTION
    // EXPAND
    return null;
  }

  private static EdmMongoContextFacade wrapWithRootMongoPath(
      EdmPropertyMongoPathResolver delegate, String rootMongoPath) {
    if (delegate instanceof EdmMongoContextFacade
        && rootMongoPath.equals(((EdmMongoContextFacade) delegate).getRootMongoPath())) {
      return (EdmMongoContextFacade) delegate;
    }
    return new RootPrefixEdmMongoContextFacade(delegate, rootMongoPath);
  }

  private static final class RootPrefixEdmMongoContextFacade implements EdmMongoContextFacade {
    private final EdmPropertyMongoPathResolver delegate;
    private final String rootMongoPath;

    private RootPrefixEdmMongoContextFacade(
        EdmPropertyMongoPathResolver delegate, String rootMongoPath) {
      this.delegate = delegate;
      this.rootMongoPath = rootMongoPath;
    }

    @Override
    public MongoPathResolution resolveMongoPathForEDMPath(UriInfoResource uriInfoResource) {
      String resolved;
      try {
        if (delegate instanceof EdmMongoContextFacade) {
          MongoPathResolution res =
              ((EdmMongoContextFacade) delegate).resolveMongoPathForEDMPath(uriInfoResource);
          resolved = res != null ? res.getMongoPath() : null;
        } else {
          String edmPath =
              uriInfoResource.getUriResourceParts().stream()
                  .map(p -> ((UriResourceProperty) p).getProperty().getName())
                  .collect(Collectors.joining("/"));
          MongoPathResolution res = delegate.resolveMongoPathForEDMPath(edmPath);
          resolved = res != null ? res.getMongoPath() : null;
        }
      } catch (EntityPropertiesMongoPathContext.InvalidEDMPathException ex) {
        resolved =
            uriInfoResource.getUriResourceParts().stream()
                .map(p -> ((UriResourceProperty) p).getProperty().getName())
                .collect(Collectors.joining("."));
      }
      if (resolved == null) {
        return null;
      }
      String finalPath = prefixIfNeeded(resolved);
      return () -> finalPath;
    }

    @Override
    public MongoPathResolution resolveMongoPathForEDMPath(String edmPath) {
      String resolved;
      try {
        MongoPathResolution res = delegate.resolveMongoPathForEDMPath(edmPath);
        resolved = res != null ? res.getMongoPath() : null;
      } catch (EntityPropertiesMongoPathContext.InvalidEDMPathException ex) {
        resolved = edmPath != null ? edmPath.replace("/", ".") : null;
      }
      if (resolved == null) {
        return null;
      }
      String finalPath = prefixIfNeeded(resolved);
      return () -> finalPath;
    }

    @Override
    public String getRootMongoPath() {
      return rootMongoPath;
    }

    private String prefixIfNeeded(String path) {
      if (rootMongoPath == null || path.startsWith(rootMongoPath + ".")) {
        return path;
      }
      return rootMongoPath + "." + path;
    }
  }
}
