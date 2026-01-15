package com.github.starnowski.jamolingo.context;

import java.util.Objects;

/** Represents a configuration for mapping circular references in EDM. */
public class CircularReferenceMapping {
  /**
   * Constructs a new CircularReferenceMapping.
   *
   * @param anchorEdmPath the EDM path where recursion re-anchors
   * @param maxDepth the maximum allowed depth for recursion
   * @param strategy the strategy to use for handling the circular reference
   */
  public CircularReferenceMapping(
      String anchorEdmPath, Integer maxDepth, CircularStrategy strategy) {
    this.anchorEdmPath = anchorEdmPath;
    this.maxDepth = maxDepth;
    this.strategy = strategy;
  }

  /** Default constructor. */
  public CircularReferenceMapping() {}

  /**
   * Creates a new builder for CircularReferenceMapping.
   *
   * @return a new builder instance
   */
  public static CircularReferenceMappingBuilder builder() {
    return new CircularReferenceMappingBuilder();
  }

  /** Builder for CircularReferenceMapping. */
  public static final class CircularReferenceMappingBuilder {
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
    public CircularReferenceMappingBuilder withStrategy(CircularStrategy strategy) {
      this.strategy = strategy;
      return this;
    }

    /**
     * Sets the anchor EDM path.
     *
     * @param anchorEdmPath the EDM path
     * @return the builder instance
     */
    public CircularReferenceMappingBuilder withAnchorEdmPath(String anchorEdmPath) {
      this.anchorEdmPath = anchorEdmPath;
      return this;
    }

    /**
     * Sets the maximum allowed depth.
     *
     * @param maxDepth the maximum depth
     * @return the builder instance
     */
    public CircularReferenceMappingBuilder withMaxDepth(Integer maxDepth) {
      this.maxDepth = maxDepth;
      return this;
    }

    /**
     * Builds the CircularReferenceMapping.
     *
     * @return the new mapping configuration
     */
    public CircularReferenceMapping build() {
      return new CircularReferenceMapping(anchorEdmPath, maxDepth, strategy);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    CircularReferenceMapping that = (CircularReferenceMapping) o;
    return Objects.equals(anchorEdmPath, that.anchorEdmPath)
        && Objects.equals(maxDepth, that.maxDepth)
        && strategy == that.strategy;
  }

  @Override
  public String toString() {
    return "CircularReferenceMapping{"
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
  private String anchorEdmPath;

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
  private Integer maxDepth;

  /** Resolution strategy */
  private CircularStrategy strategy;

  /**
   * Returns the anchor EDM path.
   *
   * @return the anchor path
   */
  public String getAnchorEdmPath() {
    return anchorEdmPath;
  }

  /**
   * Sets the anchor EDM path.
   *
   * @param anchorEdmPath the anchor path
   */
  public void setAnchorEdmPath(String anchorEdmPath) {
    this.anchorEdmPath = anchorEdmPath;
  }

  /**
   * Sets the maximum allowed depth.
   *
   * @param maxDepth the maximum depth
   */
  public void setMaxDepth(Integer maxDepth) {
    this.maxDepth = maxDepth;
  }

  /**
   * Sets the resolution strategy.
   *
   * @param strategy the strategy
   */
  public void setStrategy(CircularStrategy strategy) {
    this.strategy = strategy;
  }
}
