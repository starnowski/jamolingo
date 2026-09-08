package com.github.starnowski.jamolingo.core.operators.apply

import com.github.starnowski.jamolingo.core.AbstractSpecification
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.api.uri.queryoption.ApplyItem
import org.apache.olingo.server.api.uri.queryoption.apply.Search
import org.apache.olingo.server.core.uri.parser.Parser
import spock.lang.Shared

class ODataApplyUriInfoSearchParsingTest extends AbstractSpecification {

    @Shared
    Edm edm

    def setupSpec() {
        edm = loadEmdProvider("edm/edm6_filter_main.xml")
    }

    def "should correctly parse search item from UriInfo in \$apply"() {
        given:
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", '$apply=search(database)', null, null)

        when:
        def applyOption = uriInfo.getApplyOption()

        then:
        applyOption != null
        def applyItems = applyOption.getApplyItems()
        applyItems.size() == 1
        
        def item = applyItems[0]
        item instanceof Search
        item.getKind() == ApplyItem.Kind.SEARCH
        
        def searchOption = ((Search) item).getSearchOption()
        searchOption != null
        searchOption.getSearchExpression() != null
    }

    def "should correctly parse nested search item from UriInfo in \$apply"() {
        given:
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", '$apply=groupby((plainString),search(database))', null, null)

        when:
        def applyOption = uriInfo.getApplyOption()

        then:
        applyOption != null
        def applyItems = applyOption.getApplyItems()
        applyItems.size() == 1
        
        def item = applyItems[0]
        item.getKind() == ApplyItem.Kind.GROUP_BY
        
        def nestedApplyOption = ((org.apache.olingo.server.api.uri.queryoption.apply.GroupBy) item).getApplyOption()
        nestedApplyOption != null
        nestedApplyOption.getApplyItems().size() == 1
        
        def nestedItem = nestedApplyOption.getApplyItems()[0]
        nestedItem instanceof Search
        nestedItem.getKind() == ApplyItem.Kind.SEARCH
    }
}
