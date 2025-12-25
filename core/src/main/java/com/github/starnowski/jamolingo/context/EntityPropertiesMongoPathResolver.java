package com.github.starnowski.jamolingo.context;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class EntityPropertiesMongoPathResolver {

  public static class EntityPropertiesMongoPathResolverContext {
    public boolean isGenerateOnlyLeafs() {
      return generateOnlyLeafs;
    }

    private final boolean generateOnlyLeafs;

      public EntityPropertiesMongoPathResolverContext(boolean generateOnlyLeafs) {
          this.generateOnlyLeafs = generateOnlyLeafs;
      }

      public static class EntityPropertiesMongoPathResolverContextBuilder {
        private boolean generateOnlyLeafs;

        public EntityPropertiesMongoPathResolverContextBuilder withGenerateOnlyLeafs(boolean generateOnlyLeafs) {
          this.generateOnlyLeafs = generateOnlyLeafs;
          return this;
        }

        public EntityPropertiesMongoPathResolverContext build() {
          return new EntityPropertiesMongoPathResolverContext(generateOnlyLeafs);
        }
      }
  }

  public Map<String, MongoPathEntry> resolve(EntityMapping entityMapping) {
    return resolve(entityMapping, new EntityPropertiesMongoPathResolverContext(false));
  }

  public Map<String, MongoPathEntry> resolve(EntityMapping entityMapping, EntityPropertiesMongoPathResolverContext entityPropertiesMongoPathResolverContext) {
    return compile(entityMapping, entityPropertiesMongoPathResolverContext);
  }

  private Map<String, MongoPathEntry> compile(EntityMapping entityMapping, EntityPropertiesMongoPathResolverContext entityPropertiesMongoPathResolverContext) {

    Map<String, MongoPathEntry> result = new LinkedHashMap<>();

    String entityRoot = normalize(entityMapping.getRootPath());

    if (entityMapping.getProperties() != null) {
      for (Map.Entry<String, PropertyMapping> e : entityMapping.getProperties().entrySet()) {

        compileProperty(e.getKey(), e.getValue(), entityRoot, null, result, entityPropertiesMongoPathResolverContext);
      }
    }

    return result;
  }

  // ----------------------------------------------------

  private void compileProperty(
          String propertyName,
          PropertyMapping property,
          String currentMongoBase,
          String currentEdmBase,
          Map<String, MongoPathEntry> out, EntityPropertiesMongoPathResolverContext entityPropertiesMongoPathResolverContext) {

    if (Boolean.TRUE.equals(property.getIgnore())) {
      return;
    }

    String edmPath = currentEdmBase == null ? propertyName : currentEdmBase + "." + propertyName;
    String mongoPath = resolveMongoPath(property, currentMongoBase, propertyName);

    // Leaf property
    if (property.getProperties() == null || property.getProperties().isEmpty()) {

      out.put(
          edmPath,
          new MongoPathEntry(
              edmPath, mongoPath, Boolean.TRUE.equals(property.getKey()), property.getType()));
      return;
    }

    // Complex property → recurse
    String nextMongoBase = mongoPath;

    for (Map.Entry<String, PropertyMapping> nested : property.getProperties().entrySet()) {

      compileProperty(nested.getKey(), nested.getValue(), nextMongoBase, edmPath, out, entityPropertiesMongoPathResolverContext);
    }
    if (entityPropertiesMongoPathResolverContext.isGenerateOnlyLeafs()) {
      return;
    }
    out.put(
            edmPath,
            new MongoPathEntry(
                    edmPath, mongoPath, Boolean.TRUE.equals(property.getKey()), property.getType()));
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

  public static final class MongoPathEntry {

    private final String edmPath;
    private final String mongoPath;
    private final boolean key;
    private final String type;

    public MongoPathEntry(String edmPath, String mongoPath, boolean key, String type) {

      this.edmPath = edmPath;
      this.mongoPath = mongoPath;
      this.key = key;
      this.type = type;
    }

    public String getEdmPath() {
      return edmPath;
    }

    public String getMongoPath() {
      return mongoPath;
    }

    public boolean isKey() {
      return key;
    }

    public String getType() {
      return type;
    }
  }
}
