package com.github.starnowski.jamolingo.demo;

import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.operators.count.OdataCountToMongoCountParser;
import com.github.starnowski.jamolingo.core.operators.filter.ODataFilterToMongoMatchParser;
import com.github.starnowski.jamolingo.core.operators.orderby.OdataOrderByToMongoSortParser;
import com.github.starnowski.jamolingo.core.operators.select.OdataSelectToMongoProjectParser;
import com.github.starnowski.jamolingo.core.operators.skip.OdataSkipToMongoSkipParser;
import com.github.starnowski.jamolingo.core.operators.top.OdataTopToMongoLimitParser;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ODataQueryService {

  @Autowired private Edm edm;

  @Autowired private DefaultEdmMongoContextFacade edmMongoContextFacade;

  private final ODataFilterToMongoMatchParser filterParser = new ODataFilterToMongoMatchParser();
  private final OdataSelectToMongoProjectParser selectParser =
      new OdataSelectToMongoProjectParser();
  private final OdataOrderByToMongoSortParser orderByParser = new OdataOrderByToMongoSortParser();
  private final OdataTopToMongoLimitParser topParser = new OdataTopToMongoLimitParser();
  private final OdataSkipToMongoSkipParser skipParser = new OdataSkipToMongoSkipParser();
  private final OdataCountToMongoCountParser countParser = new OdataCountToMongoCountParser();

  public List<Bson> buildAggregationPipeline(String query)
      throws UriParserException,
          UriValidationException,
          ODataApplicationException,
          ExpressionVisitException {
    UriInfo uriInfo = new Parser(edm, OData.newInstance()).parseUri("examples2", query, null, null);
    List<Bson> pipeline = new ArrayList<>();

    // 1. $filter -> $match
    pipeline.addAll(
        filterParser.parse(uriInfo.getFilterOption(), edmMongoContextFacade).getStageObjects());

    // 2. $orderby -> $sort
    pipeline.addAll(
        orderByParser.parse(uriInfo.getOrderByOption(), edmMongoContextFacade).getStageObjects());

    // 3. $skip -> $skip
    pipeline.addAll(skipParser.parse(uriInfo.getSkipOption()).getStageObjects());

    // 4. $top -> $limit
    pipeline.addAll(topParser.parse(uriInfo.getTopOption()).getStageObjects());

    // 5. $select -> $project
    pipeline.addAll(
        selectParser.parse(uriInfo.getSelectOption(), edmMongoContextFacade).getStageObjects());

    // 6. $count -> $count
    pipeline.addAll(countParser.parse(uriInfo.getCountOption()).getStageObjects());

    return pipeline;
  }
}
