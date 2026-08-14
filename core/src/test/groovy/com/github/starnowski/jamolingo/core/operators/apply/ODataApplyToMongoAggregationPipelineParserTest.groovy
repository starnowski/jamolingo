package com.github.starnowski.jamolingo.core.operators.apply

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver
import org.apache.olingo.server.api.uri.queryoption.ApplyOption
import spock.lang.Specification

class ODataApplyToMongoAggregationPipelineParserTest extends Specification {

    def "should return empty stage list when applyOption is null"() {
        given:
        def parser = new ODataApplyToMongoAggregationPipelineParser()
        def mockFacade = Mock(EdmPropertyMongoPathResolver)

        when:
        def result = parser.parse(null, mockFacade)

        then:
        result != null
        result.stageObjects.isEmpty()
    }

    def "should return empty stage list when applyOption has no items"() {
        given:
        def parser = new ODataApplyToMongoAggregationPipelineParser()
        def mockApplyOption = Mock(ApplyOption)
        mockApplyOption.getApplyItems() >> []
        def mockFacade = Mock(EdmPropertyMongoPathResolver)

        when:
        def result = parser.parse(mockApplyOption, mockFacade)

        then:
        result != null
        result.stageObjects.isEmpty()
    }
}
