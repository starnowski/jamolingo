package com.github.starnowski.jamolingo.compat.driver.operators.search;

import com.github.starnowski.jamolingo.AbstractItTest;
import com.github.starnowski.jamolingo.MongoAtlasResource;
import com.github.starnowski.jamolingo.core.operators.search.DefaultODataSearchToMongoAtlasSearchOptions;
import com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchOptions;
import com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchParser;
import com.github.starnowski.jamolingo.core.operators.search.SearchDocumentForQueryStringFactory;
import com.github.starnowski.jamolingo.core.operators.search.SearchDocumentForQueryStringFactory.QueryStringParsingResult;
import com.github.starnowski.jamolingo.core.operators.search.SearchOperatorResult;
import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.search.SearchExpression;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@QuarkusTestResource(MongoAtlasResource.class)
public class SearchOperatorTest extends AbstractItTest {

  @Inject protected MongoClient mongoClient;

  @Test
  public void shouldAddMatchStageWhenDefaultTextScoreIsProvided()
      throws UriValidationException, UriParserException, XMLStreamException {
    // GIVEN
    Edm edm = loadEmdProvider("edm/edm6_filter_main.xml");
    UriInfo uriInfo =
        new Parser(edm, OData.newInstance()).parseUri("examples2", "$search=database", null, null);
    ODataSearchToMongoAtlasSearchParser tested =
        new ODataSearchToMongoAtlasSearchParser(
            searchExpression -> new Document("queryString", new Document("query", "database")));
    ODataSearchToMongoAtlasSearchOptions options =
        DefaultODataSearchToMongoAtlasSearchOptions.builder().withDefaultTextScore(1.5).build();

    // WHEN
    SearchOperatorResult result = tested.parse(uriInfo.getSearchOption(), options);

    // THEN
    List<Bson> stages = result.getStageObjects();
    Assertions.assertEquals(2, stages.size());
    Assertions.assertTrue(((Document) stages.get(0)).containsKey("$search"));
    Assertions.assertTrue(((Document) stages.get(1)).containsKey("$match"));
    Document matchStage = (Document) ((Document) stages.get(1)).get("$match");
    Assertions.assertEquals(new Document("$gte", 1.5), matchStage.get("score"));
  }

  @ParameterizedTest
  @MethodSource("provideSearchTests")
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/search/search1.json"),
        @MongoDocument(
            database = "testdb",
            collection = "Items",
            bsonFilePath = "bson/search/search2.json")
      })
  public void shouldReturnExpectedDocumentsBasedOnSearchOperator(
      String search, Set<String> expectedPlainStrings)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException,
          InterruptedException {
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("testdb");
    MongoCollection<Document> collection = database.getCollection("Items");
    ensureSearchIndex(collection);

    Edm edm = loadEmdProvider("edm/edm6_filter_main.xml");
    UriInfo uriInfo =
        new Parser(edm, OData.newInstance()).parseUri("examples2", "$search=" + search, null, null);
    ODataSearchToMongoAtlasSearchParser tested =
        new ODataSearchToMongoAtlasSearchParser(
            new SearchDocumentForQueryStringFactory() {
              @Override
              public Bson build(
                  SearchExpression searchExpression,
                  QueryStringParsingResult queryStringParsingResult) {
                return new Document("index", "atlas_search_index")
                    .append(
                        "queryString",
                        new Document("query", queryStringParsingResult.getQuery())
                            .append("defaultPath", "plainString"));
              }
            });

    // WHEN
    SearchOperatorResult result = tested.parse(uriInfo.getSearchOption());
    List<Bson> pipeline = new ArrayList<>(result.getStageObjects());
    System.out.println(new Document("pipeline", pipeline).toJson());

    // THEN
    List<Document> results = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      results.clear();
      collection.aggregate(pipeline).into(results);
      if (results.size() == expectedPlainStrings.size()) {
        break;
      }
      Thread.sleep(500);
    }

    Assertions.assertEquals(expectedPlainStrings.size(), results.size());
    Set<String> actual =
        results.stream()
            .map(d -> d.get("plainString"))
            .filter(Objects::nonNull)
            .map(s -> (String) s)
            .collect(Collectors.toSet());
    Assertions.assertEquals(expectedPlainStrings, actual);
  }

  private void ensureSearchIndex(MongoCollection<Document> collection) {
    try {
      collection.createSearchIndex(
          "atlas_search_index", new Document("mappings", new Document("dynamic", true)));
      // Wait for index to be ready
      while (true) {
        boolean ready = false;
        for (Document index : collection.listSearchIndexes()) {
          if ("atlas_search_index".equals(index.getString("name"))
              && "READY".equals(index.getString("status"))) {
            ready = true;
            break;
          }
        }
        if (ready) break;
        Thread.sleep(500);
      }
    } catch (Exception e) {
      // Index might already exist
    }
  }

  private static java.util.stream.Stream<Arguments> provideSearchTests() {
    return java.util.stream.Stream.of(
        Arguments.of("database", Set.of("database search")),
        Arguments.of("search", Set.of("database search", "only search")),
        Arguments.of("database AND search", Set.of("database search")),
        Arguments.of("database OR \"only search\"", Set.of("database search", "only search")));
  }
}
