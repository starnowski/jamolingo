package com.github.starnowski.jamolingo.core.operators.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.bson.Document;
import org.bson.conversions.Bson;

public class ODataSearchToMongoAtlasSearchParser
    implements ODataSearchToMongoTextSearchParser<
        ODataSearchToMongoAtlasSearchOptions, SearchOperatorResultForAtlasSearch> {

  public static final String SEARCH_SCORE_DEFAULT_VARIABLE = "jamolingo_search_score";
  private final SearchDocumentFactory searchDocumentFactory;

  public ODataSearchToMongoAtlasSearchParser(SearchDocumentFactory searchDocumentFactory) {
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
    List<Bson> scoreFilterStages = new ArrayList<>();
    Document searchStage =
        new Document(
            "$search", searchDocumentFactory.build(searchOption.getSearchExpression(), options));
    searchStages.add(searchStage);
    if (options != null && options.getDefaultTextScore() != null) {
      searchStage
          .get("$search", Document.class)
          .append("scoreDetails", true); // Optional, but can be useful

      String scoreFieldName = options.getScoreFieldName();
      scoreFilterStages.add(
          new Document("$set", new Document(scoreFieldName, new Document("$meta", "searchScore"))));
      scoreFilterStages.add(
          new Document(
              "$match",
              new Document(scoreFieldName, new Document("$gte", options.getDefaultTextScore()))));
    }
    List<Bson> allStages = new ArrayList<>(searchStages);
    allStages.addAll(scoreFilterStages);
    return new DefaultSearchOperatorResult(allStages, searchStages, scoreFilterStages, options);
  }

  private static class DefaultSearchOperatorResult implements SearchOperatorResultForAtlasSearch {

    private final List<Bson> stageObjects;
    private final List<Bson> searchStages;
    private final List<Bson> scoreFilterStages;
    private final ODataSearchToMongoAtlasSearchOptions options;

    private DefaultSearchOperatorResult(
        List<Bson> stages,
        List<Bson> searchStages,
        List<Bson> scoreFilterStages,
        ODataSearchToMongoAtlasSearchOptions options) {
      this.stageObjects = Collections.unmodifiableList(stages);
      this.searchStages = Collections.unmodifiableList(searchStages);
      this.scoreFilterStages = Collections.unmodifiableList(scoreFilterStages);
      this.options = options;
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
      if (options != null && options.getDefaultTextScore() != null) {
        return List.of(options.getScoreFieldName());
      }
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
