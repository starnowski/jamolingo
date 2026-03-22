package com.github.starnowski.jamolingo.core.operators.expand;

import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;

public class ODataExpandToMongoAggregationPipelineParser {

  public ExpandOperatorResult parse(ExpandOption expandOption) {
    return parse(expandOption, DefaultExpandParserContext.builder().build());
  }

  public ExpandOperatorResult parse(
      ExpandOption expandOption, ExpandParserContext expandParserContext) {
    // TODO
    return null;
  }

  public static class DefaultExpandParserContext implements ExpandParserContext {
    private final Map<String, EdmPropertyMongoPathResolver> edmTypeMapping;
    private final Map<KeyValue<String, String>, KeyValue<String, String>>
        edmTablesToMongoDBCollections;

    public DefaultExpandParserContext(
        Map<String, EdmPropertyMongoPathResolver> edmTypeMapping,
        Map<KeyValue<String, String>, KeyValue<String, String>> edmTablesToMongoDBCollections) {
      this.edmTypeMapping = edmTypeMapping;
      this.edmTablesToMongoDBCollections = edmTablesToMongoDBCollections;
    }

    @Override
    public Map<String, EdmPropertyMongoPathResolver> getEDMTypeMapping() {
      return edmTypeMapping;
    }

    @Override
    public Map<KeyValue<String, String>, KeyValue<String, String>>
        getEDMTablesToMongoDBCollections() {
      return edmTablesToMongoDBCollections;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DefaultExpandParserContext that = (DefaultExpandParserContext) o;
      return Objects.equals(edmTypeMapping, that.edmTypeMapping)
          && Objects.equals(edmTablesToMongoDBCollections, that.edmTablesToMongoDBCollections);
    }

    @Override
    public int hashCode() {
      return Objects.hash(edmTypeMapping, edmTablesToMongoDBCollections);
    }

    @Override
    public String toString() {
      return "DefaultExpandParserContext{"
          + "edmTypeMapping="
          + edmTypeMapping
          + ", edmTablesToMongoDBCollections="
          + edmTablesToMongoDBCollections
          + '}';
    }

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private Map<String, EdmPropertyMongoPathResolver> edmTypeMapping = new HashMap<>();
      private Map<KeyValue<String, String>, KeyValue<String, String>>
          edmTablesToMongoDBCollections = new HashMap<>();

      public Builder withEdmTypeMapping(Map<String, EdmPropertyMongoPathResolver> edmTypeMapping) {
        this.edmTypeMapping = edmTypeMapping;
        return this;
      }

      public Builder withEdmTablesToMongoDBCollections(
          Map<KeyValue<String, String>, KeyValue<String, String>> edmTablesToMongoDBCollections) {
        this.edmTablesToMongoDBCollections = edmTablesToMongoDBCollections;
        return this;
      }

      public Builder withDefaultExpandParserContext(
          DefaultExpandParserContext defaultExpandParserContext) {
        this.edmTypeMapping =
            defaultExpandParserContext.edmTypeMapping != null
                ? new HashMap<>(defaultExpandParserContext.edmTypeMapping)
                : null;
        this.edmTablesToMongoDBCollections =
            defaultExpandParserContext.edmTablesToMongoDBCollections != null
                ? new HashMap<>(defaultExpandParserContext.edmTablesToMongoDBCollections)
                : null;
        return this;
      }

      public DefaultExpandParserContext build() {
        return new DefaultExpandParserContext(
            edmTypeMapping != null
                ? Collections.unmodifiableMap(new HashMap<>(edmTypeMapping))
                : null,
            edmTablesToMongoDBCollections != null
                ? Collections.unmodifiableMap(new HashMap<>(edmTablesToMongoDBCollections))
                : null);
      }
    }
  }
}
