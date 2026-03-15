package com.github.starnowski.jamolingo.core.operators.search;

import org.apache.olingo.server.api.uri.queryoption.search.SearchBinary;
import org.apache.olingo.server.api.uri.queryoption.search.SearchBinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.search.SearchExpression;
import org.apache.olingo.server.api.uri.queryoption.search.SearchTerm;
import org.apache.olingo.server.api.uri.queryoption.search.SearchUnary;
import org.apache.olingo.server.api.uri.queryoption.search.SearchUnaryOperatorKind;
import org.bson.conversions.Bson;

public abstract class SearchDocumentForQueryStringFactory implements SearchDocumentFactory {
  @Override
  public Bson build(
      SearchExpression searchExpression, ODataSearchToMongoAtlasSearchOptions options) {
    return build(searchExpression, pares(searchExpression), options);
  }

  private QueryStringParsingResult pares(SearchExpression searchExpression) {
    try {
      return new QueryStringParsingResult(
          parseSearchExpressionToString(searchExpression), true, null);
    } catch (Exception ex) {
      return new QueryStringParsingResult(null, false, ex);
    }
  }

  protected String parseSearchExpressionToString(SearchExpression searchExpression) {
    if (searchExpression instanceof SearchTerm) {
      return formatTerm(((SearchTerm) searchExpression).getSearchTerm());
    } else if (searchExpression instanceof SearchBinary) {
      SearchBinary binary = (SearchBinary) searchExpression;
      String left = parseSearchExpressionToString(binary.getLeftOperand());
      String right = parseSearchExpressionToString(binary.getRightOperand());

      if (binary.getLeftOperand() instanceof SearchBinary) {
        left = "(" + left + ")";
      }

      if (binary.getRightOperand() instanceof SearchBinary) {
        right = "(" + right + ")";
      }

      if (binary.getOperator() == SearchBinaryOperatorKind.AND
          && binary.getRightOperand() instanceof SearchUnary) {
        if (((SearchUnary) binary.getRightOperand()).getOperator() == SearchUnaryOperatorKind.NOT) {
          return left + " " + right;
        }
      }

      return left + " " + binary.getOperator().toString() + " " + right;
    } else if (searchExpression instanceof SearchUnary) {
      SearchUnary unary = (SearchUnary) searchExpression;
      String operand = parseSearchExpressionToString(unary.getOperand());
      if (unary.getOperand() instanceof SearchBinary) {
        operand = "(" + operand + ")";
      }
      return "NOT " + operand;
    }
    return "";
  }

  private String formatTerm(String term) {
    if (term.contains(" ") || term.equals("AND") || term.equals("OR") || term.equals("NOT")) {
      return "\"" + term.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
    return term;
  }

  public abstract Bson build(
      SearchExpression searchExpression,
      QueryStringParsingResult queryStringParsingResult,
      ODataSearchToMongoAtlasSearchOptions options);

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
