package com.github.starnowski.jamolingo.core.operators.orderby;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DefaultOdataOrderByToMongoSortParserContext
    implements OdataOrderByToMongoSortParserContext {

  private final List<SortProperty> prependedSortProperties;

  public DefaultOdataOrderByToMongoSortParserContext(List<SortProperty> prependedSortProperties) {
    this.prependedSortProperties =
        prependedSortProperties != null ? prependedSortProperties : Collections.emptyList();
  }

  @Override
  public List<SortProperty> getPrependedSortProperties() {
    return prependedSortProperties;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DefaultOdataOrderByToMongoSortParserContext that =
        (DefaultOdataOrderByToMongoSortParserContext) o;
    return Objects.equals(prependedSortProperties, that.prependedSortProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(prependedSortProperties);
  }

  @Override
  public String toString() {
    return "DefaultOdataOrderByToMongoSortParserContext{"
        + "prependedSortProperties="
        + prependedSortProperties
        + '}';
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private List<SortProperty> prependedSortProperties = Collections.emptyList();

    public Builder withPrependedSortProperties(List<SortProperty> prependedSortProperties) {
      this.prependedSortProperties = prependedSortProperties;
      return this;
    }

    public DefaultOdataOrderByToMongoSortParserContext build() {
      return new DefaultOdataOrderByToMongoSortParserContext(prependedSortProperties);
    }
  }
}
