package com.github.starnowski.jamolingo.demo;

import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.operators.count.OdataCountToMongoCountParser;
import com.github.starnowski.jamolingo.core.operators.filter.ODataFilterToMongoMatchParser;
import com.github.starnowski.jamolingo.core.operators.orderby.OdataOrderByToMongoSortParser;
import com.github.starnowski.jamolingo.core.operators.select.OdataSelectToMongoProjectParser;
import com.github.starnowski.jamolingo.core.operators.skip.OdataSkipToMongoSkipParser;
import com.github.starnowski.jamolingo.core.operators.top.OdataTopToMongoLimitParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;
import org.bson.conversions.Bson;

@ApplicationScoped
public class ODataQueryService {

  @Inject Edm edm;

  @Inject DefaultEdmMongoContextFacade edmMongoContextFacade;

  private final ODataFilterToMongoMatchParser filterParser = new ODataFilterToMongoMatchParser();
  private final OdataSelectToMongoProjectParser selectParser =
      new OdataSelectToMongoProjectParser();
  private final OdataOrderByToMongoSortParser orderByParser = new OdataOrderByToMongoSortParser();
  private final OdataTopToMongoLimitParser topParser = new OdataTopToMongoLimitParser();
  private final OdataSkipToMongoSkipParser skipParser = new OdataSkipToMongoSkipParser();
  private final OdataCountToMongoCountParser countParser = new OdataCountToMongoCountParser();

  public static class QueryPlan {
    private final List<Bson> dataPipeline;
    private final List<Bson> countPipeline;
    private final boolean countRequested;

    public QueryPlan(List<Bson> dataPipeline, List<Bson> countPipeline, boolean countRequested) {
      this.dataPipeline = dataPipeline;
      this.countPipeline = countPipeline;
      this.countRequested = countRequested;
    }

    public List<Bson> getDataPipeline() {
      return dataPipeline;
    }

    public List<Bson> getCountPipeline() {
      return countPipeline;
    }

    public boolean isCountRequested() {
      return countRequested;
    }
  }

  public QueryPlan buildQueryPlan(String query)
      throws UriParserException,
          UriValidationException,
          ODataApplicationException,
          ExpressionVisitException {
    UriInfo uriInfo = new Parser(edm, OData.newInstance()).parseUri("examples2", query, null, null);
    List<Bson> filterStages =
        filterParser.parse(uriInfo.getFilterOption(), edmMongoContextFacade).getStageObjects();

    List<Bson> dataPipeline = new ArrayList<>(filterStages);
    // 2. $orderby -> $sort
    dataPipeline.addAll(
        orderByParser.parse(uriInfo.getOrderByOption(), edmMongoContextFacade).getStageObjects());

    // 3. $skip -> $skip
    dataPipeline.addAll(skipParser.parse(uriInfo.getSkipOption()).getStageObjects());

    // 4. $top -> $limit
    dataPipeline.addAll(topParser.parse(uriInfo.getTopOption()).getStageObjects());

    // 5. $select -> $project
    dataPipeline.addAll(
        selectParser.parse(uriInfo.getSelectOption(), edmMongoContextFacade).getStageObjects());

    List<Bson> countPipeline = new ArrayList<>(filterStages);
    countPipeline.add(new org.bson.Document("$count", "count"));

    boolean countRequested =
        uriInfo.getCountOption() != null && uriInfo.getCountOption().getValue();

    return new QueryPlan(dataPipeline, countPipeline, countRequested);
  }
}
