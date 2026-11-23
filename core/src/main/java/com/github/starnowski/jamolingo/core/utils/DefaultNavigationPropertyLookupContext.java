package com.github.starnowski.jamolingo.core.utils;

import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Default immutable implementation of NavigationPropertyLookupContext. */
public class DefaultNavigationPropertyLookupContext implements NavigationPropertyLookupContext {

  private final String rootEdmEntityTypeName;
  private final EdmMongoContextFacade rootEdmMongoContextFacade;
  private final Map<String, EdmMongoContextFacade> edmTypeMapping;
  private final Map<KeyValue<String, String>, String> edmTablesToMongoDBCollections;

  /**
   * Constructs a new DefaultNavigationPropertyLookupContext.
   *
   * @param rootEdmEntityTypeName the root EDM entity type full name
   * @param rootEdmMongoContextFacade the root EDM mongo context facade
   * @param edmTypeMapping mapping between EDM entity full type names and their Mongo path resolvers
   * @param edmTablesToMongoDBCollections mapping between EDM entity type keys and their MongoDB
   *     collection names
   */
  public DefaultNavigationPropertyLookupContext(
      String rootEdmEntityTypeName,
      EdmMongoContextFacade rootEdmMongoContextFacade,
      Map<String, EdmMongoContextFacade> edmTypeMapping,
      Map<KeyValue<String, String>, String> edmTablesToMongoDBCollections) {
    this.rootEdmEntityTypeName = rootEdmEntityTypeName;
    this.rootEdmMongoContextFacade = rootEdmMongoContextFacade;
    this.edmTypeMapping = edmTypeMapping;
    this.edmTablesToMongoDBCollections = edmTablesToMongoDBCollections;
  }

  @Override
  public String getRootEdmEntityTypeName() {
    return rootEdmEntityTypeName;
  }

  @Override
  public EdmMongoContextFacade getRootEdmMongoContextFacade() {
    return rootEdmMongoContextFacade;
  }

  @Override
  public Map<String, EdmMongoContextFacade> getEdmTypeMapping() {
    return edmTypeMapping;
  }

  @Override
  public Map<KeyValue<String, String>, String> getEdmTablesToMongoDBCollections() {
    return edmTablesToMongoDBCollections;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DefaultNavigationPropertyLookupContext that = (DefaultNavigationPropertyLookupContext) o;
    return Objects.equals(rootEdmEntityTypeName, that.rootEdmEntityTypeName)
        && Objects.equals(rootEdmMongoContextFacade, that.rootEdmMongoContextFacade)
        && Objects.equals(edmTypeMapping, that.edmTypeMapping)
        && Objects.equals(edmTablesToMongoDBCollections, that.edmTablesToMongoDBCollections);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        rootEdmEntityTypeName,
        rootEdmMongoContextFacade,
        edmTypeMapping,
        edmTablesToMongoDBCollections);
  }

  @Override
  public String toString() {
    return "DefaultNavigationPropertyLookupContext{"
        + "rootEdmEntityTypeName='"
        + rootEdmEntityTypeName
        + '\''
        + ", rootEdmMongoContextFacade="
        + rootEdmMongoContextFacade
        + ", edmTypeMapping="
        + edmTypeMapping
        + ", edmTablesToMongoDBCollections="
        + edmTablesToMongoDBCollections
        + '}';
  }

  /**
   * Creates a new Builder for DefaultNavigationPropertyLookupContext.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for DefaultNavigationPropertyLookupContext. */
  public static class Builder {
    private String rootEdmEntityTypeName;
    private EdmMongoContextFacade rootEdmMongoContextFacade;
    private Map<String, EdmMongoContextFacade> edmTypeMapping = new HashMap<>();
    private Map<KeyValue<String, String>, String> edmTablesToMongoDBCollections = new HashMap<>();

    /**
     * Copies all properties from an existing DefaultNavigationPropertyLookupContext instance.
     *
     * @param context the instance to copy from
     * @return this builder
     */
    public Builder withDefaultNavigationPropertyLookupContext(
        DefaultNavigationPropertyLookupContext context) {
      this.rootEdmEntityTypeName = context.rootEdmEntityTypeName;
      this.rootEdmMongoContextFacade = context.rootEdmMongoContextFacade;
      this.edmTypeMapping = context.edmTypeMapping;
      this.edmTablesToMongoDBCollections = context.edmTablesToMongoDBCollections;
      return this;
    }

    /**
     * Sets the root EDM entity type full name.
     *
     * @param rootEdmEntityTypeName the root EDM entity type full name
     * @return this builder
     */
    public Builder withRootEdmEntityTypeName(String rootEdmEntityTypeName) {
      this.rootEdmEntityTypeName = rootEdmEntityTypeName;
      return this;
    }

    /**
     * Sets the root EDM mongo context facade.
     *
     * @param rootEdmMongoContextFacade the root EDM mongo context facade
     * @return this builder
     */
    public Builder withRootEdmMongoContextFacade(EdmMongoContextFacade rootEdmMongoContextFacade) {
      this.rootEdmMongoContextFacade = rootEdmMongoContextFacade;
      return this;
    }

    /**
     * Sets the mapping between EDM entity full type names and their Mongo path resolvers.
     *
     * @param edmTypeMapping the EDM type mapping
     * @return this builder
     */
    public Builder withEdmTypeMapping(Map<String, EdmMongoContextFacade> edmTypeMapping) {
      this.edmTypeMapping = edmTypeMapping;
      return this;
    }

    /**
     * Sets the mapping between EDM entity type keys and their MongoDB collection names.
     *
     * @param edmTablesToMongoDBCollections the collection mapping
     * @return this builder
     */
    public Builder withEdmTablesToMongoDBCollections(
        Map<KeyValue<String, String>, String> edmTablesToMongoDBCollections) {
      this.edmTablesToMongoDBCollections = edmTablesToMongoDBCollections;
      return this;
    }

    /**
     * Builds a new DefaultNavigationPropertyLookupContext instance.
     *
     * @return the created DefaultNavigationPropertyLookupContext
     */
    public DefaultNavigationPropertyLookupContext build() {
      return new DefaultNavigationPropertyLookupContext(
          rootEdmEntityTypeName,
          rootEdmMongoContextFacade,
          edmTypeMapping,
          edmTablesToMongoDBCollections);
    }
  }
}
