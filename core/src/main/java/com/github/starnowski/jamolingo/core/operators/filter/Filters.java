package com.github.starnowski.jamolingo.core.operators.filter;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.bson.Document;
import org.bson.conversions.Bson;

/** Utility class for creating MongoDB filter documents. */
public class Filters {

  /**
   * Creates a filter that matches all documents where the value of the field is not equal to the
   * specified value.
   *
   * @param left the field name
   * @param right the value
   * @return the filter
   */
  public static Bson ne(String left, Object right) {

    return new Document(left, new Document("$ne", right));
  }

  /**
   * Creates a filter that matches all documents where the value of the field is greater than the
   * specified value.
   *
   * @param left the field name
   * @param right the value
   * @return the filter
   */
  public static Bson gt(String left, Object right) {
    return new Document(left, new Document("$gt", right));
  }

  /**
   * Creates a filter that matches all documents where the value of the field is greater than or
   * equal to the specified value.
   *
   * @param left the field name
   * @param right the value
   * @return the filter
   */
  public static Bson gte(String left, Object right) {

    return new Document(left, new Document("$gte", right));
  }

  /**
   * Creates a filter that matches all documents where the value of the field is less than the
   * specified value.
   *
   * @param left the field name
   * @param right the value
   * @return the filter
   */
  public static Bson lt(String left, Object right) {

    return new Document(left, new Document("$lt", right));
  }

  /**
   * Creates a filter that matches all documents where the value of the field is less than or equal
   * to the specified value.
   *
   * @param left the field name
   * @param right the value
   * @return the filter
   */
  public static Bson lte(String left, Object right) {
    return new Document(left, new Document("$lte", right));
  }

  /**
   * Creates a filter that performs a logical AND operation on two filters.
   *
   * @param left the first filter
   * @param right the second filter
   * @return the filter
   */
  public static Bson and(Bson left, Bson right) {
    return new Document("$and", Arrays.asList(left, right));
  }

  /**
   * Creates a filter that performs a logical OR operation on two filters.
   *
   * @param left the first filter
   * @param right the second filter
   * @return the filter
   */
  public static Bson or(Bson left, Bson right) {
    return new Document("$or", Arrays.asList(left, right));
  }

  /**
   * Creates a filter that performs a logical NOT operation on the specified filter.
   *
   * @param operand the filter
   * @return the filter
   */
  public static Bson not(Bson operand) {
    return new Document("$not", operand);
  }

  /**
   * Creates a filter that matches all documents where the value of the field is equal to any of the
   * specified values.
   *
   * @param field the field name
   * @param values the values
   * @return the filter
   */
  public static Bson in(String field, List<Object> values) {

    return new Document(field, new Document("$in", values));
  }

  /**
   * Creates a filter that matches all documents where the value of the field is equal to the
   * specified value.
   *
   * @param field the field name
   * @param value the value
   * @return the filter
   */
  public static Bson eq(String field, Object value) {
    return new Document(field, value);
  }

  /**
   * Creates a filter that matches all documents where the value of the field matches the specified
   * regular expression.
   *
   * @param field the field name
   * @param compile the regular expression pattern
   * @return the filter
   */
  public static Bson regex(String field, Pattern compile) {
    return new Document(field, new Document("$regex", compile.pattern()));
  }
}
