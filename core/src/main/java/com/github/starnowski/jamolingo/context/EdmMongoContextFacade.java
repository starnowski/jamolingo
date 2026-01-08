package com.github.starnowski.jamolingo.context;

import org.apache.olingo.server.api.uri.UriInfoResource;

public interface EdmMongoContextFacade {

  MongoPathResolution resolveMongoPathForEDMPath(UriInfoResource uriInfoResource);
}
