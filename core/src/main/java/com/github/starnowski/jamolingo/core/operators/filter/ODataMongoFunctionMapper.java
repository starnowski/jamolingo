package com.github.starnowski.jamolingo.core.operators.filter;

import java.util.HashMap;
import java.util.Map;

/** Mapper for OData functions to MongoDB operators. */
public class ODataMongoFunctionMapper {

  private static final Map<String, String> ZERO_ARGUMENT_FUNCTION_MAP = new HashMap<>();
  private static final Map<String, MappedFunction> ONE_ARGUMENT_FUNCTION_MAP = new HashMap<>();

  /** Represents a function that has been mapped to a MongoDB operator. */
  public static class MappedFunction {

    private final String mappedFunction;
    private final boolean isResultBoolean;

    /**
     * Creates a new mapped function.
     *
     * @param mappedFunction the name of the MongoDB operator
     * @param isResultBoolean whether the operator returns a boolean result
     */
    public MappedFunction(String mappedFunction, boolean isResultBoolean) {
      this.mappedFunction = mappedFunction;
      this.isResultBoolean = isResultBoolean;
    }

    /**
     * Returns the name of the MongoDB operator.
     *
     * @return the operator name
     */
    public String getMappedFunction() {
      return mappedFunction;
    }

    /**
     * Returns whether the operator returns a boolean result.
     *
     * @return true if the result is a boolean, false otherwise
     */
    public boolean isResultBoolean() {
      return isResultBoolean;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      MappedFunction that = (MappedFunction) o;
      return isResultBoolean == that.isResultBoolean
          && java.util.Objects.equals(mappedFunction, that.mappedFunction);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(mappedFunction, isResultBoolean);
    }

    @Override
    public String toString() {
      return "MappedFunction{"
          + "mappedFunction='"
          + mappedFunction
          + '\''
          + ", isResultBoolean="
          + isResultBoolean
          + '}';
    }

    /**
     * Creates a new builder for MappedFunction.
     *
     * @return the builder
     */
    public static Builder builder() {
      return new Builder();
    }

    /** Builder for MappedFunction. */
    public static class Builder {

      private String mappedFunction;
      private boolean isResultBoolean;

      /**
       * Sets the name of the MongoDB operator.
       *
       * @param mappedFunction the operator name
       * @return the builder
       */
      public Builder withMappedFunction(String mappedFunction) {
        this.mappedFunction = mappedFunction;
        return this;
      }

      /**
       * Sets whether the operator returns a boolean result.
       *
       * @param isResultBoolean whether the result is a boolean
       * @return the builder
       */
      public Builder withIsResultBoolean(boolean isResultBoolean) {
        this.isResultBoolean = isResultBoolean;
        return this;
      }

      /**
       * Builds the MappedFunction.
       *
       * @return the mapped function
       */
      public MappedFunction build() {
        return new MappedFunction(mappedFunction, isResultBoolean);
      }
    }
  }

  /**
   * Helper method for creating a MappedFunction.
   *
   * @param mappedFunction the name of the MongoDB operator
   * @param isResultBoolean whether the operator returns a boolean result
   * @return the mapped function
   */
  public static MappedFunction mf(String mappedFunction, boolean isResultBoolean) {
    return new MappedFunction(mappedFunction, isResultBoolean);
  }

  static {
    // String functions
    ONE_ARGUMENT_FUNCTION_MAP.put("length", mf("$strLenCP", false));
    ONE_ARGUMENT_FUNCTION_MAP.put("tolower", mf("$toLower", false));
    ONE_ARGUMENT_FUNCTION_MAP.put("toupper", mf("$toUpper", false));
    //        ONE_ARGUMENT_FUNCTION_MAP.put("trim", "$trim");

    // Date/Time functions
    ONE_ARGUMENT_FUNCTION_MAP.put("year", mf("$year", false));
    ONE_ARGUMENT_FUNCTION_MAP.put("month", mf("$month", false));
    ONE_ARGUMENT_FUNCTION_MAP.put("day", mf("$dayOfMonth", false));
    ONE_ARGUMENT_FUNCTION_MAP.put("hour", mf("$hour", false));
    ONE_ARGUMENT_FUNCTION_MAP.put("minute", mf("$minute", false));
    ONE_ARGUMENT_FUNCTION_MAP.put("second", mf("$second", false));

    // Math
    ONE_ARGUMENT_FUNCTION_MAP.put("round", mf("$round", false));
    ONE_ARGUMENT_FUNCTION_MAP.put("floor", mf("$floor", false));
    ONE_ARGUMENT_FUNCTION_MAP.put("ceiling", mf("$ceil", false));

    // Date constants
    ZERO_ARGUMENT_FUNCTION_MAP.put("mindatetime", "ISODate(\"0001-01-01T00:00:00Z\")");
    ZERO_ARGUMENT_FUNCTION_MAP.put("maxdatetime", "ISODate(\"9999-12-31T23:59:59.999Z\")");
    ZERO_ARGUMENT_FUNCTION_MAP.put("now", "$$NOW");
  }

  /**
   * Returns the mapped function for the specified OData function name.
   *
   * @param odataFunction the OData function name
   * @return the mapped function, or null if not found
   */
  public static MappedFunction toOneArgumentMongoOperator(String odataFunction) {
    return ONE_ARGUMENT_FUNCTION_MAP.getOrDefault(odataFunction.toLowerCase(), null);
  }
}
