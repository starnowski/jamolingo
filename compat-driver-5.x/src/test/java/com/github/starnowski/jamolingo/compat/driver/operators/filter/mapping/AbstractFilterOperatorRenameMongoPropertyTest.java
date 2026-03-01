package com.github.starnowski.jamolingo.compat.driver.operators.filter.mapping;

import com.github.starnowski.jamolingo.AbstractItTest;
import com.github.starnowski.jamolingo.EmbeddedMongoResource;
import com.github.starnowski.jamolingo.common.json.JSONOverrideHelper;
import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContextBuilder;
import com.github.starnowski.jamolingo.core.mapping.EntityMapping;
import com.github.starnowski.jamolingo.core.mapping.ODataMongoMappingFactory;
import com.github.starnowski.jamolingo.core.operators.filter.FilterOperatorResult;
import com.github.starnowski.jamolingo.core.operators.filter.ODataFilterToMongoMatchParser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Assertions;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@QuarkusTest
@QuarkusTestResource(EmbeddedMongoResource.class)
public abstract class AbstractFilterOperatorRenameMongoPropertyTest extends AbstractItTest {

  @Inject protected MongoClient mongoClient;

  protected void shouldReturnExpectedDocumentsBasedOnFilterOperator(
      String filter, Set<String> expectedPlainStrings)
          throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException, URISyntaxException, IOException {
    // plainString
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("testdb");
    MongoCollection<Document> collection = database.getCollection("Items");

    Edm edm = loadEmdProvider("edm/edm6_filter_main.xml");
    ODataMongoMappingFactory factory = new ODataMongoMappingFactory();
    var odataMapping = factory.build(edm.getSchema("MyService"));
    var entityMapping = odataMapping.getEntities().get("Example2");
    JSONOverrideHelper helper = new JSONOverrideHelper();
    String mergePayload = Files.readString(Paths.get(getClass().getClassLoader().getResource("mappings/edm6_override_renamed_prefix.json").toURI()));
    entityMapping = helper.applyChangesToJson(entityMapping, mergePayload, EntityMapping.class, JSONOverrideHelper.PatchType.MERGE);

    EntityPropertiesMongoPathContextBuilder entityPropertiesMongoPathContextBuilder =
        new EntityPropertiesMongoPathContextBuilder();
    var context = entityPropertiesMongoPathContextBuilder.build(entityMapping);
    var facade = DefaultEdmMongoContextFacade.builder()
            .withEntityPropertiesMongoPathContext(context)
            .build();

    UriInfo uriInfo =
        new Parser(edm, OData.newInstance()).parseUri("examples2", "$filter=" + filter, null, null);
    ODataFilterToMongoMatchParser tested = new ODataFilterToMongoMatchParser();

    // WHEN
    FilterOperatorResult result = tested.parse(uriInfo.getFilterOption(), edm, facade);
    List<Bson> pipeline = new ArrayList<>(result.getStageObjects());
    System.out.println(new Document("pipeline", pipeline).toJson());
    List<Document> results = new ArrayList<>();
    collection.aggregate(pipeline).into(results);

    // THEN
    Assertions.assertEquals(expectedPlainStrings.size(), results.size());
    Set<String> actual =
        results.stream()
            .map(d -> d.get("renamed_plainString"))
            .filter(Objects::nonNull)
            .map(s -> (String) s)
            .collect(Collectors.toSet());
    Assertions.assertEquals(expectedPlainStrings, actual);
    logTestsObject(filter, pipeline);
  }

  private void logTestsObject(String filterString, List<Bson> pipeline) {
    String path =
        Paths.get(new File(getClass().getClassLoader().getResource(".").getFile()).getPath())
            .toAbsolutePath()
            .toString();
    System.out.println("File path is " + path);
    try (FileOutputStream outputStream =
        new FileOutputStream(path + File.separator + "testcases.txt", true)) {
      outputStream.write("<test>".getBytes(StandardCharsets.UTF_8));
      outputStream.write(
          ("<filter>$filter=" + filterString + "</filter>").getBytes(StandardCharsets.UTF_8));
      outputStream.write(
          ("<pipeline>"
                  + pipeline
                      .get(0)
                      .toBsonDocument(Document.class, this.mongoClient.getCodecRegistry())
                      .toJson()
                  + "</pipeline>")
              .getBytes(StandardCharsets.UTF_8));
      outputStream.write("</test>".getBytes(StandardCharsets.UTF_8));
      outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
