package com.github.starnowski.jamolingo.top

import com.github.starnowski.jamolingo.AbstractSpecification
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.uri.parser.Parser
import org.bson.Document
import spock.lang.Unroll

class OdataTopToMongoLimitParserTest extends AbstractSpecification {

    @Unroll
    def "should return expected limit value #topValue"() {
        given:
        Edm edm = loadEmdProvider("edm/edm1.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("Items", "\$top=" + topValue, null, null)
        OdataTopToMongoLimitParser tested = new OdataTopToMongoLimitParser()

        when:
        def result = tested.parse(uriInfo.getTopOption())

        then:
        result.getTopValue() == topValue
        result.getStageObjects().size() == 1
        result.getStageObjects().get(0) == new Document("\$limit", topValue)

        where:
        topValue << [1, 5, 10, 100]
    }

    def "should return empty list of stages when top option is null"() {
        given:
        OdataTopToMongoLimitParser tested = new OdataTopToMongoLimitParser()

        when:
        def result = tested.parse(null)

        then:
        result.getTopValue() == 0
        result.getStageObjects().isEmpty()
    }
}
