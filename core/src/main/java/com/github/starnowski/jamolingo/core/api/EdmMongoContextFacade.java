package com.github.starnowski.jamolingo.core.api;

import com.github.starnowski.jamolingo.core.context.MongoPathResolution;
import org.apache.olingo.server.api.uri.UriInfoResource;

/** Facade for resolving Mongo paths from EDM paths. */
public interface EdmMongoContextFacade extends EdmPropertyMongoPathResolver {

  /**
   * Resolves the Mongo path for the given EDM path represented by {@link UriInfoResource}.
   *
   * @param uriInfoResource the URI info resource representing the EDM path
   * @return the resolved Mongo path resolution
   */
  MongoPathResolution resolveMongoPathForEDMPath(UriInfoResource uriInfoResource);

  /**
   * Returns the root Mongo path to prefix to all resolved paths.
   *
   * @return the root Mongo path
   */
  String getRootMongoPath();
}
