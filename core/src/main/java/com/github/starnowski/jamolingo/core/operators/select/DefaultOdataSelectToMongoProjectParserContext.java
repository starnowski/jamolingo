package com.github.starnowski.jamolingo.core.operators.select;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DefaultOdataSelectToMongoProjectParserContext
    implements OdataSelectToMongoProjectParserContext {

  private final Set<String> additionalFields;

  public DefaultOdataSelectToMongoProjectParserContext(Set<String> additionalFields) {
    this.additionalFields =
        additionalFields != null
            ? Collections.unmodifiableSet(additionalFields)
            : Collections.emptySet();
  }

  @Override
  public Set<String> getAdditionalFields() {
    return additionalFields;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DefaultOdataSelectToMongoProjectParserContext that =
        (DefaultOdataSelectToMongoProjectParserContext) o;
    return Objects.equals(additionalFields, that.additionalFields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(additionalFields);
  }

  @Override
  public String toString() {
    return "DefaultOdataSelectToMongoProjectParserContext{"
        + "additionalFields="
        + additionalFields
        + '}';
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> additionalFields = Collections.emptySet();

    public Builder withAdditionalFields(Set<String> additionalFields) {
      this.additionalFields = additionalFields != null
              ? Collections.unmodifiableSet(additionalFields)
              : Collections.emptySet();
      return this;
    }

    public Builder appendAdditionalFields(Set<String> additionalFields) {
      if (additionalFields == null) {
        return this;
      }
      Set<String> tmp = new HashSet<>();
      tmp.addAll(this.additionalFields);
      tmp.addAll(additionalFields);
      this.additionalFields = Collections.unmodifiableSet(tmp);
      return this;
    }

    public DefaultOdataSelectToMongoProjectParserContext build() {
      return new DefaultOdataSelectToMongoProjectParserContext(additionalFields);
    }
  }
}
