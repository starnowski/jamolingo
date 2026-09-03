package com.github.starnowski.jamolingo.core.operators.apply

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver
import org.apache.olingo.server.api.uri.queryoption.apply.Top
import org.apache.olingo.server.api.uri.queryoption.TopOption
import spock.lang.Specification
import org.bson.Document

class TopItemParserTest extends Specification {
    def "should return limit stage based on TopOption"() {
        given:
        def parser = new TopItemParser()
        def mockTop = Mock(Top)
        def mockTopOption = Mock(TopOption)
        mockTop.getTopOption() >> mockTopOption
        mockTopOption.getValue() >> 5

        when:
        def result = parser.parse(mockTop, Mock(EdmPropertyMongoPathResolver))

        then:
        result.stageObjects.size() == 1
        result.stageObjects[0] == new Document('$limit', 5)
    }
}
