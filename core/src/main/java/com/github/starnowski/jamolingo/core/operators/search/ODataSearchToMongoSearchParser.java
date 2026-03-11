package com.github.starnowski.jamolingo.core.operators.search;

import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.bson.Document;
import org.bson.conversions.Bson;

public class ODataSearchToMongoSearchParser {

  public SearchOperatorResult parse(
      SearchOption searchOption, SearchDocumentFactory searchDocumentFactory) {
    return new DefaultSearchOperatorResult(
        new Document("$search", searchDocumentFactory.build(searchOption.getSearchExpression())));
  }

  private static class DefaultSearchOperatorResult implements SearchOperatorResult {

    private final List<Bson> stageObjects;

    private DefaultSearchOperatorResult(Bson searchStage) {
      this.stageObjects = List.of(searchStage);
    }

    @Override
    public List<Bson> getStageObjects() {
      return stageObjects;
    }

    @Override
    public List<String> getUsedMongoDocumentProperties() {
      return List.of();
    }

    @Override
    public List<String> getWrittenMongoDocumentProperties() {
      return List.of();
    }

    @Override
    public List<String> getAddedMongoDocumentProperties() {
      // TODO $meta field is going to be added
      return List.of();
    }

    @Override
    public List<String> getRemovedMongoDocumentProperties() {
      return List.of();
    }

    @Override
    public boolean isDocumentShapeRedefined() {
      return false;
    }
  }
}
