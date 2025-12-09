package com.github.starnowski.jamolingo.select

import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider
import org.apache.olingo.commons.core.edm.EdmProviderImpl
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.core.MetadataParser
import org.bson.Document
import org.bson.conversions.Bson
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

class OdataSelectToMongoProjectParserTest extends Specification {


    @Unroll
    def "should return expected stage bson object"(){
        given:
            Bson expectedBson = loadBsonFromFile(bsonFile)
            Edm edm = loadEmdProvider(edmConfigFile)
            // TODO load EDM config
            // TODO define $select
            OdataSelectToMongoProjectParser tested = new OdataSelectToMongoProjectParser()

        when:
            def result = tested.parse()

        then:
            result.getStageObject() == result

        where:
            bsonFile |  edmConfigFile
            "select/stages/case1.json"       |  "edm/edm1.xml"
    }

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
