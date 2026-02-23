package com.github.starnowski.jamolingo.core.context;

import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import java.util.stream.Collectors;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResourceProperty;

/** Default implementation of {@link EdmMongoContextFacade}. */
public class DefaultEdmMongoContextFacade implements EdmMongoContextFacade, EdmPropertyMongoPathResolver {

  private final EntityPropertiesMongoPathContext entityPropertiesMongoPathContext;
  private final EdmPathContextSearch edmPathContextSearch;

  /**
   * Constructs a new DefaultEdmMongoContextFacade.
   *
   * @param entityPropertiesMongoPathContext the context for resolving entity properties to Mongo
   *     paths
   * @param edmPathContextSearch configuration for searching EDM paths
   */
  public DefaultEdmMongoContextFacade(
      EntityPropertiesMongoPathContext entityPropertiesMongoPathContext,
      EdmPathContextSearch edmPathContextSearch) {
    this.entityPropertiesMongoPathContext = entityPropertiesMongoPathContext;
    this.edmPathContextSearch = edmPathContextSearch;
  }

  /**
   * Creates a new builder for DefaultEdmMongoContextFacade.
   *
   * @return a new builder instance
   */
  public static DefaultEdmMongoContextFacadeBuilder builder() {
    return new DefaultEdmMongoContextFacadeBuilder();
  }

  @Override
  public MongoPathResolution resolveMongoPathForEDMPath(UriInfoResource uriInfoResource) {
    if (entityPropertiesMongoPathContext == null) {
      String result = preparePath(uriInfoResource, ".");
      return new InnerMongoPathResolution(result);
    } else {
      String edmPath = preparePath(uriInfoResource, "/");
      return edmPathContextSearch == null
          ? entityPropertiesMongoPathContext.resolveMongoPathForEDMPath(edmPath)
          : entityPropertiesMongoPathContext.resolveMongoPathForEDMPath(
              edmPath, edmPathContextSearch);
    }
  }

  private static String preparePath(UriInfoResource uriInfoResource, String delimiter) {
    return uriInfoResource.getUriResourceParts().stream()
        .map(p -> ((UriResourceProperty) p).getProperty().getName())
        .collect(Collectors.joining(delimiter));
  }

  @Override
  public MongoPathResolution resolveMongoPathForEDMPath(String edmPath) {
    //TODO
    if (entityPropertiesMongoPathContext == null) {
      return new InnerMongoPathResolution(edmPath.replace("/", "."));
    }
    return edmPathContextSearch == null
            ? entityPropertiesMongoPathContext.resolveMongoPathForEDMPath(edmPath)
            : entityPropertiesMongoPathContext.resolveMongoPathForEDMPath(
            edmPath, edmPathContextSearch);
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

  /** Builder for DefaultEdmMongoContextFacade. */
  public static class DefaultEdmMongoContextFacadeBuilder {
    /**
     * Sets the entity properties Mongo path context.
     *
     * @param entityPropertiesMongoPathContext the context
     * @return the builder instance
     */
    public DefaultEdmMongoContextFacadeBuilder withEntityPropertiesMongoPathContext(
        EntityPropertiesMongoPathContext entityPropertiesMongoPathContext) {
      this.entityPropertiesMongoPathContext = entityPropertiesMongoPathContext;
      return this;
    }

    private EntityPropertiesMongoPathContext entityPropertiesMongoPathContext;

    /**
     * Sets the EDM path context search configuration.
     *
     * @param edmPathContextSearch the configuration
     * @return the builder instance
     */
    public DefaultEdmMongoContextFacadeBuilder withEdmPathContextSearch(
        EdmPathContextSearch edmPathContextSearch) {
      this.edmPathContextSearch = edmPathContextSearch;
      return this;
    }

    private EdmPathContextSearch edmPathContextSearch;

    /**
     * Initializes the builder with values from an existing DefaultEdmMongoContextFacade.
     *
     * @param defaultEdmMongoContextFacade the existing instance
     * @return the builder instance
     */
    public DefaultEdmMongoContextFacadeBuilder withDefaultEdmMongoContextFacade(
        DefaultEdmMongoContextFacade defaultEdmMongoContextFacade) {
      this.entityPropertiesMongoPathContext =
          defaultEdmMongoContextFacade.entityPropertiesMongoPathContext;
      this.edmPathContextSearch = defaultEdmMongoContextFacade.edmPathContextSearch;
      return this;
    }

    /**
     * Builds the DefaultEdmMongoContextFacade.
     *
     * @return the new facade instance
     */
    public DefaultEdmMongoContextFacade build() {
      return new DefaultEdmMongoContextFacade(
          entityPropertiesMongoPathContext, edmPathContextSearch);
    }
  }
}
