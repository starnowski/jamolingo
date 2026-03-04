package com.github.starnowski.jamolingo.core.operators.filter

import com.github.starnowski.jamolingo.core.AbstractSpecification
import com.mongodb.MongoClientSettings
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.api.uri.queryoption.expression.Literal
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

class ODataFilterToMongoMatchParserWithOverrideLiteralConverterTest extends AbstractSpecification {


    @Unroll
    def "should return expected stage bson objects"(){
        given:
            System.out.println("Testing filter: " + filter)
            Bson expectedBson = Document.parse(bson)
            Edm edm = loadEmdProvider("edm/edm6_filter_main.xml")
            JsonWriterSettings settings = JsonWriterSettings.builder().build()
            CodecRegistry registry = CodecRegistries.fromRegistries(
                    CodecRegistries.fromProviders(new UuidCodecProvider(UuidRepresentation.STANDARD)),
                    MongoClientSettings.getDefaultCodecRegistry()
            )
            DocumentCodec codec = new DocumentCodec(registry)

            UriInfo uriInfo = new Parser(edm, OData.newInstance())
                    .parseUri("examples2",
                            "\$filter=" +filter
                            , null, null)
            ODataFilterToMongoMatchParser tested = new ODataFilterToMongoMatchParser()
//            ODataToBsonConverter oDataToBsonConverter = new DefaultODataToBsonConverter() {
//                @Override
//                Object toBsonValue(Object value, String type) {
//                    def valToUnwrap = value
//                    if (value instanceof org.bson.BsonString) {
//                        valToUnwrap = value.asString().getValue()
//                    }
//                    if (valToUnwrap instanceof String && valToUnwrap.startsWith("custom_string(") && valToUnwrap.endsWith(")")) {
//                        valToUnwrap = valToUnwrap.substring("custom_string(".length(), valToUnwrap.length() - 1)
//                    }
//                    return super.toBsonValue(valToUnwrap, type)
//                }
//            }
            LiteralToBsonConverter literalToBsonConverter = new DefaultLiteralToBsonConverter() {
                @Override
                Bson convert(Literal literal) {
                    Bson result = super.convert(literal)
                    if (result instanceof Document && result.containsKey('$odata.literal')) {
                        def value = result.get('$odata.literal')
                        if (value instanceof String && value.startsWith("custom_string(") && value.endsWith(")")) {
                            value = value.substring("custom_string(".length(), value.length() - 1)
                            return new Document('$odata.literal', value)
                        }
                    }
                    return result
                }
            }
            MongoFilterVisitorCommonContext context = DefaultMongoFilterVisitorCommonContext.builder()
//                    .withODataToBsonConverter(oDataToBsonConverter)
                    .withLiteralToBsonConverter(literalToBsonConverter)
                    .build()

        when:
            def result = tested.parse(uriInfo.getFilterOption(), context)

        then:
            [((Document)result.getStageObjects().get(0)).toJson(settings, codec)] == [((Document)expectedBson).toJson(settings, codec)]

        where:
            [filter, bson] << oneToOneEdmPathsMappings()
    }

    /**
     * Provides test data mapping OData filters to expected MongoDB $match documents.
     * In this provider, the mapping between EDM and MongoDB is one-to-one.
     */
    static oneToOneEdmPathsMappings() {
        [
                [ "tags/all(t:t ne 'custom_string(no such text)' and t ne 'custom_string(no such word)')", """{"\$match": {"\$and": [{"\$and": [{"tags": {"\$not": {"\$elemMatch": {"\$not": {"\$ne": "no such text"}}}}}, {"tags": {"\$not": {"\$elemMatch": {"\$not": {"\$ne": "no such word"}}}}}]}]}}""", ["tags"]],
                [ "tags/all(t:startswith(t,'custom_string(starlord)') or t in ('custom_string(star trek)', 'custom_string(star wars)'))", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$tags", []]}, "as": "t", "cond": {"\$or": [{"\$regexMatch": {"input": "\$\$t", "regex": "^\\\\Qstarlord\\\\E", "options": "i"}}, {"\$in": ["\$\$t", ["star trek", "star wars"]]}]}}}}, {"\$size": {"\$ifNull": ["\$tags", []]}}]}}]}}""", ["tags"]],
                [ "tags/all(t:contains(t,'custom_string(starlord)') or contains(t,'custom_string(trek)') or contains(t,'custom_string(wars)'))", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$tags", []]}, "as": "t", "cond": {"\$or": [{"\$or": [{"\$regexMatch": {"input": "\$\$t", "regex": "\\\\Qstarlord\\\\E", "options": "i"}}, {"\$regexMatch": {"input": "\$\$t", "regex": "\\\\Qtrek\\\\E", "options": "i"}}]}, {"\$regexMatch": {"input": "\$\$t", "regex": "\\\\Qwars\\\\E", "options": "i"}}]}}}}, {"\$size": {"\$ifNull": ["\$tags", []]}}]}}]}}""", ["tags"]],
                [ "tags/all(t:contains(t,'custom_string(starlord)'))", """{"\$match": {"\$and": [{"tags": {"\$not": {"\$elemMatch": {"\$not": {"\$regex": "\\\\Qstarlord\\\\E"}}}}}]}}""", ["tags"]],
                [ "tags/all(t:endswith(t,'custom_string(web)') or endswith(t,'custom_string(trap)'))", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$tags", []]}, "as": "t", "cond": {"\$or": [{"\$regexMatch": {"input": "\$\$t", "regex": "\\\\Qweb\\\\E\$", "options": "i"}}, {"\$regexMatch": {"input": "\$\$t", "regex": "\\\\Qtrap\\\\E\$", "options": "i"}}]}}}}, {"\$size": {"\$ifNull": ["\$tags", []]}}]}}]}}""", ["tags"]],
                [ "tags/all(t:contains(tolower(t),'custom_string(star)'))", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$tags", []]}, "as": "t", "cond": {"\$regexMatch": {"input": {"\$toLower": "\$\$t"}, "regex": "\\\\Qstar\\\\E", "options": "i"}}}}}, {"\$size": {"\$ifNull": ["\$tags", []]}}]}}]}}""", ["tags"]],
                [ "tags/all(t:contains(tolower(t),tolower('custom_string(star)')))", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$tags", []]}, "as": "t", "cond": {"\$gte": [{"\$indexOfBytes": [{"\$toLower": "\$\$t"}, {"\$toLower": "star"}]}, 0]}}}}, {"\$size": {"\$ifNull": ["\$tags", []]}}]}}]}}""", ["tags"]],
                [ "tags/all(t:startswith(toupper(t),toupper('custom_string(star)')))", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$tags", []]}, "as": "t", "cond": {"\$eq": [{"\$indexOfBytes": [{"\$toUpper": "\$\$t"}, {"\$toUpper": "star"}]}, 0]}}}}, {"\$size": {"\$ifNull": ["\$tags", []]}}]}}]}}""", ["tags"]],
                [ "tags/all(t:contains(toupper(t),'custom_string(STAR)'))", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$tags", []]}, "as": "t", "cond": {"\$regexMatch": {"input": {"\$toUpper": "\$\$t"}, "regex": "\\\\QSTAR\\\\E", "options": "i"}}}}}, {"\$size": {"\$ifNull": ["\$tags", []]}}]}}]}}""", ["tags"]],
                [ "complexList/all(c:startswith(c/someString,'custom_string(Ap)'))", """{"\$match": {"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"someString": {"\$not": {"\$regex": "^\\\\QAp\\\\E"}}}}}}]}}""", ["complexList.someString"]],
                [ "complexList/all(c:contains(c/someString,'custom_string(ana)'))", """{"\$match": {"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"someString": {"\$not": {"\$regex": "\\\\Qana\\\\E"}}}}}}]}}""", ["complexList.someString"]],
                [ "complexList/all(c:endswith(c/someString,'custom_string(erry)'))", """{"\$match": {"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"someString": {"\$not": {"\$regex": "\\\\Qerry\\\\E\$"}}}}}}]}}""", ["complexList.someString"]],
                [ "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'custom_string(val1)'))", """{"\$match": {"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"nestedComplexArray": {"\$not": {"\$not": {"\$elemMatch": {"stringVal": {"\$not": {"\$eq": "val1"}}}}}}}}}}]}}""", ["complexList.nestedComplexArray.stringVal"]],
                [ "complexList/all(c:c/nestedComplexArray/all(n:startswith(n/stringVal,'custom_string(val)')))", """{"\$match": {"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"nestedComplexArray": {"\$not": {"\$not": {"\$elemMatch": {"stringVal": {"\$not": {"\$regex": "^\\\\Qval\\\\E"}}}}}}}}}}]}}""", ["complexList.nestedComplexArray.stringVal"]],
                [ "complexList/all(c:c/nestedComplexArray/all(n:contains(n/stringVal,'custom_string(match)')))", """{"\$match": {"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"nestedComplexArray": {"\$not": {"\$not": {"\$elemMatch": {"stringVal": {"\$not": {"\$regex": "\\\\Qmatch\\\\E"}}}}}}}}}}]}}""", ["complexList.nestedComplexArray.stringVal"]],
                [ "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'custom_string(val1)' or n/stringVal eq 'custom_string(test1)'))", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}, "as": "n", "cond": {"\$or": [{"\$eq": ["\$\$n.stringVal", "val1"]}, {"\$eq": ["\$\$n.stringVal", "test1"]}]}}}}, {"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}]}}}}, {"\$size": {"\$ifNull": ["\$complexList", []]}}]}}]}}""", ["complexList.nestedComplexArray.stringVal"]],
                [ "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'custom_string(val1)' or n/stringVal eq 'custom_string(test1)') and c/someNumber ge 20)", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$and": [{"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}, "as": "n", "cond": {"\$or": [{"\$eq": ["\$\$n.stringVal", "val1"]}, {"\$eq": ["\$\$n.stringVal", "test1"]}]}}}}, {"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}]}, {"\$gte": ["\$\$c.someNumber", 20]}]}}}}, {"\$size": {"\$ifNull": ["\$complexList", []]}}]}}]}}""", ["complexList.nestedComplexArray.stringVal", "complexList.someNumber"]],
                [ "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'custom_string(val1)' or n/stringVal eq 'custom_string(test1)')) and complexList/any()", """{"\$match": {"\$and": [{"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}, "as": "n", "cond": {"\$or": [{"\$eq": ["\$\$n.stringVal", "val1"]}, {"\$eq": ["\$\$n.stringVal", "test1"]}]}}}}, {"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}]}}}}, {"\$size": {"\$ifNull": ["\$complexList", []]}}]}}, {"\$expr": {"\$gt": [{"\$size": {"\$ifNull": ["\$complexList", []]}}, 0]}}]}]}}""", ["complexList.nestedComplexArray.stringVal", "complexList"]],
                [ "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'custom_string(val1)') and c/nestedComplexArray/any()) and complexList/any(c:c/nestedComplexArray/any())", """{"\$match": {"\$and": [{"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$and": [{"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}, "as": "n", "cond": {"\$eq": ["\$\$n.stringVal", "val1"]}}}}, {"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}]}, {"\$gt": [{"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}, 0]}]}}}}, {"\$size": {"\$ifNull": ["\$complexList", []]}}]}}, {"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$gt": [{"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}, 0]}}}}, 0]}}]}]}}""", ["complexList.nestedComplexArray.stringVal", "complexList.nestedComplexArray"]],
                [ "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'custom_string(matchAll)'))", """{"\$match": {"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"nestedComplexArray": {"\$not": {"\$not": {"\$elemMatch": {"stringVal": {"\$not": {"\$eq": "matchAll"}}}}}}}}}}]}}""", ["complexList.nestedComplexArray.stringVal"]],
                [ "complexList/all(c:c/nestedComplexArray/any(n:n/numberVal ge 70 and n/stringVal eq 'custom_string(matchAll)')) and complexList/any(c:c/nestedComplexArray/any())", """{"\$match": {"\$and": [{"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"nestedComplexArray": {"\$not": {"\$elemMatch": {"numberVal": {"\$gte": 70}, "stringVal": "matchAll"}}}}}}}, {"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$gt": [{"\$size": {"\$ifNull": ["\$\$c.nestedComplexArray", []]}}, 0]}}}}, 0]}}]}]}}""", ["complexList.nestedComplexArray.numberVal", "complexList.nestedComplexArray.stringVal", "complexList.nestedComplexArray"]],
                [ "complexList/all(c:c/primitiveStringList/all(n:startswith(n,'custom_string(item1)')))", """{"\$match":{"\$and":[{"complexList":{"\$not":{"\$elemMatch":{"primitiveStringList":{"\$not":{"\$not":{"\$elemMatch":{"\$not":{"\$regex":"^\\\\Qitem1\\\\E"}}}}}}}}}]}}""", ["complexList.primitiveStringList"]],
                [ "complexList/all(c:c/primitiveStringList/all(n:startswith(n,'custom_string(item1)'))) and complexList/any(c:c/primitiveStringList/any())", """{"\$match": {"\$and": [{"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"primitiveStringList": {"\$not": {"\$not": {"\$elemMatch": {"\$not": {"\$regex": "^\\\\Qitem1\\\\E"}}}}}}}}}, {"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$complexList", []]}, "as": "c", "cond": {"\$gt": [{"\$size": {"\$ifNull": ["\$\$c.primitiveStringList", []]}}, 0]}}}}, 0]}}]}]}}""", ["complexList.primitiveStringList"]],
                [ "tags/all(t:startswith(t,'custom_string(star)') and t ne 'custom_string(starlord)')", """{"\$match": {"\$and": [{"\$and": [{"tags": {"\$not": {"\$elemMatch": {"\$not": {"\$regex": "^\\\\Qstar\\\\E"}}}}}, {"tags": {"\$not": {"\$elemMatch": {"\$not": {"\$ne": "starlord"}}}}}]}]}}""", ["tags"]],
                [ "tags/all(t:startswith(t,'custom_string(star)') or t ne 'custom_string(starlord)')", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$tags", []]}, "as": "t", "cond": {"\$or": [{"\$regexMatch": {"input": "\$\$t", "regex": "^\\\\Qstar\\\\E", "options": "i"}}, {"\$ne": ["\$\$t", "starlord"]}]}}}}, {"\$size": {"\$ifNull": ["\$tags", []]}}]}}]}}""", ["tags"]],
                [ "tags/all(t:startswith(t,'custom_string(star )') or t eq 'custom_string(starlord)')", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$tags", []]}, "as": "t", "cond": {"\$or": [{"\$regexMatch": {"input": "\$\$t", "regex": "^\\\\Qstar \\\\E", "options": "i"}}, {"\$eq": ["\$\$t", "starlord"]}]}}}}, {"\$size": {"\$ifNull": ["\$tags", []]}}]}}]}}""", ["tags"]],
                [ "complexList/all(c:contains(c/someString,'custom_string(e)'))", """{"\$match": {"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"someString": {"\$not": {"\$regex": "\\\\Qe\\\\E"}}}}}}]}}""", ["complexList.someString"]],
                [ "complexList/all(c:c/someString eq 'custom_string(Application)')", """{"\$match": {"\$and": [{"complexList": {"\$not": {"\$elemMatch": {"someString": {"\$not": {"\$eq": "Application"}}}}}}]}}""", ["complexList.someString"]],
                [ "complexList/any(c:c/nestedComplexArray/any(n:n/stringVal eq 'custom_string(val1)'))", """{"\$match": {"\$and": [{"complexList": {"\$elemMatch": {"nestedComplexArray": {"\$elemMatch": {"stringVal": "val1"}}}}}]}}""", ["complexList.nestedComplexArray.stringVal"]],
                [ "complexList/any(c:c/nestedComplexArray/any(n:startswith(n/stringVal,'custom_string(val)')))", """{"\$match": {"\$and": [{"complexList": {"\$elemMatch": {"nestedComplexArray": {"\$elemMatch": {"stringVal": {"\$regex": "^\\\\Qval\\\\E"}}}}}}]}}""", ["complexList.nestedComplexArray.stringVal"]],
                [ "complexList/any(c:c/nestedComplexArray/any(n:contains(n/stringVal,'custom_string(match)')))", """{"\$match": {"\$and": [{"complexList": {"\$elemMatch": {"nestedComplexArray": {"\$elemMatch": {"stringVal": {"\$regex": "\\\\Qmatch\\\\E"}}}}}}]}}""", ["complexList.nestedComplexArray.stringVal"]],
                [ "complexList/any(c:c/nestedComplexArray/any(n:n/stringVal eq 'custom_string(val1)' or n/stringVal eq 'custom_string(test1)'))", """{"\$match": {"\$and": [{"complexList": {"\$elemMatch": {"\$or": [{"nestedComplexArray": {"\$elemMatch": {"stringVal": "val1"}}}, {"nestedComplexArray": {"\$elemMatch": {"stringVal": "test1"}}}]}}}]}}""", ["complexList.nestedComplexArray.stringVal"]],
                [ "complexList/any(c:c/nestedComplexArray/any(n:n/stringVal eq 'custom_string(val1)' or n/stringVal eq 'custom_string(test1)') and c/someNumber ge 20)", """{"\$match": {"\$and": [{"complexList": {"\$elemMatch": {"\$or": [{"nestedComplexArray": {"\$elemMatch": {"stringVal": "val1"}}}, {"nestedComplexArray": {"\$elemMatch": {"stringVal": "test1"}}}], "someNumber": {"\$gte": 20}}}}]}}""", ["complexList.nestedComplexArray.stringVal", "complexList.someNumber"]],
                [ "complexList/any(c:c/primitiveStringList/any(n:startswith(n,'custom_string(item11)')))", """{"\$match": {"\$and": [{"complexList": {"\$elemMatch": {"primitiveStringList": {"\$elemMatch": {"\$regex": "^\\\\Qitem11\\\\E"}}}}}]}}""", ["complexList.primitiveStringList"]],
                [ "tags/any(t:t eq 'custom_string(word wide web)')", """{"\$match": {"\$and": [{"tags": {"\$elemMatch": {"\$eq": "word wide web"}}}]}}""", ["tags"]],
                [ "tags/any(t:startswith(t,'custom_string(star)'))", """{"\$match": {"\$and": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qstar\\\\E"}}}]}}""", ["tags"]],
                [ "tags/any(t:contains(t,'custom_string(spider)'))", """{"\$match": {"\$and": [{"tags": {"\$elemMatch": {"\$regex": "\\\\Qspider\\\\E"}}}]}}""", ["tags"]],
                [ "tags/any(t:contains(tolower(t),'custom_string(star)'))", """{"\$match": {"\$and": [{"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$tags", []]}, "as": "t", "cond": {"\$regexMatch": {"input": {"\$toLower": "\$\$t"}, "regex": "\\\\Qstar\\\\E", "options": "i"}}}}}, 0]}}]}}""", ["tags"]],
                [ "tags/any(t:endswith(toupper(t),'custom_string(TRAP)'))", """{"\$match": {"\$and": [{"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$tags", []]}, "as": "t", "cond": {"\$regexMatch": {"input": {"\$toUpper": "\$\$t"}, "regex": "\\\\QTRAP\\\\E\$", "options": "i"}}}}}, 0]}}]}}""", ["tags"]],
                [ "tags/any(t:t ne 'custom_string(no such text)' and t ne 'custom_string(no such word)')", """{"\$match": {"\$and": [{"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$tags", []]}, "as": "t", "cond": {"\$and": [{"\$ne": ["\$\$t", "no such text"]}, {"\$ne": ["\$\$t", "no such word"]}]}}}}, 0]}}]}}""", ["tags"]],
                [ "tags/any(t:startswith(t,'custom_string(star)') and t ne 'custom_string(starlord)')", """{"\$match": {"\$and": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qstar\\\\E", "\$ne": "starlord"}}}]}}""", ["tags"]],
                [ "tags/any(t:startswith(t,'custom_string(star)') or t ne 'custom_string(starlord)')", """{"\$match": {"\$and": [{"\$or": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qstar\\\\E"}}}, {"tags": {"\$elemMatch": {"\$ne": "starlord"}}}]}]}}""", ["tags"]],
                [ "tags/any(t:startswith(t,'custom_string(star )') or t eq 'custom_string(starlord)')", """{"\$match": {"\$and": [{"\$or": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qstar \\\\E"}}}, {"tags": {"\$elemMatch": {"\$eq": "starlord"}}}]}]}}""", ["tags"]],
                [ "tags/any(t:startswith(t,'custom_string(starlord)') or t in ('custom_string(star trek)', 'custom_string(star wars)'))", """{"\$match": {"\$and": [{"\$or": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qstarlord\\\\E"}}}, {"tags": {"\$elemMatch": {"\$in": ["star trek", "star wars"]}}}]}]}}""", ["tags"]],
                [ "tags/any(t:contains(t,'custom_string(starlord)') or contains(t,'custom_string(trek)') or contains(t,'custom_string(wars)'))", """{"\$match": {"\$and": [{"\$or": [{"tags": {"\$elemMatch": {"\$regex": "\\\\Qstarlord\\\\E"}}}, {"tags": {"\$elemMatch": {"\$regex": "\\\\Qtrek\\\\E"}}}, {"tags": {"\$elemMatch": {"\$regex": "\\\\Qwars\\\\E"}}}]}]}}""", ["tags"]],
                [ "tags/any(t:contains(t,'custom_string(starlord)'))", """{"\$match": {"\$and": [{"tags": {"\$elemMatch": {"\$regex": "\\\\Qstarlord\\\\E"}}}]}}""", ["tags"]],
                [ "tags/any(t:endswith(t,'custom_string(web)') or endswith(t,'custom_string(trap)'))", """{"\$match": {"\$and": [{"\$or": [{"tags": {"\$elemMatch": {"\$regex": "\\\\Qweb\\\\E\$"}}}, {"tags": {"\$elemMatch": {"\$regex": "\\\\Qtrap\\\\E\$"}}}]}]}}""", ["tags"]],
                [ "complexList/any(c:c/someString eq 'custom_string(Apple)')", """{"\$match": {"\$and": [{"complexList": {"\$elemMatch": {"someString": "Apple"}}}]}}""", ["complexList.someString"]],
                [ "complexList/any(c:c/someString eq 'custom_string(Banana)' or c/someString eq 'custom_string(Cherry)')", """{"\$match": {"\$and": [{"\$or": [{"complexList": {"\$elemMatch": {"someString": "Banana"}}}, {"complexList": {"\$elemMatch": {"someString": "Cherry"}}}]}]}}""", ["complexList.someString"]],
                [ "complexList/any(c:startswith(c/someString,'custom_string(Ap)'))", """{"\$match": {"\$and": [{"complexList": {"\$elemMatch": {"someString": {"\$regex": "^\\\\QAp\\\\E"}}}}]}}""", ["complexList.someString"]],
                [ "complexList/any(c:contains(c/someString,'custom_string(ana)'))", """{"\$match": {"\$and": [{"complexList": {"\$elemMatch": {"someString": {"\$regex": "\\\\Qana\\\\E"}}}}]}}""", ["complexList.someString"]],
                [ "complexList/any(c:endswith(c/someString,'custom_string(erry)'))", """{"\$match": {"\$and": [{"complexList": {"\$elemMatch": {"someString": {"\$regex": "\\\\Qerry\\\\E\$"}}}}]}}""", ["complexList.someString"]],
                [ "complexList/any(c:contains(c/someString,'custom_string(e)'))", """{"\$match": {"\$and": [{"complexList": {"\$elemMatch": {"someString": {"\$regex": "\\\\Qe\\\\E"}}}}]}}""", ["complexList.someString"]],
                [ "complexList/any(c:c/someString eq 'custom_string(Application)')", """{"\$match": {"\$and": [{"complexList": {"\$elemMatch": {"someString": "Application"}}}]}}""", ["complexList.someString"]],
                [ "nestedObject/tokens/any(t:t ne 'custom_string(no such text)')", """{"\$match": {"\$and": [{"nestedObject.tokens": {"\$elemMatch": {"\$ne": "no such text"}}}]}}""", ["nestedObject.tokens"]],
                [ "nestedObject/tokens/any(t:t eq 'custom_string(first example)') and nestedObject/numbers/any(t:t gt 5 and t lt 27)", """{"\$match": {"\$and": [{"\$and": [{"nestedObject.tokens": {"\$elemMatch": {"\$eq": "first example"}}}, {"nestedObject.numbers": {"\$elemMatch": {"\$gt": 5, "\$lt": 27}}}]}]}}""", ["nestedObject.tokens", "nestedObject.numbers"]],
                [ "trim('custom_string(   Poem   )') eq 'custom_string(Poem)'", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$trim": {"input": "   Poem   "}}, "Poem"]}}]}}""", []],
                [ "tags/any(t:startswith(t,'custom_string(spider)') and t ne 'custom_string(spiderweb)' or startswith(t,'custom_string(spider)') and t ne 'custom_string(spider)' or contains(t,'custom_string(wide)') and t ne 'custom_string(word wide)')", """{"\$match": {"\$and": [{"\$or": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$ne": "spiderweb"}}}, {"tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$ne": "spider"}}}, {"tags": {"\$elemMatch": {"\$regex": "\\\\Qwide\\\\E", "\$ne": "word wide"}}}]}]}}""", ["tags"]],
                [ "tags/any(t:startswith(t,'custom_string(spider)') and t ne 'custom_string(spiderweb)' or endswith(t,'custom_string(web)') and t ne 'custom_string(spiderwebgg)' or contains(t,'custom_string(wide)') and t ne 'custom_string(word wide)')", """{"\$match": {"\$and": [{"\$or": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$ne": "spiderweb"}}}, {"tags": {"\$elemMatch": {"\$regex": "\\\\Qweb\\\\E\$", "\$ne": "spiderwebgg"}}}, {"tags": {"\$elemMatch": {"\$regex": "\\\\Qwide\\\\E", "\$ne": "word wide"}}}]}]}}""", ["tags"]],
                [ "plainString eq 'custom_string(eOMtThyhVNLWUZNRcBaQKxI)'", """{"\$match": {"\$and": [{"plainString": "eOMtThyhVNLWUZNRcBaQKxI"}]}}""", ["plainString"]],
                [ "tolower(plainString) eq 'custom_string(eomtthyhvnlwuznrcbaqkxi)'", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$toLower": "\$plainString"}, "eomtthyhvnlwuznrcbaqkxi"]}}]}}""", ["plainString"]],
                [ "tolower(plainString) eq tolower('custom_string(eOMtThyhVNLWUZNRcBaQKxI)')", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$toLower": "\$plainString"}, {"\$toLower": "eOMtThyhVNLWUZNRcBaQKxI"}]}}]}}""", ["plainString"]],
                [ "toupper(plainString) eq 'custom_string(EOMTTHYHVNLWUZNRCBAQKXI)'", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$toUpper": "\$plainString"}, "EOMTTHYHVNLWUZNRCBAQKXI"]}}]}}""", ["plainString"]],
                [ "plainString eq 'custom_string(Some text)'", """{"\$match": {"\$and": [{"plainString": "Some text"}]}}""", ["plainString"]],
                [ "plainString in ('custom_string(Some text)', 'custom_string(no such text)')", """{"\$match": {"\$and": [{"plainString": {"\$in": ["Some text", "no such text"]}}]}}""", ["plainString"]],
                [ "startswith(plainString,'custom_string(So)')", """{"\$match": {"\$and": [{"plainString": {"\$regex": "^\\\\QSo\\\\E"}}]}}""", ["plainString"]],
                [ "startswith(plainString,'custom_string(So)') and plainString eq 'custom_string(Some text)'", """{"\$match": {"\$and": [{"\$and": [{"plainString": {"\$regex": "^\\\\QSo\\\\E"}}, {"plainString": "Some text"}]}]}}""", ["plainString"]],
                [ "startswith(plainString,'custom_string(Some t)') and smallInteger eq -1188957731", """{"\$match": {"\$and": [{"\$and": [{"plainString": {"\$regex": "^\\\\QSome t\\\\E"}}, {"smallInteger": -1188957731}]}]}}""", ["plainString", "smallInteger"]],
                [ "startswith(plainString,'custom_string(Po)') or smallInteger eq -113", """{"\$match": {"\$and": [{"\$or": [{"plainString": {"\$regex": "^\\\\QPo\\\\E"}}, {"smallInteger": -113}]}]}}""", ["plainString", "smallInteger"]],
                [ "plainString eq 'custom_string(eOMtThyhVNLWUZNRcBaQKxI)' and uuidProp eq b921f1dd-3cbc-0495-fdab-8cd14d33f0aa", """{"\$match": {"\$and": [{"\$and": [{"plainString": "eOMtThyhVNLWUZNRcBaQKxI"}, {"uuidProp": {"\$binary": {"base64": "uSHx3Ty8BJX9q4zRTTPwqg==", "subType": "04"}}}]}]}}""", ["plainString", "uuidProp"]],
                [ "plainString eq 'custom_string(eOMtThyhVNLWUZNRcBaQKxI)' and password eq 'custom_string(password1)'", """{"\$match": {"\$and": [{"\$and": [{"plainString": "eOMtThyhVNLWUZNRcBaQKxI"}, {"password": "password1"}]}]}}""", ["plainString", "password"]],
                [ "plainString eq 'custom_string(eOMtThyhVNLWUZNRcBaQKxI)' or password eq 'custom_string(password1)'", """{"\$match": {"\$and": [{"\$or": [{"plainString": "eOMtThyhVNLWUZNRcBaQKxI"}, {"password": "password1"}]}]}}""", ["plainString", "password"]],
                [ "tags/any(t:t eq 'custom_string(developer)') or tags/any(t:t eq 'custom_string(LLM)')", """{"\$match": {"\$and": [{"\$or": [{"tags": {"\$elemMatch": {"\$eq": "developer"}}}, {"tags": {"\$elemMatch": {"\$eq": "LLM"}}}]}]}}""", ["tags"]],
                [ "tags/any(t:t in ('custom_string(developer)', 'custom_string(LLM)'))", """{"\$match": {"\$and": [{"tags": {"\$elemMatch": {"\$in": ["developer", "LLM"]}}}]}}""", ["tags"]],
                [ "tolower(plainString) eq 'custom_string(poem)'", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$toLower": "\$plainString"}, "poem"]}}]}}""", ["plainString"]],
                [ "toupper(plainString) eq 'custom_string(POEM)'", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$toUpper": "\$plainString"}, "POEM"]}}]}}""", ["plainString"]],
                [ "tags/any(t:startswith(t,'custom_string(dev)'))", """{"\$match": {"\$and": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qdev\\\\E"}}}]}}""", ["tags"]],
                [ "tags/any(t:startswith(t,'custom_string(dev)') and length(t) eq 9)", """{"\$match": {"\$and": [{"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$tags", []]}, "as": "t", "cond": {"\$and": [{"\$regexMatch": {"input": "\$\$t", "regex": "^\\\\Qdev\\\\E", "options": "i"}}, {"\$eq": [{"\$strLenCP": "\$\$t"}, 9]}]}}}}, 0]}}]}}""", ["tags"]],
                [ "tags/any(t:tolower(t) eq 'custom_string(developer)')", """{"\$match": {"\$and": [{"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$tags", []]}, "as": "t", "cond": {"\$eq": [{"\$toLower": "\$\$t"}, "developer"]}}}}, 0]}}]}}""", ["tags"]],
                [ "tags/any(t:startswith(t,'custom_string(spider)') and endswith(t, 'custom_string(web)'))", """{"\$match": {"\$and": [{"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$tags", []]}, "as": "t", "cond": {"\$and": [{"\$regexMatch": {"input": "\$\$t", "regex": "^\\\\Qspider\\\\E", "options": "i"}}, {"\$regexMatch": {"input": "\$\$t", "regex": "\\\\Qweb\\\\E\$", "options": "i"}}]}}}}, 0]}}]}}""", ["tags"]],
                [ "tags/any(t:startswith(t,'custom_string(spider)') and t eq 'custom_string(spiderweb)')", """{"\$match": {"\$and": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$eq": "spiderweb"}}}]}}""", ["tags"]],
                [ "tags/any(t:startswith(t,'custom_string(spider)') and t ne 'custom_string(spiderweb)')", """{"\$match": {"\$and": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$ne": "spiderweb"}}}]}}""", ["tags"]],
                [ "tags/any(t:startswith(t,'custom_string(spider)') and t ne 'custom_string(spiderweb)' or endswith(t,'custom_string(web)') and t ne 'custom_string(spiderweb)')", """{"\$match": {"\$and": [{"\$or": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$ne": "spiderweb"}}}, {"tags": {"\$elemMatch": {"\$regex": "\\\\Qweb\\\\E\$", "\$ne": "spiderweb"}}}]}]}}""", ["tags"]],
                [ "tags/any(t:startswith(t,'custom_string(spider)') and t ne 'custom_string(spiderweb)' or endswith(t,'custom_string(web)') and t ne 'custom_string(spiderweb)' or contains(t,'custom_string(wide)') and t ne 'custom_string(word wide)')", """{"\$match": {"\$and": [{"\$or": [{"tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$ne": "spiderweb"}}}, {"tags": {"\$elemMatch": {"\$regex": "\\\\Qweb\\\\E\$", "\$ne": "spiderweb"}}}, {"tags": {"\$elemMatch": {"\$regex": "\\\\Qwide\\\\E", "\$ne": "word wide"}}}]}]}}""", ["tags"]]
        ]
    }
}

