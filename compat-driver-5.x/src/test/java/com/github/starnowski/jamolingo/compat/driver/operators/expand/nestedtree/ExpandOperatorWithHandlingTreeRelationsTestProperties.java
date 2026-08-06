package com.github.starnowski.jamolingo.compat.driver.operators.expand.nestedtree;

import com.github.starnowski.jamolingo.common.beans.KeyValue;

public class ExpandOperatorWithHandlingTreeRelationsTestProperties {
  public static final String CATEGORY_COLLECTION = "MyService.Category";
  public static final String TREETYPE1_COLLECTION = "MyService.TreeType1";
  public static final String TREETYPE2_COLLECTION = "MyService.TreeType2";
  public static final String TREETYPE3_COLLECTION = "MyService.TreeType3";
  public static final String TREETYPE4_COLLECTION = "MyService.TreeType4";
  public static final KeyValue<String, String> TREETYPE1_MONGO_COLLECTION_USAGE_INFO =
      new KeyValue<>(TREETYPE1_COLLECTION, "treeType1s");
  public static final KeyValue<String, String> TREETYPE2_MONGO_COLLECTION_USAGE_INFO =
      new KeyValue<>(TREETYPE2_COLLECTION, "treeType2s");
}
