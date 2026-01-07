package com.github.starnowski.jamolingo.context;

import java.util.Objects;

public class CircularReferenceMapping {
  public CircularReferenceMapping withAnchorEdmPath(String anchorEdmPath) {
    this.anchorEdmPath = anchorEdmPath;
    return this;
  }

  public CircularReferenceMapping withMaxDepth(Integer maxDepth) {
    this.maxDepth = maxDepth;
    return this;
  }

  public CircularReferenceMapping withStrategy(CircularStrategy strategy) {
    this.strategy = strategy;
    return this;
  }

  public void setAnchorEdmPath(String anchorEdmPath) {
    this.anchorEdmPath = anchorEdmPath;
  }

  public void setMaxDepth(Integer maxDepth) {
    this.maxDepth = maxDepth;
  }

  public void setStrategy(CircularStrategy strategy) {
    this.strategy = strategy;
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
  private String anchorEdmPath;

  public Integer getMaxDepth() {
    return maxDepth;
  }

  public CircularStrategy getStrategy() {
    return strategy;
  }

  /** Max allowed depth (optional) */
  private Integer maxDepth;

  /** Resolution strategy */
  private CircularStrategy strategy;

  public String getAnchorEdmPath() {
    return anchorEdmPath;
  }
}
