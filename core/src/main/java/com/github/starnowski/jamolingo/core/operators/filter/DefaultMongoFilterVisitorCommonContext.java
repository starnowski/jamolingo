package com.github.starnowski.jamolingo.core.operators.filter;

import java.util.Objects;

public class DefaultMongoFilterVisitorCommonContext implements MongoFilterVisitorCommonContext {

  private final LiteralToBsonConverter literalToBsonConverter;
  private final ODataToBsonConverter oDataToBsonConverter;

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

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private LiteralToBsonConverter literalToBsonConverter = new DefaultLiteralToBsonConverter();
    private ODataToBsonConverter oDataToBsonConverter = new DefaultODataToBsonConverter();

    public Builder withDefaultMongoFilterVisitorCommonContext(
        DefaultMongoFilterVisitorCommonContext context) {
      this.literalToBsonConverter = context.literalToBsonConverter;
      this.oDataToBsonConverter = context.oDataToBsonConverter;
      return this;
    }

    public Builder withLiteralToBsonConverter(LiteralToBsonConverter literalToBsonConverter) {
      this.literalToBsonConverter = literalToBsonConverter;
      return this;
    }

    public Builder withODataToBsonConverter(ODataToBsonConverter oDataToBsonConverter) {
      this.oDataToBsonConverter = oDataToBsonConverter;
      return this;
    }

    public DefaultMongoFilterVisitorCommonContext build() {
      return new DefaultMongoFilterVisitorCommonContext(
          literalToBsonConverter, oDataToBsonConverter);
    }
  }
}
