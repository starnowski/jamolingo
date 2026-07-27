package com.github.starnowski.jamolingo.core.operators.expand

import com.github.starnowski.jamolingo.core.AbstractSpecification
import com.github.starnowski.jamolingo.core.operators.expand.ExpandParserContext
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.uri.parser.Parser
import org.bson.Document
import spock.lang.Unroll

class ODataExpandToMongoAggregationPipelineParserTest extends AbstractSpecification {

    def "should default isUseLookupForLevelGreaterThanOne to false in ExpandParserContext interface"() {
        given:
        ExpandParserContext context = new ExpandParserContext() {
            @Override
            Map getEDMTypeMapping() { return null }

            @Override
            Map getEDMTablesToMongoDBCollections() { return null }

            @Override
            Integer getMaxLevel() { return null }
        }

        expect:
        !context.isUseLookupForLevelGreaterThanOne()
    }

    def "should construct DefaultExpandParserContext and correctly set isUseLookupForLevelGreaterThanOne"() {
        when:
        def context = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(true)
                .build()

        then:
        context.isUseLookupForLevelGreaterThanOne()

        when:
        def context2 = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(false)
                .build()

        then:
        !context2.isUseLookupForLevelGreaterThanOne()
    }

    def "should correctly copy properties including isUseLookupForLevelGreaterThanOne in builder"() {
        given:
        def context = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(true)
                .build()

        when:
        def contextCopy = DefaultExpandParserContext.builder()
                .withDefaultExpandParserContext(context)
                .build()

        then:
        contextCopy.isUseLookupForLevelGreaterThanOne()
    }

    def "should correctly implement equals, hashCode, and toString in DefaultExpandParserContext"() {
        given:
        def context1 = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(true)
                .withMaxLevel(10)
                .build()
        def context2 = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(true)
                .withMaxLevel(10)
                .build()
        def context3 = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(false)
                .withMaxLevel(10)
                .build()

        expect:
        context1 == context2
        context1 != context3
        context1.hashCode() == context2.hashCode()
        context1.hashCode() != context3.hashCode()
        context1.toString().contains("useLookupForLevelGreaterThanOne=true")
        context3.toString().contains("useLookupForLevelGreaterThanOne=false")
    }

    def 'should parse expand with $levels > 1 using $graphLookup when isUseLookupForLevelGreaterThanOne is false'() {
        given:
        Edm edm = loadEmdProvider("edm/edm_expand.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", "\$expand=children(\$levels=2)", null, null)

        def context = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(false)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        def result = parser.parse(uriInfo.getExpandOption(), context)

        then:
        // By default, it generates $graphLookup
        result.getStageObjects().any { stage ->
            stage instanceof Document && stage.containsKey("\$graphLookup")
        }
        !result.getStageObjects().any { stage ->
            stage instanceof Document && stage.containsKey("\$lookup")
        }
    }

    def 'should parse expand with $levels > 1 using $lookup when isUseLookupForLevelGreaterThanOne is true'() {
        given:
        Edm edm = loadEmdProvider("edm/edm_expand.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", "\$expand=children(\$levels=2)", null, null)

        def context = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(true)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        def result = parser.parse(uriInfo.getExpandOption(), context)

        then:
        // Since isUseLookupForLevelGreaterThanOne is true, it should generate $lookup instead of $graphLookup
        result.getStageObjects().any { stage ->
            stage instanceof Document && stage.containsKey("\$lookup")
        }
        !result.getStageObjects().any { stage ->
            stage instanceof Document && stage.containsKey("\$graphLookup")
        }
    }

    @Unroll
    def 'should correctly populate getExpandElements for complex expand #expandQuery'() {
        given:
        Edm edm = loadEmdProvider(edmFilePath)
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri(entitySetName, expandQuery, null, null)

        def context = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(lookUpUserForGraph)
                .withMaxLevel(maxLevel)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        def result = parser.parse(uriInfo.getExpandOption(), context)
        def expandElements = result.getExpandElements()

        then:
        verifyExpandElements(expandElements, expectedResult)

        where:
        expandQuery | edmFilePath | entitySetName | lookUpUserForGraph | maxLevel || expectedResult
        // Example 1
        "\$expand=parent(\$levels=2;\$expand=children(\$levels=2))" | "edm/edm_expand.xml" | "examples2" | true | 5 || [
            "parent": [
                edmPath: "parent", mongoPath: "parent", fetchType: "LOOKUP", level: 2, maxLevelRequest: false, localKeyProperty: "parentId", foreignKeyProperty: "_id",
                expandElements: [
                    "children": [
                        edmPath: "children", mongoPath: "children", fetchType: "LOOKUP", level: 2, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "parentId", expandElements: [:]
                    ]
                ]
            ]
        ]
        // Example 2
        "\$expand=parent(\$levels=2;\$expand=children(\$levels=2))" | "edm/edm_expand.xml" | "examples2" | false | 5 || [
            "parent": [
                edmPath: "parent", mongoPath: "parent", fetchType: "GRAPHLOOKUP", level: 2, maxLevelRequest: false, localKeyProperty: "parentId", foreignKeyProperty: "_id",
                expandElements: [
                    "children": [
                        edmPath: "children", mongoPath: "children", fetchType: "GRAPHLOOKUP", level: 2, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "parentId", expandElements: [:]
                    ]
                ]
            ]
        ]
        // Example 3
        "\$expand=children(\$levels=max;\$expand=treeType2s(\$expand=children(\$levels=max)))" | "edm/edm_tree.xml" | "treeType1s" | true | 5 || [
            "children": [
                edmPath: "children", mongoPath: "children", fetchType: "LOOKUP", level: 5, maxLevelRequest: true, localKeyProperty: "_id", foreignKeyProperty: "parentId",
                expandElements: [
                    "treeType2s": [
                        edmPath: "treeType2s", mongoPath: "treeType2s", fetchType: "LOOKUP", level: 1, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "treeType1Id",
                        expandElements: [
                            "children": [
                                edmPath: "children", mongoPath: "children", fetchType: "LOOKUP", level: 5, maxLevelRequest: true, localKeyProperty: "_id", foreignKeyProperty: "parentId", expandElements: [:]
                            ]
                        ]
                    ]
                ]
            ]
        ]
        // Example 4
        "\$expand=category,children,treeType2s" | "edm/edm_tree.xml" | "treeType1s" | true | 5 || [
            "category": [
                edmPath: "category", mongoPath: "category", fetchType: "LOOKUP", level: 1, maxLevelRequest: false, localKeyProperty: "categoryId", foreignKeyProperty: "_id", expandElements: [:]
            ],
            "children": [
                edmPath: "children", mongoPath: "children", fetchType: "LOOKUP", level: 1, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "parentId", expandElements: [:]
            ],
            "treeType2s": [
                edmPath: "treeType2s", mongoPath: "treeType2s", fetchType: "LOOKUP", level: 1, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "treeType1Id", expandElements: [:]
            ]
        ]
        // Example 5
        "\$expand=parent(\$levels=2;\$expand=children(\$levels=2),treeType2s)" | "edm/edm_tree.xml" | "treeType1s" | true | 5 || [
            "parent": [
                edmPath: "parent", mongoPath: "parent", fetchType: "LOOKUP", level: 2, maxLevelRequest: false, localKeyProperty: "parentId", foreignKeyProperty: "_id",
                expandElements: [
                    "children": [
                        edmPath: "children", mongoPath: "children", fetchType: "LOOKUP", level: 2, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "parentId", expandElements: [:]
                    ],
                    "treeType2s": [
                        edmPath: "treeType2s", mongoPath: "treeType2s", fetchType: "LOOKUP", level: 1, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "treeType1Id", expandElements: [:]
                    ]
                ]
            ]
        ]
        // Example 6
        "\$expand=parent(\$levels=2;\$expand=children(\$levels=2),treeType2s)" | "edm/edm_tree.xml" | "treeType1s" | false | 5 || [
                "parent": [
                        edmPath: "parent", mongoPath: "parent", fetchType: "GRAPHLOOKUP", level: 2, maxLevelRequest: false, localKeyProperty: "parentId", foreignKeyProperty: "_id",
                        expandElements: [
                                "children": [
                                        edmPath: "children", mongoPath: "children", fetchType: "GRAPHLOOKUP", level: 2, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "parentId", expandElements: [:]
                                ],
                                "treeType2s": [
                                        edmPath: "treeType2s", mongoPath: "treeType2s", fetchType: "LOOKUP", level: 1, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "treeType1Id", expandElements: [:]
                                ]
                        ]
                ]
        ]
    }

    private void verifyExpandElements(Map<String, ExpandElement> actual, Map<String, Object> expected) {
        if (expected == null || expected.isEmpty()) {
            assert actual == null || actual.isEmpty()
            return
        }
        assert actual != null
        assert actual.size() == expected.size()
        expected.each { key, expectedElement ->
            def actualElement = actual.get(key)
            assert actualElement != null
            assert actualElement.getEdmPath() == expectedElement.edmPath
            assert actualElement.getMongoPath() == expectedElement.mongoPath
            assert actualElement.getFetchType().name() == expectedElement.fetchType
            assert actualElement.getLevel() == expectedElement.level
            assert actualElement.getMaxLevelRequest() == expectedElement.maxLevelRequest
            assert actualElement.getLocalKeyProperty() == expectedElement.localKeyProperty
            assert actualElement.getForeignKeyProperty() == expectedElement.foreignKeyProperty
            verifyExpandElements(actualElement.getExpandElements(), expectedElement.expandElements as Map<String, Object>)
        }
    }
}
