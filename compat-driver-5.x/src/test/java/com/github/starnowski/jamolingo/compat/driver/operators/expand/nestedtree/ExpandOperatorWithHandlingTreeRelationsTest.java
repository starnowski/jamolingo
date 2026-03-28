package com.github.starnowski.jamolingo.compat.driver.operators.expand.nestedtree;

import com.github.starnowski.jamolingo.AbstractItTest;
import com.github.starnowski.jamolingo.EmbeddedMongoResource;
import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.compat.driver.operators.filter.FilterTestsCasesAggregator;
import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.xml.stream.XMLStreamException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@QuarkusTest
@QuarkusTestResource(value = EmbeddedMongoResource.class, restrictToAnnotatedClass = true)
public class ExpandOperatorWithHandlingTreeRelationsTest extends AbstractItTest {


  private static Stream<Arguments>
  provideData() {
    //TODO
    return Stream.of(
            Arguments.of(
                    "complexList/any(c:c/someString eq 'Apple')", Set.of("Doc1", "Doc4"), "FETCH + IXSCAN"));
  }

  @ParameterizedTest
  @MethodSource("provideData")
  @MongoSetup(
      mongoDocuments = {
        @MongoDocument(
            database = "testdb",
            collection = "categories",
            bsonFilePath = "bson/expand/query/category_1.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_1.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_2.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_3.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_4.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_5.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_6.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_7.json"),
        @MongoDocument(
            database = "testdb",
            collection = "examples",
            bsonFilePath = "bson/expand/query/example2_root.json")
      })
  public void shouldReturnExpectedDocumentsForQueryObject(
      String filter, Set<String> expectedPlainStrings)
      throws UriValidationException,
          UriParserException,
          XMLStreamException,
          ExpressionVisitException,
          ODataApplicationException {
    //todo
  }

}
