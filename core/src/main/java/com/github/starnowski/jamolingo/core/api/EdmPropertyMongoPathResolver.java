package com.github.starnowski.jamolingo.core.api;

import com.github.starnowski.jamolingo.core.context.MongoPathResolution;

public interface EdmPropertyMongoPathResolver {

  MongoPathResolution resolveMongoPathForEDMPath(String edmPath);
}
