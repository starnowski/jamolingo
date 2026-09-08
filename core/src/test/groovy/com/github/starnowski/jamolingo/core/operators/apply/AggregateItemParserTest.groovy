package com.github.starnowski.jamolingo.core.operators.apply

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver
import org.apache.olingo.server.api.uri.queryoption.ApplyItem
import spock.lang.Specification

class AggregateItemParserTest extends Specification {
    def "should throw IllegalArgumentException when non-Aggregate item is passed"() {
        given:
        def parser = new AggregateItemParser()

        when:
        parser.parse(Mock(ApplyItem), Mock(EdmPropertyMongoPathResolver))

        then:
        thrown(IllegalArgumentException)
    }

    def "should correctly parse count_distinct method"() {
        given:
        def parser = new AggregateItemParser()
        def pathResolver = Mock(EdmPropertyMongoPathResolver)
        
        def aggregateItem = Mock(org.apache.olingo.server.api.uri.queryoption.apply.Aggregate)
        def aggregateExpr = Mock(org.apache.olingo.server.api.uri.queryoption.apply.AggregateExpression)
        
        aggregateItem.getExpressions() >> [aggregateExpr]
        
        def member = Mock(org.apache.olingo.server.api.uri.queryoption.expression.Member)
        def memberUriInfo = Mock(org.apache.olingo.server.api.uri.UriInfoResource)
        def memberUriResource = Mock(org.apache.olingo.server.api.uri.UriResource)
        
        aggregateExpr.getExpression() >> member
        member.getResourcePath() >> memberUriInfo
        memberUriInfo.getUriResourceParts() >> [memberUriResource]
        memberUriResource.getSegmentValue() >> "prop1"
        
        aggregateExpr.getAlias() >> "countVal"
        aggregateExpr.getStandardMethod() >> org.apache.olingo.server.api.uri.queryoption.apply.AggregateExpression.StandardMethod.COUNT_DISTINCT
        
        def res1 = Mock(com.github.starnowski.jamolingo.core.context.MongoPathResolution)
        res1.getMongoPath() >> "prop1"
        pathResolver.resolveMongoPathForEDMPath("prop1") >> res1

        when:
        def result = parser.parse(aggregateItem, pathResolver)

        then:
        result.stageObjects.size() == 2
        def groupStage = result.stageObjects[0] as org.bson.Document
        groupStage.containsKey('$group')
        def groupDoc = groupStage.get('$group') as org.bson.Document
        groupDoc.get('_id') == null
        
        def countDoc = groupDoc.get('countVal_distinctArray') as org.bson.Document
        countDoc.containsKey('$addToSet')
        countDoc.get('$addToSet') == '$prop1'

        def projectStage = result.stageObjects[1] as org.bson.Document
        projectStage.containsKey('$project')
        def projectDoc = projectStage.get('$project') as org.bson.Document
        projectDoc.get('_id') == 0
        
        def countProjectDoc = projectDoc.get('countVal') as org.bson.Document
        countProjectDoc.containsKey('$size')
        countProjectDoc.get('$size') == '$countVal_distinctArray'
    }
}
