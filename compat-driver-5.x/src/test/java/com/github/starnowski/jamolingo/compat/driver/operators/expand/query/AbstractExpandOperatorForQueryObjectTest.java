package com.github.starnowski.jamolingo.compat.driver.operators.expand.query;

import com.github.starnowski.jamolingo.AbstractItTest;
import com.github.starnowski.jamolingo.EmbeddedMongoResource;
import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.operators.expand.ExpandOperatorResult;
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser;
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
import org.apache.olingo.server.core.uri.parser.Parser;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;

@QuarkusTest
@QuarkusTestResource(value = EmbeddedMongoResource.class, restrictToAnnotatedClass = true)
public abstract class AbstractExpandOperatorForQueryObjectTest extends AbstractItTest {

  @Inject protected MongoClient mongoClient;

  protected void shouldReturnExpectedDocumentsBasedOnQueryObjectForFilterOperator(
      String filter,
      Set<String> expectedPlainStrings,
      Map<KeyValue<String, String>, KeyValue<String, String>> edmTablesToMongoDBCollections)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException {
    // plainString
    // GIVEN
    MongoDatabase database = mongoClient.getDatabase("testdb");
    MongoCollection<Document> collection = database.getCollection("Items");
    Edm edm = loadEmdProvider("edm/edm7_graph_lookup.xml");
    UriInfo uriInfo =
        new Parser(edm, OData.newInstance())
            .parseUri(
                "examples2", "$filter=_id eq 100&$expand=children($filter=" + filter + ";$levels=2)", null, null);
    ODataExpandToMongoAggregationPipelineParser tested =
        new ODataExpandToMongoAggregationPipelineParser();

    // WHEN
    ExpandOperatorResult result =
        tested.parse(
            uriInfo.getExpandOption(),
            ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext.builder()
                .withEdmTablesToMongoDBCollections(edmTablesToMongoDBCollections)
                .build());
    List<Document> results = collection.aggregate(result.getStageObjects()).into(new ArrayList<>());

    // THEN
    Set<String> actual =
        results.get(0).getList("examples", Document.class, List.of()).stream()
            .map(d -> d.get("plainString"))
            .filter(Objects::nonNull)
            .map(s -> (String) s)
            .collect(Collectors.toSet());
    Assertions.assertEquals(expectedPlainStrings, actual);
  }
  // TODO Add same tests but without $level
}
