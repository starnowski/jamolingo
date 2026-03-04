package com.github.starnowski.jamolingo.core.operators.filter;

/** Interface for converting OData literal values into BSON-compatible objects. */
public interface ODataToBsonConverter {

  /**
   * Converts a Java Object into the correct BSON-typed object based on the given OData EDM type.
   *
   * @param value The object value.
   * @param edmType The OData EDM type (e.g. "Edm.Int32", "Edm.Guid").
   * @return An Object suitable for MongoDB (e.g. Integer, Long, Boolean, Date, UUID, Binary).
   */
  Object toBsonValue(Object value, String edmType);
}
