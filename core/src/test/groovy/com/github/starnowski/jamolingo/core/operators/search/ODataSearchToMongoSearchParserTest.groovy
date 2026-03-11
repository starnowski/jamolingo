package com.github.starnowski.jamolingo.core.operators.search

import com.github.starnowski.jamolingo.core.AbstractSpecification
import com.mongodb.MongoClientSettings
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.api.uri.queryoption.search.SearchExpression
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

class ODataSearchToMongoSearchParserTest extends AbstractSpecification {


    /**
     * Verifies that the generated MongoDB $match stage matches the expected BSON document.
     */
    @Unroll
    def "should return expected stage bson objects"(){
        given:
        System.out.println("Testing search: " + searchValue)
        Bson expectedBson = Document.parse(expectedBsonJson)
        Edm edm = loadEmdProvider("edm/edm6_filter_main.xml")
        JsonWriterSettings settings = JsonWriterSettings.builder().build()
        CodecRegistry registry = CodecRegistries.fromRegistries(
                CodecRegistries.fromProviders(new UuidCodecProvider(UuidRepresentation.STANDARD)),
                MongoClientSettings.getDefaultCodecRegistry()
        )
        DocumentCodec codec = new DocumentCodec(registry)

        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2",
                        "\$search=" +searchValue
                        , null, null)
        ODataSearchToMongoSearchParser tested = new ODataSearchToMongoSearchParser()

        when:
        def result = tested.parse(uriInfo.getSearchOption(), new SearchDocumentForQueryStringFactory() {
            @Override
            Bson build(SearchExpression searchExpression, SearchDocumentForQueryStringFactory.QueryStringParsingResult queryStringParsingResult) {
                return new Document().append("index", "default")
                        .append("queryString", new Document()
                                .append("path", Arrays.asList("name","description"))
                                .append("query", queryStringParsingResult.getQuery())
                        )
            }
        })

        then:
        [((Document)result.getStageObjects().get(0)).toJson(settings, codec)] == [expectedBson.toJson(settings, codec)]

        where:
            searchValue                     ||  expectedBsonJson
            """database AND search"""       || """{ "\$search": { "index": "default", "queryString": { "query": "database AND search", "path": ["name","description"] }}}"""
            """database OR search"""        || """{ "\$search": { "index": "default", "queryString": { "query": "database OR search", "path": ["name","description"] }}}"""
            """database NOT legacy"""       || """{ "\$search": { "index": "default", "queryString": { "query": "database NOT legacy", "path": ["name","description"] }}}"""
            """\"AND\""""                   || """{ "\$search": { "index": "default", "queryString": { "query": "\\"AND\\"", "path": ["name","description"] }}}"""
    }


}
