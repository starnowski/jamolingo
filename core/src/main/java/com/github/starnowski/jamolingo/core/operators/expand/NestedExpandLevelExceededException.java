package com.github.starnowski.jamolingo.core.operators.expand;

/** Exception thrown when the requested nested expand level exceeds the maximum allowed level. */
public class NestedExpandLevelExceededException extends ExpandException {

  private final int requestedLevel;
  private final int maxLevel;

  /**
   * Constructs a new NestedExpandLevelExceededException.
   *
   * @param edmPath the EDM path where the limit was exceeded
   * @param requestedLevel the requested nested expand level
   * @param maxLevel the maximum allowed nested expand level
   */
  public NestedExpandLevelExceededException(String edmPath, int requestedLevel, int maxLevel) {
    super(
        edmPath,
        String.format(
            "The requested nested expand level %d for path '%s' exceeds the maximum allowed level %d.",
            requestedLevel, edmPath, maxLevel));
    this.requestedLevel = requestedLevel;
    this.maxLevel = maxLevel;
  }

  /**
   * Returns the requested nested expand level that exceeded the limit.
   *
   * @return the requested level
   */
  public int getRequestedLevel() {
    return requestedLevel;
  }

  /**
   * Returns the maximum allowed nested expand level.
   *
   * @return the max allowed level
   */
  public int getMaxLevel() {
    return maxLevel;
  }
}
