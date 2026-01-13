package com.github.starnowski.jamolingo;

import com.github.starnowski.jamolingo.context.DefaultEdmMongoContextFacade;
import com.github.starnowski.jamolingo.context.EntityPropertiesMongoPathContextBuilder;
import com.github.starnowski.jamolingo.context.ODataMongoMappingFactory;
import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import com.github.starnowski.jamolingo.junit5.QuarkusMongoDataLoaderExtension;
import com.github.starnowski.jamolingo.select.OdataSelectToMongoProjectParser;
import com.github.starnowski.jamolingo.select.SelectOperatorResult;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;

@QuarkusTest
@ExtendWith(QuarkusMongoDataLoaderExtension.class)
@QuarkusTestResource(EmbeddedMongoResource.class)
class SelectOperatorTest {

  @Inject MongoClient mongoClient;

  @ParameterizedTest
  @MethodSource("provideShouldReturnExpectedProjectedDocument")
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/simple_item.json"),
      })
  public void shouldReturnExpectedProjectedDocument(
      String edmPath, String selectClause, String expectedId, String expectedDataPath)
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
            "plainString",
            "ce124719-3fa3-4b8b-89cd-8bab06b03edc",
            "bson/expected_case1.json"));
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

    String json =
        Files.readString(
            Paths.get(
                new File(getClass().getClassLoader().getResource(filePath).getFile()).getPath()));
    return Document.parse(json);
  }
}
