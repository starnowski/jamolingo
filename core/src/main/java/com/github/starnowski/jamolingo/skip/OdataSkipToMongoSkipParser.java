package com.github.starnowski.jamolingo.skip;

import java.util.Collections;
import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Parses OData $skip system query options and translates them into MongoDB $skip aggregation
 * stages.
 */
public class OdataSkipToMongoSkipParser {

  /**
   * Parses the given SkipOption.
   *
   * @param skipOption the OData skip option to parse
   * @return the result of the parsing containing the MongoDB skip stage
   */
  public SkipOperatorResult parse(SkipOption skipOption) {
    if (skipOption == null) {
      return new DefaultSkipOperatorResult(0, false);
    }
    return new DefaultSkipOperatorResult(skipOption.getValue(), true);
  }

  private static class DefaultSkipOperatorResult implements SkipOperatorResult {

    private final int skipValue;
    private final boolean present;

    private DefaultSkipOperatorResult(int skipValue, boolean present) {
      this.skipValue = skipValue;
      this.present = present;
    }

    @Override
    public int getSkipValue() {
      return skipValue;
    }

    @Override
    public List<Bson> getStageObjects() {
      if (!present) {
        return Collections.emptyList();
      }
      return Collections.singletonList(new Document("$skip", skipValue));
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
