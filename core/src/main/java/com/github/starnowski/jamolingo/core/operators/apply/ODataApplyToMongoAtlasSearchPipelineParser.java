package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchLookupParser;
import com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchOptions;
import com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchParser;
import com.github.starnowski.jamolingo.core.operators.search.SearchOperatorResultForAtlasSearch;

/** ODataApplyToMongoAtlasSearchPipelineParser type. */
public class ODataApplyToMongoAtlasSearchPipelineParser
    implements ApplySearchToMongoPipelineParser {

  private final ODataSearchToMongoAtlasSearchParser atlasSearchParser;
  private final ODataSearchToMongoAtlasSearchLookupParser lookupSearchParser;
  private final ODataSearchToMongoAtlasSearchOptions options;

  public ODataApplyToMongoAtlasSearchPipelineParser(
      ODataSearchToMongoAtlasSearchParser atlasSearchParser) {
    this(atlasSearchParser, null, null);
  }

  public ODataApplyToMongoAtlasSearchPipelineParser(
      ODataSearchToMongoAtlasSearchParser atlasSearchParser,
      ODataSearchToMongoAtlasSearchOptions options) {
    this(atlasSearchParser, null, options);
  }

  public ODataApplyToMongoAtlasSearchPipelineParser(
      ODataSearchToMongoAtlasSearchParser atlasSearchParser,
      ODataSearchToMongoAtlasSearchLookupParser lookupSearchParser) {
    this(atlasSearchParser, lookupSearchParser, null);
  }

  public ODataApplyToMongoAtlasSearchPipelineParser(
      ODataSearchToMongoAtlasSearchParser atlasSearchParser,
      ODataSearchToMongoAtlasSearchLookupParser lookupSearchParser,
      ODataSearchToMongoAtlasSearchOptions options) {
    this.atlasSearchParser = atlasSearchParser;
    this.lookupSearchParser = lookupSearchParser;
    this.options = options;
  }

  @Override
  /** parse method. */
  public ApplyOperatorResult parse(SearchApplyItemContext context) {
    ODataSearchToMongoAtlasSearchParser parserToUse;
    if (context.isFirstApplyItem()) {
      if (atlasSearchParser == null) {
        throw new IllegalArgumentException(
            "Atlas $search parser is not configured for the first stage in the pipeline.");
      }
      parserToUse = atlasSearchParser;
    } else {
      if (lookupSearchParser != null) {
        parserToUse = lookupSearchParser;
      } else if (atlasSearchParser instanceof ODataSearchToMongoAtlasSearchLookupParser) {
        parserToUse = atlasSearchParser;
      } else {
        throw new IllegalArgumentException(
            "Atlas $search must be the first stage in the pipeline.");
      }
    }
    SearchOperatorResultForAtlasSearch result =
        options != null
            ? parserToUse.parse(context.getSearch().getSearchOption(), options)
            : parserToUse.parse(context.getSearch().getSearchOption());
    return DefaultApplyOperatorResult.builder().withStageObjects(result.getStageObjects()).build();
  }
}
