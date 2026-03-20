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

class ODataFilterToMongoMatchParserWithOverrideODataToBsonConverterTest extends AbstractSpecification {


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
            ODataToBsonConverter oDataToBsonConverter = new DefaultODataToBsonConverter() {
                @Override
                Object toBsonValue(Object value, String type) {
                    if ("Edm.DateTimeOffset".equals(type)) {
                        String stringValue = null
                        if (value instanceof String) {
                            stringValue = (String) value
                        } else if (value instanceof org.bson.BsonString) {
                            stringValue = ((org.bson.BsonString) value).getValue()
                        }

                        if (stringValue != null) {
                            java.time.Instant instant = java.time.Instant.parse(stringValue)
                            java.time.ZonedDateTime zonedDateTime = instant.atZone(java.time.ZoneOffset.UTC)
                            java.time.ZonedDateTime firstDayOfYear = zonedDateTime.withMonth(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
                            return java.util.Date.from(firstDayOfYear.toInstant())
                        }
                    }
                    return super.toBsonValue(value, type)
                }
            }
            MongoFilterVisitorCommonContext context = DefaultMongoFilterVisitorCommonContext.builder()
                    .withODataToBsonConverter(oDataToBsonConverter)
                    .build()

        when:
            def result = tested.parse(uriInfo.getFilterOption(), context)

        then:
            [((Document)result.getStageObjects().get(0)).toJson(settings, codec)] == [((Document)expectedBson).toJson(settings, codec)]

        where:
            [filter, bson] << oneToOneEdmPathsMappings()
    }

    @Unroll
    def "should return expected query object"(){
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
            ODataToBsonConverter oDataToBsonConverter = new DefaultODataToBsonConverter() {
                @Override
                Object toBsonValue(Object value, String type) {
                    if ("Edm.DateTimeOffset".equals(type)) {
                        String stringValue = null
                        if (value instanceof String) {
                            stringValue = (String) value
                        } else if (value instanceof org.bson.BsonString) {
                            stringValue = ((org.bson.BsonString) value).getValue()
                        }

                        if (stringValue != null) {
                            java.time.Instant instant = java.time.Instant.parse(stringValue)
                            java.time.ZonedDateTime zonedDateTime = instant.atZone(java.time.ZoneOffset.UTC)
                            java.time.ZonedDateTime firstDayOfYear = zonedDateTime.withMonth(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
                            return java.util.Date.from(firstDayOfYear.toInstant())
                        }
                    }
                    return super.toBsonValue(value, type)
                }
            }
            MongoFilterVisitorCommonContext context = DefaultMongoFilterVisitorCommonContext.builder()
                    .withODataToBsonConverter(oDataToBsonConverter)
                    .build()

        when:
            def result = tested.parseQueryObject(uriInfo.getFilterOption(), context)

        then:
        ((Document)result.getQueryObject()).toJson(settings, codec) == ((Document)expectedBson).get("\$match", Document.class).toJson(settings, codec)

        where:
            [filter, bson] << oneToOneEdmPathsMappings()
    }

    /**
     * Provides test data mapping OData filters to expected MongoDB $match documents.
     * In this provider, the mapping between EDM and MongoDB is one-to-one.
     */
    static oneToOneEdmPathsMappings() {
        [
                [ "timestamp ge 2024-07-20T10:00:00.00Z and timestamp le 2026-07-20T20:00:00.00Z", """{"\$match": {"\$and": [{"\$and": [{"timestamp": {"\$gte": {"\$date": "2024-01-01T00:00:00Z"}}}, {"timestamp": {"\$lte": {"\$date": "2026-01-01T00:00:00Z"}}}]}]}}"""]
        ]
    }
}

