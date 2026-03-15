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
      // We need to project the score to use it in $match
      // But $search in Atlas can also use 'score' in some ways?
      // Actually, to use { $meta: "searchScore" } in $match, it's NOT possible in Atlas Search
      // directly
      // unless it's projected first.
      // Wait, MongoDB docs say:
      // You can use $meta in $match only if it was already projected.
      // BUT for Atlas Search, we often use the 'score' field if we project it.

      // Re-reading prompt: "checks if value of '{ $meta: \"textScore\" }' is larger or equal"
      // This refers to standard MongoDB Text Search, but this parser is for Atlas Search.
      // For Atlas Search it is "searchScore".

      scoreFilterStages.add(
          new Document(
              "$match",
              new Document("score", new Document("$gte", options.getDefaultTextScore()))));
    }
    List<Bson> allStages = new ArrayList<>(searchStages);
    allStages.addAll(scoreFilterStages);
    return new DefaultSearchOperatorResult(allStages, searchStages, scoreFilterStages);
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
