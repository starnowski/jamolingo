package com.github.starnowski.jamolingo.demo;

import com.mongodb.client.MongoClient;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;

@Path("/query")
@Produces(MediaType.APPLICATION_JSON)
public class DemoResource {

  @Inject ODataQueryService oDataQueryService;

  @Inject MongoClient mongoClient;

  @GET
  public Map<String, Object> query(
      @QueryParam("filter") String filter,
      @QueryParam("select") String select,
      @QueryParam("orderby") String orderby,
      @QueryParam("top") String top,
      @QueryParam("skip") String skip,
      @QueryParam("count") String count)
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
      mongoClient
          .getDatabase("demos")
          .getCollection("items")
          .aggregate(plan.getCountPipeline())
          .into(countResult);
      long totalCount =
          countResult.isEmpty() ? 0 : ((Number) countResult.get(0).get("count")).longValue();
      response.put("@odata.count", totalCount);
    }

    List<Document> results = new ArrayList<>();
    mongoClient
        .getDatabase("demos")
        .getCollection("items")
        .aggregate(plan.getDataPipeline())
        .into(results);
    response.put("value", results);
    return response;
  }
}
