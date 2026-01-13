package com.github.starnowski.jamolingo;

import com.github.starnowski.jamolingo.context.DefaultEdmMongoContextFacade;
import com.github.starnowski.jamolingo.context.EntityPropertiesMongoPathContextBuilder;
import com.github.starnowski.jamolingo.context.ODataMongoMappingFactory;
import com.github.starnowski.jamolingo.junit5.QuarkusMongoDataLoaderExtension;
import com.github.starnowski.jamolingo.select.OdataSelectToMongoProjectParser;
import com.github.starnowski.jamolingo.select.SelectOperatorResult;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider;
import org.apache.olingo.commons.core.edm.EdmProviderImpl;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.core.MetadataParser;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@ExtendWith(QuarkusMongoDataLoaderExtension.class)
@QuarkusTestResource(EmbeddedMongoResource.class)
class SelectOperatorTest {

  @Inject MongoClient mongoClient;

  @ParameterizedTest
  @MethodSource("provideTestCases")
  public void shouldReturnExpectedProjectedDocument(
      String edmPath, String selectClause, String inputDataPath, String expectedDataPath)
      throws UriValidationException, UriParserException, XMLStreamException, IOException {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("testdb");
    MongoCollection<Document> collection = database.getCollection("Items");
    collection.drop();
    collection.insertOne(loadDocument(inputDataPath));

    Edm edm = loadEmdProvider(edmPath);
    ODataMongoMappingFactory factory = new ODataMongoMappingFactory();
    var odataMapping = factory.build(edm.getSchema("Demo"));
    var entityMapping = odataMapping.getEntities().get("Item");
    EntityPropertiesMongoPathContextBuilder entityPropertiesMongoPathContextBuilder =
        new EntityPropertiesMongoPathContextBuilder();
    var context = entityPropertiesMongoPathContextBuilder.build(entityMapping);

    UriInfo uriInfo =
        new Parser(edm, OData.newInstance())
            .parseUri("Items", "$select=" + selectClause, null, null);
    OdataSelectToMongoProjectParser tested = new OdataSelectToMongoProjectParser();

    // WHEN
    SelectOperatorResult result =
        tested.parse(uriInfo.getSelectOption(), new DefaultEdmMongoContextFacade(context, null));
    Bson projectStage = result.getStageObject();

    List<Document> results = new ArrayList<>();
    collection.aggregate(Arrays.asList(projectStage)).into(results);

    // THEN
    Assertions.assertEquals(1, results.size());
    Document actual = results.get(0);
    Document expected = loadDocument(expectedDataPath);
    // Remove _id for comparison if present in actual but not expected
    actual.remove("_id");
    Assertions.assertEquals(expected, actual);
  }

  private static Stream<Arguments> provideTestCases() {
    return Stream.of(
        Arguments.of(
            "edm/edm1.xml", "plainString", "bson/simple_item.json", "bson/expected_case1.json"),
        Arguments.of(
            "edm/edm2_with_nested_collections.xml",
            "plainString,Name,Addresses/Street,Addresses/ZipCode",
            "bson/item_edm2.json",
            "bson/expected_case2.json"));
  }

  private Edm loadEmdProvider(String filePath) throws XMLStreamException {
    Reader reader =
        new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(filePath), StandardCharsets.UTF_8);
    // Parse it into a CsdlEdmProvider
    MetadataParser parser = new MetadataParser();
    CsdlEdmProvider provider = parser.buildEdmProvider(reader);

    // Build Edm model from provider
    return new EdmProviderImpl(provider);
  }

  private Document loadDocument(String filePath) throws IOException {
    try (Reader reader =
        new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(filePath), StandardCharsets.UTF_8)) {
      String json =
          new BufferedReader(reader).lines().collect(Collectors.joining(System.lineSeparator()));
      return Document.parse(json);
    }
  }

  //    private Edm createEdm() {
  //        CsdlEdmProvider provider = new CsdlEdmProvider() {
  //            @Override
  //            public List<CsdlSchema> getSchemas() {
  //                CsdlSchema schema = new CsdlSchema();
  //                schema.setNamespace("Demo");
  //
  //                CsdlEntityType entityType = new CsdlEntityType();
  //                entityType.setName("Item");
  //                CsdlProperty property = new
  // CsdlProperty().setName("plainString").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
  //                entityType.setProperties(Collections.singletonList(property));
  //
  //                schema.setEntityTypes(Collections.singletonList(entityType));
  //
  //                CsdlEntityContainer container = new CsdlEntityContainer();
  //                container.setName("Container");
  //                CsdlEntitySet entitySet = new CsdlEntitySet().setName("Items").setType(new
  // FullQualifiedName("Demo", "Item"));
  //                container.setEntitySets(Collections.singletonList(entitySet));
  //
  //                schema.setEntityContainer(container);
  //
  //                return Collections.singletonList(schema);
  //            }
  //
  //            @Override
  //            public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {
  //                if (entityTypeName.equals(new FullQualifiedName("Demo", "Item"))) {
  //                    return new CsdlEntityType()
  //                            .setName("Item")
  //                            .setProperties(Collections.singletonList(
  //                                    new
  // CsdlProperty().setName("plainString").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
  //                            ));
  //                }
  //                return null;
  //            }
  //
  //            @Override
  //            public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String
  // entitySetName) {
  //                if (entityContainer.equals(new FullQualifiedName("Demo", "Container")) &&
  // entitySetName.equals("Items")) {
  //                    return new CsdlEntitySet().setName("Items").setType(new
  // FullQualifiedName("Demo", "Item"));
  //                }
  //                return null;
  //            }
  //
  //            @Override
  //            public CsdlEntityContainer getEntityContainer() {
  //                CsdlEntityContainer container = new CsdlEntityContainer();
  //                container.setName("Container");
  //                return container;
  //            }
  //
  //            @Override
  //            public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName
  // entityContainerName) {
  //                if (entityContainerName == null || entityContainerName.equals(new
  // FullQualifiedName("Demo", "Container"))) {
  //                    CsdlEntityContainerInfo info = new CsdlEntityContainerInfo();
  //                    info.setContainerName(new FullQualifiedName("Demo", "Container"));
  //                    return info;
  //                }
  //                return null;
  //            }
  //
  //            @Override
  //            public CsdlAnnotations getAnnotationsGroup(FullQualifiedName targetName, String
  // qualifier) {
  //                return null;
  //            }
  //
  //            @Override
  //            public List<CsdlAliasInfo> getAliasInfos() {
  //                return null;
  //            }
  //        };
  //        return new EdmProviderImpl(provider);
  //    }
}
