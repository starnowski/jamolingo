package com.github.starnowski.jamolingo.context;

import java.util.stream.Collectors;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResourceProperty;

public class DefaultEdmMongoContextFacade implements EdmMongoContextFacade {

  private final EntityPropertiesMongoPathContext entityPropertiesMongoPathContext;
  private final EdmPathContextSearch edmPathContextSearch;

  public DefaultEdmMongoContextFacade(
      EntityPropertiesMongoPathContext entityPropertiesMongoPathContext,
      EdmPathContextSearch edmPathContextSearch) {
    this.entityPropertiesMongoPathContext = entityPropertiesMongoPathContext;
    this.edmPathContextSearch = edmPathContextSearch;
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

    public DefaultEdmMongoContextFacadeBuilder withEdmPathContextSearch(
        EdmPathContextSearch edmPathContextSearch) {
      this.edmPathContextSearch = edmPathContextSearch;
      return this;
    }

    private EdmPathContextSearch edmPathContextSearch;

    public DefaultEdmMongoContextFacadeBuilder withDefaultEdmMongoContextFacade(
        DefaultEdmMongoContextFacade defaultEdmMongoContextFacade) {
      this.entityPropertiesMongoPathContext =
          defaultEdmMongoContextFacade.entityPropertiesMongoPathContext;
      this.edmPathContextSearch = defaultEdmMongoContextFacade.edmPathContextSearch;
      return this;
    }

    public DefaultEdmMongoContextFacade build() {
      return new DefaultEdmMongoContextFacade(
          entityPropertiesMongoPathContext, edmPathContextSearch);
    }
  }
}
