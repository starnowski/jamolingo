package com.github.starnowski.jamolingo;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(EmbeddedMongoResource.class)
public class TopOperatorTest extends AbstractItTest {


}
