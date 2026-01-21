package com.github.starnowski.jamolingo.select

import com.github.starnowski.jamolingo.AbstractSpecification
import com.github.starnowski.jamolingo.common.json.JSONOverrideHelper
import com.github.starnowski.jamolingo.core.api.DefaultEdmMongoContextFacade
import com.github.starnowski.jamolingo.context.EntityMapping
import com.github.starnowski.jamolingo.context.EntityPropertiesMongoPathContextBuilder
import com.github.starnowski.jamolingo.context.ODataMongoMappingFactory
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.uri.parser.Parser
import org.bson.conversions.Bson
import spock.lang.Unroll

import java.util.stream.Collectors

class OdataSelectToMongoProjectParserWithOverrideConfigTest extends AbstractSpecification {

    @Unroll
    def "should return expected stage bson object with default EdmMongoContextFacade with override edm to mongo mapping"() {
        given:
            Bson expectedBson = loadBsonFromFile(bsonFile)
            Edm edm = loadEmdProvider(edmConfigFile)
            ODataMongoMappingFactory factory = new ODataMongoMappingFactory()
            def odataMapping = factory.build(edm.getSchema("Demo"))
            def entityMapping = odataMapping.getEntities().get("Item")
            EntityPropertiesMongoPathContextBuilder entityPropertiesMongoPathContextBuilder = new EntityPropertiesMongoPathContextBuilder()
            def helper = new JSONOverrideHelper()
            entityMapping = helper.applyChangesToJson(entityMapping, mergePayload as String, EntityMapping.class, JSONOverrideHelper.PatchType.MERGE)
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
            [bsonFile, edmConfigFile, mergePayload, selectFields] << oneToOneEdmPathsMappings()
    }

    static final String EDM_1_MERGE_OVERRIDE_MONGO_NAME = """
        {
            "properties": {
                "plainString": {
                    "mongoName": "thisIsString"
                }
            }
        }
    """
    static final String EDM_1_MERGE_OVERRIDE_RELATIVE_PATH = """
        {
            "properties": {
                "plainString": {
                    "relativeTo": "child1",
                    "mongoName": "someString"
                }
            }
        }
    """

    static final String EDM_2_MERGE_OVERRIDE_NESTED_PROP = """
        {
            "properties": {
                "Addresses": {
                    "properties": {
                        "City": {
                            "mongoName": "town"
                        }
                    }
                }
            }
        }
    """

    static final String EDM_3_MERGE_OVERRIDE_NESTED_PROP = """
        {
            "properties": {
                "Addresses": {
                    "properties": {
                        "BackUpAddresses": {
                            "mongoName": "previousAddresses"
                        }
                    }
                }
            }
        }
    """

        static oneToOneEdmPathsMappings() {
        [
                ["select/stages/case1_edm1_config_override.json", "edm/edm1.xml", EDM_1_MERGE_OVERRIDE_MONGO_NAME, ["plainString"]],
                ["select/stages/case1_edm1_config_override_relative.json", "edm/edm1.xml", EDM_1_MERGE_OVERRIDE_RELATIVE_PATH, ["plainString"]],
                ["select/stages/case2_edm2_config_override.json", "edm/edm2_with_nested_collections.xml", EDM_2_MERGE_OVERRIDE_NESTED_PROP, ["plainString", "Name", "Addresses/City", "Addresses/Street"]],
                ["select/stages/case3_edm3_config_override.json", "edm/edm3_complextype_with_circular_reference_collection.xml", EDM_3_MERGE_OVERRIDE_NESTED_PROP, ["Addresses/BackUpAddresses/ZipCode"]],
                ["select/stages/case3_edm3_config_override_nested_and_simple.json", "edm/edm3_complextype_with_circular_reference_collection.xml", EDM_3_MERGE_OVERRIDE_NESTED_PROP, ["plainString", "Addresses/BackUpAddresses/ZipCode"]]
        ]
    }



    // TODO ExpandAsterisk = true (all fields defined in EDM)
}
