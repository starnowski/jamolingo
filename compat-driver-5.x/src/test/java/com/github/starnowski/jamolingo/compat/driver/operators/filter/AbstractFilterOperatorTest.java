package com.github.starnowski.jamolingo.compat.driver.operators.filter;

import com.github.starnowski.jamolingo.AbstractItTest;
import com.github.starnowski.jamolingo.EmbeddedMongoResource;
import com.github.starnowski.jamolingo.core.operators.filter.FilterOperatorResult;
import com.github.starnowski.jamolingo.core.operators.filter.ODataFilterToMongoMatchParser;
import com.github.starnowski.jamolingo.perf.ExplainAnalyzeResult;
import com.github.starnowski.jamolingo.perf.ExplainAnalyzeResultFactory;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

@QuarkusTest
@QuarkusTestResource(EmbeddedMongoResource.class)
public abstract class AbstractFilterOperatorTest extends AbstractItTest {

  @Inject protected MongoClient mongoClient;

  protected void shouldReturnExpectedDocumentsBasedOnFilterOperator(
      String filter, Set<String> expectedPlainStrings)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException {
    // plainString
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("testdb");
    MongoCollection<Document> collection = database.getCollection("Items");
    Edm edm = loadEmdProvider("edm/edm6_filter_main.xml");
    UriInfo uriInfo =
        new Parser(edm, OData.newInstance()).parseUri("examples2", "$filter=" + filter, null, null);
    ODataFilterToMongoMatchParser tested = new ODataFilterToMongoMatchParser();

    // WHEN
    FilterOperatorResult result = tested.parse(uriInfo.getFilterOption(), edm);
    List<Bson> pipeline = new ArrayList<>(result.getStageObjects());
    System.out.println(new Document("pipeline", pipeline).toJson());
    List<Document> results = new ArrayList<>();
    collection.aggregate(pipeline).into(results);

    // THEN
    Assertions.assertEquals(expectedPlainStrings.size(), results.size());
    Set<String> actual =
        results.stream()
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
          ODataApplicationException {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("testdb");
    MongoCollection<Document> collection = database.getCollection("Items");
    Edm edm = loadEmdProvider("edm/edm6_filter_main.xml");
    UriInfo uriInfo =
        new Parser(edm, OData.newInstance()).parseUri("examples2", "$filter=" + filter, null, null);
    ODataFilterToMongoMatchParser tested = new ODataFilterToMongoMatchParser();
    FilterOperatorResult result = tested.parse(uriInfo.getFilterOption(), edm);
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

  protected void createIndexesForPropertyInCollection(
      String database, String collectionName, Set<String> properties) {
    properties.forEach(pro -> createIndexForPropertyInCollection(database, collectionName, pro));
  }

  protected void createIndexForPropertyInCollection(
      String database, String collectionName, String property) {
    MongoCollection<Document> col = mongoClient.getDatabase(database).getCollection(collectionName);
    col.createIndex(new Document(property, 1));
  }

  protected void dropIndexesForCollections(String database, Set<String> collectionNames) {
    collectionNames.forEach(col -> dropIndexesForCollection(database, col));
  }

  protected void dropIndexesForCollection(String database, String collectionName) {
    try {

      MongoCollection<Document> col =
          mongoClient.getDatabase(database).getCollection(collectionName);
      col.dropIndexes();
    } catch (Exception exception) {
      // Do nothing
      exception.printStackTrace();
    }
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
