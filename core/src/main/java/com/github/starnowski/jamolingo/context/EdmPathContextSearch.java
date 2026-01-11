package com.github.starnowski.jamolingo.context;

/** Interface for configuring search parameters when resolving EDM paths to Mongo paths. */
public interface EdmPathContextSearch {

  /**
   * Returns the maximum depth for Mongo paths.
   *
   * @return the maximum depth, or null if no limit
   */
  Integer getMongoPathMaxDepth();

  /**
   * Returns the maximum circular limit per EDM path.
   *
   * @return the maximum circular limit, or null if no limit
   */
  Integer getMaxCircularLimitPerEdmPath();

  /**
   * Returns the maximum circular limit for all EDM paths combined.
   *
   * @return the maximum circular limit, or null if no limit
   */
  Integer getMaxCircularLimitForAllEdmPaths();
}
