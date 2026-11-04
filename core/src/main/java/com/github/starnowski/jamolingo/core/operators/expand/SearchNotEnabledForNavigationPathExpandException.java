package com.github.starnowski.jamolingo.core.operators.expand;

/** Exception thrown when $search is used on a navigation path for which search is not enabled. */
public class SearchNotEnabledForNavigationPathExpandException extends ExpandException {

  /**
   * Constructs a new SearchNotEnabledForNavigationPathExpandException with a default message.
   *
   * @param edmPath the EDM navigation path for which $search is not enabled
   */
  public SearchNotEnabledForNavigationPathExpandException(String edmPath) {
    this(
        edmPath,
        String.format("The $search operator is not enabled for navigation path '%s'.", edmPath));
  }

  /**
   * Constructs a new SearchNotEnabledForNavigationPathExpandException.
   *
   * @param edmPath the EDM navigation path for which $search is not enabled
   * @param message the detail message
   */
  public SearchNotEnabledForNavigationPathExpandException(String edmPath, String message) {
    super(edmPath, message);
  }
}
