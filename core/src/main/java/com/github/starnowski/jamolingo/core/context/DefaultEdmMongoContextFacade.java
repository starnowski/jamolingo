package com.github.starnowski.jamolingo.core.context;

import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import java.util.stream.Collectors;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResourceProperty;

/** Default implementation of {@link EdmMongoContextFacade}. */
public class DefaultEdmMongoContextFacade
    implements EdmMongoContextFacade, EdmPropertyMongoPathResolver {

  private final EntityPropertiesMongoPathContext entityPropertiesMongoPathContext;
  private final EdmPathContextSearch edmPathContextSearch;
  private final String rootMongoPath;

  /**
   * Constructs a new DefaultEdmMongoContextFacade.
   *
   * @param entityPropertiesMongoPathContext the context for resolving entity properties to Mongo
   *     paths
   * @param edmPathContextSearch configuration for searching EDM paths
   * @param rootMongoPath the root Mongo path to prefix to all resolved paths
   */
  public DefaultEdmMongoContextFacade(
      EntityPropertiesMongoPathContext entityPropertiesMongoPathContext,
      EdmPathContextSearch edmPathContextSearch,
      String rootMongoPath) {
    this.entityPropertiesMongoPathContext = entityPropertiesMongoPathContext;
    this.edmPathContextSearch = edmPathContextSearch;
    this.rootMongoPath = rootMongoPath;
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
    MongoPathResolution mongoPathResolution;
    if (entityPropertiesMongoPathContext == null) {
      String result = preparePath(uriInfoResource, ".");
      mongoPathResolution = new InnerMongoPathResolution(result);
    } else {
      String edmPath = preparePath(uriInfoResource, "/");
      mongoPathResolution =
          edmPathContextSearch == null
              ? entityPropertiesMongoPathContext.resolveMongoPathForEDMPath(edmPath)
              : entityPropertiesMongoPathContext.resolveMongoPathForEDMPath(
                  edmPath, edmPathContextSearch);
    }
    return wrapWithRootMongoPathIfNotNull(mongoPathResolution);
  }

  @Override
  public String getRootMongoPath() {
    return rootMongoPath;
  }

  private MongoPathResolution wrapWithRootMongoPathIfNotNull(
      MongoPathResolution mongoPathResolution) {
    if (rootMongoPath != null && mongoPathResolution != null) {
      return new RootMongoPathDecorator(rootMongoPath, mongoPathResolution);
    }
    return mongoPathResolution;
  }

  private static String preparePath(UriInfoResource uriInfoResource, String delimiter) {
    return uriInfoResource.getUriResourceParts().stream()
        .map(p -> ((UriResourceProperty) p).getProperty().getName())
        .collect(Collectors.joining(delimiter));
  }

  @Override
  public MongoPathResolution resolveMongoPathForEDMPath(String edmPath) {
    MongoPathResolution mongoPathResolution;
    if (entityPropertiesMongoPathContext == null) {
      mongoPathResolution = new InnerMongoPathResolution(edmPath.replace("/", "."));
    } else {
      mongoPathResolution =
          edmPathContextSearch == null
              ? entityPropertiesMongoPathContext.resolveMongoPathForEDMPath(edmPath)
              : entityPropertiesMongoPathContext.resolveMongoPathForEDMPath(
                  edmPath, edmPathContextSearch);
    }
    return wrapWithRootMongoPathIfNotNull(mongoPathResolution);
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

  private static final class RootMongoPathDecorator implements MongoPathResolution {
    private final String rootMongoPath;
    private final MongoPathResolution originalMongoPathResolution;

    public RootMongoPathDecorator(
        String rootMongoPath, MongoPathResolution originalMongoPathResolution) {
      this.rootMongoPath = rootMongoPath;
      this.originalMongoPathResolution = originalMongoPathResolution;
    }

    @Override
    public String getMongoPath() {
      return rootMongoPath + "." + originalMongoPathResolution.getMongoPath();
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
     * Sets the root Mongo path to prefix to all resolved paths.
     *
     * @param rootMongoPath the root Mongo path
     * @return the builder instance
     */
    public DefaultEdmMongoContextFacadeBuilder withRootMongoPath(String rootMongoPath) {
      this.rootMongoPath = rootMongoPath;
      return this;
    }

    private String rootMongoPath;

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
      this.rootMongoPath = defaultEdmMongoContextFacade.rootMongoPath;
      return this;
    }

    /**
     * Builds the DefaultEdmMongoContextFacade.
     *
     * @return the new facade instance
     */
    public DefaultEdmMongoContextFacade build() {
      return new DefaultEdmMongoContextFacade(
          entityPropertiesMongoPathContext, edmPathContextSearch, rootMongoPath);
    }
  }
}
