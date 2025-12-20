package com.github.starnowski.jamolingo.context;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityPropertiesMongoPathResolver {

  public Map<String, String> resolve(EntityMapping entityMapping) {
    // TODO
    return compile(entityMapping.getCollection(), entityMapping).entrySet().stream()
        .collect(
            Collectors.toMap(
                (entry) -> entry.getKey(), (entry) -> entry.getValue().getMongoPath()));
  }

  private Map<String, MongoPathEntry> compile(String entityName, EntityMapping entityMapping) {

    Map<String, MongoPathEntry> result = new LinkedHashMap<>();

    String entityRoot = normalize(entityMapping.getRootPath());

    if (entityMapping.getProperties() != null) {
      for (Map.Entry<String, PropertyMapping> e : entityMapping.getProperties().entrySet()) {

        compileProperty(entityName, e.getKey(), e.getValue(), entityRoot, entityName, result);
      }
    }

    return result;
  }

  // ----------------------------------------------------

  private void compileProperty(
      String entityName,
      String propertyName,
      PropertyMapping property,
      String currentMongoBase,
      String currentEdmBase,
      Map<String, MongoPathEntry> out) {

    if (Boolean.TRUE.equals(property.getIgnore())) {
      return;
    }

    String edmPath = currentEdmBase + "." + propertyName;
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

      compileProperty(entityName, nested.getKey(), nested.getValue(), nextMongoBase, edmPath, out);
    }
  }

  // ----------------------------------------------------

  private String resolveMongoPath(
      PropertyMapping property, String currentMongoBase, String propertyName) {

    // Absolute override wins
    if (property.getMongoPath() != null) {
      return normalize(property.getMongoPath());
    }

    String localName = property.getMongoName() != null ? property.getMongoName() : propertyName;

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

  private static final class MongoPathEntry {

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
