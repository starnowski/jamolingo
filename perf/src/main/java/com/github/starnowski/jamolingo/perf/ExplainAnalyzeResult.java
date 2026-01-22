package com.github.starnowski.jamolingo.perf;

public interface ExplainAnalyzeResult {

  HasIndexValue getIndexValue();

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
