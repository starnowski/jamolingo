package com.github.starnowski.jamolingo.core.operators.top;

import java.util.Collections;
import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Parses OData $top system query options and translates them into MongoDB $limit aggregation
 * stages.
 */
public class OdataTopToMongoLimitParser {

  /**
   * Parses the given TopOption.
   *
   * @param topOption the OData top option to parse
   * @return the result of the parsing containing the MongoDB limit stage
   */
  public TopOperatorResult parse(TopOption topOption) {
    if (topOption == null) {
      return new DefaultTopOperatorResult(0, false);
    }
    return new DefaultTopOperatorResult(topOption.getValue(), true);
  }

  private static class DefaultTopOperatorResult implements TopOperatorResult {

    private final int topValue;
    private final boolean present;

    private DefaultTopOperatorResult(int topValue, boolean present) {
      this.topValue = topValue;
      this.present = present;
    }

    @Override
    public int getTopValue() {
      return topValue;
    }

    @Override
    public List<Bson> getStageObjects() {
      if (!present) {
        return Collections.emptyList();
      }
      return Collections.singletonList(new Document("$limit", topValue));
    }

    @Override
    public List<String> getUsedMongoDocumentProperties() {
      return Collections.emptyList();
    }

    @Override
    public List<String> getWrittenMongoDocumentProperties() {
      return Collections.emptyList();
    }

    @Override
    public List<String> getAddedMongoDocumentProperties() {
      return Collections.emptyList();
    }

    @Override
    public List<String> getRemovedMongoDocumentProperties() {
      return Collections.emptyList();
    }

    @Override
    public boolean isDocumentShapeRedefined() {
      return false;
    }
  }
}
