package com.github.starnowski.jamolingo.select

import com.github.starnowski.jamolingo.AbstractSpecification
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.uri.parser.Parser
import org.bson.conversions.Bson
import spock.lang.Unroll

import java.util.stream.Collectors

class OdataSelectToMongoProjectParserTest extends AbstractSpecification {


    @Unroll
    def "should return expected stage bson object"(){
        given:
            Bson expectedBson = loadBsonFromFile(bsonFile)
            Edm edm = loadEmdProvider(edmConfigFile)

            UriInfo uriInfo = new Parser(edm, OData.newInstance())
                    .parseUri("Items",
                            "\$select=" +
                                    selectFields.stream().filter(Objects::nonNull)
                                            .filter(s -> !s.trim().isEmpty())
                                            .collect(Collectors.joining(","))
                            , null, null)
            OdataSelectToMongoProjectParser tested = new OdataSelectToMongoProjectParser()

        when:
            def result = tested.parse(uriInfo.getSelectOption())

        then:
            result.getStageObject() == expectedBson

        where:
            bsonFile |  edmConfigFile   | selectFields
            "select/stages/case1.json"       |  "edm/edm1.xml"  | ["plainString"]
            "select/stages/case_wildcard_without_id.json"       |  "edm/edm1.xml"  | ["*"] // ExpandAsterisk = false
            "select/stages/case2.json"       |  "edm/edm2_with_nested_collections.xml"  | ["plainString", "Name", "Addresses/Street", "Addresses/ZipCode"] // ExpandAsterisk = false
            "select/stages/case3_with_nested_complexType_circular_reference.json"       |  "edm/edm2_complextype_with_circular_reference.xml"  | ["plainString", "Name", "Addresses/Street", "Addresses/ZipCode", "Addresses/BackUpAddresses/ZipCode"] // ExpandAsterisk = false
    }

    // TODO ExpandAsterisk = true (all fields defined in EDM)
}
