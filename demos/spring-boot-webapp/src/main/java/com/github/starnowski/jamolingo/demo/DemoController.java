package com.github.starnowski.jamolingo.demo;

import com.github.starnowski.jamolingo.perf.ExplainAnalyzeResult;
import com.github.starnowski.jamolingo.perf.ExplainAnalyzeResultFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

  @Autowired private ODataQueryService oDataQueryService;

  @Autowired private MongoTemplate mongoTemplate;

  @GetMapping("/query-with-dollar-parameters")
  public Map<String, Object> queryWithDollarParameterOperators(
          HttpServletRequest request)
          throws Exception {
    ODataQueryService.QueryPlan plan = oDataQueryService.buildQueryPlan(request.getQueryString());
    return executeQueryPlan(plan);
  }

  @GetMapping("/query")
  public Map<String, Object> query(
      @RequestParam(name = "filter", required = false) String filter,
      @RequestParam(name = "select", required = false) String select,
      @RequestParam(name = "orderby", required = false) String orderby,
      @RequestParam(name = "top", required = false) String top,
      @RequestParam(name = "skip", required = false) String skip,
      @RequestParam(name = "count", required = false) String count)
      throws Exception {
    String query = buildQueryString(filter, select, orderby, top, skip, count);

    ODataQueryService.QueryPlan plan = oDataQueryService.buildQueryPlan(query);
    return executeQueryPlan(plan);
  }

  @GetMapping("/query-index-check")
  public ResponseEntity<Object> queryWithIndexCheck(
      @RequestParam(name = "filter", required = false) String filter,
      @RequestParam(name = "select", required = false) String select,
      @RequestParam(name = "orderby", required = false) String orderby,
      @RequestParam(name = "top", required = false) String top,
      @RequestParam(name = "skip", required = false) String skip,
      @RequestParam(name = "count", required = false) String count)
      throws Exception {
    String query = buildQueryString(filter, select, orderby, top, skip, count);

    ODataQueryService.QueryPlan plan = oDataQueryService.buildQueryPlan(query);

    // Check index usage
    Document explainDoc =
        mongoTemplate.getCollection("items").aggregate(plan.getDataPipeline()).explain();
    ExplainAnalyzeResultFactory explainAnalyzeResultFactory = new ExplainAnalyzeResultFactory();
    ExplainAnalyzeResult explainResult = explainAnalyzeResultFactory.build(explainDoc);

    if (explainResult == null
        || explainResult.getIndexValue() == null
        || !(ExplainAnalyzeResult.IndexValueRepresentation.IXSCAN
                .getValue()
                .equals(explainResult.getIndexValue().getValue())
            || ExplainAnalyzeResult.IndexValueRepresentation.FETCH_IXSCAN
                .getValue()
                .equals(explainResult.getIndexValue().getValue()))) {
      return ResponseEntity.badRequest().body(Map.of("message", "No index used"));
    }

    return ResponseEntity.ok(executeQueryPlan(plan));
  }

  private String buildQueryString(
      String filter, String select, String orderby, String top, String skip, String count) {
    StringBuilder queryBuilder = new StringBuilder();
    if (filter != null) queryBuilder.append("$filter=").append(filter).append("&");
    if (select != null) queryBuilder.append("$select=").append(select).append("&");
    if (orderby != null) queryBuilder.append("$orderby=").append(orderby).append("&");
    if (top != null) queryBuilder.append("$top=").append(top).append("&");
    if (skip != null) queryBuilder.append("$skip=").append(skip).append("&");
    if (count != null) queryBuilder.append("$count=").append(count).append("&");

    String query = queryBuilder.toString();
    if (query.endsWith("&")) {
      query = query.substring(0, query.length() - 1);
    }
    return query;
  }

  private Map<String, Object> executeQueryPlan(ODataQueryService.QueryPlan plan) {
    Map<String, Object> response = new LinkedHashMap<>();

    if (plan.isCountRequested()) {
      List<Document> countResult = new ArrayList<>();
      mongoTemplate.getCollection("items").aggregate(plan.getCountPipeline()).into(countResult);
      long totalCount =
          countResult.isEmpty() ? 0 : ((Number) countResult.get(0).get("count")).longValue();
      response.put("@odata.count", totalCount);
    }

    List<Document> results = new ArrayList<>();
    mongoTemplate.getCollection("items").aggregate(plan.getDataPipeline()).into(results);
    response.put("value", results);
    return response;
  }
}
