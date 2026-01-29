package com.github.starnowski.jamolingo.core.operators.filter

import com.github.starnowski.jamolingo.core.AbstractSpecification
import com.github.starnowski.jamolingo.core.operators.select.OdataSelectToMongoProjectParser
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.uri.parser.Parser
import org.bson.Document
import org.bson.conversions.Bson
import spock.lang.Unroll

import java.util.stream.Collectors

class ODataFilterToMongoMatchParserTest extends AbstractSpecification {

    @Unroll
    def "should return expected stage bson objects"(){
        given:
        Bson expectedBson = Document.parse(bson)
        Edm edm = loadEmdProvider(edmConfigFile)

        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri(path,
                        "\$filter=" +filter
                        , null, null)
        ODataFilterToMongoMatchParser tested = new ODataFilterToMongoMatchParser()

        when:
        def result = tested.parse(uriInfo.getFilterOption(), edm)

        then:
        result.getStageObjects() == [expectedBson]

        where:
        [edmConfigFile, path , filter, bson] << oneToOneEdmPathsMappings()
    }

    static oneToOneEdmPathsMappings() {
        [
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/all(t:t ne 'no such text' and t ne 'no such word')", """{"\$match": {"\$and": [{"\$and": [{"tags": {"\$not": {"\$elemMatch": {"\$not": {"\$ne": "no such text"}}}}}, {"tags": {"\$not": {"\$elemMatch": {"\$not": {"\$ne": "no such word"}}}}}]}]}}"""]
        ]
    }

}
