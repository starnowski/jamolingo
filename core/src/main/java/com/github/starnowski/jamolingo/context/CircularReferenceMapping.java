package com.github.starnowski.jamolingo.context;

import java.util.Objects;

public class CircularReferenceMapping {
  public CircularReferenceMapping(
      String anchorEdmPath, Integer maxDepth, CircularStrategy strategy) {
    this.anchorEdmPath = anchorEdmPath;
    this.maxDepth = maxDepth;
    this.strategy = strategy;
  }

  public static CircularReferenceMappingBuilder builder() {
    return new CircularReferenceMappingBuilder();
  }

  public static final class CircularReferenceMappingBuilder {
    /** Resolution strategy */
    private CircularStrategy strategy;

    /** EDM path where recursion re-anchors Example: "Item.Addresses" */
    private String anchorEdmPath;

    /** Max allowed depth (optional) */
    private Integer maxDepth;

    public CircularReferenceMappingBuilder withStrategy(CircularStrategy strategy) {
      this.strategy = strategy;
      return this;
    }

    public CircularReferenceMappingBuilder withAnchorEdmPath(String anchorEdmPath) {
      this.anchorEdmPath = anchorEdmPath;
      return this;
    }

    public CircularReferenceMappingBuilder withMaxDepth(Integer maxDepth) {
      this.maxDepth = maxDepth;
      return this;
    }

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
        + '\''
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

  public Integer getMaxDepth() {
    return maxDepth;
  }

  public CircularStrategy getStrategy() {
    return strategy;
  }

  /** Max allowed depth (optional) */
  private final Integer maxDepth;

  /** Resolution strategy */
  private final CircularStrategy strategy;

  public String getAnchorEdmPath() {
    return anchorEdmPath;
  }
}
