package com.github.starnowski.jamolingo.demo;

import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContext;
import com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContextBuilder;
import com.github.starnowski.jamolingo.core.mapping.ODataMongoMapping;
import com.github.starnowski.jamolingo.core.mapping.ODataMongoMappingFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLStreamException;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider;
import org.apache.olingo.commons.core.edm.EdmProviderImpl;
import org.apache.olingo.server.core.MetadataParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class AppConfig {

  @Bean
  public Edm edm() throws IOException, XMLStreamException {
    ClassPathResource resource = new ClassPathResource("edm/edm6_filter_main.xml");
    try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
      MetadataParser parser = new MetadataParser();
      CsdlEdmProvider provider = parser.buildEdmProvider(reader);
      return new EdmProviderImpl(provider);
    }
  }

  @Bean
  public ODataMongoMapping oDataMongoMapping(Edm edm) {
    ODataMongoMappingFactory factory = new ODataMongoMappingFactory();
    ODataMongoMapping mapping = factory.build(edm, "MyService");
    // Map "examples2" entity set to "items" collection
    if (mapping.getEntities().containsKey("Example2")) {
      mapping.getEntities().get("Example2").setTable("items");
    }
    return mapping;
  }

  @Bean
  public EntityPropertiesMongoPathContext entityPropertiesMongoPathContext(
      ODataMongoMapping oDataMongoMapping) {
    EntityPropertiesMongoPathContextBuilder builder = new EntityPropertiesMongoPathContextBuilder();
    return builder.build(oDataMongoMapping.getEntities().get("Example2"));
  }

  @Bean
  public DefaultEdmMongoContextFacade edmMongoContextFacade(
      EntityPropertiesMongoPathContext entityPropertiesMongoPathContext) {
    return DefaultEdmMongoContextFacade.builder()
        .withEntityPropertiesMongoPathContext(entityPropertiesMongoPathContext)
        .build();
  }
}
