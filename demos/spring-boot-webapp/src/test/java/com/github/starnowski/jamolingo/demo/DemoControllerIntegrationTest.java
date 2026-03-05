package com.github.starnowski.jamolingo.demo;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import com.github.starnowski.jamolingo.junit5.SpringMongoDataLoaderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    classes = {JamolingoDemoApplication.class},
    properties = {"spring.data.mongodb.uri=mongodb://localhost:27017/demos"})
@AutoConfigureMockMvc
@ExtendWith(SpringMongoDataLoaderExtension.class)
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
public class DemoControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  public void shouldFilterByPlainString() throws Exception {
    mockMvc
        .perform(get("/query").param("filter", "plainString eq 'Poem'"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.value", hasSize(1)))
        .andExpect(jsonPath("$.value[0].plainString", is("Poem")));
  }

  @Test
  public void shouldOrderAndLimit() throws Exception {
    mockMvc
        .perform(get("/query").param("orderby", "plainString desc").param("top", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.value", hasSize(2)))
        .andExpect(jsonPath("$.value[0].plainString", is("example2")))
        .andExpect(jsonPath("$.value[1].plainString", is("example1")));
  }

  @Test
  public void shouldSkipAndLimit() throws Exception {
    // sorted desc: example2, example1, eOMtThyhVNLWUZNRcBaQKxI, Some text, Poem, Oleksa, Mario
    mockMvc
        .perform(
            get("/query").param("orderby", "plainString desc").param("skip", "3").param("top", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.value", hasSize(1)))
        .andExpect(jsonPath("$.value[0].plainString", is("Some text")));
  }

  @Test
  public void shouldSelectFields() throws Exception {
    mockMvc
        .perform(
            get("/query").param("filter", "plainString eq 'Poem'").param("select", "plainString"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.value[0].plainString", is("Poem")))
        .andExpect(jsonPath("$.value[0].tags").doesNotExist());
  }

  @Test
  public void shouldReturnCount() throws Exception {
    mockMvc
        .perform(get("/query").param("filter", "contains(plainString, 'e')").param("count", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.value", hasSize(6)))
        .andExpect(jsonPath("$['@odata.count']", is(6)));
  }
}
