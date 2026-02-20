package com.github.starnowski.jamolingo.core.operators.filter;

import java.util.HashMap;
import java.util.Map;

public class ODataMongoFunctionMapper {

  private static final Map<String, String> ZERO_ARGUMENT_FUNCTION_MAP = new HashMap<>();
  private static final Map<String, MappedFunction> ONE_ARGUMENT_FUNCTION_MAP = new HashMap<>();

  public record MappedFunction(String mappedFunction, boolean isResultBoolean) {}

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

  public static MappedFunction toOneArgumentMongoOperator(String odataFunction) {
    return ONE_ARGUMENT_FUNCTION_MAP.getOrDefault(odataFunction.toLowerCase(), null);
  }
}
