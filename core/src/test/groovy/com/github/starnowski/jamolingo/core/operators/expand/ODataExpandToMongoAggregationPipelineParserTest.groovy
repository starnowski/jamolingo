package com.github.starnowski.jamolingo.core.operators.expand

import com.github.starnowski.jamolingo.core.AbstractSpecification
import com.github.starnowski.jamolingo.core.operators.expand.ExpandParserContext
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.uri.parser.Parser
import org.bson.Document

class ODataExpandToMongoAggregationPipelineParserTest extends AbstractSpecification {

    def "should default isUseLookupForLevelGreaterThanOne to false in ExpandParserContext interface"() {
        given:
        ExpandParserContext context = new ExpandParserContext() {
            @Override
            Map getEDMTypeMapping() { return null }

            @Override
            Map getEDMTablesToMongoDBCollections() { return null }

            @Override
            Integer getMaxLevel() { return null }
        }

        expect:
        !context.isUseLookupForLevelGreaterThanOne()
    }

    def "should construct DefaultExpandParserContext and correctly set isUseLookupForLevelGreaterThanOne"() {
        when:
        def context = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(true)
                .build()

        then:
        context.isUseLookupForLevelGreaterThanOne()

        when:
        def context2 = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(false)
                .build()

        then:
        !context2.isUseLookupForLevelGreaterThanOne()
    }

    def "should correctly copy properties including isUseLookupForLevelGreaterThanOne in builder"() {
        given:
        def context = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(true)
                .build()

        when:
        def contextCopy = DefaultExpandParserContext.builder()
                .withDefaultExpandParserContext(context)
                .build()

        then:
        contextCopy.isUseLookupForLevelGreaterThanOne()
    }

    def "should correctly implement equals, hashCode, and toString in DefaultExpandParserContext"() {
        given:
        def context1 = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(true)
                .withMaxLevel(10)
                .build()
        def context2 = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(true)
                .withMaxLevel(10)
                .build()
        def context3 = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(false)
                .withMaxLevel(10)
                .build()

        expect:
        context1 == context2
        context1 != context3
        context1.hashCode() == context2.hashCode()
        context1.hashCode() != context3.hashCode()
        context1.toString().contains("useLookupForLevelGreaterThanOne=true")
        context3.toString().contains("useLookupForLevelGreaterThanOne=false")
    }

    def 'should parse expand with $levels > 1 using $graphLookup when isUseLookupForLevelGreaterThanOne is false'() {
        given:
        Edm edm = loadEmdProvider("edm/edm_expand.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", "\$expand=children(\$levels=2)", null, null)

        def context = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(false)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        def result = parser.parse(uriInfo.getExpandOption(), context)

        then:
        // By default, it generates $graphLookup
        result.getStageObjects().any { stage ->
            stage instanceof Document && stage.containsKey("\$graphLookup")
        }
        !result.getStageObjects().any { stage ->
            stage instanceof Document && stage.containsKey("\$lookup")
        }
    }

    def 'should parse expand with $levels > 1 using $lookup when isUseLookupForLevelGreaterThanOne is true'() {
        given:
        Edm edm = loadEmdProvider("edm/edm_expand.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", "\$expand=children(\$levels=2)", null, null)

        def context = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(true)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        def result = parser.parse(uriInfo.getExpandOption(), context)

        then:
        // Since isUseLookupForLevelGreaterThanOne is true, it should generate $lookup instead of $graphLookup
        result.getStageObjects().any { stage ->
            stage instanceof Document && stage.containsKey("\$lookup")
        }
        !result.getStageObjects().any { stage ->
            stage instanceof Document && stage.containsKey("\$graphLookup")
        }
    }
}
