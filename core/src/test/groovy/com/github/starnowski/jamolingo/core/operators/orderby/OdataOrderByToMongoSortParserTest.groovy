package com.github.starnowski.jamolingo.core.operators.orderby

import com.github.starnowski.jamolingo.core.AbstractSpecification
import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade
import com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContextBuilder
import com.github.starnowski.jamolingo.core.mapping.ODataMongoMappingFactory
import com.github.starnowski.jamolingo.core.operators.orderby.DefaultOdataOrderByToMongoSortParserContext
import com.github.starnowski.jamolingo.core.operators.orderby.SortProperty
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.uri.parser.Parser
import org.bson.conversions.Bson
import spock.lang.Unroll

class OdataOrderByToMongoSortParserTest extends AbstractSpecification {

    @Unroll
    def "should return expected stage bson object"(){
        given:
            Bson expectedBson = loadBsonFromFile(bsonFile)
            Edm edm = loadEmdProvider(edmConfigFile)

            UriInfo uriInfo = new Parser(edm, OData.newInstance())
                    .parseUri("Items", "\$orderby=" + orderByClause, null, null)
        OdataOrderByToMongoSortParser tested = new OdataOrderByToMongoSortParser()

        when:
            def result = tested.parse(uriInfo.getOrderByOption())

        then:
            result.getStageObjects().get(0) == expectedBson

        where:
            [bsonFile, edmConfigFile, orderByClause] << testCases()
    }

    @Unroll
    def "should return expected stage bson object with default EdmMongoContextFacade with 1-to-1 edm to mongo mapping"() {
        given:
        Bson expectedBson = loadBsonFromFile(bsonFile)
        Edm edm = loadEmdProvider(edmConfigFile)
        ODataMongoMappingFactory factory = new ODataMongoMappingFactory()
        def odataMapping = factory.build(edm.getSchema("Demo"))
        def entityMapping = odataMapping.getEntities().get("Item")
        EntityPropertiesMongoPathContextBuilder entityPropertiesMongoPathContextBuilder = new EntityPropertiesMongoPathContextBuilder()
        def context = entityPropertiesMongoPathContextBuilder.build(entityMapping)

        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("Items", "\$orderby=" + orderByClause, null, null)
        OdataOrderByToMongoSortParser tested = new OdataOrderByToMongoSortParser()

        when:
        def result = tested.parse(uriInfo.getOrderByOption(), DefaultEdmMongoContextFacade.builder().withEntityPropertiesMongoPathContext(context).build())

        then:
        result.getStageObjects().get(0) == expectedBson

        where:
        [bsonFile, edmConfigFile, orderByClause] << testCases()
    }

    static testCases() {
        [
                ["orderby/stages/case1.json", "edm/edm1.xml", "plainString"],
                ["orderby/stages/case2.json", "edm/edm1.xml", "plainString desc"],
                ["orderby/stages/case3.json", "edm/edm2_with_nested_collections.xml", "plainString asc,Name desc"]
        ]
    }

    @Unroll
    def "should return expected used mongo document properties"() {
        given:
        Edm edm = loadEmdProvider(edmConfigFile)

        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("Items", "\$orderby=" + orderByClause, null, null)
        OdataOrderByToMongoSortParser tested = new OdataOrderByToMongoSortParser()

        when:
        def result = tested.parse(uriInfo.getOrderByOption())

        then:
        result.getUsedMongoDocumentProperties() == expectedUsedProperties

        where:
        [edmConfigFile, orderByClause, expectedUsedProperties] << usedPropertiesTestCases()
    }

    static usedPropertiesTestCases() {
        [
                ["edm/edm1.xml", "plainString", ["plainString"]],
                ["edm/edm1.xml", "plainString desc", ["plainString"]],
                ["edm/edm2_with_nested_collections.xml", "plainString asc,Name desc", ["plainString", "Name"]]
        ]
    }

    @Unroll
    def "should return expected stage bson object with prepended sort properties"() {
        given:
        Bson expectedBson = loadBsonFromFile(bsonFile)
        Edm edm = loadEmdProvider(edmConfigFile)
        ODataMongoMappingFactory factory = new ODataMongoMappingFactory()
        def odataMapping = factory.build(edm.getSchema("Demo"))
        def entityMapping = odataMapping.getEntities().get("Item")
        EntityPropertiesMongoPathContextBuilder entityPropertiesMongoPathContextBuilder = new EntityPropertiesMongoPathContextBuilder()
        def context = entityPropertiesMongoPathContextBuilder.build(entityMapping)
        
        UriInfo uriInfo = null
        if (orderByClause != null) {
            uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("Items", "\$orderby=" + orderByClause, null, null)
        }
        
        OdataOrderByToMongoSortParser tested = new OdataOrderByToMongoSortParser()

        when:
        def result = tested.parse(
            uriInfo != null ? uriInfo.getOrderByOption() : null, 
            DefaultEdmMongoContextFacade.builder().withEntityPropertiesMongoPathContext(context).build(), 
            sortParserContext)

        then:
        result.getStageObjects().get(0) == expectedBson

        where:
        [bsonFile, edmConfigFile, orderByClause, sortParserContext] << contextTestCases()
    }

    static contextTestCases() {
        [
                ["orderby/stages/case4_context.json", "edm/edm1.xml", "plainString", DefaultOdataOrderByToMongoSortParserContext.builder().withPrependedSortProperties([new SortProperty("prependedField1", true), new SortProperty("prependedField2", false)]).build()],
                ["orderby/stages/case5_context.json", "edm/edm1.xml", null, DefaultOdataOrderByToMongoSortParserContext.builder().withPrependedSortProperties([new SortProperty("prependedField1", false)]).build()]
        ]
    }
}
