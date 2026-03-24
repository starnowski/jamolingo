package com.github.starnowski.jamolingo.compat.driver.operators.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

public class FilterTestsCasesAggregator {

  public static final Set<String> ALL_PLAIN_STRINGS =
      Set.of(
          "eOMtThyhVNLWUZNRcBaQKxI",
          "Some text",
          "Poem",
          "Mario",
          "Oleksa",
          "example1",
          "example2");

  public static Stream<Arguments> provideShouldReturnExpectedProjectedDocumentForBasicFiltering() {
    return Stream.of(
        Arguments.of(
            "plainString eq 'eOMtThyhVNLWUZNRcBaQKxI'",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "tolower(plainString) eq 'eomtthyhvnlwuznrcbaqkxi'",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "COLLSCAN"),
        Arguments.of(
            "tolower(plainString) eq tolower('eOMtThyhVNLWUZNRcBaQKxI')",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "COLLSCAN"),
        Arguments.of(
            "toupper(plainString) eq 'EOMTTHYHVNLWUZNRCBAQKXI'",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "COLLSCAN"),
        Arguments.of("plainString eq 'Some text'", Set.of("Some text"), "FETCH + IXSCAN"),
        Arguments.of(
            "plainString in ('Some text', 'no such text')", Set.of("Some text"), "FETCH + IXSCAN"),
        Arguments.of("startswith(plainString,'So')", Set.of("Some text"), "FETCH + IXSCAN"),
        Arguments.of(
            "startswith(plainString,'So') and plainString eq 'Some text'",
            Set.of("Some text"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "startswith(plainString,'Some t') and smallInteger eq -1188957731",
            Set.of("Some text"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "startswith(plainString,'Po') or smallInteger eq -113",
            Set.of("Poem"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "timestamp ge 2024-07-20T10:00:00.00Z and timestamp le 2024-07-20T20:00:00.00Z",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "plainString eq 'eOMtThyhVNLWUZNRcBaQKxI' and uuidProp eq b921f1dd-3cbc-0495-fdab-8cd14d33f0aa",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "uuidProp eq b921f1dd-3cbc-0495-fdab-8cd14d33f0aa",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Poem", "Some text"),
            "FETCH + IXSCAN"),
        Arguments.of("toupper(plainString) eq 'POEM'", Set.of("Poem"), "COLLSCAN"),
        Arguments.of("tolower(plainString) eq 'poem'", Set.of("Poem"), "COLLSCAN"),
        Arguments.of("tags/any(t:t in ('developer', 'LLM'))", Set.of("Poem"), "FETCH + IXSCAN"),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and t ne 'spiderweb' or startswith(t,'spider') and t ne 'spider' or contains(t,'wide') and t ne 'word wide')",
            Set.of("Some text", "eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and t ne 'spiderweb' or endswith(t,'web') and t ne 'spiderwebgg' or contains(t,'wide') and t ne 'word wide')",
            Set.of("Some text", "eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "plainString eq 'eOMtThyhVNLWUZNRcBaQKxI' and password eq 'password1'",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "plainString eq 'eOMtThyhVNLWUZNRcBaQKxI' or password eq 'password1'",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "tags/any(t:t eq 'developer') or tags/any(t:t eq 'LLM')",
            Set.of("Poem"),
            "FETCH + IXSCAN"),
        Arguments.of("tags/any(t:startswith(t,'dev'))", Set.of("Poem"), "FETCH + IXSCAN"),
        Arguments.of(
            "tags/any(t:startswith(t,'dev') and length(t) eq 9)", Set.of("Poem"), "COLLSCAN"),
        Arguments.of("tags/any(t:length(t) eq 13)", Set.of("eOMtThyhVNLWUZNRcBaQKxI"), "COLLSCAN"),
        Arguments.of("tags/any(t:tolower(t) eq 'developer')", Set.of("Poem"), "COLLSCAN"),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and endswith(t, 'web'))",
            Set.of("Some text"),
            "COLLSCAN"),
        Arguments.of(
            "year(birthDate) eq 2024",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of(
            "month(birthDate) eq 6",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of(
            "day(birthDate) eq 18",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of(
            "hour(timestamp) eq 10",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of(
            "ceiling(floatValue) eq 1",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of(
            "tags/$count ge 2",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem", "Mario", "Oleksa"),
            "COLLSCAN"),
        Arguments.of("tags/$count ge 3", Set.of("Poem", "Oleksa"), "COLLSCAN"),
        Arguments.of("trim('   Poem   ') eq 'Poem'", ALL_PLAIN_STRINGS, "COLLSCAN"),
        Arguments.of(
            "round(floatValue) eq 1",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of(
            "minute(timestamp) eq 15",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of(
            "second(timestamp) eq 26",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of(
            "floor(floatValue) eq 0",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem"),
            "COLLSCAN"),
        Arguments.of("length(plainString) eq 4", Set.of("Poem"), "COLLSCAN"),
        Arguments.of(
            "nestedObject/tokens/any(t:t ne 'no such text')",
            Set.of("example1", "example2"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "tags/all(t:contains(t,'starlord') or contains(t,'trek') or contains(t,'wars'))",
            Set.of("Mario", "Oleksa", "example1", "example2"),
            "COLLSCAN"),
        Arguments.of(
            "tags/all(t:contains(t,'starlord') or contains(t,'trek') or contains(t,'wars')) and tags/any()",
            Set.of("Mario", "Oleksa"),
            "COLLSCAN"));
  }

  ///  ANY LAMBDA FILTERS
  public static Stream<Arguments> provideShouldReturnExpectedProjectedDocumentForAnyLambda() {
    return Stream.of(
        Arguments.of(
            "tags/any(t:t eq 'word wide web')",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "tags/any(t:startswith(t,'star'))", Set.of("Mario", "Oleksa"), "FETCH + IXSCAN"),
        Arguments.of(
            "tags/any(t:contains(t,'spider'))",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "numericArray/any(n:n gt 25)",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Mario", "Oleksa"),
            "FETCH + IXSCAN"),
        Arguments.of("numericArray/any(n:n lt 10)", Set.of("Some text", "Poem"), "FETCH + IXSCAN"),
        Arguments.of(
            "tags/any(t:contains(tolower(t),'star'))", Set.of("Mario", "Oleksa"), "COLLSCAN"),
        Arguments.of(
            "tags/any(t:endswith(toupper(t),'TRAP'))",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "COLLSCAN"),
        Arguments.of(
            "tags/any(t:length(t) eq 8)", Set.of("Some text", "Poem", "Oleksa"), "COLLSCAN"),
        Arguments.of("numericArray/any(n:n add 2 gt 100)", Set.of("Mario"), "COLLSCAN"),
        Arguments.of(
            List.of("tags/any(t:t ne 'no such text' and t ne 'no such word')"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem", "Mario", "Oleksa"),
            "COLLSCAN"),
        Arguments.of(
            List.of("tags/any(t:startswith(t,'star') and t ne 'starlord')"),
            Set.of("Mario", "Oleksa"),
            "FETCH + IXSCAN"),
        Arguments.of(
            List.of("tags/any(t:startswith(t,'star') or t ne 'starlord')"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem", "Mario", "Oleksa"),
            "FETCH + IXSCAN"),
        Arguments.of(
            List.of("tags/any(t:startswith(t,'star ') or t eq 'starlord')"),
            Set.of("Mario", "Oleksa"),
            "FETCH + IXSCAN"),
        Arguments.of(
            List.of("tags/any(t:startswith(t,'starlord') or t in ('star trek', 'star wars'))"),
            Set.of("Mario", "Oleksa"),
            "FETCH + IXSCAN"),
        Arguments.of(
            List.of(
                "tags/any(t:contains(t,'starlord') or contains(t,'trek') or contains(t,'wars'))"),
            Set.of("Mario", "Oleksa"),
            "FETCH + IXSCAN"),
        Arguments.of(
            List.of("tags/any(t:contains(t,'starlord'))"), Set.of("Oleksa"), "FETCH + IXSCAN"),
        Arguments.of(
            List.of("tags/any(t:endswith(t,'web') or endswith(t,'trap'))"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text"),
            "FETCH + IXSCAN"),
        Arguments.of(
            List.of("tags/any(t:length(t) eq 9)"),
            Set.of("Some text", "Poem", "Mario", "Oleksa"),
            "COLLSCAN"),
        Arguments.of(
            List.of("numericArray/any(n:n gt 5)"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Mario", "Oleksa"),
            "FETCH + IXSCAN"),
        Arguments.of(
            List.of("numericArray/any(n:n gt floor(5.05))"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Mario", "Oleksa"),
            "COLLSCAN"),
        Arguments.of(
            List.of("numericArray/any(n:n add 2 gt round(n))"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text", "Poem", "Mario", "Oleksa"),
            "COLLSCAN"),
        Arguments.of(
            List.of("numericArray/any(n:n eq 10 or n eq 20 or n eq 30)"),
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        // Additional tests from user list
        Arguments.of(
            "nestedObject/tokens/any(t:t eq 'first example') and nestedObject/numbers/any(t:t gt 5 and t lt 27)",
            Set.of("example1"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "nestedObject/tokens/any(t:t ne 'no such text')",
            Set.of("example1", "example2"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and t eq 'spiderweb')",
            Set.of("Some text"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and t ne 'spiderweb' or endswith(t,'web') and t ne 'spiderweb' or contains(t,'wide') and t ne 'word wide')",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and t ne 'spiderweb' or endswith(t,'web') and t ne 'spiderweb')",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and t ne 'spiderweb' or endswith(t,'web') and t ne 'spiderwebgg' or contains(t,'wide') and t ne 'word wide')",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and t ne 'spiderweb' or startswith(t,'spider') and t ne 'spider' or contains(t,'wide') and t ne 'word wide')",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI", "Some text"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "tags/any(t:startswith(t,'spider') and t ne 'spiderweb')",
            Set.of("eOMtThyhVNLWUZNRcBaQKxI"),
            "FETCH + IXSCAN"));
  }

  public static Stream<Arguments>
      provideShouldReturnExpectedProjectedDocumentForComplexListForAnyLambda() {
    return Stream.of(
        Arguments.of(
            "complexList/any(c:c/someString eq 'Apple')", Set.of("Doc1", "Doc4"), "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:c/someNumber gt 35)",
            Set.of("Doc2", "Doc3", "Doc4", "Doc6"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:c/someString eq 'Banana' or c/someString eq 'Cherry')",
            Set.of("Doc2", "Doc3"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:c/nestedComplexArray/any(n:n/stringVal eq 'val1'))",
            Set.of("Doc1", "Doc2", "Doc4"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:startswith(c/someString,'Ap'))",
            Set.of("Doc1", "Doc4", "Doc5"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:contains(c/someString,'ana'))", Set.of("Doc2"), "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:endswith(c/someString,'erry'))",
            Set.of("Doc3", "Doc4"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:contains(c/someString,'e'))",
            Set.of("Doc1", "Doc3", "Doc4", "Doc6"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:c/someString eq 'Application')",
            Set.of("Doc1", "Doc5"),
            "FETCH + IXSCAN"),
        // Missing complex numeric tests
        Arguments.of(
            "complexList/any(c:c/someNumber gt 5)",
            Set.of("Doc1", "Doc2", "Doc3", "Doc4", "Doc5", "Doc6"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:c/someNumber gt 25)",
            Set.of("Doc2", "Doc3", "Doc4", "Doc6"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:c/someNumber lt 25)",
            Set.of("Doc1", "Doc4", "Doc5"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:c/someNumber eq 10 or c/someNumber eq 20)",
            Set.of("Doc1", "Doc4", "Doc5"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:c/someNumber add 5 gt 20)",
            Set.of("Doc1", "Doc2", "Doc3", "Doc4", "Doc5", "Doc6"),
            "COLLSCAN"),
        Arguments.of(
            "complexList/any(c:c/someNumber gt floor(5.05))",
            Set.of("Doc1", "Doc2", "Doc3", "Doc4", "Doc5", "Doc6"),
            "COLLSCAN"),
        Arguments.of(
            "complexList/any(c:c/someNumber add 2 gt round(c/someNumber))",
            Set.of("Doc1", "Doc2", "Doc3", "Doc4", "Doc5", "Doc6"),
            "COLLSCAN"),
        Arguments.of(
            "complexList/any(c:c/someNumber eq 20)", Set.of("Doc1", "Doc5"), "FETCH + IXSCAN"),
        // Missing nested complex tests
        Arguments.of(
            "complexList/any(c:c/nestedComplexArray/any(n:startswith(n/stringVal,'val')))",
            Set.of("Doc1", "Doc2", "Doc4"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:c/nestedComplexArray/any(n:contains(n/stringVal,'match')))",
            Set.of("Doc5", "Doc6"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:c/nestedComplexArray/any(n:n/stringVal eq 'val1' or n/stringVal eq 'test1'))",
            Set.of("Doc1", "Doc2", "Doc3", "Doc4"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:c/nestedComplexArray/any(n:n/stringVal eq 'val1' or n/stringVal eq 'test1') and c/someNumber ge 20)",
            Set.of("Doc2", "Doc3", "Doc4"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:c/nestedComplexArray/any(n:n/numberVal gt 70))",
            Set.of("Doc6"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:c/nestedComplexArray/any(n:n/numberVal eq 71) and c/nestedComplexArray/any(n:n/numberVal eq 72))",
            Set.of("Doc6"),
            "COLLSCAN"),
        Arguments.of(
            "complexList/any(c:c/nestedComplexArray/$count ge 2)", Set.of("Doc6"), "COLLSCAN"),
        Arguments.of(
            "complexList/any(c:c/primitiveStringList/any(n:startswith(n,'item11')))",
            Set.of("Doc6"),
            "FETCH + IXSCAN"),
        Arguments.of(
            "complexList/any(c:c/primitiveNumberList/any(n:n gt 10))",
            Set.of("Doc6"),
            "FETCH + IXSCAN"),
        // Concat test
        Arguments.of(
            Arrays.asList("complexList/any(c:c/someNumber gt 5)", "plainString eq 'Doc1'"),
            Set.of("Doc1"),
            "FETCH + IXSCAN"));
  }
}
