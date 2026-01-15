package com.github.starnowski.jamolingo.context;

import java.util.Objects;

/** Represents an immutable record of a circular reference mapping configuration. */
public class CircularReferenceMappingRecord {
  /**
   * Constructs a new CircularReferenceMappingRecord.
   *
   * @param anchorEdmPath the EDM path where recursion re-anchors
   * @param maxDepth the maximum allowed depth for recursion
   * @param strategy the strategy to use for handling the circular reference
   */
  public CircularReferenceMappingRecord(
      String anchorEdmPath, Integer maxDepth, CircularStrategy strategy) {
    this.anchorEdmPath = anchorEdmPath;
    this.maxDepth = maxDepth;
    this.strategy = strategy;
  }

  /**
   * Creates a new builder for CircularReferenceMappingRecord.
   *
   * @return a new builder instance
   */
  public static CircularReferenceMappingRecordBuilder builder() {
    return new CircularReferenceMappingRecordBuilder();
  }

  /** Builder for CircularReferenceMappingRecord. */
  public static final class CircularReferenceMappingRecordBuilder {
    /** Resolution strategy */
    private CircularStrategy strategy;

    /** EDM path where recursion re-anchors Example: "Item.Addresses" */
    private String anchorEdmPath;

    /** Max allowed depth (optional) */
    private Integer maxDepth;

    /**
     * Sets the resolution strategy.
     *
     * @param strategy the strategy
     * @return the builder instance
     */
    public CircularReferenceMappingRecordBuilder withStrategy(CircularStrategy strategy) {
      this.strategy = strategy;
      return this;
    }

    /**
     * Sets the anchor EDM path.
     *
     * @param anchorEdmPath the EDM path
     * @return the builder instance
     */
    public CircularReferenceMappingRecordBuilder withAnchorEdmPath(String anchorEdmPath) {
      this.anchorEdmPath = anchorEdmPath;
      return this;
    }

    /**
     * Sets the maximum allowed depth.
     *
     * @param maxDepth the maximum depth
     * @return the builder instance
     */
    public CircularReferenceMappingRecordBuilder withMaxDepth(Integer maxDepth) {
      this.maxDepth = maxDepth;
      return this;
    }

    /**
     * Builds the CircularReferenceMappingRecord.
     *
     * @return the new record
     */
    public CircularReferenceMappingRecord build() {
      return new CircularReferenceMappingRecord(anchorEdmPath, maxDepth, strategy);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    CircularReferenceMappingRecord that = (CircularReferenceMappingRecord) o;
    return Objects.equals(anchorEdmPath, that.anchorEdmPath)
        && Objects.equals(maxDepth, that.maxDepth)
        && strategy == that.strategy;
  }

  @Override
  public String toString() {
    return "CircularReferenceMappingRecord{"
        + "anchorEdmPath='"
        + anchorEdmPath
        + "'"
        + ", maxDepth="
        + maxDepth
        + ", strategy="
        + strategy
        + '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(anchorEdmPath, maxDepth, strategy);
  }

  /** EDM path where recursion re-anchors Example: "Item.Addresses" */
  private final String anchorEdmPath;

  /**
   * Returns the maximum allowed depth.
   *
   * @return the maximum depth
   */
  public Integer getMaxDepth() {
    return maxDepth;
  }

  /**
   * Returns the resolution strategy.
   *
   * @return the strategy
   */
  public CircularStrategy getStrategy() {
    return strategy;
  }

  /** Max allowed depth (optional) */
  private final Integer maxDepth;

  /** Resolution strategy */
  private final CircularStrategy strategy;

  /**
   * Returns the anchor EDM path.
   *
   * @return the anchor path
   */
  public String getAnchorEdmPath() {
    return anchorEdmPath;
  }
}
