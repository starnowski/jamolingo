package com.github.starnowski.jamolingo.select

import org.bson.Document
import org.bson.conversions.Bson
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Paths

class OdataSelectToMongoProjectParserTest extends Specification {


    @Unroll
    def "should return expected stage bson object"(){
        given:
            Bson expectedBson = loadBsonFromFile(bsonFile)
            // TODO load EDM config
            // TODO define $select
            OdataSelectToMongoProjectParser tested = new OdataSelectToMongoProjectParser()

        when:
            def result = tested.parse()

        then:
            result.getStageObject() == result

        where:
            bsonFile |  edmConfigFile
            "select/stages/case1.json"       |  ""
    }

    def Bson loadBsonFromFile(String filePath) {
        String bson = Files.readString(Paths.get(new File(getClass().getClassLoader().getResource(filePath).getFile()).getPath()))
        Document.parse(bson)
    }
}
