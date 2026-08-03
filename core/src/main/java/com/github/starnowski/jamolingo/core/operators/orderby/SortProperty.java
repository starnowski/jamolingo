package com.github.starnowski.jamolingo.core.operators.orderby;

import java.util.Objects;

public class SortProperty {

  private final String propertyName;
  private final boolean descending;

  public SortProperty(String propertyName, boolean descending) {
    this.propertyName = propertyName;
    this.descending = descending;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public boolean isDescending() {
    return descending;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SortProperty that = (SortProperty) o;
    return descending == that.descending && Objects.equals(propertyName, that.propertyName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(propertyName, descending);
  }

  @Override
  public String toString() {
    return "SortProperty{"
        + "propertyName='"
        + propertyName
        + '\''
        + ", descending="
        + descending
        + '}';
  }
}
