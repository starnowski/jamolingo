package com.github.starnowski.jamolingo.core.operators.select

import com.github.starnowski.jamolingo.core.AbstractSpecification
import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade
import com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContextBuilder
import com.github.starnowski.jamolingo.core.mapping.ODataMongoMappingFactory
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.uri.parser.Parser
import org.bson.conversions.Bson
import spock.lang.Unroll

import java.util.stream.Collectors

class OdataSelectToMongoProjectParserTest extends AbstractSpecification {


    @Unroll
    def "should return expected stage bson object"(){
        given:
            Bson expectedBson = loadBsonFromFile(bsonFile)
            Edm edm = loadEmdProvider(edmConfigFile)

            UriInfo uriInfo = new Parser(edm, OData.newInstance())
                    .parseUri("Items",
                            "\$select=" +
                                    selectFields.stream().filter(Objects::nonNull)
                                            .filter(s -> !s.trim().isEmpty())
                                            .collect(Collectors.joining(","))
                            , null, null)
        OdataSelectToMongoProjectParser tested = new OdataSelectToMongoProjectParser()

        when:
            def result = tested.parse(uriInfo.getSelectOption())

        then:
            result.getStageObject() == expectedBson

        where:
            [bsonFile, edmConfigFile, selectFields] << oneToOneEdmPathsMappings()
    }

    @Unroll
    def "should return expected stage bson object with default EdmMongoContextFacade with 1-to-1 edm to mongo mapping"() {
        given:
        Bson expectedBson = loadBsonFromFile(bsonFile)
        Edm edm = loadEmdProvider(edmConfigFile)
        ODataMongoMappingFactory factory = new ODataMongoMappingFactory()
        def odataMapping = factory.build(edm.getSchema("Demo"))
        def entityMapping = odataMapping.getEntities().get("Item")
        EntityPropertiesMongoPathContextBuilder entityPropertiesMongoPathContextBuilder = new EntityPropertiesMongoPathContextBuilder()
        def context = entityPropertiesMongoPathContextBuilder.build(entityMapping)


        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("Items",
                        "\$select=" +
                                selectFields.stream().filter(Objects::nonNull)
                                        .filter(s -> !s.trim().isEmpty())
                                        .collect(Collectors.joining(","))
                        , null, null)
        OdataSelectToMongoProjectParser tested = new OdataSelectToMongoProjectParser()

        when:
        def result = tested.parse(uriInfo.getSelectOption(), new DefaultEdmMongoContextFacade(context, null))

        then:
        result.getStageObject() == expectedBson

        where:
        [bsonFile, edmConfigFile, selectFields] << oneToOneEdmPathsMappings()
    }

        static oneToOneEdmPathsMappings() {
        [
                ["select/stages/case1.json"       ,  "edm/edm1.xml"  , ["plainString"]],
                ["select/stages/case_wildcard_without_id.json"       ,  "edm/edm1.xml"  , ["*"]],// ExpandAsterisk = false
                ["select/stages/case2.json"       ,  "edm/edm2_with_nested_collections.xml"  , ["plainString", "Name", "Addresses/Street", "Addresses/ZipCode"]], // ExpandAsterisk = false
                ["select/stages/case2_with_whole_nested_object.json"       ,  "edm/edm2_with_nested_collections.xml"  , ["plainString", "Name", "Addresses"]], // ExpandAsterisk = false
                ["select/stages/case3_with_nested_complexType_circular_reference.json"       ,  "edm/edm2_complextype_with_circular_reference.xml"  , ["plainString", "Name", "Addresses/Street", "Addresses/ZipCode", "Addresses/BackUpAddresses/ZipCode"]], // ExpandAsterisk = false
                ["select/stages/case3_with_nested_complexType_circular_reference.json"       ,  "edm/edm3_complextype_with_circular_reference_collection.xml"  , ["plainString", "Name", "Addresses/Street", "Addresses/ZipCode", "Addresses/BackUpAddresses/ZipCode"]] // ExpandAsterisk = false
        ]
    }



    // TODO ExpandAsterisk = true (all fields defined in EDM)

    @Unroll
    def "should return expected wildCard flag"() {
        given:
        Edm edm = loadEmdProvider(edmConfigFile)

        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("Items",
                        "\$select=" +
                                selectFields.stream().filter(Objects::nonNull)
                                        .filter(s -> !s.trim().isEmpty())
                                        .collect(Collectors.joining(","))
                        , null, null)
        OdataSelectToMongoProjectParser tested = new OdataSelectToMongoProjectParser()

        when:
        def result = tested.parse(uriInfo.getSelectOption())

        then:
        result.isWildCard() == expectedWildCard

        where:
        [edmConfigFile, selectFields, expectedWildCard] << wildCardTestCases()
    }

    static wildCardTestCases() {
        [
                ["edm/edm1.xml"  , ["*"], true],
                ["edm/edm1.xml"  , ["plainString"], false]
        ]
    }

    @Unroll
    def "should return expected selected fields"() {
        given:
        Edm edm = loadEmdProvider(edmConfigFile)

        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("Items",
                        "\$select=" +
                                selectFields.stream().filter(Objects::nonNull)
                                        .filter(s -> !s.trim().isEmpty())
                                        .collect(Collectors.joining(","))
                        , null, null)
        OdataSelectToMongoProjectParser tested = new OdataSelectToMongoProjectParser()

        when:
        def result = tested.parse(uriInfo.getSelectOption())

        then:
        result.getSelectedFields() == expectedSelectedFields as Set

        where:
        [edmConfigFile, selectFields, expectedSelectedFields] << selectedFieldsTestCases()
    }

    static selectedFieldsTestCases() {
        [
                ["edm/edm1.xml"  , ["plainString"], ["plainString"]],
                ["edm/edm1.xml"  , ["*"], []],
                ["edm/edm2_with_nested_collections.xml"  , ["plainString", "Name", "Addresses/Street", "Addresses/ZipCode"], ["plainString", "Name", "Addresses.Street", "Addresses.ZipCode"]],
                ["edm/edm2_with_nested_collections.xml"  , ["plainString", "Name", "Addresses"], ["plainString", "Name", "Addresses"]]
        ]
    }
}
