package com.github.starnowski.jamolingo.core.operators.filter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;
import org.bson.types.Binary;
import org.bson.types.Decimal128;

public class ODataToBsonConverter {

  /**
   * Converts a Java String into the correct BSON-typed object based on the given OData EDM type.
   *
   * @param value The string value.
   * @param edmType The OData EDM type (e.g. "Edm.Int32", "Edm.Guid").
   * @return An Object suitable for MongoDB (e.g. Integer, Long, Boolean, Date, UUID, Binary).
   */
  public static Object toBsonValue(String value, String edmType) {
    if (value == null || edmType == null) {
      return null;
    }

    switch (edmType) {
      case "Edm.String":
        return value;

      case "Edm.Boolean":
        return Boolean.parseBoolean(value);

      case "Edm.Int32":
        return Integer.valueOf(value);

      case "Edm.Int64":
        return Long.valueOf(value);

      case "Edm.Decimal":
        return Decimal128.parse(value);

      case "Edm.Single":
        return Float.valueOf(value);

      case "Edm.Double":
        return Double.valueOf(value);

      case "Edm.Guid":
        return UUID.fromString(value);

      case "Edm.Date":
        // Expecting yyyy-MM-dd
        LocalDate localDate = LocalDate.parse(value);
        return java.util.Date.from(localDate.atStartOfDay(ZoneOffset.UTC).toInstant());

      case "Edm.DateTimeOffset":
        // Expecting RFC3339/ISO 8601
        Instant instant = Instant.parse(value);
        return java.util.Date.from(instant);

      case "Edm.Binary":
        // Base64 encoded string
        return new Binary(Base64.getDecoder().decode(value));

      case "Edm.Stream":
        // For simplicity, store as raw binary
        return new Binary(value.getBytes(StandardCharsets.UTF_8));

        // TODO
        //            case "Edm.ComplexType":
        //                // Would require JSON -> Document mapping
        //                throw new UnsupportedOperationException("Complex types require JSON
        // parsing into Document");

      default:
        // Fallback: keep as string
        return value;
    }
  }
}
