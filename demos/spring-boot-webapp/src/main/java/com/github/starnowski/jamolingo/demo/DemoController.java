package com.github.starnowski.jamolingo.demo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

  @Autowired private ODataQueryService oDataQueryService;

  @Autowired private MongoTemplate mongoTemplate;

  @GetMapping("/query")
  public Map<String, Object> query(
      @RequestParam(name = "filter", required = false) String filter,
      @RequestParam(name = "select", required = false) String select,
      @RequestParam(name = "orderby", required = false) String orderby,
      @RequestParam(name = "top", required = false) String top,
      @RequestParam(name = "skip", required = false) String skip,
      @RequestParam(name = "count", required = false) String count)
      throws Exception {
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

    ODataQueryService.QueryPlan plan = oDataQueryService.buildQueryPlan(query);
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
