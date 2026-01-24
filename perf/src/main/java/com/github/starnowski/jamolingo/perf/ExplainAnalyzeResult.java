package com.github.starnowski.jamolingo.perf;

import java.util.List;
import org.bson.conversions.Bson;

public interface ExplainAnalyzeResult {

  HasIndexValue getIndexValue();

  List<Bson> getIndexMatchStages();

  Throwable getException();

  interface HasIndexValue {
    String getValue();
  }

  enum IndexValueRepresentation implements HasIndexValue {
    IXSCAN,
    FETCH_IXSCAN("FETCH + IXSCAN"),
    COLLSCAN,
    FETCH;

    private final String value;

    IndexValueRepresentation() {
      this(null);
    }

    IndexValueRepresentation(String value) {
      this.value = value;
    }

    @Override
    public String getValue() {
      return value == null ? name() : value;
    }
  }
}
