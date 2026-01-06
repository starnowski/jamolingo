package com.github.starnowski.jamolingo.context;

import org.apache.olingo.server.api.uri.UriInfoResource;

public class DefaultEdmMongoContextFacade implements EdmMongoContextFacade {

  private final EntityPropertiesMongoPathContext entityPropertiesMongoPathContext;

  public DefaultEdmMongoContextFacade(
      EntityPropertiesMongoPathContext entityPropertiesMongoPathContext) {
    this.entityPropertiesMongoPathContext = entityPropertiesMongoPathContext;
  }

  public static DefaultEdmMongoContextFacadeBuilder builder() {
    return new DefaultEdmMongoContextFacadeBuilder();
  }

  @Override
  public MongoPathResolution resolveMongoPathForEDMPath(UriInfoResource uriInfoResource) {
    return null;
  }

  public static class DefaultEdmMongoContextFacadeBuilder {
    public DefaultEdmMongoContextFacadeBuilder withEntityPropertiesMongoPathContext(
        EntityPropertiesMongoPathContext entityPropertiesMongoPathContext) {
      this.entityPropertiesMongoPathContext = entityPropertiesMongoPathContext;
      return this;
    }

    private EntityPropertiesMongoPathContext entityPropertiesMongoPathContext;

    public DefaultEdmMongoContextFacadeBuilder withDefaultEdmMongoContextFacade(
        DefaultEdmMongoContextFacade defaultEdmMongoContextFacade) {
      this.entityPropertiesMongoPathContext =
          defaultEdmMongoContextFacade.entityPropertiesMongoPathContext;
      return this;
    }

    public DefaultEdmMongoContextFacade build() {
      return new DefaultEdmMongoContextFacade(entityPropertiesMongoPathContext);
    }
  }
}
