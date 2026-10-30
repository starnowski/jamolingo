package com.github.starnowski.jamolingo.core.operators.expand

import com.github.starnowski.jamolingo.common.beans.KeyValue
import com.github.starnowski.jamolingo.core.AbstractSpecification
import com.github.starnowski.jamolingo.core.operators.expand.ExpandParserContext
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser
import com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchOptions
import com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchParser
import com.github.starnowski.jamolingo.core.operators.search.SearchDocumentForQueryStringFactory
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.api.uri.queryoption.search.SearchExpression
import org.apache.olingo.server.core.uri.parser.Parser
import org.bson.Document
import org.bson.conversions.Bson
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
    def 'should throw ExpandLevelExceededException when requested level #requestedLevel is higher than max level 2 for query #expandQuery'() {
        given:
        Edm edm = loadEmdProvider("edm/edm_expand.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", expandQuery, null, null)

        def context = DefaultExpandParserContext.builder()
                .withMaxLevel(2)
                .withThrowExceptionOnExpandLevelsExceeded(true)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        parser.parse(uriInfo.getExpandOption(), context)

        then:
        def e = thrown(ExpandLevelExceededException)
        e.getEdmPath() == expectedEdmPath
        e.getRequestedLevel() == requestedLevel
        e.getMaxLevel() == 2

        where:
        expandQuery | requestedLevel | expectedEdmPath
        "\$expand=children(\$levels=3)" | 3 | "children"
        "\$expand=children(\$levels=5)" | 5 | "children"
        "\$expand=parent(\$levels=4)" | 4 | "parent"
        "\$expand=children(\$expand=parent(\$levels=3))" | 3 | "children.parent"
        "\$expand=children(\$levels=1;\$expand=children(\$levels=1;\$expand=children(\$levels=1)))" | 3 | "children"
    }

    @Unroll
    def 'should NOT throw ExpandLevelExceededException when requested level is lower or equal to max level 2 for query #expandQuery'() {
        given:
        Edm edm = loadEmdProvider("edm/edm_expand.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", expandQuery, null, null)

        def context = DefaultExpandParserContext.builder()
                .withMaxLevel(2)
                .withThrowExceptionOnExpandLevelsExceeded(true)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        parser.parse(uriInfo.getExpandOption(), context)

        then:
        noExceptionThrown()

        where:
        expandQuery | _
        "\$expand=children(\$levels=1;\$expand=children(\$levels=1))" | _
        "\$expand=children(\$levels=2)" | _
    }

    @Unroll
    def 'should throw ExpandLevelExceededException when requested level #requestedLevel is higher than max level 2 with useLookupForLevelGreaterThanOne true for query #expandQuery'() {
        given:
        Edm edm = loadEmdProvider("edm/edm_expand.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", expandQuery, null, null)

        def context = DefaultExpandParserContext.builder()
                .withMaxLevel(2)
                .withUseLookupForLevelGreaterThanOne(true)
                .withThrowExceptionOnExpandLevelsExceeded(true)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        parser.parse(uriInfo.getExpandOption(), context)

        then:
        def e = thrown(ExpandLevelExceededException)
        e.getEdmPath() == expectedEdmPath
        e.getRequestedLevel() == requestedLevel
        e.getMaxLevel() == 2

        where:
        expandQuery | requestedLevel | expectedEdmPath
        "\$expand=children(\$levels=3)" | 3 | "children"
        "\$expand=children(\$levels=5)" | 5 | "children"
        "\$expand=parent(\$levels=4)" | 4 | "parent"
        "\$expand=children(\$expand=parent(\$levels=3))" | 3 | "children.parent"
        "\$expand=children(\$levels=1;\$expand=children(\$levels=1;\$expand=children(\$levels=1)))" | 3 | "children"
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
                edmPath: "parent", mongoPath: "parent", fetchType: "LOOKUP", level: 2, maxLevelRequest: false, localKeyProperty: "parentId", foreignKeyProperty: "_id", foreignCollection: "MyService.Example2", edmEntityFullName: "MyService.Example2", collection: false, depthVariableName: null,
                expandElements: [
                    "children": [
                        edmPath: "children", mongoPath: "children", fetchType: "LOOKUP", level: 2, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "parentId", foreignCollection: "MyService.Example2", edmEntityFullName: "MyService.Example2", collection: true, depthVariableName: null, expandElements: [:]
                    ]
                ]
            ]
        ]
        // Example 2
        "\$expand=parent(\$levels=2;\$expand=children(\$levels=2))" | "edm/edm_expand.xml" | "examples2" | false | 5 || [
            "parent": [
                edmPath: "parent", mongoPath: "parent", fetchType: "GRAPHLOOKUP", level: 2, maxLevelRequest: false, localKeyProperty: "parentId", foreignKeyProperty: "_id", foreignCollection: "MyService.Example2", edmEntityFullName: "MyService.Example2", collection: false, depthVariableName: "parent_odata_graphlookup_depth_variable",
                expandElements: [
                    "children": [
                        edmPath: "children", mongoPath: "children", fetchType: "GRAPHLOOKUP", level: 2, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "parentId", foreignCollection: "MyService.Example2", edmEntityFullName: "MyService.Example2", collection: true, depthVariableName: "children_odata_graphlookup_depth_variable", expandElements: [:]
                    ]
                ]
            ]
        ]
        // Example 3
        "\$expand=children(\$levels=max;\$expand=treeType2s(\$expand=children(\$levels=max)))" | "edm/edm_tree.xml" | "treeType1s" | true | 5 || [
            "children": [
                edmPath: "children", mongoPath: "children", fetchType: "LOOKUP", level: 5, maxLevelRequest: true, localKeyProperty: "_id", foreignKeyProperty: "parentId", foreignCollection: "MyService.TreeType1", edmEntityFullName: "MyService.TreeType1", collection: true, depthVariableName: null,
                expandElements: [
                    "treeType2s": [
                        edmPath: "treeType2s", mongoPath: "treeType2s", fetchType: "LOOKUP", level: 1, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "treeType1Id", foreignCollection: "MyService.TreeType2", edmEntityFullName: "MyService.TreeType2", collection: true, depthVariableName: null,
                        expandElements: [
                            "children": [
                                edmPath: "children", mongoPath: "children", fetchType: "LOOKUP", level: 5, maxLevelRequest: true, localKeyProperty: "_id", foreignKeyProperty: "parentId", foreignCollection: "MyService.TreeType2", edmEntityFullName: "MyService.TreeType2", collection: true, depthVariableName: null, expandElements: [:]
                            ]
                        ]
                    ]
                ]
            ]
        ]
        // Example 4
        "\$expand=category,children,treeType2s" | "edm/edm_tree.xml" | "treeType1s" | true | 5 || [
            "category": [
                edmPath: "category", mongoPath: "category", fetchType: "LOOKUP", level: 1, maxLevelRequest: false, localKeyProperty: "categoryId", foreignKeyProperty: "_id", foreignCollection: "MyService.Category", edmEntityFullName: "MyService.Category", collection: false, depthVariableName: null, expandElements: [:]
            ],
            "children": [
                edmPath: "children", mongoPath: "children", fetchType: "LOOKUP", level: 1, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "parentId", foreignCollection: "MyService.TreeType1", edmEntityFullName: "MyService.TreeType1", collection: true, depthVariableName: null, expandElements: [:]
            ],
            "treeType2s": [
                edmPath: "treeType2s", mongoPath: "treeType2s", fetchType: "LOOKUP", level: 1, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "treeType1Id", foreignCollection: "MyService.TreeType2", edmEntityFullName: "MyService.TreeType2", collection: true, depthVariableName: null, expandElements: [:]
            ]
        ]
        // Example 5
        "\$expand=parent(\$levels=2;\$expand=children(\$levels=2),treeType2s)" | "edm/edm_tree.xml" | "treeType1s" | true | 5 || [
            "parent": [
                edmPath: "parent", mongoPath: "parent", fetchType: "LOOKUP", level: 2, maxLevelRequest: false, localKeyProperty: "parentId", foreignKeyProperty: "_id", foreignCollection: "MyService.TreeType1", edmEntityFullName: "MyService.TreeType1", collection: false, depthVariableName: null,
                expandElements: [
                    "children": [
                        edmPath: "children", mongoPath: "children", fetchType: "LOOKUP", level: 2, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "parentId", foreignCollection: "MyService.TreeType1", edmEntityFullName: "MyService.TreeType1", collection: true, depthVariableName: null, expandElements: [:]
                    ],
                    "treeType2s": [
                        edmPath: "treeType2s", mongoPath: "treeType2s", fetchType: "LOOKUP", level: 1, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "treeType1Id", foreignCollection: "MyService.TreeType2", edmEntityFullName: "MyService.TreeType2", collection: true, depthVariableName: null, expandElements: [:]
                    ]
                ]
            ]
        ]
        // Example 6
        "\$expand=parent(\$levels=2;\$expand=children(\$levels=2),treeType2s)" | "edm/edm_tree.xml" | "treeType1s" | false | 5 || [
                "parent": [
                        edmPath: "parent", mongoPath: "parent", fetchType: "GRAPHLOOKUP", level: 2, maxLevelRequest: false, localKeyProperty: "parentId", foreignKeyProperty: "_id", foreignCollection: "MyService.TreeType1", edmEntityFullName: "MyService.TreeType1", collection: false, depthVariableName: "parent_odata_graphlookup_depth_variable",
                        expandElements: [
                                "children": [
                                        edmPath: "children", mongoPath: "children", fetchType: "GRAPHLOOKUP", level: 2, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "parentId", foreignCollection: "MyService.TreeType1", edmEntityFullName: "MyService.TreeType1", collection: true, depthVariableName: "children_odata_graphlookup_depth_variable", expandElements: [:]
                                ],
                                "treeType2s": [
                                        edmPath: "treeType2s", mongoPath: "treeType2s", fetchType: "LOOKUP", level: 1, maxLevelRequest: false, localKeyProperty: "_id", foreignKeyProperty: "treeType1Id", foreignCollection: "MyService.TreeType2", edmEntityFullName: "MyService.TreeType2", collection: true, depthVariableName: null, expandElements: [:]
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
            assert actualElement.getForeignCollection() == expectedElement.foreignCollection
            assert actualElement.getEdmEntityFullName() == expectedElement.edmEntityFullName
            assert actualElement.getCollection() == expectedElement.collection
            assert actualElement.getDepthVariableName() == expectedElement.depthVariableName
            verifyExpandElements(actualElement.getExpandElements(), expectedElement.expandElements as Map<String, Object>)
        }
    }

    def "should return default values for search parser methods in ExpandParserContext interface"() {
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
        context.getGlobalSearchParser() == null
        context.getNavigationPropertySearchParsers() == [:]
        context.getEdmTablesToSearchParsers() == [:]
    }

    def "should configure and copy search parser properties in DefaultExpandParserContext and verify equals, hashCode, and toString"() {
        given:
        def globalParser = createSearchParser("global_idx", ["name"])
        def navParser = createSearchParser("nav_idx", ["title"])
        def typeParser = createSearchParser("type_idx", ["desc"])
        def navMap = ["children": navParser]
        def typeMap = [(new KeyValue<>("MyService", "Example2")): typeParser]

        when:
        def context1 = DefaultExpandParserContext.builder()
                .withGlobalSearchParser(globalParser)
                .withNavigationPropertySearchParsers(navMap)
                .withEdmTablesToSearchParsers(typeMap)
                .build()
        def contextCopy = DefaultExpandParserContext.builder()
                .withDefaultExpandParserContext(context1)
                .build()
        def contextDifferent = DefaultExpandParserContext.builder()
                .withGlobalSearchParser(navParser)
                .build()

        then:
        context1.getGlobalSearchParser().is(globalParser)
        context1.getNavigationPropertySearchParsers() == navMap
        context1.getEdmTablesToSearchParsers() == typeMap
        contextCopy == context1
        contextCopy.hashCode() == context1.hashCode()
        contextCopy != contextDifferent
        contextCopy.hashCode() != contextDifferent.hashCode()
        context1.toString().contains("globalSearchParser=")
        context1.toString().contains("navigationPropertySearchParsers=")
        context1.toString().contains("edmTablesToSearchParsers=")
    }

    def 'should parse $expand with $search using global ODataSearchToMongoAtlasSearchParser'() {
        given:
        Edm edm = loadEmdProvider("edm/edm_expand.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("examples2", "\$expand=children(\$search=Vehicle)", null, null)

        def globalParser = createSearchParser("global_idx", ["name"])
        def context = DefaultExpandParserContext.builder()
                .withGlobalSearchParser(globalParser)
                .build()
        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        def result = parser.parse(uriInfo.getExpandOption(), context)

        then:
        result.getStageObjects().size() == 1
        Document lookup = (Document) ((Document) result.getStageObjects().get(0)).get("\$lookup")
        List<Document> pipeline = lookup.getList("pipeline", Document.class)
        pipeline.size() == 1
        pipeline.get(0) == Document.parse("""{ "\$search": { "index": "global_idx", "queryString": { "query": "Vehicle", "path": ["name"] } } }""")
    }

    def 'should resolve ODataSearchToMongoAtlasSearchParser according to priority: navigation property > entity type > global'() {
        given:
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("treeType1s", "\$expand=children(\$search=TermOne),treeType2s(\$search=TermTwo;\$expand=treeType3s(\$search=TermThree))", null, null)

        def globalParser = createSearchParser("global_idx", ["global_field"])
        def typeParser = createSearchParser("type_idx", ["type_field"])
        def navParser = createSearchParser("nav_idx", ["nav_field"])

        def context = DefaultExpandParserContext.builder()
                .withGlobalSearchParser(globalParser)
                .withEdmTablesToSearchParsers([
                        (new KeyValue<>("MyService", "TreeType1")): typeParser,
                        (new KeyValue<>("MyService", "TreeType2")): typeParser
                ])
                .withNavigationPropertySearchParsers([
                        "children": navParser
                ])
                .build()
        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        def result = parser.parse(uriInfo.getExpandOption(), context)

        then:
        // 1. "children" matches navigation property map (highest priority -> nav_idx, overriding MyService.TreeType1 and global)
        Document childrenLookup = (Document) ((Document) result.getStageObjects().get(0)).get("\$lookup")
        childrenLookup.getList("pipeline", Document.class).get(0) ==
                Document.parse("""{ "\$search": { "index": "nav_idx", "queryString": { "query": "TermOne", "path": ["nav_field"] } } }""")

        // 2. "treeType2s" has no nav-prop entry, matches entity type MyService.TreeType2 (second priority -> type_idx, overriding global)
        Document treeType2sLookup = (Document) ((Document) result.getStageObjects().get(1)).get("\$lookup")
        List<Document> treeType2sPipeline = treeType2sLookup.getList("pipeline", Document.class)
        treeType2sPipeline.get(0) ==
                Document.parse("""{ "\$search": { "index": "type_idx", "queryString": { "query": "TermTwo", "path": ["type_field"] } } }""")

        // 3. "treeType2s.treeType3s" has no nav-prop or entity-type entry, falls back to global parser (lowest priority -> global_idx)
        Document treeType3sLookup = (Document) treeType2sPipeline.get(1).get("\$lookup")
        treeType3sLookup.getList("pipeline", Document.class).get(0) ==
                Document.parse("""{ "\$search": { "index": "global_idx", "queryString": { "query": "TermThree", "path": ["global_field"] } } }""")
    }

    def 'should parse multi-level $expand with $levels > 1 and nested $expand with $search using $lookup'() {
        given:
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("treeType1s", "\$expand=children(\$levels=2;\$search=Vehicle;\$expand=treeType2s(\$search=SubItem))", null, null)

        def globalParser = createSearchParser("global_idx", ["name"])
        def nestedNavParser = createSearchParser("nested_nav_idx", ["title"])
        def context = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(true)
                .withGlobalSearchParser(globalParser)
                .withNavigationPropertySearchParsers([
                        "children.treeType2s": nestedNavParser
                ])
                .build()
        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        def result = parser.parse(uriInfo.getExpandOption(), context)

        then:
        result.getStageObjects().size() == 1
        Document level1Lookup = (Document) ((Document) result.getStageObjects().get(0)).get("\$lookup")
        List<Document> level1Pipeline = level1Lookup.getList("pipeline", Document.class)
        // Level 1 pipeline has: $search (for children), nested $lookup (level 2 for children), nested $lookup (for treeType2s)
        level1Pipeline.size() == 3
        level1Pipeline.get(0) == Document.parse("""{ "\$search": { "index": "global_idx", "queryString": { "query": "Vehicle", "path": ["name"] } } }""")

        Document level2Lookup = (Document) level1Pipeline.get(1).get("\$lookup")
        List<Document> level2Pipeline = level2Lookup.getList("pipeline", Document.class)
        // Level 2 pipeline has: $search (for children) and nested $lookup (for treeType2s)
        level2Pipeline.size() == 2
        level2Pipeline.get(0) == Document.parse("""{ "\$search": { "index": "global_idx", "queryString": { "query": "Vehicle", "path": ["name"] } } }""")

        Document nestedTreeType2sLookup = (Document) level2Pipeline.get(1).get("\$lookup")
        nestedTreeType2sLookup.getList("pipeline", Document.class).get(0) ==
                Document.parse("""{ "\$search": { "index": "nested_nav_idx", "queryString": { "query": "SubItem", "path": ["title"] } } }""")
    }

    private static ODataSearchToMongoAtlasSearchParser createSearchParser(String indexName, List<String> paths) {
        return new ODataSearchToMongoAtlasSearchParser(new SearchDocumentForQueryStringFactory() {
            @Override
            Bson build(SearchExpression searchExpression, SearchDocumentForQueryStringFactory.QueryStringParsingResult queryStringParsingResult, ODataSearchToMongoAtlasSearchOptions options) {
                return new Document().append("index", indexName)
                        .append("queryString", new Document()
                                .append("query", queryStringParsingResult.getQuery())
                                .append("path", paths)
                        )
            }
        })
    }
}
