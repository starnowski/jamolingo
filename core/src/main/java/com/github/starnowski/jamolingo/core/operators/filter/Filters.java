package com.github.starnowski.jamolingo.core.operators.filter;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.bson.Document;
import org.bson.conversions.Bson;

public class Filters {

  public static Bson ne(String left, Object right) {

    return new Document(left, new Document("$ne", right));
  }

  public static Bson gt(String left, Object right) {
    return new Document(left, new Document("$gt", right));
  }

  public static Bson gte(String left, Object right) {

    return new Document(left, new Document("$gte", right));
  }

  public static Bson lt(String left, Object right) {

    return new Document(left, new Document("$lt", right));
  }

  public static Bson lte(String left, Object right) {
    return new Document(left, new Document("$lte", right));
  }

  public static Bson and(Bson left, Bson right) {
    return new Document("$and", Arrays.asList(left, right));
  }

  public static Bson or(Bson left, Bson right) {
    return new Document("$or", Arrays.asList(left, right));
  }

  public static Bson not(Bson operand) {
    return new Document("$not", operand);
  }

  public static Bson in(String field, List<Object> values) {
    return new Document("$in", Arrays.asList(field, values));
  }

  public static Bson eq(String field, Object value) {
    return new Document(field, value);
  }

  public static Bson regex(String field, Pattern compile) {
    return new Document(field, new Document("$regex", compile.pattern()));
  }
}
