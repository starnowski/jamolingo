package com.github.starnowski.jamolingo.core.api;

import com.github.starnowski.jamolingo.core.context.MongoPathResolution;
import org.apache.olingo.server.api.uri.UriInfoResource;

public interface EdmPropertyMongoPathResolver {

    MongoPathResolution resolveMongoPathForEDMPath(String edmPath);
}
