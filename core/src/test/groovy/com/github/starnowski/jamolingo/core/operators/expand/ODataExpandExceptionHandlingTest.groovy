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
                .withUseLookupForLevelGreaterThanOne(true)
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
                .withUseLookupForLevelGreaterThanOne(true)
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
                .withUseLookupForLevelGreaterThanOne(true)
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
                .withUseLookupForLevelGreaterThanOne(true)
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
                .withUseLookupForLevelGreaterThanOne(true)
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
                .withUseLookupForLevelGreaterThanOne(true)
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
                .withUseLookupForLevelGreaterThanOne(true)
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

    @Unroll
    def "should throw SearchNotEnabledForNavigationPathExpandException when search parser is not configured for navigation path: #query (useLookupForLevelGreaterThanOne=#useLookup)"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("treeType1s", query, null, null)

        def context = DefaultExpandParserContext.builder()
                .withMaxLevel(5)
                .withUseLookupForLevelGreaterThanOne(useLookup)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        parser.parse(uriInfo.getExpandOption(), context)

        then:
        def e = thrown(SearchNotEnabledForNavigationPathExpandException)
        e.getEdmPath() == expectedEdmPath
        e.getMessage().contains(expectedEdmPath)

        where:
        query                                                                                           | useLookup | expectedEdmPath
        '$expand=children($search=Vehicle)'                                                             | false     | 'children'
        '$expand=treeType2s($expand=treeType3s($search=Vehicle))'                                       | false     | 'treeType2s.treeType3s'
        '$expand=children($levels=2;$search=Vehicle)'                                                   | false     | 'children'
        '$expand=children($levels=2;$search=Vehicle)'                                                   | true      | 'children'
        '$expand=children($apply=compute(index add 10 as indexPlus10);$search=Vehicle)'                 | true      | 'children'
        '$expand=children($apply=search(Vehicle))'                                                      | true      | 'children'
        '$expand=children($apply=compute(index add 10 as indexPlus10)/search(Vehicle))'                 | true      | 'children'
        '$expand=treeType2s($expand=treeType3s($apply=compute(index add 10 as indexPlus10);$search=A))' | true      | 'treeType2s.treeType3s'
    }

    def "should construct SearchNotEnabledForNavigationPathExpandException with custom message"() {
        when:
        def exception = new SearchNotEnabledForNavigationPathExpandException("children.treeType2s", "Custom message")

        then:
        exception.getEdmPath() == "children.treeType2s"
        exception.getMessage() == "Custom message"
    }

    @Unroll
    def "should throw CannotHandleExpandWithApplyByGraphLookupException when apply is used with graphLookup stage: #query"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("treeType1s", query, null, null)

        def context = DefaultExpandParserContext.builder()
                .withMaxLevel(5)
                .withUseLookupForLevelGreaterThanOne(false)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        parser.parse(uriInfo.getExpandOption(), context)

        then:
        def e = thrown(CannotHandleExpandWithApplyByGraphLookupException)
        e.getEdmPath() == expectedEdmPath
        e.getMessage().contains(expectedEdmPath)
        e.getMessage().contains('$graphLookup')
        e.getMessage().contains('$expand')

        where:
        query                                                                                                               | expectedEdmPath
        '$expand=children($levels=max;$apply=groupby((categoryId),aggregate($count as categoryCount)))'                     | 'children'
        '$expand=children($levels=2;$apply=compute(index add 10 as indexPlus10))'                                          | 'children'
        '$expand=children($levels=max;$apply=filter(index eq 1))'                                                           | 'children'
        '$expand=children($levels=3;$apply=aggregate($count as totalCount))'                                               | 'children'
        '$expand=treeType2s($expand=treeType3s($levels=max;$apply=groupby((categoryId),aggregate($count as totalCount))))' | 'treeType2s.treeType3s'
        '$expand=treeType2s($levels=max;$apply=filter(index eq 1))'                                                         | 'treeType2s'
    }

    @Unroll
    def "should not throw CannotHandleExpandWithApplyByGraphLookupException when apply is handled by lookup stage or single level: #query (useLookupForLevelGreaterThanOne=#useLookup)"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("treeType1s", query, null, null)

        def context = DefaultExpandParserContext.builder()
                .withMaxLevel(5)
                .withUseLookupForLevelGreaterThanOne(useLookup)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        def result = parser.parse(uriInfo.getExpandOption(), context)

        then:
        noExceptionThrown()
        result != null

        where:
        query                                                                                           | useLookup
        '$expand=children($levels=max;$apply=compute(index add 10 as indexPlus10))'                     | true
        '$expand=children($levels=2;$apply=filter(index eq 1))'                                         | true
        '$expand=children($levels=1;$apply=compute(index add 10 as indexPlus10))'                      | false
        '$expand=children($apply=compute(index add 10 as indexPlus10))'                                 | false
        '$expand=treeType2s($expand=treeType3s($apply=compute(index add 10 as indexPlus10)))'           | false
        '$expand=treeType2s($expand=treeType3s($levels=max;$apply=compute(index add 10 as indexPlus10)))' | true
    }

    def "should construct CannotHandleExpandWithApplyByGraphLookupException with default and custom message"() {
        when:
        def exceptionWithDefaultMsg = new CannotHandleExpandWithApplyByGraphLookupException("children")

        then:
        exceptionWithDefaultMsg.getEdmPath() == "children"
        exceptionWithDefaultMsg.getMessage() != null
        exceptionWithDefaultMsg.getMessage().contains("children")
        exceptionWithDefaultMsg.getMessage().contains('$graphLookup')
        exceptionWithDefaultMsg.getMessage().contains('$expand')

        when:
        def exceptionWithCustomMsg = new CannotHandleExpandWithApplyByGraphLookupException("children.treeType2s", "Custom graphLookup error")

        then:
        exceptionWithCustomMsg.getEdmPath() == "children.treeType2s"
        exceptionWithCustomMsg.getMessage() == "Custom graphLookup error"
    }
}
