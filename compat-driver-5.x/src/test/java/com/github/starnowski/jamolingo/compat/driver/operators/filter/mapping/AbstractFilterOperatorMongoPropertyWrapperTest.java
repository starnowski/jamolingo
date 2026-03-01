package com.github.starnowski.jamolingo.compat.driver.operators.filter.mapping;

import com.github.starnowski.jamolingo.common.json.JSONOverrideHelper;
import com.github.starnowski.jamolingo.compat.driver.operators.filter.AbstractBaseFilterOperatorTest;
import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContextBuilder;
import com.github.starnowski.jamolingo.core.mapping.EntityMapping;
import com.github.starnowski.jamolingo.core.mapping.ODataMongoMappingFactory;
import com.github.starnowski.jamolingo.core.operators.filter.FilterOperatorResult;
import com.github.starnowski.jamolingo.core.operators.filter.ODataFilterToMongoMatchParser;
import com.github.starnowski.jamolingo.perf.ExplainAnalyzeResult;
import com.github.starnowski.jamolingo.perf.ExplainAnalyzeResultFactory;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;
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

public abstract class AbstractFilterOperatorMongoPropertyWrapperTest
    extends AbstractBaseFilterOperatorTest {

  @Inject protected MongoClient mongoClient;

  protected void shouldReturnExpectedDocumentsBasedOnFilterOperator(
      String filter, Set<String> expectedPlainStrings)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException,
          URISyntaxException,
          IOException {
    // plainString
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("testdb");
    MongoCollection<Document> collection = database.getCollection("Items");

    Edm edm = loadEmdProvider("edm/edm6_filter_main.xml");
    ODataMongoMappingFactory factory = new ODataMongoMappingFactory();
    var odataMapping = factory.build(edm.getSchema("MyService"));
    var entityMapping = odataMapping.getEntities().get("Example2");
    JSONOverrideHelper helper = new JSONOverrideHelper();
    String mergePayload =
        Files.readString(
            Paths.get(
                getClass()
                    .getClassLoader()
                    .getResource("mappings/edm6_override_wrapper_object.json")
                    .toURI()));
    entityMapping =
        helper.applyChangesToJson(
            entityMapping, mergePayload, EntityMapping.class, JSONOverrideHelper.PatchType.MERGE);

    EntityPropertiesMongoPathContextBuilder entityPropertiesMongoPathContextBuilder =
        new EntityPropertiesMongoPathContextBuilder();
    var context = entityPropertiesMongoPathContextBuilder.build(entityMapping);
    var facade =
        DefaultEdmMongoContextFacade.builder()
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
            .map(d -> d.get("plainString_wrapper"))
            .map(d -> (Document) d)
            .map(d -> d.get("plainString"))
            .filter(Objects::nonNull)
            .map(s -> (String) s)
            .collect(Collectors.toSet());
    Assertions.assertEquals(expectedPlainStrings, actual);
    logTestsObject(filter, pipeline);
  }

  protected void shouldUsedExpectedIndexesBasedOnFilterOperator(String filter, String expectedIndex)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException,
          IOException,
          URISyntaxException {
    // GIVEN
    dropIndexesForCollection("testdb", "Items");
    MongoDatabase database = mongoClient.getDatabase("testdb");
    MongoCollection<Document> collection = database.getCollection("Items");
    Edm edm = loadEmdProvider("edm/edm6_filter_main.xml");
    ODataMongoMappingFactory factory = new ODataMongoMappingFactory();
    var odataMapping = factory.build(edm.getSchema("MyService"));
    var entityMapping = odataMapping.getEntities().get("Example2");
    JSONOverrideHelper helper = new JSONOverrideHelper();
    String mergePayload =
        Files.readString(
            Paths.get(
                getClass()
                    .getClassLoader()
                    .getResource("mappings/edm6_override_wrapper_object.json")
                    .toURI()));
    entityMapping =
        helper.applyChangesToJson(
            entityMapping, mergePayload, EntityMapping.class, JSONOverrideHelper.PatchType.MERGE);

    EntityPropertiesMongoPathContextBuilder entityPropertiesMongoPathContextBuilder =
        new EntityPropertiesMongoPathContextBuilder();
    var context = entityPropertiesMongoPathContextBuilder.build(entityMapping);
    var facade =
        DefaultEdmMongoContextFacade.builder()
            .withEntityPropertiesMongoPathContext(context)
            .build();

    UriInfo uriInfo =
        new Parser(edm, OData.newInstance()).parseUri("examples2", "$filter=" + filter, null, null);
    ODataFilterToMongoMatchParser tested = new ODataFilterToMongoMatchParser();
    FilterOperatorResult result = tested.parse(uriInfo.getFilterOption(), edm, facade);
    /*
     * Important! This test purpose is not to validate correct used properties.
     * Such tests are part of the core module where tests checks if the ODataFilterToMongoMatchParser
     * returns correct properties.
     */
    List<String> usedProperties = result.getUsedMongoDocumentProperties();
    createIndexesForPropertyInCollection("testdb", "Items", new HashSet<>(usedProperties));
    List<Bson> pipeline = new ArrayList<>(result.getStageObjects());
    ExplainAnalyzeResultFactory explainAnalyzeResultFactory = new ExplainAnalyzeResultFactory();
    Document explainDoc = collection.aggregate(pipeline).explain();

    // WHEN
    ExplainAnalyzeResult explainResult = explainAnalyzeResultFactory.build(explainDoc);

    // THEN
    Assertions.assertEquals(expectedIndex, explainResult.getIndexValue().getValue());
  }
}
