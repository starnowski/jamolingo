package com.github.starnowski.jamolingo.skip

import com.github.starnowski.jamolingo.AbstractSpecification
import com.github.starnowski.jamolingo.core.operators.skip.OdataSkipToMongoSkipParser
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.uri.parser.Parser
import org.bson.Document
import spock.lang.Unroll

class OdataSkipToMongoSkipParserTest extends AbstractSpecification {

    @Unroll
    def "should return expected skip value #skipValue"() {
        given:
        Edm edm = loadEmdProvider("edm/edm1.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("Items", "\$skip=" + skipValue, null, null)
        OdataSkipToMongoSkipParser tested = new OdataSkipToMongoSkipParser()

        when:
        def result = tested.parse(uriInfo.getSkipOption())

        then:
        result.getSkipValue() == skipValue
        result.getStageObjects().size() == 1
        result.getStageObjects().get(0) == new Document("\$skip", skipValue)

        where:
        skipValue << [1, 5, 10, 100]
    }

    def "should return empty list of stages when skip option is null"() {
        given:
        OdataSkipToMongoSkipParser tested = new OdataSkipToMongoSkipParser()

        when:
        def result = tested.parse(null)

        then:
        result.getSkipValue() == 0
        result.getStageObjects().isEmpty()
    }
}
