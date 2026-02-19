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
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'val1'))", """{"\$match": {"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"nestedComplexArray": {"\$not": {"\$not": {"\$elemMatch": {"stringVal": {"\$not": {"\$eq": "val1"}}}}}}}}}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/nestedComplexArray/all(n:startswith(n/stringVal,'val')))", """{"\$match": {"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"nestedComplexArray": {"\$not": {"\$not": {"\$elemMatch": {"stringVal": {"\$not": {"\$regex": "^\\\\Qval\\\\E"}}}}}}}}}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/nestedComplexArray/all(n:contains(n/stringVal,'match')))", """{"\$match": {"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"nestedComplexArray": {"\$not": {"\$not": {"\$elemMatch": {"stringVal": {"\$not": {"\$regex": "\\\\Qmatch\\\\E"}}}}}}}}}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'val1' or n/stringVal eq 'test1'))", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}, "as": "n", "cond": {"\$or": [{"\$eq": ["\$\$n.stringVal", "val1"]}, {"\$eq": ["\$\$n.stringVal", "test1"]}]}}}}, {"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}]}}}}, {"\$size": {"\$ifNull": ["\$complexList", []]}}]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'val1' or n/stringVal eq 'test1') and c/someNumber ge 20)", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$and": [{"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}, "as": "n", "cond": {"\$or": [{"\$eq": ["\$\$n.stringVal", "val1"]}, {"\$eq": ["\$\$n.stringVal", "test1"]}]}}}}, {"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}]}, {"\$gte": ["\$\$c.someNumber", 20]}]}}}}, {"\$size": {"\$ifNull": ["\$complexList", []]}}]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'val1' or n/stringVal eq 'test1')) and complexList/any()", """{"\$match": {"\$and": [{"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}, "as": "n", "cond": {"\$or": [{"\$eq": ["\$\$n.stringVal", "val1"]}, {"\$eq": ["\$\$n.stringVal", "test1"]}]}}}}, {"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}]}}}}, {"\$size": {"\$ifNull": ["\$complexList", []]}}]}}, {"\$expr": {"\$gt": [{"\$size": {"\$ifNull": ["\$complexList", []]}}, 0]}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'val1') and c/nestedComplexArray/any()) and complexList/any(c:c/nestedComplexArray/any())", """{"\$match": {"\$and": [{"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$and": [{"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}, "as": "n", "cond": {"\$eq": ["\$\$n.stringVal", "val1"]}}}}, {"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}]}, {"\$gt": [{"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}, 0]}]}}}}, {"\$size": {"\$ifNull": ["\$complexList", []]}}]}}, {"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$gt": [{"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}, 0]}}}}, 0]}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'matchAll'))", """{"\$match": {"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"nestedComplexArray": {"\$not": {"\$not": {"\$elemMatch": {"stringVal": {"\$not": {"\$eq": "matchAll"}}}}}}}}}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/nestedComplexArray/all(n:n/numberVal gt 70))", """{"\$match": {"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"nestedComplexArray": {"\$not": {"\$not": {"\$elemMatch": {"numberVal": {"\$not": {"\$gt": 70}}}}}}}}}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/nestedComplexArray/any(n:n/numberVal eq 71) and c/nestedComplexArray/any(n:n/numberVal eq 72))", """{"\$match": {"\$and": [{"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"nestedComplexArray.numberVal": {"\$not": {"\$eq": 71}}}}}}, {"complexList": {"\$not": {"\$elemMatch": {"nestedComplexArray.numberVal": {"\$not": {"\$eq": 72}}}}}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/nestedComplexArray/\$count ge 2)", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$gte": [{"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}, 2]}}}}, {"\$size": {"\$ifNull": ["\$complexList", []]}}]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/nestedComplexArray/\$count ge 2)  and complexList/any(c:c/nestedComplexArray/any())", """{"\$match": {"\$and": [{"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$gte": [{"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}, 2]}}}}, {"\$size": {"\$ifNull": ["\$complexList", []]}}]}}, {"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$gt": [{"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}, 0]}}}}, 0]}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/nestedComplexArray/any(n:n/numberVal ge 70 and n/stringVal eq 'matchAll')) and complexList/any(c:c/nestedComplexArray/any())", """{"\$match": {"\$and": [{"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"nestedComplexArray": {"\$not": {"\$elemMatch": {"numberVal": {"\$gte": 70}, "stringVal": "matchAll"}}}}}}}, {"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$gt": [{"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}, 0]}}}}, 0]}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/primitiveStringList/all(n:startswith(n,'item1')))", """{"\$match": {"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"primitiveStringList": {"\$not": {"\$not": {"\$elemMatch": {"\$not": {"\$regex": "^\\\\Qitem1\\\\E"}}}}}}}}}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/primitiveStringList/all(n:startswith(n,'item1'))) and complexList/any(c:c/primitiveStringList/any())", """{"\$match": {"\$and": [{"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"primitiveStringList": {"\$not": {"\$not": {"\$elemMatch": {"\$not": {"\$regex": "^\\\\Qitem1\\\\E"}}}}}}}}}, {"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$gt": [{"\$size": {"\$ifNull": ["\$\$c.primitiveStringList", []]}}, 0]}}}}, 0]}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/primitiveNumberList/all(n:n gt 10))", """{"\$match": {"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"primitiveNumberList": {"\$not": {"\$not": {"\$elemMatch": {"\$not": {"\$gt": 10}}}}}}}}}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/primitiveNumberList/all(n:n gt 10)) and complexList/any(c:c/primitiveStringList/any())", """{"\$match": {"\$and": [{"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"primitiveNumberList": {"\$not": {"\$not": {"\$elemMatch": {"\$not": {"\$gt": 10}}}}}}}}}, {"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$gt": [{"\$size": {"\$ifNull": ["\$\$c.primitiveStringList", []]}}, 0]}}}}, 0]}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/nestedComplexArray/all(n:n/numberVal eq c/someNumber))  and complexList/any(c:c/nestedComplexArray/any())", """{"\$match": {"\$and": [{"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}, "as": "n", "cond": {"\$eq": ["\$\$n.numberVal", "\$\$c.someNumber"]}}}}, {"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}]}}}}, {"\$size": {"\$ifNull": ["\$complexList", []]}}]}}, {"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$gt": [{"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}, 0]}}}}, 0]}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:c/nestedComplexArray/all(n:c/someNumber eq n/numberVal))  and complexList/any(c:c/nestedComplexArray/any())", """{"\$match": {"\$and": [{"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}, "as": "n", "cond": {"\$eq": ["\$\$c.someNumber", "\$\$n.numberVal"]}}}}, {"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}]}}}}, {"\$size": {"\$ifNull": ["\$complexList", []]}}]}}, {"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$gt": [{"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}, 0]}}}}, 0]}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "complexList/all(c:not c/nestedComplexArray/all(n:c/someNumber eq n/numberVal))  and complexList/any(c:c/nestedComplexArray/any())", """{"\$match": {"\$and": [{"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$not": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}, "as": "n", "cond": {"\$eq": ["\$\$c.someNumber", "\$\$n.numberVal"]}}}}, {"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}]}}}}}, {"\$size": {"\$ifNull": ["\$complexList", []]}}]}}, {"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$gt": [{"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}, 0]}}}}, 0]}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/all(t:startswith(t,'star') and t ne 'starlord')", """{"\$match": {"\$and": [{"\$and": [{"tags": {"\$not": {"\$elemMatch": {"\$not": {"\$regex": "^\\\\Qstar\\\\E"}}}}}, {"tags": {"\$not": {"\$elemMatch": {"\$not": {"\$ne": "starlord"}}}}}]}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/all(t:startswith(t,'star') or t ne 'starlord')", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$tags", []]}, "as": "t", "cond": {"\$or": [{"\$regexMatch": {"input": "\$\$t", "regex": "^\\\\Qstar\\\\E", "options": "i"}}, {"\$ne": ["\$\$t", "starlord"]}]}}}}, {"\$size": {"\$ifNull": ["\$tags", []]}}]}}]}}"""],
                ["edm/edm6_filter_main.xml", "examples2"  , "tags/all(t:startswith(t,'star ') or t eq 'starlord')", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$tags", []]}, "as": "t", "cond": {"\$or": [{"\$regexMatch": {"input": "\$\$t", "regex": "^\\\\Qstar \\\\E", "options": "i"}}, {"\$eq": ["\$\$t", "starlord"]}]}}}}, {"\$size": {"\$ifNull": ["\$tags", []]}}]}}]}}"""]
        ]
    }
}
