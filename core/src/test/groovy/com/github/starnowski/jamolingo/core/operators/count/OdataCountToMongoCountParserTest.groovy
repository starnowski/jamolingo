package com.github.starnowski.jamolingo.core.operators.count

import com.github.starnowski.jamolingo.core.AbstractSpecification
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.uri.parser.Parser
import org.bson.Document

class OdataCountToMongoCountParserTest extends AbstractSpecification {

    def "should return expected count stage with default field name when \$count=true"() {
        given:
        Edm edm = loadEmdProvider("edm/edm1.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("Items", "\$count=true", null, null)
        OdataCountToMongoCountParser tested = new OdataCountToMongoCountParser()

        when:
        def result = tested.parse(uriInfo.getCountOption())

        then:
        result.getCountFieldName() == "count"
        result.isCountOptionPresent()
        result.getStageObjects().size() == 1
        result.getStageObjects().get(0) == new Document("\$count", "count")
        result.isDocumentShapeRedefined()
        result.getAddedMongoDocumentProperties() == ["count"]
    }

    def "should return expected count stage with custom field name when \$count=true"() {
        given:
        Edm edm = loadEmdProvider("edm/edm1.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("Items", "\$count=true", null, null)
        OdataCountToMongoCountParser tested = new OdataCountToMongoCountParser()

        when:
        def result = tested.parse(uriInfo.getCountOption(), "total")

        then:
        result.getCountFieldName() == "total"
        result.isCountOptionPresent()
        result.getStageObjects().size() == 1
        result.getStageObjects().get(0) == new Document("\$count", "total")
        result.isDocumentShapeRedefined()
        result.getAddedMongoDocumentProperties() == ["total"]
    }

    def "should return empty list of stages when \$count=false"() {
        given:
        Edm edm = loadEmdProvider("edm/edm1.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("Items", "\$count=false", null, null)
        OdataCountToMongoCountParser tested = new OdataCountToMongoCountParser()

        when:
        def result = tested.parse(uriInfo.getCountOption())

        then:
        !result.isCountOptionPresent()
        result.getStageObjects().isEmpty()
        !result.isDocumentShapeRedefined()
        result.getAddedMongoDocumentProperties().isEmpty()
    }

    def "should return empty list of stages when count option is null"() {
        given:
        OdataCountToMongoCountParser tested = new OdataCountToMongoCountParser()

        when:
        def result = tested.parse(null)

        then:
        !result.isCountOptionPresent()
        result.getStageObjects().isEmpty()
    }
}
