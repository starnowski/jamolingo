package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;

/** ApplyItemParser type. */
public interface ApplyItemParser {
  ApplyOperatorResult parse(
      ApplyItem applyItem, EdmPropertyMongoPathResolver edmMongoContextFacade);

  /**
   * Parses the given apply item using the provided path resolver and apply parser context.
   *
   * @param applyItem the apply item to parse
   * @param edmMongoContextFacade the path resolver
   * @param context the apply parser context
   * @return the apply operator result
   */
  default ApplyOperatorResult parse(
      ApplyItem applyItem,
      EdmPropertyMongoPathResolver edmMongoContextFacade,
      ApplyParserContext context) {
    return parse(applyItem, edmMongoContextFacade);
  }
}
