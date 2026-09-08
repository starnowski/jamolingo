package com.github.starnowski.jamolingo.core.operators.apply

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver
import org.apache.olingo.server.api.uri.queryoption.ApplyItem
import org.apache.olingo.server.api.uri.queryoption.apply.Search
import spock.lang.Specification

class SearchItemParserTest extends Specification {

    def "should throw exception if search parser delegate is not configured"() {
        given:
        def parser = new SearchItemParser(null, true)
        def mockItem = Mock(Search)

        when:
        parser.parse(mockItem, Mock(EdmPropertyMongoPathResolver))

        then:
        def e = thrown(UnsupportedOperationException)
        e.message == "Search set transformation is not configured. Missing ApplySearchToMongoPipelineParser."
    }

    def "should pass context to search delegate parser and return result"() {
        given:
        def mockDelegate = Mock(ApplySearchToMongoPipelineParser)
        def parser = new SearchItemParser(mockDelegate, true)
        def mockItem = Mock(Search)
        def expectedResult = Mock(ApplyOperatorResult)

        when:
        def result = parser.parse(mockItem, Mock(EdmPropertyMongoPathResolver))

        then:
        1 * mockDelegate.parse({ SearchApplyItemContext context -> 
            context.getSearch() == mockItem && context.isFirstApplyItem()
        }) >> expectedResult
        result == expectedResult
    }

    def "should pass false to context if not first apply item"() {
        given:
        def mockDelegate = Mock(ApplySearchToMongoPipelineParser)
        def parser = new SearchItemParser(mockDelegate, false)
        def mockItem = Mock(Search)
        def expectedResult = Mock(ApplyOperatorResult)

        when:
        def result = parser.parse(mockItem, Mock(EdmPropertyMongoPathResolver))

        then:
        1 * mockDelegate.parse({ SearchApplyItemContext context ->
            context.getSearch() == mockItem && !context.isFirstApplyItem()
        }) >> expectedResult
        result == expectedResult
    }
}
