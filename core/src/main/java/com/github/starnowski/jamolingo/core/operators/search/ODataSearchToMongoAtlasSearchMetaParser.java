package com.github.starnowski.jamolingo.core.operators.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Parser responsible for converting OData search options into MongoDB Atlas Search $searchMeta
 * aggregation pipeline stages.
 */
public class ODataSearchToMongoAtlasSearchMetaParser
    implements ODataSearchToMongoTextSearchParser<
        ODataSearchToMongoAtlasSearchOptions, SearchOperatorResultForAtlasSearch> {

  private final SearchDocumentFactory searchDocumentFactory;

  /**
   * Constructs a new ODataSearchToMongoAtlasSearchMetaParser.
   *
   * @param searchDocumentFactory the factory to build searchMeta documents
   */
  public ODataSearchToMongoAtlasSearchMetaParser(SearchDocumentFactory searchDocumentFactory) {
    this.searchDocumentFactory = searchDocumentFactory;
  }

  @Override
  public SearchOperatorResultForAtlasSearch parse(SearchOption searchOption) {
    return parse(searchOption, null);
  }

  @Override
  public SearchOperatorResultForAtlasSearch parse(
      SearchOption searchOption, ODataSearchToMongoAtlasSearchOptions options) {
    List<Bson> searchStages = new ArrayList<>();
    Document searchMetaStage =
        new Document(
            "$searchMeta",
            searchDocumentFactory.build(searchOption.getSearchExpression(), options));
    searchStages.add(searchMetaStage);
    return new DefaultSearchOperatorResult(searchStages, searchStages, Collections.emptyList());
  }

  private static class DefaultSearchOperatorResult implements SearchOperatorResultForAtlasSearch {

    private final List<Bson> stageObjects;
    private final List<Bson> searchStages;
    private final List<Bson> scoreFilterStages;

    private DefaultSearchOperatorResult(
        List<Bson> stages, List<Bson> searchStages, List<Bson> scoreFilterStages) {
      this.stageObjects = Collections.unmodifiableList(stages);
      this.searchStages = Collections.unmodifiableList(searchStages);
      this.scoreFilterStages = Collections.unmodifiableList(scoreFilterStages);
    }

    @Override
    public List<Bson> getStageObjects() {
      return stageObjects;
    }

    @Override
    public List<Bson> getSearchStages() {
      return searchStages;
    }

    @Override
    public List<Bson> getScoreFilterStages() {
      return scoreFilterStages;
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
