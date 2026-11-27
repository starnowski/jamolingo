package com.github.starnowski.jamolingo.perf;

import com.github.starnowski.jamolingo.junit5.QuarkusMongoDataLoaderExtension;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;

@Disabled
@QuarkusTest
@QuarkusTestResource(value = EmbeddedMongoResource.class, restrictToAnnotatedClass = true)
@ExtendWith(QuarkusMongoDataLoaderExtension.class)
public class ExplainAnalyzeResultFactoryLookUpStageTest {

  // test data
  // items (parent - child)
  // item 1
  // item 2 -> parent item 1
  // item 3 -> parent item 1
  // item 4 -> parent item 1

  // test case
  // $match with _id equal 1 and $lookup "children" with index on parent
  // $match with _id equal 1 and $lookup "children" with no index on parent
  // $lookup "children" with index on parent
  // $lookup "children" with no index on parent
  // $match with _id equal 1 and $lookup "children" with index on parent and $lookup
  // "children.children" with index on parent
  // $match with _id equal 1 and $lookup "children.children" with no index on parent
  // $lookup "children.children" with index on parent
  // $lookup "children.children" with no index on parent
}
