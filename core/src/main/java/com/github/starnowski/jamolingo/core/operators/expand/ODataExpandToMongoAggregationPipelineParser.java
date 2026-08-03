package com.github.starnowski.jamolingo.core.operators.expand;

import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.operators.filter.ODataFilterToMongoMatchParser;
import com.github.starnowski.jamolingo.core.operators.orderby.DefaultOdataOrderByToMongoSortParserContext;
import com.github.starnowski.jamolingo.core.operators.orderby.OdataOrderByToMongoSortParser;
import com.github.starnowski.jamolingo.core.operators.orderby.OrderByOperatorResult;
import com.github.starnowski.jamolingo.core.operators.orderby.SortProperty;
import com.github.starnowski.jamolingo.core.operators.select.*;
import com.github.starnowski.jamolingo.core.operators.skip.OdataSkipToMongoSkipParser;
import com.github.starnowski.jamolingo.core.operators.top.OdataTopToMongoLimitParser;
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

/**
 * Parser for OData $expand system query option to MongoDB aggregation pipeline stages.
 *
 * <p>This parser translates expansion requests into $lookup or $graphLookup stages depending on the
 * levels option.
 */
public class ODataExpandToMongoAggregationPipelineParser {

  /** Suffix for $graphLookup depth variable. */
  public static final String ODATA_GRAPHLOOKUP_STAGE_DEPTH_VARIABLE_SUFFIX =
      "_odata_graphlookup_depth_variable";

  // TODO Try to remove a need of this temporary array
  /** Suffix for temporary array used in $graphLookup processing. */
  public static final String ODATA_GRAPHLOOKUP_STAGE_TMP_ARRAY_SUFFIX =
      "_odata_graphlookup_tmp_array";

  public static final String ODATA_MERGING_STAGE_TMP_ARRAY_SUFFIX = "_odata_merge_tmp_array";
  public static final String ODATA_MERGING_STAGE_TMP_ROOT_DATA_PROPERTY_SUFFIX =
      "_odata_merge_tmp_rootdata";

  /**
   * Parses the given OData expand option into expansion operator result using default context.
   *
   * @param expandOption the expand option to parse
   * @return the expansion operator result
   * @throws ExpressionVisitException if an error occurs during expression visiting
   * @throws ODataApplicationException if an error occurs during parsing
   */
  public ExpandOperatorResult parse(ExpandOption expandOption)
      throws ExpressionVisitException, ODataApplicationException {
    return parse(expandOption, DefaultExpandParserContext.builder().build());
  }

  /**
   * Parses the given OData expand option into expansion operator result using provided context.
   *
   * @param expandOption the expand option to parse
   * @param expandParserContext the expand parser context
   * @return the expansion operator result
   * @throws ExpressionVisitException if an error occurs during expression visiting
   * @throws ODataApplicationException if an error occurs during parsing
   */
  public ExpandOperatorResult parse(
      ExpandOption expandOption, ExpandParserContext expandParserContext)
      throws ExpressionVisitException, ODataApplicationException {
    List<Bson> stageObjects = new ArrayList<>();
    Map<String, ExpandElement> expandElements = new HashMap<>();
    for (ExpandItem eOption : expandOption.getExpandItems()) {
      stageObjects.addAll(
          prepareStageObjectsForExpandItem(eOption, expandParserContext, expandElements));
    }
    return new DefaultExpandOperatorResult(stageObjects, expandElements);
  }

  private ExpandOperatorResult parse(
      ExpandOption expandOption,
      ExpandParserContext expandParserContext,
      ParserExpandItemContext parserExpandItemContext)
      throws ExpressionVisitException, ODataApplicationException {
    List<Bson> stageObjects = new ArrayList<>();
    Map<String, ExpandElement> expandElements = new HashMap<>();
    for (ExpandItem eOption : expandOption.getExpandItems()) {
      stageObjects.addAll(
          prepareStageObjectsForExpandItem(
              eOption, expandParserContext, parserExpandItemContext, expandElements));
    }
    return new DefaultExpandOperatorResult(stageObjects, expandElements);
  }

  private Collection<? extends Bson> prepareStageObjectsForExpandItem(
      ExpandItem eOption,
      ExpandParserContext expandParserContext,
      Map<String, ExpandElement> expandElements)
      throws ExpressionVisitException, ODataApplicationException {
    return prepareStageObjectsForExpandItem(
        eOption,
        expandParserContext,
        new ParserExpandItemContext(null, null, false),
        expandElements);
  }

  private Collection<? extends Bson> prepareStageObjectsForExpandItem(
      ExpandItem eOption,
      ExpandParserContext expandParserContext,
      ParserExpandItemContext parserExpandItemContext,
      Map<String, ExpandElement> expandElements)
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

      // TODO Add property in configuration that allow to specify the collection name
      String targetCollection =
          mongoCollectionName == null
              ? targetEntityType.getFullQualifiedName().getFullQualifiedNameAsString()
              : mongoCollectionName;
      List<Bson> pipeline = new ArrayList<>();

      String navPropertyWithRootPrefix =
          parserExpandItemContext.getRoot() == null
              ? navProp.getName()
              : parserExpandItemContext.getRoot() + "." + navProp.getName();
      String lookupMongoStartWith =
          parserExpandItemContext.getRoot() == null
              ? mongoStartWith
              : parserExpandItemContext.getRoot() + "." + mongoStartWith;
      if (eOption.getLevelsOption() != null
          && (eOption.getLevelsOption().isMax() || eOption.getLevelsOption().getValue() > 1)
          && !expandParserContext.isUseLookupForLevelGreaterThanOne()) {
        // TODO Check approach with executing the nested $lookup stages (based on the levels value)
        // TODO Add default behaviour when $level value is larger than max then thrown an exception
        // TODO Add custom behaviour when $level value is larger than max then set the $level value
        // with the max value, required setting in the ExpandParserContext option
        // Adding $graphLookup
        Document graphLookup = new Document();
        Document graphLookupInnerObject =
            new Document()
                .append("from", targetCollection)
                .append("startWith", "$" + lookupMongoStartWith)
                .append("connectFromField", mongoConnectFrom)
                .append("connectToField", mongoConnectTo)
                .append(
                    "maxDepth",
                    translateODataExpandLevelsToGraphLookupMaxDepth(eOption, expandParserContext))
                .append("as", navPropertyWithRootPrefix);
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
        boolean removeDepthProperty = !expandParserContext.propagateGraphLookUpJoinKeys();
        if (eOption.getFilterOption() != null) {
          pipeline.add(
              prepareReduceStageThatRemovesOrphansFromGraphLookupStage(
                  navPropertyWithRootPrefix, depthVariable, mongoConnectTo, mongoConnectFrom));
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
              odataOrderByToMongoSortParser.parse(
                  eOption.getOrderByOption(),
                  facade,
                  DefaultOdataOrderByToMongoSortParserContext.builder()
                      .withPrependedSortProperties(
                          Arrays.asList(new SortProperty(depthVariable, false)))
                      .build());
          // TODO Add the first sort property as "navPropertyWithRootPrefix + "." + depthVariable"
          // with asc
          sortDocument =
              (Document) ((Document) orderByResult.getStageObjects().get(0)).get("$sort");
          pipeline.add(
              new Document(
                  "$set",
                  new Document(
                      navPropertyWithRootPrefix,
                      new Document(
                          "$sortArray",
                          new Document("input", "$" + navPropertyWithRootPrefix)
                              .append("sortBy", sortDocument)))));
        }
        if (eOption.getTopOption() != null || eOption.getSkipOption() != null) {
          pipeline.add(
              prepareArrayWithChildrenArrayGroupByParentAndLevel(
                  navPropertyWithRootPrefix,
                  navProp.getName(),
                  depthVariable,
                  mongoConnectTo,
                  mongoConnectFrom));
          if (eOption.getOrderByOption() != null) {
            pipeline.add(
                prepareArrayWithSortedChildrenArray(
                    navPropertyWithRootPrefix, navProp.getName(), sortDocument));
          }
          pipeline.add(
              prepareArrayWithSkippedAndLimitedChildrenArray(
                  navPropertyWithRootPrefix,
                  navProp.getName(),
                  eOption.getSkipOption() == null ? null : eOption.getSkipOption().getValue(),
                  eOption.getTopOption() == null ? null : eOption.getTopOption().getValue()));
          pipeline.add(
              prepareFlatterArrayBasedOnChildrenArray(
                  navPropertyWithRootPrefix, navProp.getName()));
          pipeline.add(
              new Document(
                  "$set",
                  new Document(
                      navPropertyWithRootPrefix,
                      "$" + navPropertyWithRootPrefix + ODATA_GRAPHLOOKUP_STAGE_TMP_ARRAY_SUFFIX)));
          pipeline.add(
              prepareReduceStageThatRemovesOrphansFromGraphLookupStage(
                  navPropertyWithRootPrefix, depthVariable, mongoConnectTo, mongoConnectFrom));

          pipeline.add(
              new Document(
                  "$unset", navPropertyWithRootPrefix + ODATA_GRAPHLOOKUP_STAGE_TMP_ARRAY_SUFFIX));
        }
        SelectOperatorOptionsForMapOperator selectResult = null;
        if (eOption.getSelectOption() != null) {
          // TODO check if element does not have nested $expand, if yes then check if potential
          // excluded property would not be
          // TODO a foreign key for external collection
          // TODO In such case we would have to add the foreign property to include and after
          // processing the nested $expand
          // TODO remove it
          OdataSelectToMongoProjectParser odataSelectToMongoProjectParser =
              new OdataSelectToMongoProjectParser();
          DefaultOdataSelectToMongoProjectParserContext.Builder
              odataSelectToMongoProjectParserContextBuilder =
                  DefaultOdataSelectToMongoProjectParserContext.builder();
          if (expandParserContext.propagateGraphLookUpJoinKeys()) {
            odataSelectToMongoProjectParserContextBuilder.appendAdditionalFields(
                Set.of(mongoConnectFrom, mongoConnectTo));
          }
          selectResult =
              odataSelectToMongoProjectParser.computeValueForMapOperator(
                  eOption.getSelectOption(),
                  DefaultEdmMongoContextFacade.builder().build(),
                  odataSelectToMongoProjectParserContextBuilder.build());
          // TODO create object that returns select properties for graphLookup
          // TODO It should return the SelectOperatorResult operator that should be applied instead
          // of just the fields names
          // TODO Create helper component that combine selected fields from the SelectOperatorResult
          // collection

          pipeline.add(prepareArrayWithSelectedProperties(navPropertyWithRootPrefix, selectResult));
        }
        if (removeDepthProperty) {
          // Removing the "depthVariable" property from results
          pipeline.add(new Document("$unset", navPropertyWithRootPrefix + "." + depthVariable));
        }
        Map<String, ExpandElement> nestedElements = null;
        if (eOption.getExpandOption() != null) {
          pipeline.add(
              new Document(
                  "$unwind",
                  new Document("path", "$" + navPropertyWithRootPrefix)
                      .append("preserveNullAndEmptyArrays", true)));
          Set<String> newIdProperties = new HashSet<>(parserExpandItemContext.getIdProperties());
          newIdProperties.add(lookupMongoStartWith);
          ExpandOperatorResult nestedExpandResult =
              parse(
                  eOption.getExpandOption(),
                  expandParserContext,
                  new ParserExpandItemContext(navPropertyWithRootPrefix, newIdProperties, true));
          nestedElements = nestedExpandResult.getExpandElements();
          pipeline.addAll(nestedExpandResult.getStageObjects());
          pipeline.add(prepareCleanUpStageForSingleObjectProperty(navPropertyWithRootPrefix));
          pipeline.addAll(prepareMergingDocumentStages(navPropertyWithRootPrefix, newIdProperties));
          // TODO group if nav is collection

          // TODO Remove properties that were foreign keys

        }

        int levelValue = 1;
        boolean maxLevelRequest = false;
        if (eOption.getLevelsOption() != null) {
          maxLevelRequest = eOption.getLevelsOption().isMax();
          levelValue =
              maxLevelRequest
                  ? expandParserContext.getMaxLevel()
                  : eOption.getLevelsOption().getValue();
        }
        // TODO add clean-up property
        GraphLookUpCleanUpInfo graphLookUpCleanUpInfo = null;
        if (selectResult != null) {
          // TODO
          graphLookUpCleanUpInfo =
              GraphLookUpCleanUpInfo.builder()
                  .withRemoveLocalKeyProperty(
                      !(selectResult.isWildCard()
                          || selectResult.getRequestedFields().contains(edmStartWith)))
                  .withRemoveForeignKeyProperty(
                      !(selectResult.isWildCard()
                          || selectResult.getRequestedFields().contains(edmConnectTo)))
                  .build();
        }
        expandElements.put(
            navProp.getName(),
            ExpandElement.builder()
                .withEdmPath(navProp.getName())
                .withMongoPath(navProp.getName())
                .withFetchType(FetchType.GRAPHLOOKUP)
                .withLevel(levelValue)
                .withMaxLevelRequest(maxLevelRequest)
                .withLocalKeyProperty(edmStartWith)
                .withForeignKeyProperty(edmConnectTo)
                .withForeignCollection(targetCollection)
                .withEdmEntityFullName(targetFullTypeName)
                .withCollection(navProp.isCollection())
                .withDepthVariableName(depthVariable)
                .withExpandElements(nestedElements)
                .withGraphLookUpCleanUpInfo(graphLookUpCleanUpInfo)
                .build());
        return pipeline;
      } else {
        Map<String, ExpandElement> nestedElements = null;
        ExpandOperatorResult nestedExpandResult = null;
        if (eOption.getExpandOption() != null) {
          nestedExpandResult = parse(eOption.getExpandOption(), expandParserContext);
          nestedElements = nestedExpandResult.getExpandElements();
        }

        if (eOption.getLevelsOption() != null
            && (eOption.getLevelsOption().isMax() || eOption.getLevelsOption().getValue() > 1)) {
          int maxDepth =
              translateODataExpandLevelsToGraphLookupMaxDepth(eOption, expandParserContext);
          pipeline.addAll(
              prepareLookUpStage(
                  targetCollection,
                  lookupMongoStartWith,
                  mongoConnectTo,
                  eOption,
                  expandParserContext,
                  parserExpandItemContext,
                  navPropertyWithRootPrefix,
                  navProp,
                  1,
                  maxDepth + 1,
                  nestedExpandResult));
        } else {
          pipeline.addAll(
              prepareLookUpStage(
                  targetCollection,
                  lookupMongoStartWith,
                  mongoConnectTo,
                  eOption,
                  expandParserContext,
                  parserExpandItemContext,
                  navPropertyWithRootPrefix,
                  navProp,
                  1,
                  1,
                  nestedExpandResult));
        }

        int levelValue = 1;
        boolean maxLevelRequest = false;
        if (eOption.getLevelsOption() != null) {
          maxLevelRequest = eOption.getLevelsOption().isMax();
          levelValue =
              maxLevelRequest
                  ? expandParserContext.getMaxLevel()
                  : eOption.getLevelsOption().getValue();
        }
        expandElements.put(
            navProp.getName(),
            ExpandElement.builder()
                .withEdmPath(navProp.getName())
                .withMongoPath(navProp.getName())
                .withFetchType(FetchType.LOOKUP)
                .withLevel(levelValue)
                .withMaxLevelRequest(maxLevelRequest)
                .withLocalKeyProperty(edmStartWith)
                .withForeignKeyProperty(edmConnectTo)
                .withForeignCollection(targetCollection)
                .withEdmEntityFullName(targetFullTypeName)
                .withCollection(navProp.isCollection())
                .withDepthVariableName(null)
                .withExpandElements(nestedElements)
                .build());

        return pipeline;
      }
    }
    return List.of();
  }

  private List<Bson> prepareLookUpStage(
      String targetCollection,
      String lookupMongoStartWith,
      String mongoConnectTo,
      ExpandItem eOption,
      ExpandParserContext expandParserContext,
      ParserExpandItemContext parserExpandItemContext,
      String navPropertyWithRootPrefix,
      EdmNavigationProperty navProp,
      int currentLevel,
      int maxLevel,
      ExpandOperatorResult nestedExpandResult)
      throws ExpressionVisitException, ODataApplicationException {
    // Adding $lookup
    List<Bson> pipeline = new ArrayList<>();
    Document lookup = new Document();
    Document lookupInnerObject =
        new Document()
            .append("from", targetCollection)
            .append("localField", lookupMongoStartWith)
            .append("foreignField", mongoConnectTo);

    if (eOption.getFilterOption() != null
        || eOption.getOrderByOption() != null
        || eOption.getTopOption() != null
        || eOption.getSkipOption() != null
        || eOption.getSelectOption() != null
        || currentLevel != maxLevel
        || eOption.getExpandOption() != null) {
      ODataFilterToMongoMatchParser oDataFilterToMongoMatchParser =
          new ODataFilterToMongoMatchParser();
      OdataOrderByToMongoSortParser odataOrderByToMongoSortParser =
          new OdataOrderByToMongoSortParser();
      OdataTopToMongoLimitParser odataTopToMongoLimitParser = new OdataTopToMongoLimitParser();
      OdataSkipToMongoSkipParser odataSkipToMongoSkipParser = new OdataSkipToMongoSkipParser();
      OdataSelectToMongoProjectParser odataSelectToMongoProjectParser =
          new OdataSelectToMongoProjectParser();
      EdmMongoContextFacade facade =
          DefaultEdmMongoContextFacade.builder().withEntityPropertiesMongoPathContext(null).build();

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
      if (eOption.getSkipOption() != null) {
        lookupPipeline.addAll(
            odataSkipToMongoSkipParser.parse(eOption.getSkipOption()).getStageObjects());
      }
      if (eOption.getTopOption() != null) {
        lookupPipeline.addAll(
            odataTopToMongoLimitParser.parse(eOption.getTopOption()).getStageObjects());
      }
      boolean isNotLastLevelLookUp = currentLevel != maxLevel;
      SelectOperatorResult selectOperatorResult = null;
      // TODO check if _id or collection can be lost
      if (eOption.getSelectOption() != null) {
        DefaultOdataSelectToMongoProjectParserContext.Builder
            odataSelectToMongoProjectParserContextBuilder =
                DefaultOdataSelectToMongoProjectParserContext.builder();
        odataSelectToMongoProjectParserContextBuilder.appendAdditionalFields(
            Set.of(navPropertyWithRootPrefix));
        if (isNotLastLevelLookUp) {
          odataSelectToMongoProjectParserContextBuilder.appendAdditionalFields(
              Set.of(lookupMongoStartWith));
        }
        if (nestedExpandResult != null) {
          odataSelectToMongoProjectParserContextBuilder.appendAdditionalFields(
              nestedExpandResult.getExpandElements().keySet());
        }
        selectOperatorResult =
            odataSelectToMongoProjectParser.parse(
                eOption.getSelectOption(),
                facade,
                odataSelectToMongoProjectParserContextBuilder.build());
      }
      if (currentLevel != maxLevel) {
        lookupPipeline.addAll(
            prepareLookUpStage(
                targetCollection,
                lookupMongoStartWith,
                mongoConnectTo,
                eOption,
                expandParserContext,
                parserExpandItemContext,
                navPropertyWithRootPrefix,
                navProp,
                currentLevel + 1,
                maxLevel,
                nestedExpandResult));
      }
      if (nestedExpandResult != null) {
        lookupPipeline.addAll(nestedExpandResult.getStageObjects());
      }
      if (selectOperatorResult != null) {
        lookupPipeline.addAll(selectOperatorResult.getStageObjects());
      }
      if (selectOperatorResult != null
          && !selectOperatorResult.getRequestedFields().contains(lookupMongoStartWith)) {
        // Remove lookupMongoStartWith as not selected field
        lookupPipeline.add(new Document("$unset", lookupMongoStartWith));
      }
      lookupInnerObject.append("pipeline", lookupPipeline);
    }
    lookupInnerObject.append("as", navPropertyWithRootPrefix);
    lookup.append("$lookup", lookupInnerObject);
    pipeline.add(lookup);
    if (!navProp.isCollection()) {
      // For single-valued navigation properties (one-to-one or many-to-one),
      // unwind the lookup result array to render a single object.
      // preserveNullAndEmptyArrays is set to true to ensure the parent document
      // is not dropped if the related resource is missing (OData $expand behavior).
      pipeline.add(
          new Document(
              "$unwind",
              new Document("path", "$" + navPropertyWithRootPrefix)
                  .append("preserveNullAndEmptyArrays", true)));
    }
    if (eOption.getExpandOption() != null) {
      //      if (navProp.isCollection()) {
      //        pipeline.add(
      //            new Document(
      //                "$unwind",
      //                new Document("path", "$" + navPropertyWithRootPrefix)
      //                    .append("preserveNullAndEmptyArrays", true)));
      //      }
      //      Set<String> newIdProperties = new
      // HashSet<>(parserExpandItemContext.getIdProperties());
      //      newIdProperties.add(lookupMongoStartWith);
      //      ExpandOperatorResult nestedExpandResult =
      //          parse(
      //              eOption.getExpandOption(),
      //              expandParserContext,
      //              new ParserExpandItemContext(navPropertyWithRootPrefix, newIdProperties,
      // true));
      //      pipeline.addAll(nestedExpandResult.getStageObjects());
      //      if (navProp.isCollection()) {
      //        pipeline.add(prepareCleanUpStageForSingleObjectProperty(navPropertyWithRootPrefix));
      //        pipeline.addAll(prepareMergingDocumentStages(navPropertyWithRootPrefix,
      // newIdProperties));
      //      } else {
      //        if (parserExpandItemContext.isAddCleanUpEmptyPropertiesStage()) {
      //
      // pipeline.add(prepareCleanUpStageForSingleObjectProperty(navPropertyWithRootPrefix));
      //        }
      //      }
      // TODO group if nav is collection

      // TODO Remove properties that were foreign keys

    } else {
      if (parserExpandItemContext.isAddCleanUpEmptyPropertiesStage()) {
        if (navProp.isCollection()) {
          pipeline.add(prepareCleanUpStageForArrayProperty(navPropertyWithRootPrefix));
        } else {
          pipeline.add(prepareCleanUpStageForSingleObjectProperty(navPropertyWithRootPrefix));
        }
      }
    }

    return pipeline;
  }

  private Bson prepareCleanUpStageForSingleObjectProperty(String navProperty) {
    return Document.parse(
        """
                    {
                                              $addFields: {
                                                "%1$s": {
                                                  $cond: {
                                                    if: {
                                                      $eq: [
                                                        { $size: { $objectToArray: { $ifNull: ["$%1$s", {}] } } },
                                                        0
                                                      ]
                                                    },
                                                    then: "$$REMOVE",
                                                    else: "$%1$s"
                                                  }
                                                }
                                              }
                                            }
            """
            .formatted(navProperty));
  }

  private Bson prepareCleanUpStageForArrayProperty(String navProperty) {
    return Document.parse(
        """
                    {
                                            "$addFields": {
                                              "%1$s": {
                                                "$cond": {
                                                  "if": {
                                                    "$eq": [{ "$size": "$%1$s" }, 0]
                                                  },
                                                  "then": "$$REMOVE",
                                                  "else": "$%1$s"
                                                }
                                              }
                                            }
                                         }
                    """
            .formatted(navProperty));
  }

  private static final class ParserExpandItemContext {
    private final String root;
    private final Set<String> idProperties;

    public boolean isAddCleanUpEmptyPropertiesStage() {
      return addCleanUpEmptyPropertiesStage;
    }

    private final boolean addCleanUpEmptyPropertiesStage;

    public ParserExpandItemContext(
        String root, Set<String> idProperties, boolean addCleanUpEmptyPropertiesStage) {
      this.root = root;
      this.idProperties =
          Collections.unmodifiableSet(idProperties == null ? Collections.emptySet() : idProperties);
      this.addCleanUpEmptyPropertiesStage = addCleanUpEmptyPropertiesStage;
    }

    public String getRoot() {
      return root;
    }

    public Set<String> getIdProperties() {
      return idProperties;
    }
  }

  private Collection<? extends Bson> prepareMergingDocumentStages(
      String navPropertyWithRootPrefix, Set<String> idFields) {
    List<Bson> results = new ArrayList<>();
    // TODO setting _id strategy
    Document idObject = new Document();
    int i = 0;
    for (String idField : idFields) {
      idObject.append("id_" + (i++), "$" + idField);
    }
    results.add(
        Document.parse(
            """
                    {
                             $group: {
                               _id: %1$s,
                               "tmp_array": { $push: "$%2$s" },
                               "tmp_root": { $first: "$$ROOT" }
                             }
                           }
                 """
                .formatted(idObject.toJson(), navPropertyWithRootPrefix)));
    results.add(
        Document.parse(
            """
                            {
                                     $set: {
                                       "tmp_root.%1$s": "$tmp_array"
                                     }
                                   }
                         """
                .formatted(navPropertyWithRootPrefix)));
    results.add(
        Document.parse(
            """
              {
                $replaceRoot: {
                  newRoot: "$tmp_root"
                }
              }
              """));
    return results;
  }

  private static Bson prepareArrayWithSelectedProperties(
      String navPropertyWithRootPrefix, SelectOperatorOptionsForMapOperator selectResult) {
    SelectOperatorResultToBsonDocumentConverter selectOptionToMapConverter =
        new SelectOperatorResultToBsonDocumentConverter();
    Document mapObject =
        selectOptionToMapConverter.convert(
            selectResult.getSelectedFields(), "item", selectResult.getArrayFields());
    return Document.parse(
        """
                {
                    $set: {
                      %1$s: {
                        $map: {
                          input: { $ifNull: ["$%1$s", []] },
                          as: "item",
                          in: %2$s
                        }
                      }
                    }
                  }
                """
            .formatted(navPropertyWithRootPrefix, mapObject.toJson()));
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
   * @param navPropertyWithRootPrefix the navigation property being expanded
   * @param depthVariable the name of the field storing the recursion depth
   * @param mongoConnectTo the field name used for the "connectToField" in $graphLookup
   * @param mongoConnectFrom the field name used for the "connectFromField" in $graphLookup
   * @return a BSON Document representing the $set stage
   */
  private static Document prepareReduceStageThatRemovesOrphansFromGraphLookupStage(
      String navPropertyWithRootPrefix,
      String depthVariable,
      String mongoConnectTo,
      String mongoConnectFrom) {
    return new Document(
        "$set",
        new Document(
            navPropertyWithRootPrefix,
            new Document(
                "$reduce",
                new Document(
                        "input",
                        new Document(
                            "$sortArray",
                            new Document("input", "$" + navPropertyWithRootPrefix)
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
      String navPropertyWithRootPrefix,
      String navPropName,
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
                                  [{ %4$s: "$$this.%4$s", %3$s: "$$this.%3$s", %5$s: ["$$this"] }]
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
                                            %5$s: { $concatArrays: ["$$bucket.%5$s", ["$$this"]] }
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
                navPropertyWithRootPrefix + ODATA_GRAPHLOOKUP_STAGE_TMP_ARRAY_SUFFIX,
                navPropertyWithRootPrefix,
                depthVariable,
                mongoConnectTo,
                navPropName));
  }

  private static Document prepareArrayWithSortedChildrenArray(
      String navPropertyWithRootPrefix, String navPropName, Document sortObject) {

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
                navPropertyWithRootPrefix + ODATA_GRAPHLOOKUP_STAGE_TMP_ARRAY_SUFFIX,
                navPropName,
                sortObject.toJson()));
  }

  private static Document prepareArrayWithSkippedAndLimitedChildrenArray(
      String navPropertyWithRootPrefix, String navPropName, Integer skip, Integer top) {

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
                navPropertyWithRootPrefix + ODATA_GRAPHLOOKUP_STAGE_TMP_ARRAY_SUFFIX,
                navPropName,
                skip == null ? 0 : skip,
                top == null
                    ? """
                                    { $size: "$$item.%2$s" }
                                    """
                    : top));
  }

  private static Document prepareFlatterArrayBasedOnChildrenArray(
      String navPropertyWithRootPrefix, String navPropName) {

    return Document.parse(
        """
                    {
                        $set: {
                          %1$s: {
                            $reduce: {
                                    input: "$%1$s",
                                    initialValue: [],
                                    in: {
                                      $concatArrays: ["$$value", "$$this.%3$s"]
                                    }
                            }
                          }
                        }
                     }
                    """
            .formatted(
                navPropertyWithRootPrefix + ODATA_GRAPHLOOKUP_STAGE_TMP_ARRAY_SUFFIX,
                navPropertyWithRootPrefix,
                navPropName));
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
    private final Map<String, ExpandElement> expandElements;

    private DefaultExpandOperatorResult(List<Bson> stageObjects) {
      this.stageObjects = stageObjects;
      this.expandElements = Collections.emptyMap();
    }

    private DefaultExpandOperatorResult(
        List<Bson> stageObjects, Map<String, ExpandElement> expandElements) {
      this.stageObjects = stageObjects;
      this.expandElements =
          expandElements != null
              ? Collections.unmodifiableMap(new HashMap<>(expandElements))
              : Collections.emptyMap();
    }

    @Override
    public Map<String, ExpandElement> getExpandElements() {
      return expandElements;
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

  /** Default implementation of {@link ExpandParserContext}. */
  public static class DefaultExpandParserContext implements ExpandParserContext {
    private final Map<String, EdmPropertyMongoPathResolver> edmTypeMapping;
    private final Map<KeyValue<String, String>, String> edmTablesToMongoDBCollections;
    private final Integer maxLevel;
    private final boolean useLookupForLevelGreaterThanOne;
    private final boolean propagateGraphLookUpJoinKeys;

    /**
     * Constructs a new DefaultExpandParserContext.
     *
     * @param edmTypeMapping mapping between EDM type names and their Mongo path resolvers
     * @param edmTablesToMongoDBCollections mapping between EDM entity sets and their MongoDB
     *     collection names
     * @param maxLevel maximum level of recursion for $expand
     */
    public DefaultExpandParserContext(
        Map<String, EdmPropertyMongoPathResolver> edmTypeMapping,
        Map<KeyValue<String, String>, String> edmTablesToMongoDBCollections,
        Integer maxLevel) {
      this(edmTypeMapping, edmTablesToMongoDBCollections, maxLevel, false);
    }

    /**
     * Constructs a new DefaultExpandParserContext.
     *
     * @param edmTypeMapping mapping between EDM type names and their Mongo path resolvers
     * @param edmTablesToMongoDBCollections mapping between EDM entity sets and their MongoDB
     *     collection names
     * @param maxLevel maximum level of recursion for $expand
     * @param useLookupForLevelGreaterThanOne true if the $lookup stage should be used to handle
     *     $level greater than 1
     */
    public DefaultExpandParserContext(
        Map<String, EdmPropertyMongoPathResolver> edmTypeMapping,
        Map<KeyValue<String, String>, String> edmTablesToMongoDBCollections,
        Integer maxLevel,
        boolean useLookupForLevelGreaterThanOne) {
      this(
          edmTypeMapping,
          edmTablesToMongoDBCollections,
          maxLevel,
          useLookupForLevelGreaterThanOne,
          false);
    }

    /**
     * Constructs a new DefaultExpandParserContext.
     *
     * @param edmTypeMapping mapping between EDM type names and their Mongo path resolvers
     * @param edmTablesToMongoDBCollections mapping between EDM entity sets and their MongoDB
     *     collection names
     * @param maxLevel maximum level of recursion for $expand
     * @param useLookupForLevelGreaterThanOne true if the $lookup stage should be used to handle
     *     $level greater than 1
     * @param propagateGraphLookUpJoinKeys true if join keys used for the $graphLookup stage should
     *     be propagated
     */
    public DefaultExpandParserContext(
        Map<String, EdmPropertyMongoPathResolver> edmTypeMapping,
        Map<KeyValue<String, String>, String> edmTablesToMongoDBCollections,
        Integer maxLevel,
        boolean useLookupForLevelGreaterThanOne,
        boolean propagateGraphLookUpJoinKeys) {
      this.edmTypeMapping = edmTypeMapping;
      this.edmTablesToMongoDBCollections = edmTablesToMongoDBCollections;
      this.maxLevel = maxLevel;
      this.useLookupForLevelGreaterThanOne = useLookupForLevelGreaterThanOne;
      this.propagateGraphLookUpJoinKeys = propagateGraphLookUpJoinKeys;
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
    public boolean isUseLookupForLevelGreaterThanOne() {
      return useLookupForLevelGreaterThanOne;
    }

    @Override
    public boolean propagateGraphLookUpJoinKeys() {
      return propagateGraphLookUpJoinKeys;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DefaultExpandParserContext that = (DefaultExpandParserContext) o;
      return useLookupForLevelGreaterThanOne == that.useLookupForLevelGreaterThanOne
          && propagateGraphLookUpJoinKeys == that.propagateGraphLookUpJoinKeys
          && Objects.equals(edmTypeMapping, that.edmTypeMapping)
          && Objects.equals(edmTablesToMongoDBCollections, that.edmTablesToMongoDBCollections)
          && Objects.equals(maxLevel, that.maxLevel);
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          edmTypeMapping,
          edmTablesToMongoDBCollections,
          maxLevel,
          useLookupForLevelGreaterThanOne,
          propagateGraphLookUpJoinKeys);
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
          + ", useLookupForLevelGreaterThanOne="
          + useLookupForLevelGreaterThanOne
          + ", propagateGraphLookUpJoinKeys="
          + propagateGraphLookUpJoinKeys
          + '}';
    }

    /**
     * Returns a new builder for DefaultExpandParserContext.
     *
     * @return the builder instance
     */
    public static Builder builder() {
      return new Builder();
    }

    /** Builder for {@link DefaultExpandParserContext}. */
    public static class Builder {
      private Map<String, EdmPropertyMongoPathResolver> edmTypeMapping = new HashMap<>();
      private Map<KeyValue<String, String>, String> edmTablesToMongoDBCollections = new HashMap<>();
      private Integer maxLevel = DEFAULT_MAX_LEVEL;
      private boolean useLookupForLevelGreaterThanOne = false;
      private boolean propagateGraphLookUpJoinKeys = false;

      /**
       * Sets the mapping between EDM type names and their Mongo path resolvers.
       *
       * @param edmTypeMapping the type mapping
       * @return the builder instance
       */
      public Builder withEdmTypeMapping(Map<String, EdmPropertyMongoPathResolver> edmTypeMapping) {
        this.edmTypeMapping = edmTypeMapping;
        return this;
      }

      /**
       * Sets the mapping between EDM entity sets and their MongoDB collection names.
       *
       * @param edmTablesToMongoDBCollections the collection mapping
       * @return the builder instance
       */
      public Builder withEdmTablesToMongoDBCollections(
          Map<KeyValue<String, String>, String> edmTablesToMongoDBCollections) {
        this.edmTablesToMongoDBCollections = edmTablesToMongoDBCollections;
        return this;
      }

      /**
       * Sets the maximum level of recursion for $expand.
       *
       * @param maxLevel the maximum level
       * @return the builder instance
       */
      public Builder withMaxLevel(Integer maxLevel) {
        this.maxLevel = maxLevel;
        return this;
      }

      /**
       * Sets whether the $lookup stage should be used to handle $level greater than 1.
       *
       * @param useLookupForLevelGreaterThanOne true to use $lookup, false to use $graphLookup
       * @return the builder instance
       */
      public Builder withUseLookupForLevelGreaterThanOne(boolean useLookupForLevelGreaterThanOne) {
        this.useLookupForLevelGreaterThanOne = useLookupForLevelGreaterThanOne;
        return this;
      }

      /**
       * Sets whether join keys used for the $graphLookup stage should be propagated.
       *
       * @param propagateGraphLookUpJoinKeys true to propagate, false otherwise
       * @return the builder instance
       */
      public Builder withPropagateGraphLookUpJoinKeys(boolean propagateGraphLookUpJoinKeys) {
        this.propagateGraphLookUpJoinKeys = propagateGraphLookUpJoinKeys;
        return this;
      }

      /**
       * Initializes the builder with values from an existing context.
       *
       * @param defaultExpandParserContext the context to copy values from
       * @return the builder instance
       */
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
        this.useLookupForLevelGreaterThanOne =
            defaultExpandParserContext.useLookupForLevelGreaterThanOne;
        this.propagateGraphLookUpJoinKeys = defaultExpandParserContext.propagateGraphLookUpJoinKeys;
        return this;
      }

      /**
       * Builds the DefaultExpandParserContext instance.
       *
       * @return the constructed context
       */
      public DefaultExpandParserContext build() {
        return new DefaultExpandParserContext(
            edmTypeMapping != null
                ? Collections.unmodifiableMap(new HashMap<>(edmTypeMapping))
                : null,
            edmTablesToMongoDBCollections != null
                ? Collections.unmodifiableMap(new HashMap<>(edmTablesToMongoDBCollections))
                : null,
            maxLevel,
            useLookupForLevelGreaterThanOne,
            propagateGraphLookUpJoinKeys);
      }
    }
  }
}
