package com.github.starnowski.jamolingo.core.operators.search;

import org.apache.olingo.server.api.uri.queryoption.search.SearchExpression;
import org.bson.conversions.Bson;

public abstract class SearchDocumentForQueryStringFactory implements SearchDocumentFactory {
  @Override
  public Bson build(SearchExpression searchExpression) {
    return build(searchExpression, pares(searchExpression));
  }

  private QueryStringParsingResult pares(SearchExpression searchExpression) {
    try {
      return new QueryStringParsingResult("", true, null);
    } catch (Exception ex) {
      return new QueryStringParsingResult(null, false, ex);
    }
  }

  protected String parseSearchExpressionToString(SearchExpression searchExpression) {
    // TODO
    return "";
  }

  public abstract Bson build(
      SearchExpression searchExpression, QueryStringParsingResult queryStringParsingResult);

  public static class QueryStringParsingResult {

    public String getQuery() {
      return query;
    }

    public boolean isSuccess() {
      return success;
    }

    private final String query;
    private final boolean success;

    public Exception getCause() {
      return cause;
    }

    private final Exception cause;

    public QueryStringParsingResult(String query, boolean success, Exception cause) {
      this.query = query;
      this.success = success;
      this.cause = cause;
    }
  }
}
