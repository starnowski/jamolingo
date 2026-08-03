package com.github.starnowski.jamolingo.core.operators.expand;

import java.util.Objects;

public class GraphLookUpCleanUpInfo {

  private final boolean removeLocalKeyProperty;
  private final boolean removeForeignKeyProperty;

  public GraphLookUpCleanUpInfo(boolean removeLocalKeyProperty, boolean removeForeignKeyProperty) {
    this.removeLocalKeyProperty = removeLocalKeyProperty;
    this.removeForeignKeyProperty = removeForeignKeyProperty;
  }

  public boolean isRemoveLocalKeyProperty() {
    return removeLocalKeyProperty;
  }

  public boolean isRemoveForeignKeyProperty() {
    return removeForeignKeyProperty;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GraphLookUpCleanUpInfo that = (GraphLookUpCleanUpInfo) o;
    return removeLocalKeyProperty == that.removeLocalKeyProperty
        && removeForeignKeyProperty == that.removeForeignKeyProperty;
  }

  @Override
  public int hashCode() {
    return Objects.hash(removeLocalKeyProperty, removeForeignKeyProperty);
  }

  @Override
  public String toString() {
    return "GraphLookUpCleanUpInfo{"
        + "removeLocalKeyProperty="
        + removeLocalKeyProperty
        + ", removeForeignKeyProperty="
        + removeForeignKeyProperty
        + '}';
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private boolean removeLocalKeyProperty;
    private boolean removeForeignKeyProperty;

    public Builder withGraphLookUpCleanUpInfo(GraphLookUpCleanUpInfo graphLookUpCleanUpInfo) {
      if (graphLookUpCleanUpInfo != null) {
        this.removeLocalKeyProperty = graphLookUpCleanUpInfo.removeLocalKeyProperty;
        this.removeForeignKeyProperty = graphLookUpCleanUpInfo.removeForeignKeyProperty;
      }
      return this;
    }

    public Builder withRemoveLocalKeyProperty(boolean removeLocalKeyProperty) {
      this.removeLocalKeyProperty = removeLocalKeyProperty;
      return this;
    }

    public Builder withRemoveForeignKeyProperty(boolean removeForeignKeyProperty) {
      this.removeForeignKeyProperty = removeForeignKeyProperty;
      return this;
    }

    public GraphLookUpCleanUpInfo build() {
      return new GraphLookUpCleanUpInfo(removeLocalKeyProperty, removeForeignKeyProperty);
    }
  }
}
