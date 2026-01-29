package com.github.starnowski.jamolingo.core.operators.filter

import com.github.starnowski.jamolingo.core.AbstractSpecification
import com.mongodb.MongoClientSettings
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.uri.parser.Parser
import org.bson.Document
import org.bson.UuidRepresentation
import org.bson.codecs.DocumentCodec
import org.bson.codecs.UuidCodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import org.bson.json.JsonWriterSettings
import spock.lang.Unroll

class ODataFilterToMongoMatchParserTest extends AbstractSpecification {

    @Unroll
    def "should return expected stage bson objects"(){
        given:
        Bson expectedBson = Document.parse(bson)
        Edm edm = loadEmdProvider(edmConfigFile)
        JsonWriterSettings settings = JsonWriterSettings.builder().build()
        CodecRegistry registry = CodecRegistries.fromRegistries(
                CodecRegistries.fromProviders(new UuidCodecProvider(UuidRepresentation.STANDARD)),
                MongoClientSettings.getDefaultCodecRegistry()
        )
        DocumentCodec codec = new DocumentCodec(registry)

        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri(path,
                        "\$filter=" +filter
                        , null, null)
        ODataFilterToMongoMatchParser tested = new ODataFilterToMongoMatchParser()

        when:
        def result = tested.parse(uriInfo.getFilterOption(), edm)

        then:
        [((Document)result.getStageObjects().get(0)).toJson(settings, codec)] == [((Document)expectedBson).toJson(settings, codec)]

        where:
        [edmConfigFile, path , filter, bson] << oneToOneEdmPathsMappings()
    }

    static oneToOneEdmPathsMappings() {
        [
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/all(t:t ne 'no such text' and t ne 'no such word')", """{"\$match": {"\$and": [{"\$and": [{"tags": {"\$not": {"\$elemMatch": {"\$not": {"\$ne": "no such text"}}}}}, {"tags": {"\$not": {"\$elemMatch": {"\$not": {"\$ne": "no such word"}}}}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/all(t:startswith(t,'star') and t ne 'starlord')", """{"\$match": {"\$and": [{"\$and": [{"tags": {"\$not": {"\$elemMatch": {"\$not": {"\$regex": "^\\\\Qstar\\\\E"}}}}}, {"tags": {"\$not": {"\$elemMatch": {"\$not": {"\$ne": "starlord"}}}}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "nestedObject/tokens/any(t:t ne 'no such text')", """{"\$match": {"\$and": [{"nestedObject.tokens": {"\$ne": "no such text"}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "nestedObject/tokens/any(t:t eq 'first example') and nestedObject/numbers/any(t:t gt 5 and t lt 27)", """{"\$match": {"\$and": [{"\$and": [{"nestedObject.tokens": "first example"}, {"nestedObject.numbers": {"\$elemMatch": {"\$gt": 5, "\$lt": 27}}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "nestedObject/tokens/any(t:t eq 'normalize(''First example'')')", """{"\$match": {"\$and": [{"nestedObject.tokens": "normalize(''First example'')"}]}}"""],
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
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/any(t:t eq 'developer') or tags/any(t:t eq 'LLM')", """{"\$match": {"\$and": [{"\$or": [{"tags": "developer"}, {"tags": "LLM"}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/any(t:startswith(t,'spider') and t ne 'spiderweb' or endswith(t,'web') and t ne 'spiderweb')", """{"\$match": {"\$and": [{"\$or": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$ne": "spiderweb"}}}, {"tags": {"\$elemMatch": {"\$regex": "\\\\Qweb\\\\E\$", "\$ne": "spiderweb"}}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/any(t:startswith(t,'spider') and t ne 'spiderweb' or endswith(t,'web') and t ne 'spiderweb' or contains(t,'wide') and t ne 'word wide')", """{"\$match": {"\$and": [{"\$or": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$ne": "spiderweb"}}}, {"tags": {"\$elemMatch": {"\$regex": "\\\\Qweb\\\\E\$", "\$ne": "spiderweb"}}}, {"tags": {"\$elemMatch": {"\$regex": "\\\\Qwide\\\\E", "\$ne": "word wide"}}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "toupper(plainString) eq 'EOMTTHYHVNLWUZNRCBAQKXI'", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$toUpper": "\$plainString"}, "EOMTTHYHVNLWUZNRCBAQKXI"]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "plainString in ('Some text', 'no such text')", """{"\$match": {"\$and": [{"plainString": {"\$in": ["Some text", "no such text"]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "plainString eq 'eOMtThyhVNLWUZNRcBaQKxI' and uuidProp eq b921f1dd-3cbc-0495-fdab-8cd14d33f0aa", """{"\$match": {"\$and": [{"\$and": [{"plainString": "eOMtThyhVNLWUZNRcBaQKxI"}, {"uuidProp": {"\$binary": {"base64": "uSHx3Ty8BJX9q4zRTTPwqg==", "subType": "04"}}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/any(t:t in ('developer', 'LLM'))", """{"\$match": {"\$and": [{"tags": {"\$in": ["developer", "LLM"]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tolower(plainString) eq 'poem'", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$toLower": "\$plainString"}, "poem"]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "toupper(plainString) eq 'POEM'", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$toUpper": "\$plainString"}, "POEM"]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "length(plainString) eq 4", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$strLenCP": "\$plainString"}, 4]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/any(t:startswith(t,'dev'))", """{"\$match": {"\$and": [{"tags": {"\$regex": "^\\\\Qdev\\\\E"}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/any(t:startswith(t,'dev') and length(t) eq 9)", """{"\$match": {"\$and": [{"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": "\$tags", "as": "t", "cond": {"\$and": [{"\$regexMatch": {"input": "\$\$t", "regex": "^\\\\Qdev\\\\E", "options": "i"}}, {"\$eq": [{"\$strLenCP": "\$\$t"}, 9]}]}}}}, 0]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/any(t:length(t) eq 13)", """{"\$match": {"\$and": [{"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": "\$tags", "as": "t", "cond": {"\$eq": [{"\$strLenCP": "\$\$t"}, 13]}}}}, 0]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/any(t:tolower(t) eq 'developer')", """{"\$match": {"\$and": [{"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": "\$tags", "as": "t", "cond": {"\$eq": [{"\$toLower": "\$\$t"}, "developer"]}}}}, 0]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/any(t:startswith(t,'spider') and endswith(t, 'web'))", """{"\$match": {"\$and": [{"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": "\$tags", "as": "t", "cond": {"\$and": [{"\$regexMatch": {"input": "\$\$t", "regex": "^\\\\Qspider\\\\E", "options": "i"}}, {"\$regexMatch": {"input": "\$\$t", "regex": "\\\\Qweb\\\\E\$", "options": "i"}}]}}}}, 0]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/any(t:startswith(t,'spider') and t eq 'spiderweb')", """{"\$match": {"\$and": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$eq": "spiderweb"}}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/any(t:startswith(t,'spider') and t ne 'spiderweb')", """{"\$match": {"\$and": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$ne": "spiderweb"}}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "trim('   Poem   ') eq 'Poem'", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$trim": {"input": "   Poem   "}}, "Poem"]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "year(birthDate) eq 2024", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$year": "\$birthDate"}, 2024]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "month(birthDate) eq 6", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$month": "\$birthDate"}, 6]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "day(birthDate) eq 18", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$dayOfMonth": "\$birthDate"}, 18]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "hour(timestamp) eq 10", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$hour": "\$timestamp"}, 10]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "minute(timestamp) eq 15", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$minute": "\$timestamp"}, 15]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "second(timestamp) eq 26", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$second": "\$timestamp"}, 26]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "ceiling(floatValue) eq 1", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$ceil": "\$floatValue"}, 1]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "floor(floatValue) eq 0", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$floor": "\$floatValue"}, 0]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "round(floatValue) eq 1", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$round": "\$floatValue"}, 1]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/any(t:startswith(t,'spider') and t ne 'spiderweb' or startswith(t,'spider') and t ne 'spider' or contains(t,'wide') and t ne 'word wide')", """{"\$match": {"\$and": [{"\$or": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$ne": "spiderweb"}}}, {"tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$ne": "spider"}}}, {"tags": {"\$elemMatch": {"\$regex": "\\\\Qwide\\\\E", "\$ne": "word wide"}}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/any(t:startswith(t,'spider') and t ne 'spiderweb' or endswith(t,'web') and t ne 'spiderwebgg' or contains(t,'wide') and t ne 'word wide')", """{"\$match": {"\$and": [{"\$or": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$ne": "spiderweb"}}}, {"tags": {"\$elemMatch": {"\$regex": "\\\\Qweb\\\\E\$", "\$ne": "spiderwebgg"}}}, {"tags": {"\$elemMatch": {"\$regex": "\\\\Qwide\\\\E", "\$ne": "word wide"}}}]}]}}"""]
        ]
    }
}
