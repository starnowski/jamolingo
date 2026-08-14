package com.github.starnowski.jamolingo.core.operators.apply

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver
import org.apache.olingo.server.api.uri.queryoption.apply.Skip
import org.apache.olingo.server.api.uri.queryoption.SkipOption
import spock.lang.Specification
import org.bson.Document

class SkipItemParserTest extends Specification {
    def "should return skip stage based on SkipOption"() {
        given:
        def parser = new SkipItemParser()
        def mockSkip = Mock(Skip)
        def mockSkipOption = Mock(SkipOption)
        mockSkip.getSkipOption() >> mockSkipOption
        mockSkipOption.getValue() >> 10

        when:
        def result = parser.parse(mockSkip, Mock(EdmPropertyMongoPathResolver))

        then:
        result.stageObjects.size() == 1
        result.stageObjects[0] == new Document('$skip', 10)
    }
}
