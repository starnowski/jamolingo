package com.github.starnowski.jamolingo.core.operators.expand;

/**
 * Exception thrown when an $expand operator contains an inner $apply operator and is configured to
 * be handled by the $graphLookup stage.
 */
public class CannotHandleExpandWithApplyByGraphLookupException extends ExpandException {

  /**
   * Constructs a new CannotHandleExpandWithApplyByGraphLookupException with a default message.
   *
   * @param edmPath the EDM navigation path
   */
  public CannotHandleExpandWithApplyByGraphLookupException(String edmPath) {
    this(
        edmPath,
        String.format(
            "The $expand operator for navigation path '%s' cannot be handled by $graphLookup when inner $apply operator is present.",
            edmPath));
  }

  /**
   * Constructs a new CannotHandleExpandWithApplyByGraphLookupException.
   *
   * @param edmPath the EDM navigation path
   * @param message the detail message
   */
  public CannotHandleExpandWithApplyByGraphLookupException(String edmPath, String message) {
    super(edmPath, message);
  }
}
