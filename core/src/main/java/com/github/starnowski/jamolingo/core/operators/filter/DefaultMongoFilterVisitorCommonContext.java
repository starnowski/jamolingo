package com.github.starnowski.jamolingo.core.operators.filter;

import java.util.Objects;

/** Default implementation of the MongoFilterVisitorCommonContext. */
public class DefaultMongoFilterVisitorCommonContext implements MongoFilterVisitorCommonContext {

  private final LiteralToBsonConverter literalToBsonConverter;
  private final ODataToBsonConverter oDataToBsonConverter;

  /**
   * Creates a new DefaultMongoFilterVisitorCommonContext.
   *
   * @param literalToBsonConverter the literal to BSON converter
   * @param oDataToBsonConverter the OData to BSON converter
   */
  public DefaultMongoFilterVisitorCommonContext(
      LiteralToBsonConverter literalToBsonConverter, ODataToBsonConverter oDataToBsonConverter) {
    this.literalToBsonConverter = literalToBsonConverter;
    this.oDataToBsonConverter = oDataToBsonConverter;
  }

  @Override
  public LiteralToBsonConverter literalToBsonConverter() {
    return literalToBsonConverter;
  }

  @Override
  public ODataToBsonConverter oDataToBsonConverter() {
    return oDataToBsonConverter;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DefaultMongoFilterVisitorCommonContext that = (DefaultMongoFilterVisitorCommonContext) o;
    return Objects.equals(literalToBsonConverter, that.literalToBsonConverter)
        && Objects.equals(oDataToBsonConverter, that.oDataToBsonConverter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(literalToBsonConverter, oDataToBsonConverter);
  }

  @Override
  public String toString() {
    return "DefaultMongoFilterVisitorCommonContext{"
        + "literalToBsonConverter="
        + literalToBsonConverter
        + ", oDataToBsonConverter="
        + oDataToBsonConverter
        + '}';
  }

  /**
   * Creates a new builder for DefaultMongoFilterVisitorCommonContext.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for DefaultMongoFilterVisitorCommonContext. */
  public static class Builder {
    private LiteralToBsonConverter literalToBsonConverter = new DefaultLiteralToBsonConverter();
    private ODataToBsonConverter oDataToBsonConverter = new DefaultODataToBsonConverter();

    /**
     * Initializes the builder with values from an existing context.
     *
     * @param context the context to copy values from
     * @return the builder
     */
    public Builder withDefaultMongoFilterVisitorCommonContext(
        DefaultMongoFilterVisitorCommonContext context) {
      this.literalToBsonConverter = context.literalToBsonConverter;
      this.oDataToBsonConverter = context.oDataToBsonConverter;
      return this;
    }

    /**
     * Sets the literal to BSON converter.
     *
     * @param literalToBsonConverter the converter
     * @return the builder
     */
    public Builder withLiteralToBsonConverter(LiteralToBsonConverter literalToBsonConverter) {
      this.literalToBsonConverter = literalToBsonConverter;
      return this;
    }

    /**
     * Sets the OData to BSON converter.
     *
     * @param oDataToBsonConverter the converter
     * @return the builder
     */
    public Builder withODataToBsonConverter(ODataToBsonConverter oDataToBsonConverter) {
      this.oDataToBsonConverter = oDataToBsonConverter;
      return this;
    }

    /**
     * Builds the DefaultMongoFilterVisitorCommonContext.
     *
     * @return the context
     */
    public DefaultMongoFilterVisitorCommonContext build() {
      return new DefaultMongoFilterVisitorCommonContext(
          literalToBsonConverter, oDataToBsonConverter);
    }
  }
}
