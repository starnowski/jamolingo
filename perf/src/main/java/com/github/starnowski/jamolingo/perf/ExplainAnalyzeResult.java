package com.github.starnowski.jamolingo.perf;

import java.util.List;
import org.bson.conversions.Bson;

/**
 * Represents the result of an explain analyze operation, providing details about index usage and
 * matching stages.
 */
public interface ExplainAnalyzeResult {

  /**
   * Returns the index usage value (e.g., IXSCAN, COLLSCAN).
   *
   * @return the index usage value
   */
  HasIndexValue getIndexValue();

  /**
   * Returns the list of match stages derived from the index scan.
   *
   * <p><b>Note:</b> This method is experimental and its behavior might change in future releases.
   *
   * @return the list of match stages
   */
  List<Bson> getIndexMatchStages();

  /**
   * Returns the exception that occurred during the resolution of index match stages, if any.
   *
   * <p><b>Note:</b> This method is experimental and its behavior might change in future releases.
   *
   * @return the exception, or null if no exception occurred
   */
  Throwable getResolutionIndexMatchStagesException();

  /** Interface representing a value that has a string representation. */
  interface HasIndexValue {
    String getValue();
  }

  /** Standard representations of index usage values. */
  enum IndexValueRepresentation implements HasIndexValue {
    IXSCAN,
    FETCH_IXSCAN("FETCH + IXSCAN"),
    COLLSCAN,
    FETCH;

    private final String value;

    IndexValueRepresentation() {
      this(null);
    }

    IndexValueRepresentation(String value) {
      this.value = value;
    }

    @Override
    public String getValue() {
      return value == null ? name() : value;
    }
  }
}
