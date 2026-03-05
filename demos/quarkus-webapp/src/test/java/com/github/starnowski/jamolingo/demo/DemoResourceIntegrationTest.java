package com.github.starnowski.jamolingo.demo;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import com.github.starnowski.jamolingo.junit5.QuarkusMongoDataLoaderExtension;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@QuarkusTest
@ExtendWith(QuarkusMongoDataLoaderExtension.class)
@MongoSetup(
    mongoDocuments = {
      @MongoDocument(database = "demos", collection = "items", bsonFilePath = "bson/doc1.json"),
      @MongoDocument(database = "demos", collection = "items", bsonFilePath = "bson/doc2.json"),
      @MongoDocument(database = "demos", collection = "items", bsonFilePath = "bson/doc3.json"),
      @MongoDocument(database = "demos", collection = "items", bsonFilePath = "bson/doc4.json"),
      @MongoDocument(database = "demos", collection = "items", bsonFilePath = "bson/doc5.json"),
      @MongoDocument(database = "demos", collection = "items", bsonFilePath = "bson/doc6.json"),
      @MongoDocument(database = "demos", collection = "items", bsonFilePath = "bson/doc7.json")
    })
public class DemoResourceIntegrationTest {

  @Test
  public void shouldFilterByPlainString() {
    given()
        .queryParam("filter", "plainString eq 'Poem'")
        .when()
        .get("/query")
        .then()
        .statusCode(200)
        .body("value", hasSize(1))
        .body("value[0].plainString", is("Poem"));
  }

  @Test
  public void shouldOrderAndLimit() {
    given()
        .queryParam("orderby", "plainString desc")
        .queryParam("top", "2")
        .when()
        .get("/query")
        .then()
        .statusCode(200)
        .body("value", hasSize(2))
        .body("value[0].plainString", is("example2"))
        .body("value[1].plainString", is("example1"));
  }

  @Test
  public void shouldSkipAndLimit() {
    given()
        .queryParam("orderby", "plainString desc")
        .queryParam("skip", "3")
        .queryParam("top", "1")
        .when()
        .get("/query")
        .then()
        .statusCode(200)
        .body("value", hasSize(1))
        .body("value[0].plainString", is("Some text"));
  }

  @Test
  public void shouldSelectFields() {
    given()
        .queryParam("filter", "plainString eq 'Poem'")
        .queryParam("select", "plainString")
        .when()
        .get("/query")
        .then()
        .statusCode(200)
        .body("value[0].plainString", is("Poem"))
        .body("value[0].tags", is((Object) null));
  }

  @Test
  public void shouldReturnCount() {
    given()
        .queryParam("filter", "contains(plainString, 'e')")
        .queryParam("count", "true")
        .when()
        .get("/query")
        .then()
        .statusCode(200)
        .body("value", hasSize(6))
        .body("'@odata.count'", is(6));
  }
}
