package com.github.starnowski.jamolingo.core.api;

import com.github.starnowski.jamolingo.context.MongoPathResolution;
import org.apache.olingo.server.api.uri.UriInfoResource;

/** Facade for resolving Mongo paths from EDM paths. */
public interface EdmMongoContextFacade {

  /**
   * Resolves the Mongo path for the given EDM path represented by {@link UriInfoResource}.
   *
   * @param uriInfoResource the URI info resource representing the EDM path
   * @return the resolved Mongo path resolution
   */
  MongoPathResolution resolveMongoPathForEDMPath(UriInfoResource uriInfoResource);
}
