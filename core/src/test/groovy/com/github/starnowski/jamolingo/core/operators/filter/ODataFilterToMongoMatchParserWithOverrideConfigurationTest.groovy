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

class ODataFilterToMongoMatchParserWithOverrideConfigurationTest extends AbstractSpecification {

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

        when:
            def result = tested.parse(uriInfo.getFilterOption(), edm)

        then:
            [((Document)result.getStageObjects().get(0)).toJson(settings, codec)] == [((Document)expectedBson).toJson(settings, codec)]

        where:
            [filter, bson] << oneToOneEdmPathsMappings()
    }

    @Unroll
    def "should return expected used MongoDB properties"(){
        given:
            System.out.println("Testing filter: " + filter)
            Edm edm = loadEmdProvider("edm/edm6_filter_main.xml")
            UriInfo uriInfo = new Parser(edm, OData.newInstance())
                    .parseUri("examples2",
                            "\$filter=" +filter
                            , null, null)
            ODataFilterToMongoMatchParser tested = new ODataFilterToMongoMatchParser()

        when:
            def result = tested.parse(uriInfo.getFilterOption(), edm)

        then:
            new HashSet<>(result.getUsedMongoDocumentProperties()) == new HashSet(expectedFields as List)

        where:
            [filter, bson, expectedFields] << oneToOneEdmPathsMappings()
    }

    static oneToOneEdmPathsMappings() {
        [
                [ "tags/all(t:t ne 'no such text' and t ne 'no such word')", """{"\$match": {"\$and": [{"\$and": [{"renamed_tags": {"\$not": {"\$elemMatch": {"\$not": {"\$ne": "no such text"}}}}}, {"renamed_tags": {"\$not": {"\$elemMatch": {"\$not": {"\$ne": "no such word"}}}}}]}]}}""", ["renamed_tags"]],
        ]
    }
}

