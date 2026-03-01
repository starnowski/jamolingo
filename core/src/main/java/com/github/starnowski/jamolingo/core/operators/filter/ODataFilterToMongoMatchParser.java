package com.github.starnowski.jamolingo.core.operators.filter;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.olingo.commons.api.edm.Edm;
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
   * @param edm the Entity Data Model
   * @return the result of the parsing operation
   * @throws ODataApplicationException if an error occurs during parsing
   * @throws ExpressionVisitException if an error occurs during expression visiting
   */
  public FilterOperatorResult parse(FilterOption filter, Edm edm)
      throws ODataApplicationException, ExpressionVisitException {
    return parse(filter, edm, DefaultEdmMongoContextFacade.builder().build());
  }

  /**
   * Parses the given OData filter option into a FilterOperatorResult using the specified path
   * resolver.
   *
   * @param filter the OData filter option
   * @param edm the Entity Data Model
   * @param edmMongoContextFacade the path resolver for mapping EDM properties to MongoDB paths
   * @return the result of the parsing operation
   * @throws ODataApplicationException if an error occurs during parsing
   * @throws ExpressionVisitException if an error occurs during expression visiting
   */
  public FilterOperatorResult parse(
      FilterOption filter, Edm edm, EdmPropertyMongoPathResolver edmMongoContextFacade)
      throws ODataApplicationException, ExpressionVisitException {
    if (filter == null) return new DefaultFilterOperatorResult();
    if (edmMongoContextFacade == null) {
      throw new IllegalArgumentException("The edmMongoContextFacade can not be nul;");
    }
    Expression expr = filter.getExpression();
    MongoFilterVisitor rootMongoFilterVisitor = new MongoFilterVisitor(edm, edmMongoContextFacade);
    Bson result = MongoFilterVisitor.unwrapWrapperIfNeeded(expr.accept(rootMongoFilterVisitor));
    return new DefaultFilterOperatorResult(
        List.of(new Document("$match", new Document("$and", List.of(result)))),
        rootMongoFilterVisitor.getUsedMongoDBProperties());
  }

  private static class DefaultFilterOperatorResult implements FilterOperatorResult {

    private final List<Bson> stageObjects;
    private final Set<String> userMongoDBProperties;

    public DefaultFilterOperatorResult() {
      this(Collections.emptyList(), Collections.emptySet());
    }

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
}
