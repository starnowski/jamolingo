package com.github.starnowski.jamolingo.core.operators.apply;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/** Default implementation of {@link ApplyParserContext}. */
public class DefaultApplyParserContext implements ApplyParserContext {

  private final String rootMongoPath;
  private final Set<String> idProperties;
  private final String mongoConnectTo;
  private final String depthVariable;

  /**
   * Constructs a new DefaultApplyParserContext.
   *
   * @param rootMongoPath the root Mongo path prefix
   * @param idProperties the root document identifier property paths
   * @param mongoConnectTo the foreign key property name
   * @param depthVariable the recursion depth variable property name
   */
  public DefaultApplyParserContext(
      String rootMongoPath, Set<String> idProperties, String mongoConnectTo, String depthVariable) {
    this.rootMongoPath = rootMongoPath;
    this.idProperties =
        idProperties != null ? Collections.unmodifiableSet(idProperties) : Collections.emptySet();
    this.mongoConnectTo = mongoConnectTo;
    this.depthVariable = depthVariable;
  }

  @Override
  public String getRootMongoPath() {
    return rootMongoPath;
  }

  @Override
  public Set<String> getIdProperties() {
    return idProperties;
  }

  @Override
  public String getMongoConnectTo() {
    return mongoConnectTo;
  }

  @Override
  public String getDepthVariable() {
    return depthVariable;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DefaultApplyParserContext that = (DefaultApplyParserContext) o;
    return Objects.equals(rootMongoPath, that.rootMongoPath)
        && Objects.equals(idProperties, that.idProperties)
        && Objects.equals(mongoConnectTo, that.mongoConnectTo)
        && Objects.equals(depthVariable, that.depthVariable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rootMongoPath, idProperties, mongoConnectTo, depthVariable);
  }

  @Override
  public String toString() {
    return "DefaultApplyParserContext{"
        + "rootMongoPath='"
        + rootMongoPath
        + '\''
        + ", idProperties="
        + idProperties
        + ", mongoConnectTo='"
        + mongoConnectTo
        + '\''
        + ", depthVariable='"
        + depthVariable
        + '\''
        + '}';
  }

  /**
   * Creates a new builder for DefaultApplyParserContext.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for DefaultApplyParserContext. */
  public static class Builder {
    private String rootMongoPath;
    private Set<String> idProperties = Collections.emptySet();
    private String mongoConnectTo;
    private String depthVariable;

    /**
     * Copies values from an existing DefaultApplyParserContext.
     *
     * @param defaultApplyParserContext the existing context
     * @return the builder instance
     */
    public Builder withDefaultApplyParserContext(
        DefaultApplyParserContext defaultApplyParserContext) {
      this.rootMongoPath = defaultApplyParserContext.rootMongoPath;
      this.idProperties = defaultApplyParserContext.idProperties;
      this.mongoConnectTo = defaultApplyParserContext.mongoConnectTo;
      this.depthVariable = defaultApplyParserContext.depthVariable;
      return this;
    }

    /**
     * Sets the root Mongo path prefix.
     *
     * @param rootMongoPath the root Mongo path prefix
     * @return the builder instance
     */
    public Builder withRootMongoPath(String rootMongoPath) {
      this.rootMongoPath = rootMongoPath;
      return this;
    }

    /**
     * Sets the root document identifier property paths.
     *
     * @param idProperties the set of identifier property paths
     * @return the builder instance
     */
    public Builder withIdProperties(Set<String> idProperties) {
      this.idProperties = idProperties;
      return this;
    }

    /**
     * Sets the foreign key property name.
     *
     * @param mongoConnectTo the foreign key property name
     * @return the builder instance
     */
    public Builder withMongoConnectTo(String mongoConnectTo) {
      this.mongoConnectTo = mongoConnectTo;
      return this;
    }

    /**
     * Sets the recursion depth variable property name.
     *
     * @param depthVariable the depth variable property name
     * @return the builder instance
     */
    public Builder withDepthVariable(String depthVariable) {
      this.depthVariable = depthVariable;
      return this;
    }

    /**
     * Builds the DefaultApplyParserContext.
     *
     * @return a new DefaultApplyParserContext instance
     */
    public DefaultApplyParserContext build() {
      return new DefaultApplyParserContext(
          rootMongoPath, idProperties, mongoConnectTo, depthVariable);
    }
  }
}
