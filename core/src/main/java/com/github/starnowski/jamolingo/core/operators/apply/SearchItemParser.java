package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;
import org.apache.olingo.server.api.uri.queryoption.apply.Search;

/** SearchItemParser type. */
public class SearchItemParser implements ApplyItemParser {

  private final ApplySearchToMongoPipelineParser searchParser;
  private final boolean isFirstApplyItem;

  /** SearchItemParser constructor. */
  public SearchItemParser(ApplySearchToMongoPipelineParser searchParser, boolean isFirstApplyItem) {
    this.searchParser = searchParser;
    this.isFirstApplyItem = isFirstApplyItem;
  }

  @Override
  public ApplyOperatorResult parse(
      ApplyItem applyItem, EdmPropertyMongoPathResolver edmMongoContextFacade) {
    if (searchParser == null) {
      throw new UnsupportedOperationException(
          "Search set transformation is not configured. Missing ApplySearchToMongoPipelineParser.");
    }

    Search search = (Search) applyItem;
    SearchApplyItemContext context =
        new SearchApplyItemContext() {
          @Override
          /** getSearch method. */
          public Search getSearch() {
            return search;
          }

          @Override
          /** isFirstApplyItem method. */
          public boolean isFirstApplyItem() {
            return isFirstApplyItem;
          }
        };

    return searchParser.parse(context);
  }
}
