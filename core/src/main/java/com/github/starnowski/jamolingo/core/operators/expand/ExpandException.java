package com.github.starnowski.jamolingo.core.operators.expand;

public class ExpandException extends RuntimeException {

  private final String edmPath;

  public ExpandException(String edmPath, String message) {
    super(message);
    this.edmPath = edmPath;
  }

  public String getEdmPath() {
    return edmPath;
  }
}
