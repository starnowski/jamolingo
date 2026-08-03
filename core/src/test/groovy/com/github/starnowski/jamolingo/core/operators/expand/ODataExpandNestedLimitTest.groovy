package com.github.starnowski.jamolingo.core.operators.expand

import com.github.starnowski.jamolingo.core.AbstractSpecification
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.uri.parser.Parser

class ODataExpandNestedLimitTest extends AbstractSpecification {

    def "should pass when maxAllowedNestedExpandLevel is 2 and nested expand level is 1"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_expand.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", "\$expand=children", null, null)

        def context = DefaultExpandParserContext.builder()
                .withMaxAllowedNestedExpandLevel(2)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        def result = parser.parse(uriInfo.getExpandOption(), context)

        then:
        noExceptionThrown()
        result.getStageObjects().size() > 0
    }

    def "should pass when maxAllowedNestedExpandLevel is 2 and nested expand level is 2"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_expand.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", "\$expand=children(\$expand=parent)", null, null)

        def context = DefaultExpandParserContext.builder()
                .withMaxAllowedNestedExpandLevel(2)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        def result = parser.parse(uriInfo.getExpandOption(), context)

        then:
        noExceptionThrown()
        result.getStageObjects().size() > 0
    }

    def "should throw NestedExpandLevelExceededException when maxAllowedNestedExpandLevel is 2 and nested expand level is 3"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_expand.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", "\$expand=children(\$expand=parent(\$expand=children))", null, null)

        def context = DefaultExpandParserContext.builder()
                .withMaxAllowedNestedExpandLevel(2)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        parser.parse(uriInfo.getExpandOption(), context)

        then:
        def e = thrown(NestedExpandLevelExceededException)
        e.getEdmPath() == "children.parent.children"
        e.getRequestedLevel() == 3
        e.getMaxLevel() == 2
        e.getMessage() == "The requested nested expand level 3 for path 'children.parent.children' exceeds the maximum allowed level 2."
    }
}
