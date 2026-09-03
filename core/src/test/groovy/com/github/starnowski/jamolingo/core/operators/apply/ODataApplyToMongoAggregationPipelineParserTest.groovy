package com.github.starnowski.jamolingo.core.operators.apply

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver
import org.apache.olingo.server.api.uri.queryoption.ApplyItem
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
    def "should return facet and related stages when applyOption has concat item"() {
        given:
        def parser = new ODataApplyToMongoAggregationPipelineParser()
        def mockApplyOption = Mock(ApplyOption)
        def mockConcatItem = Mock(org.apache.olingo.server.api.uri.queryoption.apply.Concat)
        def mockConcatOption1 = Mock(ApplyOption)
        def mockConcatOption2 = Mock(ApplyOption)
        def mockConcatItem1 = Mock(ApplyItem)
        def mockConcatItem2 = Mock(ApplyItem)
        
        mockConcatItem.getKind() >> ApplyItem.Kind.CONCAT
        mockConcatItem.getApplyOptions() >> [mockConcatOption1, mockConcatOption2]
        
        mockConcatOption1.getApplyItems() >> [mockConcatItem1]
        mockConcatOption2.getApplyItems() >> [mockConcatItem2]
        
        mockConcatItem1.getKind() >> ApplyItem.Kind.IDENTITY
        mockConcatItem2.getKind() >> ApplyItem.Kind.IDENTITY
        
        mockApplyOption.getApplyItems() >> [mockConcatItem]
        def mockFacade = Mock(EdmPropertyMongoPathResolver)

        when:
        def result = parser.parse(mockApplyOption, mockFacade)

        then:
        result != null
        result.stageObjects.size() == 4
        
        def facetDoc = result.stageObjects.get(0).get('$facet')
        facetDoc != null
        facetDoc.get('concat_0') == []
        facetDoc.get('concat_1') == []
        
        def projectDoc = result.stageObjects.get(1).get('$project')
        projectDoc != null
        projectDoc.get('_combinedResult') != null
        projectDoc.get('_combinedResult').get('$concatArrays') == ['$concat_0', '$concat_1']
        
        def unwindDoc = result.stageObjects.get(2).get('$unwind')
        unwindDoc == '$_combinedResult'
        
        def replaceRootDoc = result.stageObjects.get(3).get('$replaceRoot')
        replaceRootDoc.get('newRoot') == '$_combinedResult'
    }
}
