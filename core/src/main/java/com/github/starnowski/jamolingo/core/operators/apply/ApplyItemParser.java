package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import org.apache.olingo.server.api.uri.queryoption.ApplyItem;

/** ApplyItemParser type. */
public interface ApplyItemParser {
  ApplyOperatorResult parse(
      ApplyItem applyItem, EdmPropertyMongoPathResolver edmMongoContextFacade);
}
