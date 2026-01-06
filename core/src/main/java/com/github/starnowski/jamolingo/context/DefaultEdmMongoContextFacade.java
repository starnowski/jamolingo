package com.github.starnowski.jamolingo.context;

import java.util.stream.Collectors;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResourceProperty;

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
    if (entityPropertiesMongoPathContext == null) {
      String result =
          uriInfoResource.getUriResourceParts().stream()
              .map(p -> ((UriResourceProperty) p).getProperty().getName())
              .collect(Collectors.joining("."));
      return new InnerMongoPathResolution(result);
    } else {
      return null;
    }
  }

  private static final class InnerMongoPathResolution implements MongoPathResolution {

    public InnerMongoPathResolution(String mongoPath) {
      this.mongoPath = mongoPath;
    }

    private final String mongoPath;

    @Override
    public String getMongoPath() {
      return mongoPath;
    }
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
