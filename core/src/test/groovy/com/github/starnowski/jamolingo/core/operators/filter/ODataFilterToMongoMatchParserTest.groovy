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
        [result.getStageObjects().get(0).toBsonDocument().toJson()] == [expectedBson.toJson()]

        where:
        [edmConfigFile, path , filter, bson] << oneToOneEdmPathsMappings()
    }

    static oneToOneEdmPathsMappings() {
        [
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/all(t:t ne 'no such text' and t ne 'no such word')", """{"\$match": {"\$and": [{"\$and": [{"tags": {"\$not": {"\$elemMatch": {"\$not": {"\$ne": "no such text"}}}}}, {"tags": {"\$not": {"\$elemMatch": {"\$not": {"\$ne": "no such word"}}}}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/all(t:startswith(t,'star') and t ne 'starlord')", """{"\$match": {"\$and": [{"\$and": [{"tags": {"\$not": {"\$elemMatch": {"\$not": {"\$regex": "^\\\\Qstar\\\\E"}}}}}, {"tags": {"\$not": {"\$elemMatch": {"\$not": {"\$ne": "starlord"}}}}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "nestedObject/tokens/any(t:t ne 'no such text')", """{"\$match": {"\$and": [{"nestedObject.tokens": {"\$ne": "no such text"}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "nestedObject/tokens/any(t:t eq 'first example') and nestedObject/numbers/any(t:t gt 5 and t lt 27)", """{"\$match": {"\$and": [{"\$and": [{"nestedObject.tokens": "first example"}, {"nestedObject.numbers": {"\$elemMatch": {"\$gt": 5, "\$lt": 27}}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "plainString eq 'eOMtThyhVNLWUZNRcBaQKxI'", """{"\$match": {"\$and": [{"plainString": "eOMtThyhVNLWUZNRcBaQKxI"}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tolower(plainString) eq 'eomtthyhvnlwuznrcbaqkxi'", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$toLower": "\$plainString"}, "eomtthyhvnlwuznrcbaqkxi"]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tolower(plainString) eq tolower('eOMtThyhVNLWUZNRcBaQKxI')", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$toLower": "\$plainString"}, {"\$toLower": "eOMtThyhVNLWUZNRcBaQKxI"}]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "plainString eq 'Some text'", """{"\$match": {"\$and": [{"plainString": "Some text"}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "startswith(plainString,'So')", """{"\$match": {"\$and": [{"plainString": {"\$regex": "^\\\\QSo\\\\E"}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "startswith(plainString,'So') and plainString eq 'Some text'", """{"\$match": {"\$and": [{"\$and": [{"plainString": {"\$regex": "^\\\\QSo\\\\E"}}, {"plainString": "Some text"}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "startswith(plainString,'Some t') and smallInteger eq -1188957731", """{"\$match": {"\$and": [{"\$and": [{"plainString": {"\$regex": "^\\\\QSome t\\\\E"}}, {"smallInteger": -1188957731}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "startswith(plainString,'Po') or smallInteger eq -113", """{"\$match": {"\$and": [{"\$or": [{"plainString": {"\$regex": "^\\\\QPo\\\\E"}}, {"smallInteger": -113}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "timestamp ge 2024-07-20T10:00:00.00Z and timestamp le 2024-07-20T20:00:00.00Z", """{"\$match": {"\$and": [{"\$and": [{"timestamp": {"\$gte": {"\$date": "2024-07-20T10:00:00Z"}}}, {"timestamp": {"\$lte": {"\$date": "2024-07-20T20:00:00Z"}}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "uuidProp eq b921f1dd-3cbc-0495-fdab-8cd14d33f0aa", """{"\$match": {"\$and": [{"uuidProp": {"\$binary": {"base64": "uSHx3Ty8BJX9q4zRTTPwqg==", "subType": "04"}}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "plainString eq 'eOMtThyhVNLWUZNRcBaQKxI' and password eq 'password1'", """{"\$match": {"\$and": [{"\$and": [{"plainString": "eOMtThyhVNLWUZNRcBaQKxI"}, {"password": "password1"}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "plainString eq 'eOMtThyhVNLWUZNRcBaQKxI' or password eq 'password1'", """{"\$match": {"\$and": [{"\$or": [{"plainString": "eOMtThyhVNLWUZNRcBaQKxI"}, {"password": "password1"}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/any(t:t eq 'developer') or tags/any(t:t eq 'LLM')", """{"\$match": {"\$and": [{"\$or": [{"tags": "developer"}, {"tags": "LLM"}]}]}}"""]
        ]
    }

}
