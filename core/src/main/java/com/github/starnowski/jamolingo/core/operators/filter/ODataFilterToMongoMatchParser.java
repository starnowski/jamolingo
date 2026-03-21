package com.github.starnowski.jamolingo.core.operators.filter;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.bson.Document;
import org.bson.conversions.Bson;

/** Parser that converts OData filter options into MongoDB match stages. */
public class ODataFilterToMongoMatchParser {

  /**
   * Parses the given OData filter option into a FilterOperatorResult.
   *
   * @param filter the OData filter option
   * @return the result of the parsing operation
   * @throws ODataApplicationException if an error occurs during parsing
   * @throws ExpressionVisitException if an error occurs during expression visiting
   */
  public FilterOperatorResult parse(FilterOption filter)
      throws ODataApplicationException, ExpressionVisitException {
    return parse(filter, DefaultEdmMongoContextFacade.builder().build());
  }

  /**
   * Parses the given OData filter option into a FilterOperatorResult using the specified common
   * context.
   *
   * @param filter the OData filter option
   * @param mongoFilterVisitorCommonContext the common context for the filter visitor
   * @return the result of the parsing operation
   * @throws ODataApplicationException if an error occurs during parsing
   * @throws ExpressionVisitException if an error occurs during expression visiting
   */
  public FilterOperatorResult parse(
      FilterOption filter, MongoFilterVisitorCommonContext mongoFilterVisitorCommonContext)
      throws ODataApplicationException, ExpressionVisitException {
    return parse(
        filter, DefaultEdmMongoContextFacade.builder().build(), mongoFilterVisitorCommonContext);
  }

  /**
   * Parses the given OData filter option into a FilterOperatorResult using the specified path
   * resolver.
   *
   * @param filter the OData filter option
   * @param edmMongoContextFacade the path resolver for mapping EDM properties to MongoDB paths
   * @return the result of the parsing operation
   * @throws ODataApplicationException if an error occurs during parsing
   * @throws ExpressionVisitException if an error occurs during expression visiting
   */
  public FilterOperatorResult parse(
      FilterOption filter, EdmPropertyMongoPathResolver edmMongoContextFacade)
      throws ODataApplicationException, ExpressionVisitException {
    return parse(
        filter, edmMongoContextFacade, DefaultMongoFilterVisitorCommonContext.builder().build());
  }

  /**
   * Parses the given OData filter option into a FilterOperatorResult using the specified path
   * resolver and common context.
   *
   * @param filter the OData filter option
   * @param edmMongoContextFacade the path resolver for mapping EDM properties to MongoDB paths
   * @param mongoFilterVisitorCommonContext the common context for the filter visitor
   * @return the result of the parsing operation
   * @throws ODataApplicationException if an error occurs during parsing
   * @throws ExpressionVisitException if an error occurs during expression visiting
   */
  public FilterOperatorResult parse(
      FilterOption filter,
      EdmPropertyMongoPathResolver edmMongoContextFacade,
      MongoFilterVisitorCommonContext mongoFilterVisitorCommonContext)
      throws ODataApplicationException, ExpressionVisitException {
    if (filter == null) return new DefaultFilterOperatorResult();
    if (edmMongoContextFacade == null) {
      throw new IllegalArgumentException("The edmMongoContextFacade can not be nul;");
    }
    Expression expr = filter.getExpression();
    MongoFilterVisitor rootMongoFilterVisitor =
        new MongoFilterVisitor(edmMongoContextFacade, mongoFilterVisitorCommonContext);
    Bson result = MongoFilterVisitor.unwrapWrapperIfNeeded(expr.accept(rootMongoFilterVisitor));
    return new DefaultFilterOperatorResult(
        List.of(new Document("$match", new Document("$and", List.of(result)))),
        rootMongoFilterVisitor.getUsedMongoDBProperties());
  }

  /**
   * Parses the given OData filter option into a FilterOperatorQueryObjectResult.
   *
   * @param filter the OData filter option
   * @return the result of the parsing operation
   * @throws ODataApplicationException if an error occurs during parsing
   * @throws ExpressionVisitException if an error occurs during expression visiting
   */
  public FilterOperatorQueryObjectResult parseQueryObject(FilterOption filter)
      throws ODataApplicationException, ExpressionVisitException {
    return parseQueryObject(filter, DefaultEdmMongoContextFacade.builder().build());
  }

  /**
   * Parses the given OData filter option into a FilterOperatorQueryObjectResult using the specified
   * common context.
   *
   * @param filter the OData filter option
   * @param mongoFilterVisitorCommonContext the common context for the filter visitor
   * @return the result of the parsing operation
   * @throws ODataApplicationException if an error occurs during parsing
   * @throws ExpressionVisitException if an error occurs during expression visiting
   */
  public FilterOperatorQueryObjectResult parseQueryObject(
      FilterOption filter, MongoFilterVisitorCommonContext mongoFilterVisitorCommonContext)
      throws ODataApplicationException, ExpressionVisitException {
    return parseQueryObject(
        filter, DefaultEdmMongoContextFacade.builder().build(), mongoFilterVisitorCommonContext);
  }

  /**
   * Parses the given OData filter option into a FilterOperatorQueryObjectResult using the specified
   * path resolver.
   *
   * @param filter the OData filter option
   * @param edmMongoContextFacade the path resolver for mapping EDM properties to MongoDB paths
   * @return the result of the parsing operation
   * @throws ODataApplicationException if an error occurs during parsing
   * @throws ExpressionVisitException if an error occurs during expression visiting
   */
  public FilterOperatorQueryObjectResult parseQueryObject(
      FilterOption filter, EdmPropertyMongoPathResolver edmMongoContextFacade)
      throws ODataApplicationException, ExpressionVisitException {
    return parseQueryObject(
        filter, edmMongoContextFacade, DefaultMongoFilterVisitorCommonContext.builder().build());
  }

  /**
   * Parses the given OData filter option into a FilterOperatorQueryObjectResult using the specified
   * path resolver and common context.
   *
   * @param filter the OData filter option
   * @param edmMongoContextFacade the path resolver for mapping EDM properties to MongoDB paths
   * @param mongoFilterVisitorCommonContext the common context for the filter visitor
   * @return the result of the parsing operation
   * @throws ODataApplicationException if an error occurs during parsing
   * @throws ExpressionVisitException if an error occurs during expression visiting
   */
  public FilterOperatorQueryObjectResult parseQueryObject(
      FilterOption filter,
      EdmPropertyMongoPathResolver edmMongoContextFacade,
      MongoFilterVisitorCommonContext mongoFilterVisitorCommonContext)
      throws ODataApplicationException, ExpressionVisitException {
    if (filter == null) return new DefaultFilterOperatorQueryObjectResult(null, false, null);
    if (edmMongoContextFacade == null) {
      throw new IllegalArgumentException("The edmMongoContextFacade can not be nul;");
    }
    Expression expr = filter.getExpression();
    MongoFilterVisitor rootMongoFilterVisitor =
        new MongoFilterVisitor(edmMongoContextFacade, mongoFilterVisitorCommonContext);
    Bson result = MongoFilterVisitor.unwrapWrapperIfNeeded(expr.accept(rootMongoFilterVisitor));
    return new DefaultFilterOperatorQueryObjectResult(
        new Document("$and", List.of(result)), false, null);
  }

  /** Default implementation of the FilterOperatorResult. */
  private static class DefaultFilterOperatorResult implements FilterOperatorResult {

    private final List<Bson> stageObjects;
    private final Set<String> userMongoDBProperties;

    /** Creates a new empty DefaultFilterOperatorResult. */
    public DefaultFilterOperatorResult() {
      this(Collections.emptyList(), Collections.emptySet());
    }

    /**
     * Creates a new DefaultFilterOperatorResult with the specified stage objects and used
     * properties.
     *
     * @param stageObjects the list of BSON stage objects
     * @param userMongoDBProperties the set of used MongoDB properties
     */
    public DefaultFilterOperatorResult(List<Bson> stageObjects, Set<String> userMongoDBProperties) {
      this.stageObjects = stageObjects;
      this.userMongoDBProperties = userMongoDBProperties;
    }

    @Override
    public List<Bson> getStageObjects() {
      return stageObjects;
    }

    @Override
    public List<String> getUsedMongoDocumentProperties() {
      return userMongoDBProperties.stream().toList();
    }

    @Override
    public List<String> getWrittenMongoDocumentProperties() {
      return List.of();
    }

    @Override
    public List<String> getAddedMongoDocumentProperties() {
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

  private static class DefaultFilterOperatorQueryObjectResult
      implements FilterOperatorQueryObjectResult {
    private final Bson queryObject;
    private final boolean aggregationPipelineRequired;
    private final Throwable cause;

    public DefaultFilterOperatorQueryObjectResult(
        Bson queryObject, boolean aggregationPipelineRequired, Throwable cause) {
      this.queryObject = queryObject;
      this.aggregationPipelineRequired = aggregationPipelineRequired;
      this.cause = cause;
    }

    public Bson getQueryObject() {
      return queryObject;
    }

    public boolean isAggregationPipelineRequired() {
      return aggregationPipelineRequired;
    }

    public Throwable getCause() {
      return cause;
    }
  }
}
