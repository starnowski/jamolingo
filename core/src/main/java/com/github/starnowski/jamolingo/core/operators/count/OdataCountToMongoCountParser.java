package com.github.starnowski.jamolingo.core.operators.count;

import java.util.Collections;
import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Parses OData $count system query options and translates them into MongoDB $count aggregation
 * stages.
 */
public class OdataCountToMongoCountParser {

  private static final String DEFAULT_COUNT_FIELD_NAME = "count";

  /**
   * Parses the given CountOption with the default count field name "count".
   *
   * @param countOption the OData count option to parse
   * @return the result of the parsing containing the MongoDB count stage
   */
  public CountOperatorResult parse(CountOption countOption) {
    return parse(countOption, DEFAULT_COUNT_FIELD_NAME);
  }

  /**
   * Parses the given CountOption with the provided count field name.
   *
   * @param countOption the OData count option to parse
   * @param countFieldName the name to use for the count field
   * @return the result of the parsing containing the MongoDB count stage
   */
  public CountOperatorResult parse(CountOption countOption, String countFieldName) {
    if (countOption == null || !countOption.getValue()) {
      return new DefaultCountOperatorResult(countFieldName, false);
    }
    return new DefaultCountOperatorResult(countFieldName, true);
  }

  private static class DefaultCountOperatorResult implements CountOperatorResult {

    private final String countFieldName;
    private final boolean present;

    private DefaultCountOperatorResult(String countFieldName, boolean present) {
      this.countFieldName = countFieldName;
      this.present = present;
    }

    @Override
    public String getCountFieldName() {
      return countFieldName;
    }

    @Override
    public boolean isCountOptionPresent() {
      return present;
    }

    @Override
    public List<Bson> getStageObjects() {
      if (!present) {
        return Collections.emptyList();
      }
      return Collections.singletonList(new Document("$count", countFieldName));
    }

    @Override
    public List<String> getUsedMongoDocumentProperties() {
      return Collections.emptyList();
    }

    @Override
    public List<String> getWrittenMongoDocumentProperties() {
      return present ? Collections.singletonList(countFieldName) : Collections.emptyList();
    }

    @Override
    public List<String> getAddedMongoDocumentProperties() {
      return present ? Collections.singletonList(countFieldName) : Collections.emptyList();
    }

    @Override
    public List<String> getRemovedMongoDocumentProperties() {
      // Since $count stage removes all original fields, this is true but maybe too much to list
      // every field
      // For now we follow the pattern of skip/top
      return Collections.emptyList();
    }

    @Override
    public boolean isDocumentShapeRedefined() {
      return present;
    }
  }
}
