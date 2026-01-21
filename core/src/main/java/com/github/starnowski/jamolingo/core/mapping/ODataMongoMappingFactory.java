package com.github.starnowski.jamolingo.core.mapping;

import static com.github.starnowski.jamolingo.core.utils.Constants.ODATA_PATH_SEPARATOR_CHARACTER;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.starnowski.jamolingo.context.EntityMapping;
import com.github.starnowski.jamolingo.context.PropertyMapping;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;

public class ODataMongoMappingFactory {

  public ODataMongoMapping build(EdmSchema edmSchema) {
    ODataMongoMapping mapping = new ODataMongoMapping();
    Map<String, EntityMapping> entities = new LinkedHashMap<>();

    for (EdmEntityType entityType : edmSchema.getEntityTypes()) {
      entities.put(entityType.getName(), mapEntity(entityType));
    }

    mapping.setEntities(entities);
    return mapping;
  }

  private static class EntityPropertyMappingContext {
    private final String currentEdmPath;
    private final Map<String, String> edmTypeAndEdmPath;

    public EntityPropertyMappingContext(
        String currentEdmPath, Map<String, String> edmTypeAndEdmPath) {
      this.currentEdmPath = currentEdmPath;
      this.edmTypeAndEdmPath =
          edmTypeAndEdmPath == null ? Collections.emptyMap() : edmTypeAndEdmPath;
    }

    public String getCurrentEdmPath() {
      return currentEdmPath;
    }

    public Map<String, String> getEdmTypeAndEdmPath() {
      return edmTypeAndEdmPath;
    }
  }

  public ODataMongoMapping build(Edm edm, String schema) {
    return build(edm.getSchema(schema));
  }

  // ------------------ Entity ------------------

  private EntityMapping mapEntity(EdmEntityType entityType) {

    EntityMapping entity = new EntityMapping();

    entity.setCollection(defaultCollectionName(entityType));
    entity.setProperties(mapProperties(entityType));
    return entity;
  }

  // ------------------ Properties ------------------

  private Map<String, PropertyMapping> mapProperties(EdmEntityType entityType) {

    Map<String, PropertyMapping> props = new LinkedHashMap<>();
    // Resolve Complex types to shorter circular reference edm path
    Map<String, String> edmTypeAndEdmPath =
        enrichedCircularReferenceMapBasedOnPropertiesForEdmStructuredType(null, entityType, null);

    // Primitive + Complex properties
    for (String propName : entityType.getPropertyNames()) {
      EdmProperty prop = entityType.getStructuralProperty(propName);
      props.put(
          prop.getName(),
          mapProperty(
              prop,
              entityType,
              new EntityPropertyMappingContext(prop.getName(), edmTypeAndEdmPath)));
    }

    return props;
  }

  private Map<String, String> enrichedCircularReferenceMapBasedOnPropertiesForEdmStructuredType(
      Map<String, String> base, EdmStructuredType type, String currentEdmTypePath) {
    Map<String, String> edmTypeAndEdmPath = base == null ? new HashMap<>() : new HashMap<>(base);
    for (String propName : type.getPropertyNames()) {
      EdmProperty prop = type.getStructuralProperty(propName);
      Map.Entry<String, String> entry = tryToExtractEdmPathForComplexType(prop, currentEdmTypePath);
      if (entry != null) {
        edmTypeAndEdmPath.putIfAbsent(entry.getKey(), entry.getValue());
      }
    }
    return edmTypeAndEdmPath;
  }

  private Map.Entry<String, String> tryToExtractEdmPathForComplexType(
      EdmProperty property, String currentEdmTypePath) {
    if (EdmTypeKind.COMPLEX.equals(property.getType().getKind())) {
      EdmComplexType complexType = (EdmComplexType) property.getType();
      return Map.entry(complexType.getName(), buildEDMPath(currentEdmTypePath, property.getName()));
    }
    return null;
  }

  private PropertyMapping mapProperty(
      EdmProperty prop,
      EdmStructuredType declaringType,
      EntityPropertyMappingContext entityPropertyMappingContext) {

    PropertyMapping pm = new PropertyMapping();
    pm.setType(prop.getType().getNamespace() + "." + prop.getType().getName());

    // --- Key detection ---
    if (declaringType instanceof EdmEntityType) {
      EdmEntityType et = (EdmEntityType) declaringType;
      if (et.getKeyPropertyRefs().stream().anyMatch(k -> k.getName().equals(prop.getName()))) {
        pm.setKey(true);
      }
    }

    // --- Complex vs primitive ---
    if (EdmTypeKind.COMPLEX.equals(prop.getType().getKind())) {
      EdmComplexType complexType = (EdmComplexType) prop.getType();
      String typeName = complexType.getName();
      if (entityPropertyMappingContext.getEdmTypeAndEdmPath().containsKey(typeName)
          && !entityPropertyMappingContext
              .getCurrentEdmPath()
              .equals(entityPropertyMappingContext.getEdmTypeAndEdmPath().get(typeName))) {
        pm.setCircularReferenceMapping(
            CircularReferenceMapping.builder()
                .withStrategy(CircularStrategy.EMBED_LIMITED)
                .withAnchorEdmPath(
                    entityPropertyMappingContext.getEdmTypeAndEdmPath().get(typeName))
                .build());
        return pm;
      } else {
        Map<String, String> updatedMap =
            enrichedCircularReferenceMapBasedOnPropertiesForEdmStructuredType(
                entityPropertyMappingContext.getEdmTypeAndEdmPath(),
                complexType,
                entityPropertyMappingContext.getCurrentEdmPath());

        //            new HashMap<>(entityPropertyMappingContext.getEdmTypeAndEdmPath());
        //        updatedMap.put(typeName, entityPropertyMappingContext.getCurrentEdmPath());
        EntityPropertyMappingContext updatedEntityPropertyMappingContext =
            new EntityPropertyMappingContext(
                entityPropertyMappingContext.getCurrentEdmPath(), updatedMap);
        pm.setProperties(mapComplexProperties(complexType, updatedEntityPropertyMappingContext));
      }
    }

    // Mongo-specific fields intentionally NOT set here

    return pm;
  }

  private Map<String, PropertyMapping> mapComplexProperties(
      EdmComplexType complexType, EntityPropertyMappingContext entityPropertyMappingContext) {

    Map<String, PropertyMapping> props = new LinkedHashMap<>();

    for (String propName : complexType.getPropertyNames()) {
      EdmProperty prop = complexType.getStructuralProperty(propName);
      props.put(
          prop.getName(),
          mapProperty(
              prop,
              complexType,
              new EntityPropertyMappingContext(
                  buildEDMPath(entityPropertyMappingContext.getCurrentEdmPath(), prop.getName()),
                  new HashMap<>(entityPropertyMappingContext.getEdmTypeAndEdmPath()))));
    }

    return props;
  }

  private String buildEDMPath(String base, String currentNode) {
    return base == null ? currentNode : base + ODATA_PATH_SEPARATOR_CHARACTER + currentNode;
  }

  // ------------------ Defaults ------------------

  private String defaultCollectionName(EdmEntityType entityType) {
    return entityType.getName();
  }
}
