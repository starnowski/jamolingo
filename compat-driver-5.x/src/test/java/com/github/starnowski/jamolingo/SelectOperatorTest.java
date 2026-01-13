package com.github.starnowski.jamolingo;

import com.github.starnowski.jamolingo.junit5.MongoDocument;
import com.github.starnowski.jamolingo.junit5.MongoSetup;
import com.github.starnowski.jamolingo.junit5.QuarkusMongoDataLoaderExtension;
import com.mongodb.client.MongoClient;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.json.JSONException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;

@QuarkusTest
@ExtendWith(QuarkusMongoDataLoaderExtension.class)
class SelectOperatorTest {

    @Inject
    private MongoClient mongoClient;

    public static Stream<Arguments> provideShouldReturnBadRequestForInvalidPayload() {
        return Stream.of(
                Arguments.of("examples/invalid_request_example2.json", "examples/oas_response_example2.json")
        );
    }

    private static String prepareResponseForQueryWithExpectedJsonObject(String json) {
        return """
                {
                  "results": [
                    %s
                  ],
                  "winningPlan": "COLLSCAN"
                
                }
                """
                .formatted(json);
    }

    public static Stream<Arguments> provideShouldReturnResponseStringBasedOnFiltersExample2() {
        return Stream.of(
                Arguments.of(List.of("plainString"), prepareResponseForQueryWithExpectedJsonObject("""
                        {
                            "plainString": "example1"
                        }
                        """)),
                Arguments.of(List.of("plainString", "nestedObject"), prepareResponseForQueryWithExpectedJsonObject("""
                        {
                            "plainString": "example1",
                            "nestedObject": {
                                "tokens": ["first example", "1 ex"],
                                "numbers": [1, 2, 3, 4, 5, 6, 26, 27, 28],
                                "index": "c"
                              }
                        }
                        """)),
                Arguments.of(List.of("plainString", "nestedObject/tokens", "nestedObject/index"), prepareResponseForQueryWithExpectedJsonObject("""
                        {
                            "plainString": "example1",
                            "nestedObject": {
                                "tokens": ["first example", "1 ex"],
                                "index": "c"
                              }
                        }
                        """)),
                Arguments.of(List.of("plainString", "nestedObject/tokens"), prepareResponseForQueryWithExpectedJsonObject("""
                        {
                            "plainString": "example1",
                            "nestedObject": {
                                "tokens": ["first example", "1 ex"]
                              }
                        }
                        """)),
                Arguments.of(List.of("*"), prepareResponseForQueryWithExpectedJsonObject("""
                        {
                              "plainString": "example1",
                              "password": "bbb",
                              "nestedObject": {
                                "tokens": ["first example", "1 ex"],
                                "numbers": [1, 2, 3, 4, 5, 6, 26, 27, 28],
                                "index": "c"
                              }
                        }
                        """))


        );
    }

    @PostConstruct
    public void init() {
    }


    @ParameterizedTest
    @MethodSource({
            "provideShouldReturnResponseStringBasedOnFiltersExample2"
    })
    @MongoSetup(mongoDocuments = {
            @MongoDocument(database = "test", bsonFilePath = "examples/query/example2_6.json", collection = "examples")
    })
    public void shouldReturnResponseStringBasedOnFiltersExample2(List<String> select, String expectedResponse) throws IOException, JSONException {
        // WHEN
        ExtractableResponse<Response> getResponse = given()
                .when()
                .queryParams(Map.of("$select", select))
                .get("/examples2/simple-query")
                .then()
                .statusCode(200).extract();

        // THEN
        JSONAssert.assertEquals(expectedResponse, getResponse.asString(), true);
    }
}