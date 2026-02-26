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
                [ "complexList/any(c:c/nestedComplexArray/any(n:n/stringVal eq 'val1'))", """{"\$match": {"\$and": [{"renamed_complexList": {"\$elemMatch": {"renamed_nestedComplexArray": {"\$elemMatch": {"renamed_stringVal": "val1"}}}}}]}}""", ["renamed_complexList.renamed_nestedComplexArray.renamed_stringVal"]]
        ]
    }
}
