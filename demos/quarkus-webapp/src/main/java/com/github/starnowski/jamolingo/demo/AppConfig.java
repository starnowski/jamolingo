package com.github.starnowski.jamolingo.demo;

import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContext;
import com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContextBuilder;
import com.github.starnowski.jamolingo.core.mapping.ODataMongoMapping;
import com.github.starnowski.jamolingo.core.mapping.ODataMongoMappingFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLStreamException;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider;
import org.apache.olingo.commons.core.edm.EdmProviderImpl;
import org.apache.olingo.server.core.MetadataParser;

@ApplicationScoped
public class AppConfig {

  @Produces
  @ApplicationScoped
  public Edm edm() throws IOException, XMLStreamException {
    try (InputStream is =
        Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("edm/edm6_filter_main.xml")) {
      if (is == null) {
        throw new IOException("Resource not found: edm/edm6_filter_main.xml");
      }
      try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
        MetadataParser parser = new MetadataParser();
        CsdlEdmProvider provider = parser.buildEdmProvider(reader);
        return new EdmProviderImpl(provider);
      }
    }
  }

  @Produces
  @ApplicationScoped
  public ODataMongoMapping oDataMongoMapping(Edm edm) {
    ODataMongoMappingFactory factory = new ODataMongoMappingFactory();
    ODataMongoMapping mapping = factory.build(edm, "MyService");
    if (mapping.getEntities().containsKey("Example2")) {
      mapping.getEntities().get("Example2").setTable("items");
    }
    return mapping;
  }

  @Produces
  @ApplicationScoped
  public EntityPropertiesMongoPathContext entityPropertiesMongoPathContext(
      ODataMongoMapping oDataMongoMapping) {
    EntityPropertiesMongoPathContextBuilder builder = new EntityPropertiesMongoPathContextBuilder();
    return builder.build(oDataMongoMapping.getEntities().get("Example2"));
  }

  @Produces
  @ApplicationScoped
  public DefaultEdmMongoContextFacade edmMongoContextFacade(
      EntityPropertiesMongoPathContext entityPropertiesMongoPathContext) {
    return DefaultEdmMongoContextFacade.builder()
        .withEntityPropertiesMongoPathContext(entityPropertiesMongoPathContext)
        .build();
  }
}
