package com.github.starnowski.jamolingo.core.operators.expand;

import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import java.util.Map;

public interface ExpandParserContext {

  Integer DEFAULT_MAX_LEVEL = 5;

  Map<String, EdmPropertyMongoPathResolver> getEDMTypeMapping();

  Map<KeyValue<String, String>, String> getEDMTablesToMongoDBCollections();

  Integer getMaxLevel();
}
