package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchOptions;
import com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchParser;
import com.github.starnowski.jamolingo.core.operators.search.SearchOperatorResultForAtlasSearch;

public class ODataApplyToMongoAtlasSearchPipelineParser
    implements ApplySearchToMongoPipelineParser {

  private final ODataSearchToMongoAtlasSearchParser atlasSearchParser;
  private final ODataSearchToMongoAtlasSearchOptions options;

  public ODataApplyToMongoAtlasSearchPipelineParser(
      ODataSearchToMongoAtlasSearchParser atlasSearchParser) {
    this(atlasSearchParser, null);
  }

  public ODataApplyToMongoAtlasSearchPipelineParser(
      ODataSearchToMongoAtlasSearchParser atlasSearchParser,
      ODataSearchToMongoAtlasSearchOptions options) {
    this.atlasSearchParser = atlasSearchParser;
    this.options = options;
  }

  @Override
  public ApplyOperatorResult parse(SearchApplyItemContext context) {
    if (!context.isFirstApplyItem()) {
      throw new IllegalArgumentException("Atlas $search must be the first stage in the pipeline.");
    }
    SearchOperatorResultForAtlasSearch result =
        options != null
            ? atlasSearchParser.parse(context.getSearch().getSearchOption(), options)
            : atlasSearchParser.parse(context.getSearch().getSearchOption());
    return DefaultApplyOperatorResult.builder().withStageObjects(result.getStageObjects()).build();
  }
}
