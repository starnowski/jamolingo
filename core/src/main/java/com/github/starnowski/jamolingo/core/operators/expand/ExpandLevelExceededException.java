package com.github.starnowski.jamolingo.core.operators.expand;

public class ExpandLevelExceededException extends ExpandException {

  private final int requestedLevel;
  private final int maxLevel;

  public ExpandLevelExceededException(String edmPath, int requestedLevel, int maxLevel) {
    super(
        edmPath,
        "Requested expand level for path '"
            + edmPath
            + "' is higher than allowed maximal level. Requested: "
            + requestedLevel
            + ", Max: "
            + maxLevel);
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
