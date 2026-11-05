package com.github.starnowski.jamolingo.core.operators.apply;

import java.util.Set;

/** Interface representing parsing context for $apply operations in nested/graphLookup execution. */
public interface ApplyParserContext {

  /**
   * Returns the root Mongo path prefix (e.g. "children").
   *
   * @return the root Mongo path prefix
   */
  String getRootMongoPath();

  /**
   * Returns the root document identifier property paths.
   *
   * @return the set of identifier property paths
   */
  Set<String> getIdProperties();

  /**
   * Returns the foreign key property name (e.g. "parentId").
   *
   * @return the foreign key property name
   */
  String getMongoConnectTo();

  /**
   * Returns the recursion depth variable property name (e.g. "children___odata_depth").
   *
   * @return the depth variable property name
   */
  String getDepthVariable();
}
