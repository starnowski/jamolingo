package com.github.starnowski.jamolingo.core.operators.expand;

import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import java.util.Map;

public interface ExpandParserContext {

  Map<String, EdmPropertyMongoPathResolver> getEDMTypeMapping();

  Map<KeyValue<String, String>, KeyValue<String, String>> getEDMTablesToMongoDBCollections();
}
