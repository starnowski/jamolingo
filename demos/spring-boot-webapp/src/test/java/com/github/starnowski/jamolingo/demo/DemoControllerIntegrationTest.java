package com.github.starnowski.jamolingo.demo;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    classes = {JamolingoDemoApplication.class, TestMongoConfig.class},
    properties = {
      "spring.data.mongodb.uri=mongodb://localhost:27018/demos",
      "spring.liquibase.url=mongodb://localhost:27018/demos",
      "spring.liquibase.enabled=true"
    })
@AutoConfigureMockMvc
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
