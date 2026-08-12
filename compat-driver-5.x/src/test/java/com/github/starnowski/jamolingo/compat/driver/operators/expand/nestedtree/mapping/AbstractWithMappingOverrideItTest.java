package com.github.starnowski.jamolingo.compat.driver.operators.expand.nestedtree.mapping;

import com.github.starnowski.jamolingo.AbstractItTest;
import com.github.starnowski.jamolingo.EmbeddedMongoResource;
import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.olingo.commons.api.edm.Edm;

@QuarkusTest
@QuarkusTestResource(value = EmbeddedMongoResource.class, restrictToAnnotatedClass = true)
public class AbstractWithMappingOverrideItTest extends AbstractItTest {

  protected ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.Builder
      createParserContextBuilder() {
    java.util.Map<String, EdmMongoContextFacade> edmTypeMapping = new java.util.HashMap<>();
    com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContextBuilder builder =
        new com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContextBuilder();

    com.github.starnowski.jamolingo.core.mapping.ODataMongoMappingFactory factory =
        new com.github.starnowski.jamolingo.core.mapping.ODataMongoMappingFactory();
    Edm edm = null;
    try {
      edm = loadEmdProvider("edm/tree_types.xml");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    com.github.starnowski.jamolingo.core.mapping.ODataMongoMapping odataMapping =
        factory.build(edm.getSchema("MyService"));

    com.github.starnowski.jamolingo.core.mapping.EntityMapping category =
        odataMapping.getEntities().get("Category");
    category.getProperties().get("name").setMongoPath("renamed_name");
    edmTypeMapping.put(
        "MyService.Category",
        com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade.builder()
            .withEntityPropertiesMongoPathContext(builder.build(category))
            .build());

    com.github.starnowski.jamolingo.core.mapping.EntityMapping t1 =
        odataMapping.getEntities().get("TreeType1");
    t1.getProperties().get("index").setMongoPath("renamed_index");
    t1.getProperties().get("parentId").setMongoPath("renamed_parentId");
    t1.getProperties().get("categoryId").setMongoPath("renamed_categoryId");
    edmTypeMapping.put(
        "MyService.TreeType1",
        com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade.builder()
            .withEntityPropertiesMongoPathContext(builder.build(t1))
            .build());

    com.github.starnowski.jamolingo.core.mapping.EntityMapping t2 =
        odataMapping.getEntities().get("TreeType2");
    t2.getProperties().get("index").setMongoPath("renamed_index");
    t2.getProperties().get("parentId").setMongoPath("renamed_parentId");
    t2.getProperties().get("categoryId").setMongoPath("renamed_categoryId");
    t2.getProperties().get("treeType1Id").setMongoPath("renamed_treeType1Id");
    edmTypeMapping.put(
        "MyService.TreeType2",
        com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade.builder()
            .withEntityPropertiesMongoPathContext(builder.build(t2))
            .build());

    com.github.starnowski.jamolingo.core.mapping.EntityMapping t3 =
        odataMapping.getEntities().get("TreeType3");
    t3.getProperties().get("index").setMongoPath("renamed_index");
    t3.getProperties().get("parentId").setMongoPath("renamed_parentId");
    t3.getProperties().get("categoryId").setMongoPath("renamed_categoryId");
    t3.getProperties().get("treeType2Id").setMongoPath("renamed_treeType2Id");
    edmTypeMapping.put(
        "MyService.TreeType3",
        com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade.builder()
            .withEntityPropertiesMongoPathContext(builder.build(t3))
            .build());

    com.github.starnowski.jamolingo.core.mapping.EntityMapping t4 =
        odataMapping.getEntities().get("TreeType4");
    t4.getProperties().get("index").setMongoPath("renamed_index");
    t4.getProperties().get("parentId").setMongoPath("renamed_parentId");
    t4.getProperties().get("categoryId").setMongoPath("renamed_categoryId");
    t4.getProperties().get("treeType3Id").setMongoPath("renamed_treeType3Id");
    edmTypeMapping.put(
        "MyService.TreeType4",
        com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade.builder()
            .withEntityPropertiesMongoPathContext(builder.build(t4))
            .build());

    return ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.builder()
        .withEdmTypeMapping(edmTypeMapping);
  }
}
