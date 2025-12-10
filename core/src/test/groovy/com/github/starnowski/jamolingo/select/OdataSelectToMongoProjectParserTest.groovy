package com.github.starnowski.jamolingo.select

import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider
import org.apache.olingo.commons.core.edm.EdmProviderImpl
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.MetadataParser
import org.apache.olingo.server.core.uri.parser.Parser
import org.bson.Document
import org.bson.conversions.Bson
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

class OdataSelectToMongoProjectParserTest extends Specification {


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
    }

    // TODO ExpandAsterisk = true (all fields defined in EDM)

    def Bson loadBsonFromFile(String filePath) {
        String bson = Files.readString(Paths.get(new File(getClass().getClassLoader().getResource(filePath).getFile()).getPath()))
        Document.parse(bson)
    }

    def Edm  loadEmdProvider(String filePath){
        Reader reader = new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(filePath),
                StandardCharsets.UTF_8
        )
        // Parse it into a CsdlEdmProvider
        MetadataParser parser = new MetadataParser()
        CsdlEdmProvider provider = parser.buildEdmProvider(reader)

        // Build Edm model from provider
        return new EdmProviderImpl(provider)
    }
}
