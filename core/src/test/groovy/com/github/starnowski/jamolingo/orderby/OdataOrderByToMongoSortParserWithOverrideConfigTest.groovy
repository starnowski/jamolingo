package com.github.starnowski.jamolingo.orderby

import com.github.starnowski.jamolingo.AbstractSpecification
import com.github.starnowski.jamolingo.common.json.JSONOverrideHelper
import com.github.starnowski.jamolingo.context.DefaultEdmMongoContextFacade
import com.github.starnowski.jamolingo.context.EntityMapping
import com.github.starnowski.jamolingo.context.EntityPropertiesMongoPathContextBuilder
import com.github.starnowski.jamolingo.context.ODataMongoMappingFactory
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.uri.parser.Parser
import org.bson.conversions.Bson
import spock.lang.Unroll

class OdataOrderByToMongoSortParserWithOverrideConfigTest extends AbstractSpecification {

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
                    .parseUri("Items", "\$orderby=" + orderByClause, null, null)
            OdataOrderByToMongoSortParser tested = new OdataOrderByToMongoSortParser()

        when:
            def result = tested.parse(uriInfo.getOrderByOption(), new DefaultEdmMongoContextFacade(context, null))

        then:
            result.getStageObjects().get(0) == expectedBson

        where:
            [bsonFile, edmConfigFile, mergePayload, orderByClause] << testCases()
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

    static testCases() {
        [
                ["orderby/stages/case1_edm1_config_override.json", "edm/edm1.xml", EDM_1_MERGE_OVERRIDE_MONGO_NAME, "plainString asc"],
                ["orderby/stages/case1_edm1_config_override_relative.json", "edm/edm1.xml", EDM_1_MERGE_OVERRIDE_RELATIVE_PATH, "plainString desc"],
                ["orderby/stages/case2_edm2_config_override.json", "edm/edm2_with_nested_collections.xml", EDM_1_MERGE_OVERRIDE_MONGO_NAME, "plainString asc,Name desc"]
        ]
    }
}
