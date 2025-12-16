package com.github.starnowski.jamolingo.context;

import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmSchema;

public class ODataMongoMappingFactory {

  public ODataMongoMapping build(EdmSchema edmSchema) {
    // TODO
    return null;
  }

  public ODataMongoMapping build(Edm edm, String schema) {
    return build(edm.getSchema(schema));
  }
}
