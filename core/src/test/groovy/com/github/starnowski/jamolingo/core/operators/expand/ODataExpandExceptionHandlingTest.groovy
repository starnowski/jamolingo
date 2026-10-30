package com.github.starnowski.jamolingo.core.operators.expand

import com.github.starnowski.jamolingo.core.AbstractSpecification
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.uri.parser.Parser
import spock.lang.Unroll

class ODataExpandExceptionHandlingTest extends AbstractSpecification {

    @Unroll
    def "should throw MissingNavigationPropertyExpandException when navigation property is missing due to apply reshaping: #query"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("treeType1s", query, null, null)

        def context = DefaultExpandParserContext.builder()
                .withMaxLevel(5)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        parser.parse(uriInfo.getExpandOption(), context)

        then:
        def e = thrown(MissingNavigationPropertyExpandException)
        e.getEdmPath() == expectedEdmPath
        e.getMissingNavigationProperty() == missingNavProperty

        where:
        query | expectedEdmPath | missingNavProperty
        '$expand=children($levels=max;$apply=groupby((categoryId),aggregate($count as categoryCount));$expand=parent)' | 'children.parent' | 'parent'
    }

    @Unroll
    def "should not throw exception when ignoreGloballyMissingNavigationProperty is true: #query"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("treeType1s", query, null, null)

        def context = DefaultExpandParserContext.builder()
                .withMaxLevel(5)
                .withIgnoreGloballyMissingNavigationProperty(true)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        def result = parser.parse(uriInfo.getExpandOption(), context)

        then:
        noExceptionThrown()
        result != null

        where:
        query | _
        '$expand=children($levels=max;$apply=groupby((categoryId),aggregate($count as categoryCount));$expand=parent)' | _
    }

    @Unroll
    def "should not throw exception when ignoreForSpecificExpandElementMissingNavigationProperty contains the edm path: #query"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("treeType1s", query, null, null)

        def context = DefaultExpandParserContext.builder()
                .withMaxLevel(5)
                .withIgnoreForSpecificExpandElementMissingNavigationProperty(Set.of(expectedEdmPath))
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        def result = parser.parse(uriInfo.getExpandOption(), context)

        then:
        noExceptionThrown()
        result != null

        where:
        query | expectedEdmPath
        '$expand=children($levels=max;$apply=groupby((categoryId),aggregate($count as categoryCount));$expand=parent)' | 'children.parent'
    }

    @Unroll
    def "should throw MissingPropertyExpandException when property used in filter is missing due to apply reshaping: #query"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("treeType1s", query, null, null)

        def context = DefaultExpandParserContext.builder()
                .withMaxLevel(5)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        parser.parse(uriInfo.getExpandOption(), context)

        then:
        def e = thrown(MissingPropertyExpandException)
        e.getEdmPath() == expectedEdmPath
        e.getMissingProperty() == missingProperty

        where:
        query | expectedEdmPath | missingProperty
        '$expand=children($levels=max;$apply=groupby((categoryId),aggregate($count as categoryCount));$filter=index eq 1)' | 'children' | 'index'
    }

    @Unroll
    def "should throw MissingPropertyExpandException when property used in orderby is missing due to apply reshaping: #query"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("treeType1s", query, null, null)

        def context = DefaultExpandParserContext.builder()
                .withMaxLevel(5)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        parser.parse(uriInfo.getExpandOption(), context)

        then:
        def e = thrown(MissingPropertyExpandException)
        e.getEdmPath() == expectedEdmPath
        e.getMissingProperty() == missingProperty

        where:
        query | expectedEdmPath | missingProperty
        '$expand=children($levels=max;$apply=groupby((categoryId),aggregate($count as categoryCount));$orderby=index desc)' | 'children' | 'index'
    }

    @Unroll
    def "should throw MissingPropertyExpandException when property used in select is missing due to apply reshaping: #query"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("treeType1s", query, null, null)

        def context = DefaultExpandParserContext.builder()
                .withMaxLevel(5)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        parser.parse(uriInfo.getExpandOption(), context)

        then:
        def e = thrown(MissingPropertyExpandException)
        e.getEdmPath() == expectedEdmPath
        e.getMissingProperty() == missingProperty

        where:
        query | expectedEdmPath | missingProperty
        '$expand=children($levels=max;$apply=groupby((categoryId),aggregate($count as categoryCount));$select=index)' | 'children' | 'index'
    }

    @Unroll
    def "should not throw exception for missing property when ignoreGloballyMissingProperty is true: #query"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("treeType1s", query, null, null)

        def context = DefaultExpandParserContext.builder()
                .withMaxLevel(5)
                .withIgnoreGloballyMissingProperty(true)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        def result = parser.parse(uriInfo.getExpandOption(), context)

        then:
        noExceptionThrown()
        result != null

        where:
        query | _
        '$expand=children($levels=max;$apply=groupby((categoryId),aggregate($count as categoryCount));$filter=index eq 1)' | _
        '$expand=children($levels=max;$apply=groupby((categoryId),aggregate($count as categoryCount));$orderby=index desc)' | _
        '$expand=children($levels=max;$apply=groupby((categoryId),aggregate($count as categoryCount));$select=index)' | _
    }
}
