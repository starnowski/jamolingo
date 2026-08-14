package com.github.starnowski.jamolingo.core.operators.apply

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver
import org.apache.olingo.server.api.uri.queryoption.ApplyItem
import org.apache.olingo.server.api.uri.queryoption.apply.GroupBy
import org.apache.olingo.server.api.uri.queryoption.apply.GroupByItem
import org.apache.olingo.server.api.uri.UriResource
import com.github.starnowski.jamolingo.core.context.MongoPathResolution
import org.bson.Document
import org.apache.olingo.server.api.uri.queryoption.ApplyOption
import spock.lang.Specification

class GroupByItemParserTest extends Specification {
    def "should map simple grouping properties to _id in group stage"() {
        given:
        def pathResolver = Mock(EdmPropertyMongoPathResolver)
        def applyParser = new ODataApplyToMongoAggregationPipelineParser()
        def parser = new GroupByItemParser(applyParser)
        
        def groupBy = Mock(GroupBy)
        def groupByItem1 = Mock(GroupByItem)
        def uriResource1 = Mock(UriResource)
        
        uriResource1.getSegmentValue() >> "prop1"
        groupByItem1.getPath() >> [uriResource1]
        groupBy.getGroupByItems() >> [groupByItem1]
        
        def res1 = Mock(MongoPathResolution)
        res1.getMongoPath() >> "prop1"
        pathResolver.resolveMongoPathForEDMPath("prop1") >> res1

        when:
        def result = parser.parse(groupBy, pathResolver)

        then:
        result.stageObjects.size() == 1
        def groupStage = result.stageObjects[0] as Document
        groupStage.containsKey('$group')
        def idDoc = groupStage.get('$group') as Document
        def idVal = idDoc.get('_id') as Document
        idVal.containsKey('prop1')
        idVal.get('prop1') == '$prop1'
    }

    def "should map multiple grouping properties to _id in group stage"() {
        given:
        def pathResolver = Mock(EdmPropertyMongoPathResolver)
        def applyParser = new ODataApplyToMongoAggregationPipelineParser()
        def parser = new GroupByItemParser(applyParser)
        
        def groupBy = Mock(GroupBy)
        def groupByItem1 = Mock(GroupByItem)
        def uriResource1 = Mock(UriResource)
        uriResource1.getSegmentValue() >> "prop1"
        groupByItem1.getPath() >> [uriResource1]
        
        def groupByItem2 = Mock(GroupByItem)
        def uriResource2 = Mock(UriResource)
        def uriResource3 = Mock(UriResource)
        uriResource2.getSegmentValue() >> "prop2"
        uriResource3.getSegmentValue() >> "subProp"
        groupByItem2.getPath() >> [uriResource2, uriResource3]
        
        groupBy.getGroupByItems() >> [groupByItem1, groupByItem2]
        
        def res1 = Mock(MongoPathResolution)
        res1.getMongoPath() >> "prop1"
        pathResolver.resolveMongoPathForEDMPath("prop1") >> res1
        
        def res2 = Mock(MongoPathResolution)
        res2.getMongoPath() >> "prop2.subProp"
        pathResolver.resolveMongoPathForEDMPath("prop2/subProp") >> res2

        when:
        def result = parser.parse(groupBy, pathResolver)

        then:
        result.stageObjects.size() == 1
        def groupStage = result.stageObjects[0] as Document
        groupStage.containsKey('$group')
        def idDoc = groupStage.get('$group') as Document
        def idVal = idDoc.get('_id') as Document
        idVal.containsKey('prop1')
        idVal.get('prop1') == '$prop1'
        idVal.containsKey('prop2.subProp')
        idVal.get('prop2.subProp') == '$prop2.subProp'
    }

    def "should append stages from inner ApplyOption if nested transformations exist"() {
        given:
        def pathResolver = Mock(EdmPropertyMongoPathResolver)
        def innerStage = new Document('$limit', 5)
        def applyParser = new ODataApplyToMongoAggregationPipelineParser() {
            @Override
            ApplyOperatorResult parse(ApplyOption applyOption, EdmPropertyMongoPathResolver edmMongoContextFacade) {
                return DefaultApplyOperatorResult.builder().withStageObjects([innerStage]).build()
            }
        }
        def parser = new GroupByItemParser(applyParser)
        
        def groupBy = Mock(GroupBy)
        def groupByItem1 = Mock(GroupByItem)
        def uriResource1 = Mock(UriResource)
        
        uriResource1.getSegmentValue() >> "prop1"
        groupByItem1.getPath() >> [uriResource1]
        groupBy.getGroupByItems() >> [groupByItem1]
        
        def innerApplyOption = Mock(ApplyOption)
        groupBy.getApplyOption() >> innerApplyOption
        
        def res1 = Mock(MongoPathResolution)
        res1.getMongoPath() >> "prop1"
        pathResolver.resolveMongoPathForEDMPath("prop1") >> res1
        
        when:
        def result = parser.parse(groupBy, pathResolver)

        then:
        result.stageObjects.size() == 2
        def groupStage = result.stageObjects[0] as Document
        groupStage.containsKey('$group')
        result.stageObjects[1] == innerStage
    }
}
