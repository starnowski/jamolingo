package com.github.starnowski.jamolingo.context;

import java.util.LinkedHashMap;
import java.util.Map;
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
      props.put(prop.getName(), mapProperty(prop, entityType));
    }

    return props;
  }

  private PropertyMapping mapProperty(EdmProperty prop, EdmStructuredType declaringType) {

    PropertyMapping pm = new PropertyMapping();

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

      pm.setProperties(mapComplexProperties(complexType));
    }

    // Mongo-specific fields intentionally NOT set here

    return pm;
  }

  private Map<String, PropertyMapping> mapComplexProperties(EdmComplexType complexType) {

    Map<String, PropertyMapping> props = new LinkedHashMap<>();

    for (String propName : complexType.getPropertyNames()) {
      EdmProperty prop = complexType.getStructuralProperty(propName);
      props.put(prop.getName(), mapProperty(prop, complexType));
    }

    return props;
  }

  // ------------------ Defaults ------------------

  private String defaultCollectionName(EdmEntityType entityType) {
    return entityType.getName();
  }
}
