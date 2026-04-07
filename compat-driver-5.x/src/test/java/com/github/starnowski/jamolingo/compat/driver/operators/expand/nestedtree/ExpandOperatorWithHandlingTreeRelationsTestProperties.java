package com.github.starnowski.jamolingo.compat.driver.operators.expand.nestedtree;

import com.github.starnowski.jamolingo.common.beans.KeyValue;

public class ExpandOperatorWithHandlingTreeRelationsTestProperties {
  protected static final String CATEGORY_COLLECTION = "MyService.Category";
  protected static final String TREETYPE1_COLLECTION = "MyService.TreeType1";
  protected static final String TREETYPE2_COLLECTION = "MyService.TreeType2";
  protected static final String TREETYPE3_COLLECTION = "MyService.TreeType3";
  protected static final String TREETYPE4_COLLECTION = "MyService.TreeType4";
  protected static final KeyValue<String, String> TREETYPE1_MONGO_COLLECTION_USAGE_INFO =
      new KeyValue<>(TREETYPE1_COLLECTION, "treeType1s");
  protected static final KeyValue<String, String> TREETYPE2_MONGO_COLLECTION_USAGE_INFO =
      new KeyValue<>(TREETYPE2_COLLECTION, "treeType2s");
}
