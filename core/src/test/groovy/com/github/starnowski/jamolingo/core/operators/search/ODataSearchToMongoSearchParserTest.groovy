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
                                .append("query", queryStringParsingResult.getQuery())
                                .append("path", Arrays.asList("name","description"))
                                )
            }
        })

        then:
        [((Document)result.getStageObjects().get(0)).toJson(settings, codec)] == [expectedBson.toJson(settings, codec)]

        where:
            searchValue                                                 ||  expectedBsonJson
            """database AND search"""                                   || """{ "\$search": { "index": "default", "queryString": { "query": "database AND search", "path": ["name","description"] }}}"""
            """database OR search"""                                    || """{ "\$search": { "index": "default", "queryString": { "query": "database OR search", "path": ["name","description"] }}}"""
            """database NOT legacy"""                                   || """{ "\$search": { "index": "default", "queryString": { "query": "database NOT legacy", "path": ["name","description"] }}}"""
            """\"AND\""""                                               || """{ "\$search": { "index": "default", "queryString": { "query": "\\"AND\\"", "path": ["name","description"] }}}"""
            """\"OR\""""                                                || """{ "\$search": { "index": "default", "queryString": { "query": "\\"OR\\"", "path": ["name","description"] }}}"""
            """\"NOT\""""                                               || """{ "\$search": { "index": "default", "queryString": { "query": "\\"NOT\\"", "path": ["name","description"] }}}"""
            """\"AND operator\""""                                      || """{ "\$search": { "index": "default", "queryString": { "query": "\\"AND operator\\"", "path": ["name","description"] }}}"""
            """\"rock AND roll\""""                                     || """{ "\$search": { "index": "default", "queryString": { "query": "\\"rock AND roll\\"", "path": ["name","description"] }}}"""
            """"OR condition" AND database"""                           || """{ "\$search": { "index": "default", "queryString": { "query": "\\"OR condition\\" AND database", "path": ["name","description"] }}}"""
            """"NOT operator" OR logic"""                               || """{ "\$search": { "index": "default", "queryString": { "query": "\\"NOT operator\\" OR logic", "path": ["name","description"] }}}"""
            """(database OR search) AND index"""                        || """{ "\$search": { "index": "default", "queryString": { "query": "(database OR search) AND index", "path": ["name","description"] }}}"""
            """("AND" OR "OR") AND logic"""                             || """{ "\$search": { "index": "default", "queryString": { "query": "(\\"AND\\" OR \\"OR\\") AND logic", "path": ["name","description"] }}}"""
            """(database OR "AND") AND system"""                        || """{ "\$search": { "index": "default", "queryString": { "query": "(database OR \\"AND\\") AND system", "path": ["name","description"] }}}"""
            """\"database OR search\""""                                || """{ "\$search": { "index": "default", "queryString": { "query": "\\"database OR search\\"", "path": ["name","description"] }}}"""
            """\"AND OR NOT\""""                                        || """{ "\$search": { "index": "default", "queryString": { "query": "\\"AND OR NOT\\"", "path": ["name","description"] }}}"""
            """database AND ("OR" OR "NOT")"""                          || """{ "\$search": { "index": "default", "queryString": { "query": "database AND (\\"OR\\" OR \\"NOT\\")", "path": ["name","description"] }}}"""
            """\"logical AND operator\""""                              || """{ "\$search": { "index": "default", "queryString": { "query": "\\"logical AND operator\\"", "path": ["name","description"] }}}"""
            """("database search" OR "full text") AND engine"""         || """{ "\$search": { "index": "default", "queryString": { "query": "(\\"database search\\" OR \\"full text\\") AND engine", "path": ["name","description"] }}}"""
            """\"\\"AND\\" keyword\""""                                 || """{ "\$search": { "index": "default", "queryString": { "query": "\\"\\\\\\"AND\\\\\\" keyword\\"", "path": ["name","description"] }}}"""
            """(database AND search) OR ("AND operator" AND logic)"""   || """{ "\$search": { "index": "default", "queryString": { "query": "(database AND search) OR (\\"AND operator\\" AND logic)", "path": ["name","description"] }}}"""
    }


}
