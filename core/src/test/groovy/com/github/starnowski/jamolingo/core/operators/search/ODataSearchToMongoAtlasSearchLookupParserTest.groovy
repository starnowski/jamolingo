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

class ODataSearchToMongoAtlasSearchLookupParserTest extends AbstractSpecification {

    @Unroll
    def "should generate expected #searchValue stage from ODataSearchToMongoAtlasSearchMetaParser"() {
        given:
        Bson expectedBson = Document.parse(expectedBsonJson)
        Edm edm = loadEmdProvider("edm/edm6_filter_main.xml")
        JsonWriterSettings settings = JsonWriterSettings.builder().build()
        CodecRegistry registry = CodecRegistries.fromRegistries(
                CodecRegistries.fromProviders(new UuidCodecProvider(UuidRepresentation.STANDARD)),
                MongoClientSettings.getDefaultCodecRegistry()
        )
        DocumentCodec codec = new DocumentCodec(registry)

        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", "\$search=" + searchValue, null, null)

        ODataSearchToMongoAtlasSearchMetaParser tested = new ODataSearchToMongoAtlasSearchMetaParser(
                new SearchDocumentForQueryStringFactory() {
                    @Override
                    Bson build(SearchExpression searchExpression, SearchDocumentForQueryStringFactory.QueryStringParsingResult queryStringParsingResult, ODataSearchToMongoAtlasSearchOptions options) {
                        return new Document("index", "your_search_index")
                                .append("count", new Document("type", "total"))
                                .append("compound", new Document()
                                        .append("filter", List.of(new Document("equals", new Document("path", "_id").append("value", "\$\$targetId"))))
                                        .append("must", List.of(new Document("queryString", new Document("query", queryStringParsingResult.getQuery()).append("defaultPath", "content"))))
                                )
                    }
                }
        )

        when:
        def result = tested.parse(uriInfo.getSearchOption())

        then:
        result.getStageObjects().size() == 1
        result.getSearchStages().size() == 1
        result.getScoreFilterStages().isEmpty()
        result.getUsedMongoDocumentProperties().isEmpty()
        result.getWrittenMongoDocumentProperties().isEmpty()
        result.getAddedMongoDocumentProperties().isEmpty()
        result.getRemovedMongoDocumentProperties().isEmpty()
        !result.isDocumentShapeRedefined()
        [((Document) result.getStageObjects().get(0)).toJson(settings, codec)] == [expectedBson.toJson(settings, codec)]

        where:
        searchValue                 || expectedBsonJson
        "database"                  || """{ "\$searchMeta": { "index": "your_search_index", "count": { "type": "total" }, "compound": { "filter": [{ "equals": { "path": "_id", "value": "\$\$targetId" } }], "must": [{ "queryString": { "query": "database", "defaultPath": "content" } }] } } }"""
        "database AND search"       || """{ "\$searchMeta": { "index": "your_search_index", "count": { "type": "total" }, "compound": { "filter": [{ "equals": { "path": "_id", "value": "\$\$targetId" } }], "must": [{ "queryString": { "query": "database AND search", "defaultPath": "content" } }] } } }"""
        "\"required keyword\""      || """{ "\$searchMeta": { "index": "your_search_index", "count": { "type": "total" }, "compound": { "filter": [{ "equals": { "path": "_id", "value": "\$\$targetId" } }], "must": [{ "queryString": { "query": "\\"required keyword\\"", "defaultPath": "content" } }] } } }"""
    }

    def "should generate exact two-stage lookup and match pipeline matching SEARCH_HELPER specification"() {
        given:
        Edm edm = loadEmdProvider("edm/edm6_filter_main.xml")
        JsonWriterSettings settings = JsonWriterSettings.builder().build()
        CodecRegistry registry = CodecRegistries.fromRegistries(
                CodecRegistries.fromProviders(new UuidCodecProvider(UuidRepresentation.STANDARD)),
                MongoClientSettings.getDefaultCodecRegistry()
        )
        DocumentCodec codec = new DocumentCodec(registry)

        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", "\$search=\"required keyword\"", null, null)

        SearchDocumentFactory factory = new SearchDocumentFactory() {
            @Override
            Bson build(SearchExpression searchExpression, ODataSearchToMongoAtlasSearchOptions options) {
                return new Document("index", "your_search_index")
                        .append("count", new Document("type", "total"))
                        .append("compound", new Document()
                                .append("filter", List.of(new Document("equals", new Document("path", "_id").append("value", "\$\$targetId"))))
                                .append("must", List.of(new Document("text", new Document("query", searchExpression.asSearchTerm().getSearchTerm()).append("path", "content"))))
                        )
            }
        }

        ODataSearchToMongoAtlasSearchLookupParser tested = new ODataSearchToMongoAtlasSearchLookupParser(
                "targetCollection",
                "metaResult",
                new ODataSearchToMongoAtlasSearchMetaParser(factory)
        )

        Bson expectedLookupStage = Document.parse("""
            {
              "\$lookup": {
                "from": "targetCollection",
                "localField": "_id",
                "foreignField": "_id",
                "let": { "targetId": "\$_id" },
                "pipeline": [
                  {
                    "\$searchMeta": {
                      "index": "your_search_index",
                      "count": { "type": "total" },
                      "compound": {
                        "filter": [{ "equals": { "path": "_id", "value": "\$\$targetId" } }],
                        "must": [{ "text": { "query": "required keyword", "path": "content" } }]
                      }
                    }
                  }
                ],
                "as": "metaResult"
              }
            }
        """)
        Bson expectedMatchStage = Document.parse("""
            {
              "\$match": {
                "metaResult.count.total": { "\$gt": 0 }
              }
            }
        """)

        when:
        def result = tested.parse(uriInfo.getSearchOption())

        then:
        result.getStageObjects().size() == 2
        result.getSearchStages().size() == 2
        result.getScoreFilterStages().isEmpty()
        [((Document) result.getStageObjects().get(0)).toJson(settings, codec)] == [expectedLookupStage.toJson(settings, codec)]
        [((Document) result.getStageObjects().get(1)).toJson(settings, codec)] == [expectedMatchStage.toJson(settings, codec)]
        result.getUsedMongoDocumentProperties() == ["_id"]
        result.getAddedMongoDocumentProperties() == ["metaResult"]
        result.getWrittenMongoDocumentProperties() == ["metaResult"]
        result.getRemovedMongoDocumentProperties().isEmpty()
        !result.isDocumentShapeRedefined()
    }

    @Unroll
    def "should support custom collectionName '#collectionName' and asPropertyName '#asPropertyName' (expected '#expectedAs')"() {
        given:
        Edm edm = loadEmdProvider("edm/edm6_filter_main.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", "\$search=laptop", null, null)

        SearchDocumentFactory factory = new SearchDocumentForQueryStringFactory() {
            @Override
            Bson build(SearchExpression searchExpression, SearchDocumentForQueryStringFactory.QueryStringParsingResult queryStringParsingResult, ODataSearchToMongoAtlasSearchOptions options) {
                return new Document("index", "idx")
                        .append("count", new Document("type", "total"))
                        .append("compound", new Document()
                                .append("filter", List.of(new Document("equals", new Document("path", "_id").append("value", "\$\$" + ODataSearchToMongoAtlasSearchLookupParser.TARGET_ID_VARIABLE_NAME))))
                                .append("must", List.of(new Document("queryString", new Document("query", queryStringParsingResult.getQuery()).append("defaultPath", "description"))))
                        )
            }
        }

        ODataSearchToMongoAtlasSearchLookupParser tested = ODataSearchToMongoAtlasSearchLookupParser.builder()
                .withCollectionName(collectionName)
                .withAsPropertyName(asPropertyName)
                .withSearchDocumentFactory(factory)
                .build()

        when:
        def result = tested.parse(uriInfo.getSearchOption(), DefaultODataSearchToMongoAtlasSearchOptions.builder().build())

        then:
        tested.getCollectionName() == collectionName
        tested.getAsPropertyName() == expectedAs
        tested.getSearchMetaParser() != null
        result.getStageObjects().size() == 2
        Document lookupDoc = ((Document) result.getStageObjects().get(0)).get("\$lookup", Document.class)
        lookupDoc.getString("from") == collectionName
        lookupDoc.getString("localField") == "_id"
        lookupDoc.getString("foreignField") == "_id"
        lookupDoc.get("let", Document.class) == new Document("targetId", "\$_id")
        lookupDoc.getString("as") == expectedAs
        Document matchDoc = ((Document) result.getStageObjects().get(1)).get("\$match", Document.class)
        matchDoc.get(expectedAs + ".count.total", Document.class) == new Document("\$gt", 0)
        result.getUsedMongoDocumentProperties() == ["_id"]
        result.getAddedMongoDocumentProperties() == [expectedAs]
        result.getWrittenMongoDocumentProperties() == [expectedAs]

        where:
        collectionName      | asPropertyName    || expectedAs
        "Items"             | "customMeta"      || "customMeta"
        "Orders"            | null              || "metaResult"
        "Products"          | ""                || "metaResult"
    }

    def "should support convenience constructors with default asPropertyName"() {
        given:
        Edm edm = loadEmdProvider("edm/edm6_filter_main.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", "\$search=test", null, null)
        SearchDocumentFactory factory = { SearchExpression exp, ODataSearchToMongoAtlasSearchOptions opts ->
            new Document("index", "default").append("count", new Document("type", "total"))
        } as SearchDocumentFactory

        when:
        def parserFromFactory = new ODataSearchToMongoAtlasSearchLookupParser("MyCollection", factory)
        def parserFromCustomAsAndFactory = new ODataSearchToMongoAtlasSearchLookupParser("MyCollection", "myMeta", factory)
        def parserFromMetaParser = new ODataSearchToMongoAtlasSearchLookupParser("MyCollection", new ODataSearchToMongoAtlasSearchMetaParser(factory))
        def builderWithMetaParser = ODataSearchToMongoAtlasSearchLookupParser.builder()
                .withCollectionName("MyCollection")
                .withSearchMetaParser(new ODataSearchToMongoAtlasSearchMetaParser(factory))
                .build()

        then:
        parserFromFactory.getCollectionName() == "MyCollection"
        parserFromFactory.getAsPropertyName() == ODataSearchToMongoAtlasSearchLookupParser.DEFAULT_AS_PROPERTY_NAME
        parserFromCustomAsAndFactory.getAsPropertyName() == "myMeta"
        parserFromMetaParser.getAsPropertyName() == ODataSearchToMongoAtlasSearchLookupParser.DEFAULT_AS_PROPERTY_NAME
        builderWithMetaParser.getAsPropertyName() == ODataSearchToMongoAtlasSearchLookupParser.DEFAULT_AS_PROPERTY_NAME
        parserFromFactory.parse(uriInfo.getSearchOption()).getStageObjects().size() == 2
        parserFromCustomAsAndFactory.parse(uriInfo.getSearchOption()).getStageObjects().size() == 2
        parserFromMetaParser.parse(uriInfo.getSearchOption()).getStageObjects().size() == 2
        builderWithMetaParser.parse(uriInfo.getSearchOption()).getStageObjects().size() == 2
    }
}
