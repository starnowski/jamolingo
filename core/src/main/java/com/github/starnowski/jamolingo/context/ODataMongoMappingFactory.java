package com.github.starnowski.jamolingo.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;

import static com.github.starnowski.jamolingo.context.Constants.ODATA_PATH_SEPARATOR_CHARACTER;

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

    // Primitive + Complex properties
    for (String propName : entityType.getPropertyNames()) {
      EdmProperty prop = entityType.getStructuralProperty(propName);
      props.put(
          prop.getName(),
          mapProperty(prop, entityType, new EntityPropertyMappingContext(prop.getName(), null)));
    }

    return props;
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
      if (entityPropertyMappingContext.getEdmTypeAndEdmPath().containsKey(typeName)) {
        pm.setCircularReferenceMapping(
            new CircularReferenceMapping()
                .withStrategy(CircularStrategy.EMBED_LIMITED)
                .withAnchorEdmPath(
                    entityPropertyMappingContext.getEdmTypeAndEdmPath().get(typeName)));
        return pm;
      } else {
        Map<String, String> updatedMap =
            new HashMap<>(entityPropertyMappingContext.getEdmTypeAndEdmPath());
        updatedMap.put(typeName, entityPropertyMappingContext.getCurrentEdmPath());
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
                  entityPropertyMappingContext.getCurrentEdmPath() + ODATA_PATH_SEPARATOR_CHARACTER + prop.getName(),
                  new HashMap<>(entityPropertyMappingContext.getEdmTypeAndEdmPath()))));
    }

    return props;
  }

  // ------------------ Defaults ------------------

  private String defaultCollectionName(EdmEntityType entityType) {
    return entityType.getName();
  }
}
