package com.github.starnowski.jamolingo.core.operators.apply

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver
import org.apache.olingo.server.api.uri.queryoption.ApplyItem
import spock.lang.Specification

class FilterItemParserTest extends Specification {
    def "should throw exception for non-Filter item"() {
        given:
        def parser = new FilterItemParser()
        def mockItem = Mock(ApplyItem)

        when:
        parser.parse(mockItem, Mock(EdmPropertyMongoPathResolver))

        then:
        thrown(IllegalArgumentException)
    }
}
