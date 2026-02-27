package com.github.starnowski.jamolingo.core.operators.filter

import com.github.starnowski.jamolingo.core.AbstractSpecification
import com.github.starnowski.jamolingo.common.json.JSONOverrideHelper
import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade
import com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContextBuilder
import com.github.starnowski.jamolingo.core.mapping.EntityMapping
import com.github.starnowski.jamolingo.core.mapping.ODataMongoMappingFactory
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

import java.nio.file.Files
import java.nio.file.Paths

/**
 * Test class for ODataFilterToMongoMatchParser with override configuration.
 *
 * The purpose of these tests is to verify the generation of the $match stage based on the OData $filter operator
 * when the EDM model is different from the MongoDB mapping.
 * In this specific test scenario, each MongoDB property is configured with a "renamed_" prefix compared to its
 * corresponding reference in the EDM model.
 */
class ODataFilterToMongoMatchParserWithOverrideConfigurationTest extends AbstractSpecification {

    /**
     * Verifies that the generated MongoDB $match stage matches the expected BSON document.
     */
    @Unroll
    def "should return expected stage bson objects"(){
        given:
            Bson expectedBson = Document.parse(bson)
            Edm edm = loadEmdProvider("edm/edm6_filter_main.xml")
            ODataMongoMappingFactory factory = new ODataMongoMappingFactory()
            def odataMapping = factory.build(edm.getSchema("MyService"))
            def entityMapping = odataMapping.getEntities().get("Example2")
            
            def helper = new JSONOverrideHelper()
            String mergePayload = Files.readString(Paths.get(getClass().getClassLoader().getResource("mappings/edm6_override.json").toURI()))
            entityMapping = helper.applyChangesToJson(entityMapping, mergePayload, EntityMapping.class, JSONOverrideHelper.PatchType.MERGE)

            EntityPropertiesMongoPathContextBuilder entityPropertiesMongoPathContextBuilder = new EntityPropertiesMongoPathContextBuilder()
            def context = entityPropertiesMongoPathContextBuilder.build(entityMapping)
            def facade = DefaultEdmMongoContextFacade.builder()
                    .withEntityPropertiesMongoPathContext(context)
                    .build()

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

        when:
            def result = tested.parse(uriInfo.getFilterOption(), edm, facade)

        then:
            [((Document)result.getStageObjects().get(0)).toJson(settings, codec)] == [((Document)expectedBson).toJson(settings, codec)]

        where:
            [filter, bson] << oneToOneEdmPathsMappings()
    }

    /**
     * Verifies that the parser correctly identifies all the MongoDB property paths used in the generated query.
     */
    @Unroll
    def "should return expected used MongoDB properties"(){
        given:
            Edm edm = loadEmdProvider("edm/edm6_filter_main.xml")
            ODataMongoMappingFactory factory = new ODataMongoMappingFactory()
            def odataMapping = factory.build(edm.getSchema("MyService"))
            def entityMapping = odataMapping.getEntities().get("Example2")
            
            def helper = new JSONOverrideHelper()
            String mergePayload = Files.readString(Paths.get(getClass().getClassLoader().getResource("mappings/edm6_override.json").toURI()))
            entityMapping = helper.applyChangesToJson(entityMapping, mergePayload, EntityMapping.class, JSONOverrideHelper.PatchType.MERGE)

            EntityPropertiesMongoPathContextBuilder entityPropertiesMongoPathContextBuilder = new EntityPropertiesMongoPathContextBuilder()
            def context = entityPropertiesMongoPathContextBuilder.build(entityMapping)
            def facade = DefaultEdmMongoContextFacade.builder()
                    .withEntityPropertiesMongoPathContext(context)
                    .build()

            UriInfo uriInfo = new Parser(edm, OData.newInstance())
                    .parseUri("examples2",
                            "\$filter=" +filter
                            , null, null)
            ODataFilterToMongoMatchParser tested = new ODataFilterToMongoMatchParser()

        when:
            def result = tested.parse(uriInfo.getFilterOption(), edm, facade)

        then:
            new HashSet<>(result.getUsedMongoDocumentProperties()) == new HashSet(expectedFields as List)

        where:
            [filter, bson, expectedFields] << oneToOneEdmPathsMappings()
    }

    /**
     * Provides test data mapping OData filters to expected MongoDB $match documents and used properties.
     * Each test case uses the "renamed_" prefix for MongoDB properties.
     */
    static oneToOneEdmPathsMappings() {
        [
                [ "plainString eq 'abc'", """{"\$match": {"\$and": [{"renamed_plainString": "abc"}]}}""", ["renamed_plainString"]],
                [ "isActive eq true", """{"\$match": {"\$and": [{"renamed_isActive": true}]}}""", ["renamed_isActive"]],
                [ "nestedObject/index eq 'idx1'", """{"\$match": {"\$and": [{"renamed_nestedObject.renamed_index": "idx1"}]}}""", ["renamed_nestedObject.renamed_index"]],
                [ "tags/any(t:t eq 'tag1')", """{"\$match": {"\$and": [{"renamed_tags": {"\$elemMatch": {"\$eq": "tag1"}}}]}}""", ["renamed_tags"]],
                [ "tags/all(t:t ne 'no such text' and t ne 'no such word')", """{"\$match": {"\$and": [{"\$and": [{"renamed_tags": {"\$not": {"\$elemMatch": {"\$not": {"\$ne": "no such text"}}}}}, {"renamed_tags": {"\$not": {"\$elemMatch": {"\$not": {"\$ne": "no such word"}}}}}]}]}}""", ["renamed_tags"]],
                [ "complexList/any(c:c/someString eq 'val1')", """{"\$match": {"\$and": [{"renamed_complexList": {"\$elemMatch": {"renamed_someString": "val1"}}}]}}""", ["renamed_complexList.renamed_someString"]],
                [ "numericArray/all(n:n gt 5)", """{"\$match": {"\$and": [{"renamed_numericArray": {"\$not": {"\$elemMatch": {"\$not": {"\$gt": 5}}}}}]}}""", ["renamed_numericArray"]],
                [ "complexList/all(c:c/someNumber eq 20)", """{"\$match": {"\$and": [{"renamed_complexList": {"\$not": {"\$elemMatch": {"renamed_someNumber": {"\$not": {"\$eq": 20}}}}}}]}}""", ["renamed_complexList.renamed_someNumber"]],
                [ "tolower(plainString) eq 'abc'", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$toLower": "\$renamed_plainString"}, "abc"]}}]}}""", ["renamed_plainString"]],
                [ "year(birthDate) eq 2024", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$year": "\$renamed_birthDate"}, 2024]}}]}}""", ["renamed_birthDate"]],
                [ "tags/\$count ge 2", """{"\$match": {"\$and": [{"\$expr": {"\$gte": [{"\$size": {"\$ifNull": ["\$renamed_tags", []]}}, 2]}}]}}""", ["renamed_tags"]],
                [ "plainString in ('abc', 'def')", """{"\$match": {"\$and": [{"renamed_plainString": {"\$in": ["abc", "def"]}}]}}""", ["renamed_plainString"]],
                [ "startswith(plainString, 'So')", """{"\$match": {"\$and": [{"renamed_plainString": {"\$regex": "^\\\\QSo\\\\E"}}]}}""", ["renamed_plainString"]],
                [ "nestedObject/tokens/any(t:t eq 'first example') and nestedObject/numbers/any(t:t gt 5 and t lt 27)", """{"\$match": {"\$and": [{"\$and": [{"renamed_nestedObject.renamed_tokens": {"\$elemMatch": {"\$eq": "first example"}}}, {"renamed_nestedObject.renamed_numbers": {"\$elemMatch": {"\$gt": 5, "\$lt": 27}}}]}]}}""", ["renamed_nestedObject.renamed_tokens", "renamed_nestedObject.renamed_numbers"]],
                [ "timestamp ge 2024-07-20T10:00:00.00Z and timestamp le 2024-07-20T20:00:00.00Z", """{"\$match": {"\$and": [{"\$and": [{"renamed_timestamp": {"\$gte": {"\$date": "2024-07-20T10:00:00Z"}}}, {"renamed_timestamp": {"\$lte": {"\$date": "2024-07-20T20:00:00Z"}}}]}]}}""", ["renamed_timestamp"]],
                [ "complexList/any(c:c/someString eq 'Banana' or c/someString eq 'Cherry')", """{"\$match": {"\$and": [{"\$or": [{"renamed_complexList": {"\$elemMatch": {"renamed_someString": "Banana"}}}, {"renamed_complexList": {"\$elemMatch": {"renamed_someString": "Cherry"}}}]}]}}""", ["renamed_complexList.renamed_someString"]],
                [ "numericArray/any(n:n add 2 gt round(n))", """{"\$match": {"\$and": [{"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$renamed_numericArray", []]}, "as": "n", "cond": {"\$gt": [{"\$add": ["\$\$n", 2]}, {"\$round": "\$\$n"}]}}}}, 0]}}]}}""", ["renamed_numericArray"]],
                [ "length(plainString) eq 4", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$strLenCP": "\$renamed_plainString"}, 4]}}]}}""", ["renamed_plainString"]],
                [ "complexList/any(c:c/nestedComplexArray/any(n:n/stringVal eq 'val1'))", """{"\$match": {"\$and": [{"renamed_complexList": {"\$elemMatch": {"renamed_nestedComplexArray": {"\$elemMatch": {"renamed_stringVal": "val1"}}}}}]}}""", ["renamed_complexList.renamed_nestedComplexArray.renamed_stringVal"]],
                [ "tags/all(t:startswith(t,'starlord') or t in ('star trek', 'star wars'))", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$renamed_tags", []]}, "as": "t", "cond": {"\$or": [{"\$regexMatch": {"input": "\$\$t", "regex": "^\\\\Qstarlord\\\\E", "options": "i"}}, {"\$in": ["\$\$t", ["star trek", "star wars"]]}]}}}}, {"\$size": {"\$ifNull": ["\$renamed_tags", []]}}]}}]}}""", ["renamed_tags"]],
                [ "numericArray/all(n:n gt floor(5.05))", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$renamed_numericArray", []]}, "as": "n", "cond": {"\$gt": ["\$\$n", {"\$floor": 5.05}]}}}}, {"\$size": {"\$ifNull": ["\$renamed_numericArray", []]}}]}}]}}""", ["renamed_numericArray"]],
                [ "complexList/all(c:c/someNumber gt 5)", """{"\$match": {"\$and": [{"renamed_complexList": {"\$not": {"\$elemMatch": {"renamed_someNumber": {"\$not": {"\$gt": 5}}}}}}]}}""", ["renamed_complexList.renamed_someNumber"]],
                [ "complexList/all(c:c/someNumber eq 10 or c/someNumber eq 20)", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$renamed_complexList", []]}, "as": "c", "cond": {"\$or": [{"\$eq": ["\$\$c.renamed_someNumber", 10]}, {"\$eq": ["\$\$c.renamed_someNumber", 20]}]}}}}, {"\$size": {"\$ifNull": ["\$renamed_complexList", []]}}]}}]}}""", ["renamed_complexList.renamed_someNumber"]],
                [ "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'val1'))", """{"\$match": {"\$and": [{"renamed_complexList": {"\$not": {"\$elemMatch": {"renamed_nestedComplexArray": {"\$not": {"\$not": {"\$elemMatch": {"renamed_stringVal": {"\$not": {"\$eq": "val1"}}}}}}}}}}]}}""", ["renamed_complexList.renamed_nestedComplexArray.renamed_stringVal"]],
                [ "complexList/all(c:c/nestedComplexArray/all(n:n/stringVal eq 'val1' or n/stringVal eq 'test1'))", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$renamed_complexList", []]}, "as": "c", "cond": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$\$c.renamed_nestedComplexArray", []]}, "as": "n", "cond": {"\$or": [{"\$eq": ["\$\$n.renamed_stringVal", "val1"]}, {"\$eq": ["\$\$n.renamed_stringVal", "test1"]}]}}}}, {"\$size": {"\$ifNull": ["\$\$c.renamed_nestedComplexArray", []]}}]}}}}, {"\$size": {"\$ifNull": ["\$renamed_complexList", []]}}]}}]}}""", ["renamed_complexList.renamed_nestedComplexArray.renamed_stringVal"]],
                [ "complexList/all(c:c/nestedComplexArray/\$count ge 2)", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$renamed_complexList", []]}, "as": "c", "cond": {"\$gte": [{"\$size": {"\$ifNull": ["\$\$c.renamed_nestedComplexArray", []]}}, 2]}}}}, {"\$size": {"\$ifNull": ["\$renamed_complexList", []]}}]}}]}}""", ["renamed_complexList.renamed_nestedComplexArray"]],
                [ "uuidProp eq b921f1dd-3cbc-0495-fdab-8cd14d33f0aa", """{"\$match": {"\$and": [{"renamed_uuidProp": {"\$binary": {"base64": "uSHx3Ty8BJX9q4zRTTPwqg==", "subType": "04"}}}]}}""", ["renamed_uuidProp"]],
                [ "month(birthDate) eq 6", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$month": "\$renamed_birthDate"}, 6]}}]}}""", ["renamed_birthDate"]],
                [ "ceiling(floatValue) eq 1", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$ceil": "\$renamed_floatValue"}, 1]}}]}}""", ["renamed_floatValue"]],
                [ "tags/any(t:startswith(t,'spider') and t ne 'spiderweb')", """{"\$match": {"\$and": [{"renamed_tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$ne": "spiderweb"}}}]}}""", ["renamed_tags"]],
                [ "complexList/any(c:c/someNumber gt 5)", """{"\$match": {"\$and": [{"renamed_complexList": {"\$elemMatch": {"renamed_someNumber": {"\$gt": 5}}}}]}}""", ["renamed_complexList.renamed_someNumber"]],
                [ "trim('   Poem   ') eq 'Poem'", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$trim": {"input": "   Poem   "}}, "Poem"]}}]}}""", []],
                [ "complexList/any(c:c/nestedComplexArray/\$count ge 2)", """{"\$match": {"\$and": [{"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$renamed_complexList", []]}, "as": "c", "cond": {"\$gte": [{"\$size": {"\$ifNull": ["\$\$c.renamed_nestedComplexArray", []]}}, 2]}}}}, 0]}}]}}""", ["renamed_complexList.renamed_nestedComplexArray"]],
                [ "plainString eq 'eOMtThyhVNLWUZNRcBaQKxI'", """{"\$match": {"\$and": [{"renamed_plainString": "eOMtThyhVNLWUZNRcBaQKxI"}]}}""", ["renamed_plainString"]],
                [ "startswith(plainString,'Some t') and smallInteger eq -1188957731", """{"\$match": {"\$and": [{"\$and": [{"renamed_plainString": {"\$regex": "^\\\\QSome t\\\\E"}}, {"renamed_smallInteger": -1188957731}]}]}}""", ["renamed_plainString", "renamed_smallInteger"]],
                [ "complexList/all(c:c/primitiveStringList/all(n:startswith(n,'item1')))", """{"\$match":{"\$and":[{"renamed_complexList":{"\$not":{"\$elemMatch":{"renamed_primitiveStringList":{"\$not":{"\$not":{"\$elemMatch":{"\$not":{"\$regex":"^\\\\Qitem1\\\\E"}}}}}}}}}]}}""", ["renamed_complexList.renamed_primitiveStringList"]],
                [ "complexList/any(c:c/nestedComplexArray/any(n:n/numberVal eq 71) and c/nestedComplexArray/any(n:n/numberVal eq 72))", """{"\$match": {"\$and": [{"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$renamed_complexList", []]}, "as": "c", "cond": {"\$and": [{"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$\$c.renamed_nestedComplexArray", []]}, "as": "n", "cond": {"\$eq": ["\$\$n.renamed_numberVal", 71]}}}}, 0]}, {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$\$c.renamed_nestedComplexArray", []]}, "as": "n", "cond": {"\$eq": ["\$\$n.renamed_numberVal", 72]}}}}, 0]}]}}}}, 0]}}]}}""", ["renamed_complexList.renamed_nestedComplexArray.renamed_numberVal"]],
                [ "tags/all(t:contains(tolower(t),tolower('star')))", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$renamed_tags", []]}, "as": "t", "cond": {"\$gte": [{"\$indexOfBytes": [{"\$toLower": "\$\$t"}, {"\$toLower": "star"}]}, 0]}}}}, {"\$size": {"\$ifNull": ["\$renamed_tags", []]}}]}}]}}""", ["renamed_tags"]],
                [ "complexList/all(c:c/nestedComplexArray/all(n:n/numberVal eq c/someNumber))  and complexList/any(c:c/nestedComplexArray/any())", """{"\$match": {"\$and": [{"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$renamed_complexList", []]}, "as": "c", "cond": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$\$c.renamed_nestedComplexArray", []]}, "as": "n", "cond": {"\$eq": ["\$\$n.renamed_numberVal", "\$\$c.renamed_someNumber"]}}}}, {"\$size": {"\$ifNull": ["\$\$c.renamed_nestedComplexArray", []]}}]}}}}, {"\$size": {"\$ifNull": ["\$renamed_complexList", []]}}]}}, {"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$renamed_complexList", []]}, "as": "c", "cond": {"\$gt": [{"\$size": {"\$ifNull": ["\$\$c.renamed_nestedComplexArray", []]}}, 0]}}}}, 0]}}]}]}}""", ["renamed_complexList.renamed_nestedComplexArray.renamed_numberVal", "renamed_complexList.renamed_someNumber", "renamed_complexList.renamed_nestedComplexArray"]],
                [ "tags/all(t:contains(t,'starlord') or contains(t,'trek') or contains(t,'wars'))", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$renamed_tags", []]}, "as": "t", "cond": {"\$or": [{"\$or": [{"\$regexMatch": {"input": "\$\$t", "regex": "\\\\Qstarlord\\\\E", "options": "i"}}, {"\$regexMatch": {"input": "\$\$t", "regex": "\\\\Qtrek\\\\E", "options": "i"}}]}, {"\$regexMatch": {"input": "\$\$t", "regex": "\\\\Qwars\\\\E", "options": "i"}}]}}}}, {"\$size": {"\$ifNull": ["\$renamed_tags", []]}}]}}]}}""", ["renamed_tags"]],
                [ "tags/all(t:length(t) eq 9)", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$renamed_tags", []]}, "as": "t", "cond": {"\$eq": [{"\$strLenCP": "\$\$t"}, 9]}}}}, {"\$size": {"\$ifNull": ["\$renamed_tags", []]}}]}}]}}""", ["renamed_tags"]],
                [ "complexList/all(c:c/someNumber add 5 gt 20)", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$renamed_complexList", []]}, "as": "c", "cond": {"\$gt": [{"\$add": ["\$\$c.renamed_someNumber", 5]}, 20]}}}}, {"\$size": {"\$ifNull": ["\$renamed_complexList", []]}}]}}]}}""", ["renamed_complexList.renamed_someNumber"]],
                [ "tags/any(t:contains(tolower(t),'star'))", """{"\$match": {"\$and": [{"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$renamed_tags", []]}, "as": "t", "cond": {"\$regexMatch": {"input": {"\$toLower": "\$\$t"}, "regex": "\\\\Qstar\\\\E", "options": "i"}}}}}, 0]}}]}}""", ["renamed_tags"]],
                [ "day(birthDate) eq 18", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$dayOfMonth": "\$renamed_birthDate"}, 18]}}]}}""", ["renamed_birthDate"]],
                [ "hour(timestamp) eq 10", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$hour": "\$renamed_timestamp"}, 10]}}]}}""", ["renamed_timestamp"]],
                [ "floor(floatValue) eq 0", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$floor": "\$renamed_floatValue"}, 0]}}]}}""", ["renamed_floatValue"]],
                [ "round(floatValue) eq 1", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$round": "\$renamed_floatValue"}, 1]}}]}}""", ["renamed_floatValue"]],
                [ "tags/\$count ge 3", """{"\$match": {"\$and": [{"\$expr": {"\$gte": [{"\$size": {"\$ifNull": ["\$renamed_tags", []]}}, 3]}}]}}""", ["renamed_tags"]],
                [ "tolower(plainString) eq tolower('eOMtThyhVNLWUZNRcBaQKxI')", """{"\$match": {"\$and": [{"\$expr": {"\$eq": [{"\$toLower": "\$renamed_plainString"}, {"\$toLower": "eOMtThyhVNLWUZNRcBaQKxI"}]}}]}}""", ["renamed_plainString"]],
                [ "plainString in ('Some text', 'no such text')", """{"\$match": {"\$and": [{"renamed_plainString": {"\$in": ["Some text", "no such text"]}}]}}""", ["renamed_plainString"]],
                [ "startswith(plainString,'Po') or smallInteger eq -113", """{"\$match": {"\$and": [{"\$or": [{"renamed_plainString": {"\$regex": "^\\\\QPo\\\\E"}}, {"renamed_smallInteger": -113}]}]}}""", ["renamed_plainString", "renamed_smallInteger"]],
                [ "plainString eq 'eOMtThyhVNLWUZNRcBaQKxI' and password eq 'password1'", """{"\$match": {"\$and": [{"\$and": [{"renamed_plainString": "eOMtThyhVNLWUZNRcBaQKxI"}, {"renamed_password": "password1"}]}]}}""", ["renamed_plainString", "renamed_password"]],
                [ "tags/any(t:t in ('developer', 'LLM'))", """{"\$match": {"\$and": [{"renamed_tags": {"\$elemMatch": {"\$in": ["developer", "LLM"]}}}]}}""", ["renamed_tags"]],
                [ "tags/any(t:startswith(t,'spider') and t eq 'spiderweb')", """{"\$match": {"\$and": [{"renamed_tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$eq": "spiderweb"}}}]}}""", ["renamed_tags"]],
                [ "tags/any(t:startswith(t,'spider') and t ne 'spiderweb' or startswith(t,'spider') and t ne 'spider' or contains(t,'wide') and t ne 'word wide')", """{"\$match": {"\$and": [{"\$or": [{"renamed_tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$ne": "spiderweb"}}}, {"renamed_tags": {"\$elemMatch": {"\$regex": "^\\\\Qspider\\\\E", "\$ne": "spider"}}}, {"renamed_tags": {"\$elemMatch": {"\$regex": "\\\\Qwide\\\\E", "\$ne": "word wide"}}}]}]}}""", ["renamed_tags"]],
                [ "complexList/all(c:c/nestedComplexArray/all(n:n/numberVal gt 70))", """{"\$match": {"\$and": [{"renamed_complexList": {"\$not": {"\$elemMatch": {"renamed_nestedComplexArray": {"\$not": {"\$not": {"\$elemMatch": {"renamed_numberVal": {"\$not": {"\$gt": 70}}}}}}}}}}]}}""", ["renamed_complexList.renamed_nestedComplexArray.renamed_numberVal"]],
                [ "complexList/any(c:c/nestedComplexArray/any(n:n/stringVal eq 'val1' or n/stringVal eq 'test1'))", """{"\$match": {"\$and": [{"renamed_complexList": {"\$elemMatch": {"\$or": [{"renamed_nestedComplexArray": {"\$elemMatch": {"renamed_stringVal": "val1"}}}, {"renamed_nestedComplexArray": {"\$elemMatch": {"renamed_stringVal": "test1"}}}]}}}]}}""", ["renamed_complexList.renamed_nestedComplexArray.renamed_stringVal"]],
                [ "complexList/all(c:c/nestedComplexArray/any(n:n/numberVal ge 70 and n/stringVal eq 'matchAll')) and complexList/any(c:c/nestedComplexArray/any())", """{"\$match": {"\$and": [{"\$and": [{"renamed_complexList": {"\$not": {"\$elemMatch": {"renamed_nestedComplexArray": {"\$not": {"\$elemMatch": {"renamed_numberVal": {"\$gte": 70}, "renamed_stringVal": "matchAll"}}}}}}}, {"\$expr": {"\$gt": [{"\$size": {"\$filter": {"input": {"\$ifNull": ["\$renamed_complexList", []]}, "as": "c", "cond": {"\$gt": [{"\$size": {"\$ifNull": ["\$\$c.renamed_nestedComplexArray", []]}}, 0]}}}}, 0]}}]}]}}""", ["renamed_complexList.renamed_nestedComplexArray.renamed_numberVal", "renamed_complexList.renamed_nestedComplexArray.renamed_stringVal", "renamed_complexList.renamed_nestedComplexArray"]]
        ]
    }
}
