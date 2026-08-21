package com.github.starnowski.jamolingo.core.operators.apply

import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade
import com.github.starnowski.jamolingo.core.context.MongoPathResolution
import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver
import org.apache.olingo.server.api.uri.queryoption.apply.OrderBy
import org.apache.olingo.server.api.uri.queryoption.OrderByOption
import org.apache.olingo.server.api.uri.queryoption.OrderByItem
import org.apache.olingo.server.api.uri.queryoption.expression.Member
import org.apache.olingo.server.api.uri.UriInfoResource
import spock.lang.Specification
import org.bson.Document

class OrderByItemParserTest extends Specification {
    def "should return sort stage based on OrderByOption"() {
        given:
        def parser = new OrderByItemParser()
        def mockOrderBy = Mock(OrderBy)
        def mockOrderByOption = Mock(OrderByOption)
        def mockOrderByItem = Mock(OrderByItem)
        def mockMember = Mock(Member)
        def mockUriInfoResource = Mock(UriInfoResource)
        def edmMongoContextFacade = Mock(EdmMongoContextFacade)
        def mockMongoPathResolution = Mock(MongoPathResolution)
        
        mockOrderBy.getOrderByOption() >> mockOrderByOption
        mockOrderByOption.getOrders() >> [mockOrderByItem]
        mockOrderByItem.getExpression() >> mockMember
        mockMember.getResourcePath() >> mockUriInfoResource
        mockOrderByItem.isDescending() >> true
        mockMongoPathResolution.getMongoPath() >> "myField"
        edmMongoContextFacade.resolveMongoPathForEDMPath(mockUriInfoResource) >> mockMongoPathResolution

        when:
        def result = parser.parse(mockOrderBy, edmMongoContextFacade)

        then:
        result.stageObjects.size() == 1
        result.stageObjects[0] == new Document('$sort', new Document('myField', -1))
    }
}
