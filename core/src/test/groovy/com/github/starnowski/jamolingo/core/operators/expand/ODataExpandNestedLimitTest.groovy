package com.github.starnowski.jamolingo.core.operators.expand

import com.github.starnowski.jamolingo.core.AbstractSpecification
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.uri.parser.Parser
import spock.lang.Unroll

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

    @Unroll
    def "should throw NestedExpandLevelExceededException when maxAllowedNestedExpandLevel is #maxAllowedNestedExpandLevel and nested expand level is #requestedLevel"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_expand.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", expandQuery, null, null)

        def context = DefaultExpandParserContext.builder()
                .withMaxAllowedNestedExpandLevel(maxAllowedNestedExpandLevel)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        parser.parse(uriInfo.getExpandOption(), context)

        then:
        def e = thrown(NestedExpandLevelExceededException)
        e.getEdmPath() == expectedEdmPath
        e.getRequestedLevel() == requestedLevel
        e.getMaxLevel() == maxAllowedNestedExpandLevel
        e.getMessage() == "The requested nested expand level $requestedLevel for path '$expectedEdmPath' exceeds the maximum allowed level $maxAllowedNestedExpandLevel."

        where:
        maxAllowedNestedExpandLevel | requestedLevel | expectedEdmPath                     | expandQuery
        2                           | 3              | "children.parent.children"          | "\$expand=children(\$expand=parent(\$expand=children))"
        1                           | 2              | "children.parent"                   | "\$expand=children(\$expand=parent)"
        3                           | 4              | "children.parent.children.parent"   | "\$expand=children(\$expand=parent(\$expand=children(\$expand=parent)))"
        1                           | 2              | "parent.children"                   | "\$expand=parent(\$expand=children)"
        2                           | 3              | "parent.children.parent"            | "\$expand=children(\$expand=parent),parent(\$expand=children(\$expand=parent))"
    }
}
