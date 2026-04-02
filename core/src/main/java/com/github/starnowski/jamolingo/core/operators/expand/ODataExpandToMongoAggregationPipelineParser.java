package com.github.starnowski.jamolingo.core.operators.expand;

import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.operators.filter.ODataFilterToMongoMatchParser;
import com.github.starnowski.jamolingo.core.operators.orderby.OdataOrderByToMongoSortParser;
import com.github.starnowski.jamolingo.core.operators.orderby.OrderByOperatorResult;
import java.util.*;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmReferentialConstraint;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.bson.Document;
import org.bson.conversions.Bson;

public class ODataExpandToMongoAggregationPipelineParser {

  public static final String ODATA_GRAPHLOOKUP_STAGE_DEPTH_VARIABLE_SUFFIX =
      "_odata_graphlookup_depth_variable";
  public static final String ODATA_GRAPHLOOKUP_STAGE_TMP_ARRAY_SUFFIX =
      "_odata_graphlookup_tmp_array";

  public ExpandOperatorResult parse(ExpandOption expandOption)
      throws ExpressionVisitException, ODataApplicationException {
    return parse(expandOption, DefaultExpandParserContext.builder().build());
  }

  public ExpandOperatorResult parse(
      ExpandOption expandOption, ExpandParserContext expandParserContext)
      throws ExpressionVisitException, ODataApplicationException {
    List<Bson> stageObjects = new ArrayList<>();
    for (ExpandItem eOption : expandOption.getExpandItems()) {
      stageObjects.addAll(prepareStageObjectsForExpandItem(eOption, expandParserContext));
    }
    return new DefaultExpandOperatorResult(stageObjects);
  }

  private Collection<? extends Bson> prepareStageObjectsForExpandItem(
      ExpandItem eOption, ExpandParserContext expandParserContext)
      throws ExpressionVisitException, ODataApplicationException {
    UriResource lastResource =
        eOption
            .getResourcePath()
            .getUriResourceParts()
            .get(eOption.getResourcePath().getUriResourceParts().size() - 1);
    if (!(lastResource instanceof UriResourceNavigation)) {
      return List.of();
    }
    EdmNavigationProperty navProp = ((UriResourceNavigation) lastResource).getProperty();
    EdmEntityType targetEntityType = (EdmEntityType) navProp.getType();
    String targetFullTypeName = targetEntityType.getNamespace() + "." + targetEntityType.getName();

    EdmPropertyMongoPathResolver targetResolver =
        expandParserContext.getEDMTypeMapping() != null
            ? expandParserContext.getEDMTypeMapping().get(targetFullTypeName)
            : null;

    String edmStartWith = null;
    String edmConnectFrom = null;
    String edmConnectTo = null;

    List<EdmReferentialConstraint> referentialConstraints = navProp.getReferentialConstraints();
    if (referentialConstraints.isEmpty() && navProp.getPartner() != null) {
      referentialConstraints = navProp.getPartner().getReferentialConstraints();
      if (!referentialConstraints.isEmpty()) {
        EdmReferentialConstraint constraint = referentialConstraints.get(0);
        edmStartWith = constraint.getReferencedPropertyName();
        edmConnectFrom = constraint.getReferencedPropertyName();
        edmConnectTo = constraint.getPropertyName();
      }
    } else if (!referentialConstraints.isEmpty()) {
      EdmReferentialConstraint constraint = referentialConstraints.get(0);
      edmStartWith = constraint.getPropertyName();
      edmConnectFrom = constraint.getPropertyName();
      edmConnectTo = constraint.getReferencedPropertyName();
    }

    if (edmConnectFrom != null && edmConnectTo != null) {
      String mongoStartWith = edmStartWith;
      String mongoConnectFrom = edmConnectFrom;
      String mongoConnectTo = edmConnectTo;

      if (targetResolver != null) {
        mongoConnectTo = targetResolver.resolveMongoPathForEDMPath(edmConnectTo).getMongoPath();
      }

      String mongoCollectionName =
          expandParserContext
              .getEDMTablesToMongoDBCollections()
              .get(new KeyValue<>(targetEntityType.getNamespace(), targetEntityType.getName()));

      String targetCollection =
          mongoCollectionName == null ? targetEntityType.getName() : mongoCollectionName;
      List<Bson> pipeline = new ArrayList<>();

      if (eOption.getLevelsOption() != null
          && (eOption.getLevelsOption().isMax() || eOption.getLevelsOption().getValue() > 1)) {
        // TODO Check approach with executing the nested $lookup stages (based on the levels value)
        // TODO Add default behaviour when $level value is larger than max then thrown an exception
        // TODO Add custom behaviour when $level value is larger than max then set the $level value
        // with the max value, required setting in the ExpandParserContext option
        // Adding $graphLookup
        Document graphLookup = new Document();
        Document graphLookupInnerObject =
            new Document()
                .append("from", targetCollection)
                .append("startWith", "$" + mongoStartWith)
                .append("connectFromField", mongoConnectFrom)
                .append("connectToField", mongoConnectTo)
                .append(
                    "maxDepth",
                    translateODataExpandLevelsToGraphLookupMaxDepth(eOption, expandParserContext))
                .append("as", navProp.getName());
        String depthVariable = navProp.getName() + ODATA_GRAPHLOOKUP_STAGE_DEPTH_VARIABLE_SUFFIX;
        if (eOption.getFilterOption() != null) {
          ODataFilterToMongoMatchParser oDataFilterToMongoMatchParser =
              new ODataFilterToMongoMatchParser();
          graphLookupInnerObject.append(
              "restrictSearchWithMatch",
              oDataFilterToMongoMatchParser
                  .parseQueryObject(eOption.getFilterOption())
                  .getQueryObject());
        }
        graphLookupInnerObject.append("depthField", depthVariable);
        graphLookup.append("$graphLookup", graphLookupInnerObject);
        pipeline.add(graphLookup);
        boolean removeDepthProperty = true;
        if (eOption.getFilterOption() != null) {
          pipeline.add(
              prepareReduceStageThatRemovesOrphansFromGraphLookupStage(
                  navProp, depthVariable, mongoConnectTo, mongoConnectFrom));
        }

        Document sortDocument = null;
        if (eOption.getOrderByOption() != null) {
          OdataOrderByToMongoSortParser odataOrderByToMongoSortParser =
              new OdataOrderByToMongoSortParser();
          EdmMongoContextFacade facade =
              DefaultEdmMongoContextFacade.builder()
                  .withEntityPropertiesMongoPathContext(null)
                  .build();

          OrderByOperatorResult orderByResult =
              odataOrderByToMongoSortParser.parse(eOption.getOrderByOption(), facade);
          sortDocument =
              (Document) ((Document) orderByResult.getStageObjects().get(0)).get("$sort");

          pipeline.add(
              new Document(
                  "$set",
                  new Document(
                      navProp.getName(),
                      new Document(
                          "$sortArray",
                          new Document("input", "$" + navProp.getName())
                              .append("sortBy", sortDocument)))));
        }
        if (eOption.getTopOption() != null || eOption.getSkipOption() != null) {
          pipeline.add(
              prepareArrayWithChildrenArrayGroupByParentAndLevel(
                  navProp, depthVariable, mongoConnectTo, mongoConnectFrom));
          if (eOption.getOrderByOption() != null) {
            pipeline.add(prepareArrayWithSortedChildrenArray(navProp, sortDocument));
          }
          pipeline.add(
              prepareArrayWithSkippedAndLimitedChildrenArray(
                  navProp,
                  eOption.getSkipOption() == null ? null : eOption.getSkipOption().getValue(),
                  eOption.getTopOption() == null ? null : eOption.getTopOption().getValue()));
        }
        if (removeDepthProperty) {
          // Removing the "depthVariable" property from results
          pipeline.add(new Document("$unset", navProp.getName() + "." + depthVariable));
        }
        return pipeline;
      } else {
        // Adding $lookup
        Document lookup = new Document();
        Document lookupInnerObject =
            new Document()
                .append("from", targetCollection)
                .append("localField", mongoStartWith)
                .append("foreignField", mongoConnectTo);

        if (eOption.getFilterOption() != null || eOption.getOrderByOption() != null) {
          ODataFilterToMongoMatchParser oDataFilterToMongoMatchParser =
              new ODataFilterToMongoMatchParser();
          OdataOrderByToMongoSortParser odataOrderByToMongoSortParser =
              new OdataOrderByToMongoSortParser();
          EdmMongoContextFacade facade =
              DefaultEdmMongoContextFacade.builder()
                  .withEntityPropertiesMongoPathContext(null)
                  .build();

          // $lookup with pipeline
          List<Bson> lookupPipeline = new ArrayList<>();
          if (eOption.getFilterOption() != null) {
            lookupPipeline.addAll(
                oDataFilterToMongoMatchParser.parse(eOption.getFilterOption()).getStageObjects());
          }
          if (eOption.getOrderByOption() != null) {
            lookupPipeline.addAll(
                odataOrderByToMongoSortParser
                    .parse(eOption.getOrderByOption(), facade)
                    .getStageObjects());
          }
          lookupInnerObject.append("pipeline", lookupPipeline);
        }
        lookupInnerObject.append("as", navProp.getName());
        lookup.append("$lookup", lookupInnerObject);
        pipeline.add(lookup);
        if (!navProp.isCollection()) {
          // TODO Comment why preserveNullAndEmptyArrays is needed and check if OData
          pipeline.add(
              new Document(
                  "$unwind",
                  new Document("path", "$" + navProp.getName())
                      .append("preserveNullAndEmptyArrays", true)));
        }
        return pipeline;
      }
    }
    return List.of();
  }

  /**
   * Prepares a $set stage with a $reduce operator to filter out "orphan" documents from the result
   * of a $graphLookup stage.
   *
   * <p>When $graphLookup is used with a $filter (via restrictSearchWithMatch), it might include
   * documents that match the filter but are disconnected from the root because one of their
   * ancestors was filtered out. This method generates a MongoDB aggregation stage that traverses
   * the flat array of results and only keeps documents that have a valid path back to the root
   * (depth 0).
   *
   * @param navProp the navigation property being expanded
   * @param depthVariable the name of the field storing the recursion depth
   * @param mongoConnectTo the field name used for the "connectToField" in $graphLookup
   * @param mongoConnectFrom the field name used for the "connectFromField" in $graphLookup
   * @return a BSON Document representing the $set stage
   */
  private static Document prepareReduceStageThatRemovesOrphansFromGraphLookupStage(
      EdmNavigationProperty navProp,
      String depthVariable,
      String mongoConnectTo,
      String mongoConnectFrom) {
    return new Document(
        "$set",
        new Document(
            navProp.getName(),
            new Document(
                "$reduce",
                new Document(
                        "input",
                        new Document(
                            "$sortArray",
                            new Document("input", "$" + navProp.getName())
                                .append("sortBy", new Document(depthVariable, 1))))
                    .append("initialValue", List.of())
                    .append(
                        "in",
                        new Document(
                            "$let",
                            new Document(
                                    "vars",
                                    new Document("current", "$$this").append("acc", "$$value"))
                                .append(
                                    "in",
                                    new Document(
                                        "$cond",
                                        List.of(
                                            new Document(
                                                "$or",
                                                List.of(
                                                    new Document(
                                                        "$eq",
                                                        List.of("$$current." + depthVariable, 0)),
                                                    new Document(
                                                        "$in",
                                                        List.of(
                                                            "$$current." + mongoConnectTo,
                                                            "$$acc." + mongoConnectFrom)))),
                                            new Document(
                                                "$concatArrays",
                                                List.of("$$acc", List.of("$$current"))),
                                            "$$acc"))))))));
  }

  private static Document prepareArrayWithChildrenArrayGroupByParentAndLevel(
      EdmNavigationProperty navProp,
      String depthVariable,
      String mongoConnectTo,
      String mongoConnectFrom) {

    return Document.parse(
        """
            {
              $set: {
                  %1$s: {
                    $reduce: {
                      input: { $ifNull: ["$%2$s", []] },
                              initialValue: [],
                      in: {
                        $let: {
                          vars: {
                            index: { $indexOfArray: ["$$value.%4$s", "$$this.%4$s"] }
                          },
                          in: {
                            $cond: [
                            { $eq: ["$$index", -1] },
                            {
                              $concatArrays: [
                              "$$value",
                                  [{ %4$s: "$$this.%4$s", %3$s: "$$this.%3$s", %2$s: ["$$this"] }]
                                ]
                            },
                            {
                              $map: {
                                input: "$$value",
                                        as: "bucket",
                                        in: {
                                  $cond: [
                                  { $eq: ["$$bucket.%4$s", "$$this.%4$s"] },
                                  {
                                    %4$s: "$$bucket.%4$s",
                                            %2$s: { $concatArrays: ["$$bucket.%2$s", ["$$this"]] }
                                  },
                                  "$$bucket"
                                    ]
                                }
                              }
                            }
                            ]
                          }
                        }
                      }
                    }
                  }
                }
              }
            """
            .formatted(
                navProp.getName() + ODATA_GRAPHLOOKUP_STAGE_TMP_ARRAY_SUFFIX,
                navProp.getName(),
                depthVariable,
                mongoConnectTo));
  }

  private static Document prepareArrayWithSortedChildrenArray(
      EdmNavigationProperty navProp, Document sortObject) {

    return Document.parse(
        """
            {
                $set: {
                  %1$s: {
                    $map: {
                      input: { $ifNull: ["$%1$s", []] },
                      as: "item",
                      in: {
                        $mergeObjects: [
                                  "$$item",
                                  {
                                    %2$s: {
                                      $sortArray: {
                                        input: "$$item.%2$s",
                                        sortBy: %3$s
                                      }
                                    }
                                  }
                                ]
                      }
                    }
                  }
                }
              }
            """
            .formatted(
                navProp.getName() + ODATA_GRAPHLOOKUP_STAGE_TMP_ARRAY_SUFFIX,
                navProp.getName(),
                sortObject.toJson()));
  }

  private static Document prepareArrayWithSkippedAndLimitedChildrenArray(
      EdmNavigationProperty navProp, Integer skip, Integer top) {

    return Document.parse(
        """
                {
                    $set: {
                      %1$s: {
                        $map: {
                          input: { $ifNull: ["$%1$s", []] },
                          as: "item",
                          in: {
                            $mergeObjects: [
                                      "$$item",
                                      {
                                        %2$s: {
                                          $slice: [
                                            "$$item.%2$s",
                                            %3$s,
                                            %4$s
                                          ]
                                        }
                                      }
                                    ]
                          }
                        }
                      }
                    }
                  }
                """
            .formatted(
                navProp.getName() + ODATA_GRAPHLOOKUP_STAGE_TMP_ARRAY_SUFFIX,
                navProp.getName(),
                skip == null ? 0 : skip,
                top == null
                    ? """
                                    { $size: "$$item.%2$s" }
                                    """
                    : top));
  }

  /**
   * Translates the OData $levels option value to the MongoDB $graphLookup maxDepth value.
   *
   * <p>OData $levels=1 corresponds to a maxDepth of 0 in MongoDB, as maxDepth 0 includes only the
   * immediate related documents (one level of recursion).
   *
   * @param eOption the expand item containing the levels option
   * @param expandParserContext the expand parser context containing the maxLevel configuration
   * @return the calculated maxDepth for $graphLookup
   */
  private int translateODataExpandLevelsToGraphLookupMaxDepth(
      ExpandItem eOption, ExpandParserContext expandParserContext) {
    int levelsValue =
        eOption.getLevelsOption().isMax()
            ? expandParserContext.getMaxLevel()
            : eOption.getLevelsOption().getValue();
    if (levelsValue > expandParserContext.getMaxLevel()) {
      levelsValue = expandParserContext.getMaxLevel();
    }
    return levelsValue - 1;
  }

  private static class DefaultExpandOperatorResult implements ExpandOperatorResult {

    private final List<Bson> stageObjects;

    private DefaultExpandOperatorResult(List<Bson> stageObjects) {
      this.stageObjects = stageObjects;
    }

    @Override
    public List<Bson> getStageObjects() {
      return stageObjects;
    }

    @Override
    public List<String> getUsedMongoDocumentProperties() {
      return List.of();
    }

    @Override
    public List<String> getWrittenMongoDocumentProperties() {
      return List.of();
    }

    @Override
    public List<String> getAddedMongoDocumentProperties() {
      // TODO top elements for $expand
      return List.of();
    }

    @Override
    public List<String> getRemovedMongoDocumentProperties() {
      return List.of();
    }

    @Override
    public boolean isDocumentShapeRedefined() {
      return false;
    }
  }

  public static class DefaultExpandParserContext implements ExpandParserContext {
    private final Map<String, EdmPropertyMongoPathResolver> edmTypeMapping;
    private final Map<KeyValue<String, String>, String> edmTablesToMongoDBCollections;
    private final Integer maxLevel;

    public DefaultExpandParserContext(
        Map<String, EdmPropertyMongoPathResolver> edmTypeMapping,
        Map<KeyValue<String, String>, String> edmTablesToMongoDBCollections,
        Integer maxLevel) {
      this.edmTypeMapping = edmTypeMapping;
      this.edmTablesToMongoDBCollections = edmTablesToMongoDBCollections;
      this.maxLevel = maxLevel;
    }

    @Override
    public Map<String, EdmPropertyMongoPathResolver> getEDMTypeMapping() {
      return edmTypeMapping;
    }

    @Override
    public Map<KeyValue<String, String>, String> getEDMTablesToMongoDBCollections() {
      return edmTablesToMongoDBCollections;
    }

    @Override
    public Integer getMaxLevel() {
      return maxLevel;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DefaultExpandParserContext that = (DefaultExpandParserContext) o;
      return Objects.equals(edmTypeMapping, that.edmTypeMapping)
          && Objects.equals(edmTablesToMongoDBCollections, that.edmTablesToMongoDBCollections)
          && Objects.equals(maxLevel, that.maxLevel);
    }

    @Override
    public int hashCode() {
      return Objects.hash(edmTypeMapping, edmTablesToMongoDBCollections, maxLevel);
    }

    @Override
    public String toString() {
      return "DefaultExpandParserContext{"
          + "edmTypeMapping="
          + edmTypeMapping
          + ", edmTablesToMongoDBCollections="
          + edmTablesToMongoDBCollections
          + ", maxLevel="
          + maxLevel
          + '}';
    }

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private Map<String, EdmPropertyMongoPathResolver> edmTypeMapping = new HashMap<>();
      private Map<KeyValue<String, String>, String> edmTablesToMongoDBCollections = new HashMap<>();
      private Integer maxLevel = DEFAULT_MAX_LEVEL;

      public Builder withEdmTypeMapping(Map<String, EdmPropertyMongoPathResolver> edmTypeMapping) {
        this.edmTypeMapping = edmTypeMapping;
        return this;
      }

      public Builder withEdmTablesToMongoDBCollections(
          Map<KeyValue<String, String>, String> edmTablesToMongoDBCollections) {
        this.edmTablesToMongoDBCollections = edmTablesToMongoDBCollections;
        return this;
      }

      public Builder withMaxLevel(Integer maxLevel) {
        this.maxLevel = maxLevel;
        return this;
      }

      public Builder withDefaultExpandParserContext(
          DefaultExpandParserContext defaultExpandParserContext) {
        this.edmTypeMapping =
            defaultExpandParserContext.edmTypeMapping != null
                ? new HashMap<>(defaultExpandParserContext.edmTypeMapping)
                : null;
        this.edmTablesToMongoDBCollections =
            defaultExpandParserContext.edmTablesToMongoDBCollections != null
                ? new HashMap<>(defaultExpandParserContext.edmTablesToMongoDBCollections)
                : null;
        this.maxLevel = defaultExpandParserContext.maxLevel;
        return this;
      }

      public DefaultExpandParserContext build() {
        return new DefaultExpandParserContext(
            edmTypeMapping != null
                ? Collections.unmodifiableMap(new HashMap<>(edmTypeMapping))
                : null,
            edmTablesToMongoDBCollections != null
                ? Collections.unmodifiableMap(new HashMap<>(edmTablesToMongoDBCollections))
                : null,
            maxLevel);
      }
    }
  }
}
