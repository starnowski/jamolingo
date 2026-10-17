package com.github.starnowski.jamolingo.core.operators.expand;

public class MissingPropertyExpandException extends ExpandException {

  private final String missingProperty;

  public MissingPropertyExpandException(String edmPath, String missingProperty, String message) {
    super(edmPath, message);
    this.missingProperty = missingProperty;
  }

  public String getMissingProperty() {
    return missingProperty;
  }
}
