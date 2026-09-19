package com.github.starnowski.jamolingo.core.operators.expand;

public class MissingNavigationPropertyExpandException extends ExpandException {

  private final String missingNavigationProperty;

  public MissingNavigationPropertyExpandException(
      String edmPath, String missingNavigationProperty, String message) {
    super(edmPath, message);
    this.missingNavigationProperty = missingNavigationProperty;
  }

  public String getMissingNavigationProperty() {
    return missingNavigationProperty;
  }
}
