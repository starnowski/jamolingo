package com.github.starnowski.jamolingo.orderby

import com.github.starnowski.jamolingo.AbstractSpecification
import com.github.starnowski.jamolingo.context.DefaultEdmMongoContextFacade
import com.github.starnowski.jamolingo.context.EntityPropertiesMongoPathContextBuilder
import com.github.starnowski.jamolingo.context.ODataMongoMappingFactory
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
        def result = tested.parse(uriInfo.getOrderByOption(), new DefaultEdmMongoContextFacade(context, null))

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
}
