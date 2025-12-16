package com.github.starnowski.jamolingo.context

import com.github.starnowski.jamolingo.AbstractSpecification
import com.github.starnowski.jamolingo.select.OdataSelectToMongoProjectParser
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.uri.parser.Parser
import org.bson.conversions.Bson
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

class ODataMongoMappingFactoryTest extends AbstractSpecification {

    @Unroll
    def "should return expected stage bson object"(){
        given:
            def tested = new ODataMongoMappingFactory()
            Edm edm = loadEmdProvider(edmConfigFile)

        when:
            def result = tested.build(edm, schema)

        then:
            result == expectedODataMongoMapping

        where:
            edmConfigFile   | schema    || expectedODataMongoMapping
            "edm/edm1.xml"  | "Demo"    || new ODataMongoMapping().withEntities(Map.of("Item", new EntityMapping().withProperties(Map.of("plainString", new PropertyMapping()))))
    }
}
