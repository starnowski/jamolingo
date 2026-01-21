package com.github.starnowski.jamolingo;

import com.github.starnowski.jamolingo.common.json.JSONOverrideHelper;
import com.github.starnowski.jamolingo.core.mapping.EntityMapping;
import com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContextBuilder;
import com.github.starnowski.jamolingo.core.mapping.ODataMongoMappingFactory;
import com.github.starnowski.jamolingo.core.api.DefaultEdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.operators.select.OdataSelectToMongoProjectParser;
import com.github.starnowski.jamolingo.core.operators.select.SelectOperatorResult;
import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;

@QuarkusTest
@QuarkusTestResource(EmbeddedMongoResource.class)
class SelectOperatorWithOverrideConfigTest extends AbstractItTest {

  static final String EDM_1_MERGE_OVERRIDE_MONGO_NAME =
      ""
          + "        {\n"
          + "            \"properties\": {\n"
          + "                \"plainString\": {\n"
          + "                    \"mongoName\": \"thisIsString\"\n"
          + "                }\n"
          + "            }\n"
          + "        }";

  static final String EDM_3_MERGE_OVERRIDE_NESTED_PROP =
      ""
          + "        {\n"
          + "            \"properties\": {\n"
          + "                \"Addresses\": {\n"
          + "                    \"properties\": {\n"
          + "                        \"BackUpAddresses\": {\n"
          + "                            \"mongoName\": \"previousAddresses\"\n"
          + "                        }\n"
          + "                    }\n"
          + "                }\n"
          + "            }\n"
          + "        }";

  @Inject MongoClient mongoClient;

  @ParameterizedTest
  @MethodSource("provideShouldReturnExpectedProjectedDocument")
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/edm1.json"),
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/edm3.json"),
        @MongoDocument(database = "testdb", collection = "Items", bsonFilePath = "bson/edm4.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/edm1_override.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/edm3_override.json")
      })
  public void shouldReturnExpectedProjectedDocument(
      String edmPath,
      Set<String> selectClause,
      String schema,
      String entityType,
      String entitySet,
      String expectedId,
      String expectedDataPath,
      String overridePayload)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          IOException,
          JSONException {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("testdb");
    MongoCollection<Document> collection = database.getCollection("Items");

    Edm edm = loadEmdProvider(edmPath);
    ODataMongoMappingFactory factory = new ODataMongoMappingFactory();
    var odataMapping = factory.build(edm.getSchema(schema));
    var entityMapping = odataMapping.getEntities().get(entityType);
    if (overridePayload != null) {
      JSONOverrideHelper helper = new JSONOverrideHelper();
      entityMapping =
          helper.applyChangesToJson(
              entityMapping,
              overridePayload,
              EntityMapping.class,
              JSONOverrideHelper.PatchType.MERGE);
    }
    EntityPropertiesMongoPathContextBuilder entityPropertiesMongoPathContextBuilder =
        new EntityPropertiesMongoPathContextBuilder();
    var context = entityPropertiesMongoPathContextBuilder.build(entityMapping);

    UriInfo uriInfo =
        new Parser(edm, OData.newInstance())
            .parseUri(entitySet, "$select=" + String.join(",", selectClause), null, null);
    OdataSelectToMongoProjectParser tested = new OdataSelectToMongoProjectParser();

    // WHEN
    SelectOperatorResult result =
        tested.parse(uriInfo.getSelectOption(), new DefaultEdmMongoContextFacade(context, null));
    Bson projectStage = result.getStageObject();
    List<Bson> pipeline = new ArrayList<>();
    pipeline.add(new Document("$match", new Document("_id", UUID.fromString(expectedId))));
    pipeline.add(projectStage);

    List<Document> results = new ArrayList<>();
    collection.aggregate(pipeline).into(results);

    // THEN
    Assertions.assertEquals(1, results.size());
    Document actual = results.get(0);
    Document expected = loadDocument(expectedDataPath);
    Assertions.assertEquals(expected, actual);
    JSONAssert.assertEquals(expected.toJson(), actual.toJson(), true);
  }

  private static Stream<Arguments> provideShouldReturnExpectedProjectedDocument() {
    return Stream.of(
        Arguments.of(
            "edm/edm1.xml",
            Set.of("plainString"),
            "Demo",
            "Item",
            "Items",
            "ce124719-3fa3-4b8b-89cd-8bab06b03edc",
            "bson/edm1_case1.json",
            null),
        Arguments.of(
            "edm/edm3_complextype_with_circular_reference_collection.xml",
            Set.of("Addresses"),
            "Demo",
            "Item",
            "Items",
            "123e4567-e89b-12d3-a456-426614174090",
            "bson/edm3_case1.json",
            null),
        Arguments.of(
            "edm/edm4_complextype_with_long_circular_reference.xml",
            Set.of(
                "Definition/ExecutionContext/TriggeredBy",
                "Definition/WorkflowKey",
                "Definition/Steps"),
            "Workflow.Model",
            "WorkflowInstance",
            "WorkflowInstances",
            "550e8400-e29b-41d4-a716-446655440000",
            "bson/edm4_case1.json",
            null),
        Arguments.of(
            "edm/edm3_complextype_with_circular_reference_collection.xml",
            Set.of("plainString", "Name"),
            "Demo",
            "Item",
            "Items",
            "123e4567-e89b-12d3-a456-426614174090",
            "bson/edm3_case2.json",
            null),
        Arguments.of(
            "edm/edm3_complextype_with_circular_reference_collection.xml",
            Set.of("Addresses/City"),
            "Demo",
            "Item",
            "Items",
            "123e4567-e89b-12d3-a456-426614174090",
            "bson/edm3_case3.json",
            null),
        Arguments.of(
            "edm/edm4_complextype_with_long_circular_reference.xml",
            Set.of("InstanceId"),
            "Workflow.Model",
            "WorkflowInstance",
            "WorkflowInstances",
            "550e8400-e29b-41d4-a716-446655440000",
            "bson/edm4_case2.json",
            null),
        Arguments.of(
            "edm/edm4_complextype_with_long_circular_reference.xml",
            Set.of("Definition/Version"),
            "Workflow.Model",
            "WorkflowInstance",
            "WorkflowInstances",
            "550e8400-e29b-41d4-a716-446655440000",
            "bson/edm4_case3.json",
            null),
        Arguments.of(
            "edm/edm1.xml",
            Set.of("plainString"),
            "Demo",
            "Item",
            "Items",
            "8cb82df5-af62-49fc-b4f2-2df0a2d19524",
            "bson/edm1_case1_override_expected.json",
            EDM_1_MERGE_OVERRIDE_MONGO_NAME),
        Arguments.of(
            "edm/edm3_complextype_with_circular_reference_collection.xml",
            Set.of("Addresses/BackUpAddresses/ZipCode"),
            "Demo",
            "Item",
            "Items",
            "7ea5e361-d90f-4533-b3a7-f9d20dfe0e96",
            "bson/edm3_case1_override_expected.json",
            EDM_3_MERGE_OVERRIDE_NESTED_PROP));
  }
}
