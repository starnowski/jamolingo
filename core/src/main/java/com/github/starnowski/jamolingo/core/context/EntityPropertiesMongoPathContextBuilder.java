package com.github.starnowski.jamolingo.core.context;

import static com.github.starnowski.jamolingo.core.utils.Constants.ODATA_PATH_SEPARATOR_CHARACTER;

import com.github.starnowski.jamolingo.core.mapping.*;
import java.util.*;

/** Builder for creating {@link EntityPropertiesMongoPathContext} instances. */
public class EntityPropertiesMongoPathContextBuilder {

  /**
   * Builds an {@link EntityPropertiesMongoPathContext} from the given {@link EntityMapping} using
   * default resolver context.
   *
   * @param entityMapping the entity mapping configuration
   * @return the built context
   */
  public EntityPropertiesMongoPathContext build(EntityMapping entityMapping) {
    return build(entityMapping, new EntityPropertiesMongoPathResolverContext(false));
  }

  /**
   * Builds an {@link EntityPropertiesMongoPathContext} from the given {@link EntityMapping} using
   * specific resolver context.
   *
   * @param entityMapping the entity mapping configuration
   * @param entityPropertiesMongoPathResolverContext the resolver context
   * @return the built context
   */
  public EntityPropertiesMongoPathContext build(
      EntityMapping entityMapping,
      EntityPropertiesMongoPathResolverContext entityPropertiesMongoPathResolverContext) {
    return compile(entityMapping, entityPropertiesMongoPathResolverContext);
  }

  private EntityPropertiesMongoPathContext compile(
      EntityMapping entityMapping,
      EntityPropertiesMongoPathResolverContext entityPropertiesMongoPathResolverContext) {

    EntityPropertiesMongoPathContextOutPut out = new EntityPropertiesMongoPathContextOutPut();
    out.edmToMongoPath = new LinkedHashMap<>();

    String entityRoot = normalize(entityMapping.getRootPath());

    if (entityMapping.getProperties() != null) {
      for (Map.Entry<String, PropertyMapping> e : entityMapping.getProperties().entrySet()) {

        compileProperty(
            e.getKey(),
            e.getValue(),
            entityRoot,
            null,
            out,
            entityPropertiesMongoPathResolverContext);
      }
      // TODO Store properties the Set with names of edmPaths of properties, required for
      // ExpandAsterisk = true (all fields defined in EDM)
    }
    validateCircularReferences(out);
    return new DefaultEntityPropertiesMongoPathContext(out.edmToMongoPath);
  }

  private void validateCircularReferences(EntityPropertiesMongoPathContextOutPut out) {
    Map<String, MongoPathEntry> edmPathToMongoPropertyMapping = out.edmToMongoPath;
    edmPathToMongoPropertyMapping.forEach(
        (key, value) -> {
          if (value.getCircularReferenceMapping() != null
              && CircularStrategy.EMBED_LIMITED.equals(
                  value.getCircularReferenceMapping().getStrategy())
              && value.getCircularReferenceMapping().getAnchorEdmPath() != null) {
            if (!edmPathToMongoPropertyMapping.containsKey(
                value.getCircularReferenceMapping().getAnchorEdmPath())) {
              throw new EntityPropertiesMongoPathContext.InvalidAnchorPathException(
                  String.format(
                      "The anchor path '%s' defined in the circular reference mapping for '%s' is not a valid EDM path.",
                      value.getCircularReferenceMapping().getAnchorEdmPath(), key));
            }
          }
        });
  }

  private static final class EntityPropertiesMongoPathContextOutPut {
    private Map<String, MongoPathEntry> edmToMongoPath;
  }

  private void compileProperty(
      String propertyName,
      PropertyMapping property,
      String currentMongoBase,
      String currentEdmBase,
      EntityPropertiesMongoPathContextOutPut out,
      EntityPropertiesMongoPathResolverContext entityPropertiesMongoPathResolverContext) {

    if (Boolean.TRUE.equals(property.getIgnore())) {
      return;
    }

    String edmPath =
        currentEdmBase == null
            ? propertyName
            : currentEdmBase + ODATA_PATH_SEPARATOR_CHARACTER + propertyName;
    String mongoPath = resolveMongoPath(property, currentMongoBase, propertyName);

    // Leaf property
    if (property.getProperties() == null || property.getProperties().isEmpty()) {

      out.edmToMongoPath.put(
          edmPath,
          new MongoPathEntry(
              edmPath,
              mongoPath,
              Boolean.TRUE.equals(property.getKey()),
              property.getType(),
              toRecord(property.getCircularReferenceMapping()),
              property.getMaxCircularLimitPerEdmPath()));
      return;
    }

    for (Map.Entry<String, PropertyMapping> nested : property.getProperties().entrySet()) {

      compileProperty(
          nested.getKey(),
          nested.getValue(),
          mongoPath,
          edmPath,
          out,
          entityPropertiesMongoPathResolverContext);
      // TODO Store properties the Set with names of edmPaths of properties, required for
      // ExpandAsterisk = true (all fields defined in EDM)
    }
    if (entityPropertiesMongoPathResolverContext.isGenerateOnlyLeafs()) {
      return;
    }
    out.edmToMongoPath.put(
        edmPath,
        new MongoPathEntry(
            edmPath,
            mongoPath,
            Boolean.TRUE.equals(property.getKey()),
            property.getType(),
            toRecord(property.getCircularReferenceMapping()),
            property.getMaxCircularLimitPerEdmPath()));
  }

  private CircularReferenceMappingRecord toRecord(
      CircularReferenceMapping circularReferenceMapping) {
    if (circularReferenceMapping == null) {
      return null;
    }
    return CircularReferenceMappingRecord.builder()
        .withStrategy(circularReferenceMapping.getStrategy())
        .withMaxDepth(circularReferenceMapping.getMaxDepth())
        .withAnchorEdmPath(circularReferenceMapping.getAnchorEdmPath())
        .build();
  }

  // ----------------------------------------------------

  private String resolveMongoPath(
      PropertyMapping property, String currentMongoBase, String propertyName) {

    // Absolute override wins
    if (property.getMongoPath() != null) {
      return normalize(property.getMongoPath());
    }

    String localName = property.getMongoName() != null ? property.getMongoName() : propertyName;
    if (property.getFlattenedLevelUp() != null) {
      String[] mongoParentsPath = currentMongoBase.split("\\.");
      if (property.getFlattenedLevelUp() > mongoParentsPath.length) {
        throw new RuntimeException("the flattenedLevelUp with too large value");
      }
      if (property.getFlattenedLevelUp() == mongoParentsPath.length) {
        return join(null, localName);
      }
      return join(
          String.join(
              ".",
              Arrays.asList(mongoParentsPath)
                  .subList(0, mongoParentsPath.length - property.getFlattenedLevelUp())),
          localName);
    }
    String base =
        property.getRelativeTo() != null
            ? join(currentMongoBase, property.getRelativeTo())
            : currentMongoBase;

    return join(base, localName);
  }

  // ----------------------------------------------------

  private String normalize(String path) {
    if (path == null || path.isEmpty()) {
      return "";
    }
    return path.replaceAll("^\\.+|\\.+$", "");
  }

  private String join(String base, String name) {
    if (base == null || base.isEmpty()) {
      return normalize(name);
    }
    if (name == null || name.isEmpty()) {
      return normalize(base);
    }
    return normalize(base) + "." + normalize(name);
  }

  /** Context for resolving entity properties mongo paths. */
  public static class EntityPropertiesMongoPathResolverContext {
    private final boolean generateOnlyLeafs;

    /**
     * Constructs a new EntityPropertiesMongoPathResolverContext.
     *
     * @param generateOnlyLeafs true if only leaf properties should be generated, false otherwise
     */
    public EntityPropertiesMongoPathResolverContext(boolean generateOnlyLeafs) {
      this.generateOnlyLeafs = generateOnlyLeafs;
    }

    /**
     * Returns whether only leaf properties should be generated.
     *
     * @return true if only leaf properties should be generated, false otherwise
     */
    public boolean isGenerateOnlyLeafs() {
      return generateOnlyLeafs;
    }

    /** Builder for EntityPropertiesMongoPathResolverContext. */
    public static class EntityPropertiesMongoPathResolverContextBuilder {
      private boolean generateOnlyLeafs;

      /**
       * Sets whether only leaf properties should be generated.
       *
       * @param generateOnlyLeafs true if only leaf properties should be generated, false otherwise
       * @return the builder instance
       */
      public EntityPropertiesMongoPathResolverContextBuilder withGenerateOnlyLeafs(
          boolean generateOnlyLeafs) {
        this.generateOnlyLeafs = generateOnlyLeafs;
        return this;
      }

      /**
       * Builds the EntityPropertiesMongoPathResolverContext.
       *
       * @return the new context instance
       */
      public EntityPropertiesMongoPathResolverContext build() {
        return new EntityPropertiesMongoPathResolverContext(generateOnlyLeafs);
      }
    }
  }
}
