package com.github.starnowski.jamolingo.core.operators.filter;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Filters {

    public static Bson ne(Object left, Object right) {
        return new Document("$ne", Arrays.asList(left, right));
    }

    public static Bson gt(Object left, Object right) {
        return new Document("$gt", Arrays.asList(left, right));
    }

    public static Bson gte(Object left, Object right) {
        return new Document("$gte", Arrays.asList(left, right));
    }

    public static Bson lt(Object left, Object right) {
        return new Document("$lt", Arrays.asList(left, right));
    }

    public static Bson lte(Object left, Object right) {
        return new Document("lte", Arrays.asList(left, right));
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
