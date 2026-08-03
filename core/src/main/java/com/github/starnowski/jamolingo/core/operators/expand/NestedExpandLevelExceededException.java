package com.github.starnowski.jamolingo.core.operators.expand;

public class NestedExpandLevelExceededException extends ExpandException {

  private final int requestedLevel;
  private final int maxLevel;

  public NestedExpandLevelExceededException(String edmPath, int requestedLevel, int maxLevel) {
    super(
        edmPath,
        String.format(
            "The requested nested expand level %d for path '%s' exceeds the maximum allowed level %d.",
            requestedLevel, edmPath, maxLevel));
    this.requestedLevel = requestedLevel;
    this.maxLevel = maxLevel;
  }

  public int getRequestedLevel() {
    return requestedLevel;
  }

  public int getMaxLevel() {
    return maxLevel;
  }
}
