package com.github.starnowski.jamolingo.core.operators.expand;

public class MissingNavigationPropertyExpandException extends MissingPropertyExpandException {

  public MissingNavigationPropertyExpandException(
      String edmPath, String missingNavigationProperty, String message) {
    super(edmPath, missingNavigationProperty, message);
  }

  public String getMissingNavigationProperty() {
    return getMissingProperty();
  }
}
