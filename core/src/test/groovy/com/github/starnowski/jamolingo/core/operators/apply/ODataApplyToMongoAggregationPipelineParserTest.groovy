package com.github.starnowski.jamolingo.core.operators.apply

import com.github.starnowski.jamolingo.core.AbstractSpecification
import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver
import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade
import com.mongodb.MongoClientSettings
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.api.uri.queryoption.ApplyItem
import org.apache.olingo.server.api.uri.queryoption.ApplyOption
import org.apache.olingo.server.core.uri.parser.Parser
import org.bson.Document
import org.bson.UuidRepresentation
import org.bson.codecs.DocumentCodec
import org.bson.codecs.UuidCodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.json.JsonWriterSettings
import spock.lang.Unroll

class ODataApplyToMongoAggregationPipelineParserTest extends AbstractSpecification {

    def "should return empty stage list when applyOption is null"() {
        given:
        def parser = new ODataApplyToMongoAggregationPipelineParser()
        def mockFacade = Mock(EdmPropertyMongoPathResolver)

        when:
        def result = parser.parse(null, mockFacade)

        then:
        result != null
        result.stageObjects.isEmpty()
    }

    def "should return empty stage list when applyOption has no items"() {
        given:
        def parser = new ODataApplyToMongoAggregationPipelineParser()
        def mockApplyOption = Mock(ApplyOption)
        mockApplyOption.getApplyItems() >> []
        def mockFacade = Mock(EdmPropertyMongoPathResolver)

        when:
        def result = parser.parse(mockApplyOption, mockFacade)

        then:
        result != null
        result.stageObjects.isEmpty()
    }
    def "should return facet and related stages when applyOption has concat item"() {
        given:
        def parser = new ODataApplyToMongoAggregationPipelineParser()
        def mockApplyOption = Mock(ApplyOption)
        def mockConcatItem = Mock(org.apache.olingo.server.api.uri.queryoption.apply.Concat)
        def mockConcatOption1 = Mock(ApplyOption)
        def mockConcatOption2 = Mock(ApplyOption)
        def mockConcatItem1 = Mock(ApplyItem)
        def mockConcatItem2 = Mock(ApplyItem)
        
        mockConcatItem.getKind() >> ApplyItem.Kind.CONCAT
        mockConcatItem.getApplyOptions() >> [mockConcatOption1, mockConcatOption2]
        
        mockConcatOption1.getApplyItems() >> [mockConcatItem1]
        mockConcatOption2.getApplyItems() >> [mockConcatItem2]
        
        mockConcatItem1.getKind() >> ApplyItem.Kind.IDENTITY
        mockConcatItem2.getKind() >> ApplyItem.Kind.IDENTITY
        
        mockApplyOption.getApplyItems() >> [mockConcatItem]
        def mockFacade = Mock(EdmPropertyMongoPathResolver)

        when:
        def result = parser.parse(mockApplyOption, mockFacade)

        then:
        result != null
        result.stageObjects.size() == 4
        
        def facetDoc = result.stageObjects.get(0).get('$facet')
        facetDoc != null
        facetDoc.get('concat_0') == []
        facetDoc.get('concat_1') == []
        
        def projectDoc = result.stageObjects.get(1).get('$project')
        projectDoc != null
        projectDoc.get('_combinedResult') != null
        projectDoc.get('_combinedResult').get('$concatArrays') == ['$concat_0', '$concat_1']
        
        def unwindDoc = result.stageObjects.get(2).get('$unwind')
        unwindDoc == '$_combinedResult'
        
        def replaceRootDoc = result.stageObjects.get(3).get('$replaceRoot')
        replaceRootDoc.get('newRoot') == '$_combinedResult'
    }

    def "should return delegated stage when applyOption has search item"() {
        given:
        def mockSearchDelegate = Mock(ApplySearchToMongoPipelineParser)
        def parser = new ODataApplyToMongoAggregationPipelineParser(mockSearchDelegate)
        def mockApplyOption = Mock(ApplyOption)
        def mockSearchItem = Mock(org.apache.olingo.server.api.uri.queryoption.apply.Search)
        
        mockSearchItem.getKind() >> ApplyItem.Kind.SEARCH
        mockApplyOption.getApplyItems() >> [mockSearchItem]
        def mockFacade = Mock(EdmPropertyMongoPathResolver)

        def expectedStage = Mock(org.bson.conversions.Bson)
        def expectedResult = DefaultApplyOperatorResult.builder().withStageObjects([expectedStage]).build()

        when:
        def result = parser.parse(mockApplyOption, mockFacade)

        then:
        1 * mockSearchDelegate.parse({ SearchApplyItemContext ctx -> 
            ctx.isFirstApplyItem() && ctx.getSearch() == mockSearchItem 
        }) >> expectedResult
        
        
        result != null
        result.stageObjects.size() == 1
        result.stageObjects[0] == expectedStage
    }

    def "should return delegated stage when applyOption has search item nested in groupby"() {
        given:
        def mockSearchDelegate = Mock(ApplySearchToMongoPipelineParser)
        def parser = new ODataApplyToMongoAggregationPipelineParser(mockSearchDelegate)
        def mockApplyOption = Mock(ApplyOption)
        def mockGroupByItem = Mock(org.apache.olingo.server.api.uri.queryoption.apply.GroupBy)
        def mockNestedApplyOption = Mock(ApplyOption)
        def mockSearchItem = Mock(org.apache.olingo.server.api.uri.queryoption.apply.Search)
        
        mockSearchItem.getKind() >> ApplyItem.Kind.SEARCH
        mockNestedApplyOption.getApplyItems() >> [mockSearchItem]
        mockGroupByItem.getKind() >> ApplyItem.Kind.GROUP_BY
        mockGroupByItem.getGroupByItems() >> []
        mockGroupByItem.getApplyOption() >> mockNestedApplyOption
        
        mockApplyOption.getApplyItems() >> [mockGroupByItem]
        def mockFacade = Mock(EdmPropertyMongoPathResolver)

        def expectedStage = Mock(org.bson.conversions.Bson)
        def expectedResult = DefaultApplyOperatorResult.builder().withStageObjects([expectedStage]).build()

        when:
        def result = parser.parse(mockApplyOption, mockFacade)

        then:
        1 * mockSearchDelegate.parse({ SearchApplyItemContext ctx -> 
            ctx.isFirstApplyItem() && ctx.getSearch() == mockSearchItem 
        }) >> expectedResult
        
        result != null
        result.stageObjects.size() == 3 // group, project, and the nested search stage
        result.stageObjects[2] == expectedStage
    }

    def "should accumulate used properties and shape redefinition across multiple apply items"() {
        given:
        def parser = new ODataApplyToMongoAggregationPipelineParser()
        def mockApplyOption = Mock(ApplyOption)
        def mockFacade = Mock(EdmPropertyMongoPathResolver)

        def mockFilterItem = Mock(ApplyItem)
        mockFilterItem.getKind() >> ApplyItem.Kind.IDENTITY

        def mockSearchDelegate = Mock(ApplySearchToMongoPipelineParser)
        def parserWithSearch = new ODataApplyToMongoAggregationPipelineParser(mockSearchDelegate)
        
        def mockSearchItem1 = Mock(org.apache.olingo.server.api.uri.queryoption.apply.Search)
        mockSearchItem1.getKind() >> ApplyItem.Kind.SEARCH
        
        def mockSearchItem2 = Mock(org.apache.olingo.server.api.uri.queryoption.apply.Search)
        mockSearchItem2.getKind() >> ApplyItem.Kind.SEARCH
        
        mockApplyOption.getApplyItems() >> [mockSearchItem1, mockSearchItem2]

        def expectedStage1 = Mock(org.bson.conversions.Bson)
        def expectedResult1 = DefaultApplyOperatorResult.builder()
            .withStageObjects([expectedStage1])
            .withUsedMongoDocumentProperties(["prop1", "prop2"])
            .withDocumentShapeRedefined(false)
            .build()
            
        def expectedStage2 = Mock(org.bson.conversions.Bson)
        def expectedResult2 = DefaultApplyOperatorResult.builder()
            .withStageObjects([expectedStage2])
            .withUsedMongoDocumentProperties(["prop2", "prop3"])
            .withAddedMongoDocumentProperties(["newProp"])
            .withDocumentShapeRedefined(true)
            .build()

        when:
        def result = parserWithSearch.parse(mockApplyOption, mockFacade)

        then:
        1 * mockSearchDelegate.parse({ SearchApplyItemContext ctx -> ctx.getSearch() == mockSearchItem1 }) >> expectedResult1
        1 * mockSearchDelegate.parse({ SearchApplyItemContext ctx -> ctx.getSearch() == mockSearchItem2 }) >> expectedResult2
        
        result != null
        result.stageObjects.size() == 2
        result.stageObjects[0] == expectedStage1
        result.stageObjects[1] == expectedStage2
        result.usedMongoDocumentProperties == ["prop1", "prop2", "prop3"]
        result.addedMongoDocumentProperties == ["newProp"]
        result.documentShapeRedefined == true
    }

    @Unroll
    def "should return expected aggregation pipeline stages for apply query #applyQuery"() {
        given:
        System.out.println("Testing apply: " + applyQuery)
        Edm edm = loadEmdProvider(edmFile)
        JsonWriterSettings settings = JsonWriterSettings.builder().build()
        CodecRegistry registry = CodecRegistries.fromRegistries(
                CodecRegistries.fromProviders(new UuidCodecProvider(UuidRepresentation.STANDARD)),
                MongoClientSettings.getDefaultCodecRegistry()
        )
        DocumentCodec codec = new DocumentCodec(registry)

        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri(entitySet, "\$apply=" + applyQuery, null, null)
        ODataApplyToMongoAggregationPipelineParser tested = new ODataApplyToMongoAggregationPipelineParser()

        when:
        def result = tested.parse(uriInfo.getApplyOption(), DefaultEdmMongoContextFacade.builder().build())

        then:
        result.getStageObjects().collect { Document.parse(it.toBsonDocument(Document.class, registry).toJson(settings)).toJson(settings, codec) } ==
                expectedStages.collect { Document.parse(it).toJson(settings, codec) }

        where:
        [edmFile, entitySet, applyQuery, expectedStages, expectedFields] << applyTestCases()
    }

    @Unroll
    def "should return expected used MongoDB properties for apply query #applyQuery"() {
        given:
        System.out.println("Testing apply: " + applyQuery)
        Edm edm = loadEmdProvider(edmFile)
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri(entitySet, "\$apply=" + applyQuery, null, null)
        ODataApplyToMongoAggregationPipelineParser tested = new ODataApplyToMongoAggregationPipelineParser()

        when:
        def result = tested.parse(uriInfo.getApplyOption(), DefaultEdmMongoContextFacade.builder().build())

        then:
        new HashSet<>(result.getUsedMongoDocumentProperties()) == new HashSet<>(expectedFields as List)

        where:
        [edmFile, entitySet, applyQuery, expectedStages, expectedFields] << applyTestCases()
    }

    static applyTestCases() {
        [
                [
                        "edm/edm6_filter_main.xml",
                        "examples2",
                        "filter(plainString eq 'eOMtThyhVNLWUZNRcBaQKxI')",
                        ["""{"\$match": {"\$and": [{"plainString": "eOMtThyhVNLWUZNRcBaQKxI"}]}}"""],
                        ["plainString"]
                ],
                [
                        "edm/edm6_filter_main.xml",
                        "examples2",
                        "filter(plainString eq 'Some text')",
                        ["""{"\$match": {"\$and": [{"plainString": "Some text"}]}}"""],
                        ["plainString"]
                ],
                [
                        "edm/edm6_filter_main.xml",
                        "examples2",
                        "identity",
                        [],
                        []
                ],
                [
                        "edm/edm6_filter_main.xml",
                        "examples2",
                        "groupby((plainString))",
                        [
                                """{"\$group": {"_id": {"plainString": "\$plainString"}}}""",
                                """{"\$project": {"_id": 0, "plainString": "\$_id.plainString"}}"""
                        ],
                        ["plainString"]
                ],
                [
                        "edm/edm6_filter_main.xml",
                        "examples2",
                        "orderby(smallInteger,plainString)/top(2)",
                        [
                                """{"\$sort": {"smallInteger": 1, "plainString": 1}}""",
                                """{"\$limit": 2}"""
                        ],
                        ["smallInteger", "plainString"]
                ],
                [
                        "edm/edm6_filter_main.xml",
                        "examples2",
                        "orderby(smallInteger desc,plainString)/top(1)",
                        [
                                """{"\$sort": {"smallInteger": -1, "plainString": 1}}""",
                                """{"\$limit": 1}"""
                        ],
                        ["smallInteger", "plainString"]
                ],
                [
                        "edm/edm6_filter_main.xml",
                        "examples2",
                        "orderby(smallInteger,plainString)/skip(1)/top(1)",
                        [
                                """{"\$sort": {"smallInteger": 1, "plainString": 1}}""",
                                """{"\$skip": 1}""",
                                """{"\$limit": 1}"""
                        ],
                        ["smallInteger", "plainString"]
                ],
                [
                        "edm/edm6_filter_main.xml",
                        "examples2",
                        "filter(plainString eq 'Mario')/groupby((plainString))",
                        [
                                """{"\$match": {"\$and": [{"plainString": "Mario"}]}}""",
                                """{"\$group": {"_id": {"plainString": "\$plainString"}}}""",
                                """{"\$project": {"_id": 0, "plainString": "\$_id.plainString"}}"""
                        ],
                        ["plainString"]
                ],
                [
                        "edm/edm6_filter_main.xml",
                        "examples2",
                        "filter(plainString eq 'Poem')/groupby((plainString,smallInteger))",
                        [
                                """{"\$match": {"\$and": [{"plainString": "Poem"}]}}""",
                                """{"\$group": {"_id": {"plainString": "\$plainString", "smallInteger": "\$smallInteger"}}}""",
                                """{"\$project": {"_id": 0, "plainString": "\$_id.plainString", "smallInteger": "\$_id.smallInteger"}}"""
                        ],
                        ["plainString", "smallInteger"]
                ],
                [
                        "edm/edm6_filter_main.xml",
                        "examples2",
                        "topcount(2,smallInteger)",
                        [
                                """{"\$sort": {"smallInteger": -1}}""",
                                """{"\$limit": 2}"""
                        ],
                        ["smallInteger"]
                ],
                [
                        "edm/edm6_filter_main.xml",
                        "examples2",
                        "bottomcount(2,smallInteger)",
                        [
                                """{"\$sort": {"smallInteger": 1}}""",
                                """{"\$limit": 2}"""
                        ],
                        ["smallInteger"]
                ],
                [
                        "edm/edm8_apply_aggregate.xml",
                        "examples2",
                        "topsum(800,genericInteger)",
                        [
                                """{"\$sort": {"genericInteger": -1}}""",
                                """{"\$setWindowFields": {"sortBy": {"genericInteger": -1}, "output": {"__jamolingo_cumsum": {"\$sum": "\$genericInteger", "window": {"documents": ["unbounded", "current"]}}}}}""",
                                """{"\$match": {"__jamolingo_cumsum": {"\$lte": 800.0}}}""",
                                """{"\$unset": "__jamolingo_cumsum"}"""
                        ],
                        ["genericInteger"]
                ],
                [
                        "edm/edm8_apply_aggregate.xml",
                        "examples2",
                        "bottomsum(160,genericInteger)",
                        [
                                """{"\$sort": {"genericInteger": 1}}""",
                                """{"\$setWindowFields": {"sortBy": {"genericInteger": 1}, "output": {"__jamolingo_cumsum": {"\$sum": "\$genericInteger", "window": {"documents": ["unbounded", "current"]}}}}}""",
                                """{"\$match": {"__jamolingo_cumsum": {"\$lte": 160.0}}}""",
                                """{"\$unset": "__jamolingo_cumsum"}"""
                        ],
                        ["genericInteger"]
                ],
                [
                        "edm/edm8_apply_aggregate.xml",
                        "examples2",
                        "toppercent(50,genericInteger)",
                        [
                                """{"\$sort": {"genericInteger": -1}}""",
                                """{"\$setWindowFields": {"sortBy": {"genericInteger": -1}, "output": {"__jamolingo_totalsum": {"\$sum": "\$genericInteger", "window": {"documents": ["unbounded", "unbounded"]}}, "__jamolingo_cumsum": {"\$sum": "\$genericInteger", "window": {"documents": ["unbounded", "current"]}}}}}""",
                                """{"\$match": {"\$expr": {"\$lte": ["\$__jamolingo_cumsum", {"\$multiply": ["\$__jamolingo_totalsum", 0.5]}]}}}""",
                                """{"\$unset": ["__jamolingo_totalsum", "__jamolingo_cumsum"]}"""
                        ],
                        ["genericInteger"]
                ],
                [
                        "edm/edm8_apply_aggregate.xml",
                        "examples2",
                        "bottompercent(15,genericInteger)",
                        [
                                """{"\$sort": {"genericInteger": 1}}""",
                                """{"\$setWindowFields": {"sortBy": {"genericInteger": 1}, "output": {"__jamolingo_totalsum": {"\$sum": "\$genericInteger", "window": {"documents": ["unbounded", "unbounded"]}}, "__jamolingo_cumsum": {"\$sum": "\$genericInteger", "window": {"documents": ["unbounded", "current"]}}}}}""",
                                """{"\$match": {"\$expr": {"\$lte": ["\$__jamolingo_cumsum", {"\$multiply": ["\$__jamolingo_totalsum", 0.15]}]}}}""",
                                """{"\$unset": ["__jamolingo_totalsum", "__jamolingo_cumsum"]}"""
                        ],
                        ["genericInteger"]
                ],
                [
                        "edm/edm8_apply_aggregate.xml",
                        "examples2",
                        "aggregate(\$count as totalCount)",
                        [
                                """{"\$group": {"_id": null, "totalCount": {"\$sum": 1}}}""",
                                """{"\$project": {"_id": 0, "totalCount": 1}}"""
                        ],
                        []
                ],
                [
                        "edm/edm8_apply_aggregate.xml",
                        "examples2",
                        "groupby((plainString2),aggregate(genericInteger with sum as genericIntegerSum))",
                        [
                                """{"\$group": {"_id": {"plainString2": "\$plainString2"}, "genericIntegerSum": {"\$sum": "\$genericInteger"}}}""",
                                """{"\$project": {"_id": 0, "plainString2": "\$_id.plainString2", "genericIntegerSum": 1}}"""
                        ],
                        ["plainString2", "genericInteger"]
                ],
                [
                        "edm/edm8_apply_aggregate.xml",
                        "examples2",
                        "groupby((plainString2),aggregate(genericInteger with max as genericIntegerMax))",
                        [
                                """{"\$group": {"_id": {"plainString2": "\$plainString2"}, "genericIntegerMax": {"\$max": "\$genericInteger"}}}""",
                                """{"\$project": {"_id": 0, "plainString2": "\$_id.plainString2", "genericIntegerMax": 1}}"""
                        ],
                        ["plainString2", "genericInteger"]
                ],
                [
                        "edm/edm8_apply_aggregate.xml",
                        "examples2",
                        "groupby((plainString2),aggregate(genericInteger with min as genericIntegerMin))",
                        [
                                """{"\$group": {"_id": {"plainString2": "\$plainString2"}, "genericIntegerMin": {"\$min": "\$genericInteger"}}}""",
                                """{"\$project": {"_id": 0, "plainString2": "\$_id.plainString2", "genericIntegerMin": 1}}"""
                        ],
                        ["plainString2", "genericInteger"]
                ],
                [
                        "edm/edm8_apply_aggregate.xml",
                        "examples2",
                        "groupby((plainString2),aggregate(genericInteger with average as genericIntegerAvg))",
                        [
                                """{"\$group": {"_id": {"plainString2": "\$plainString2"}, "genericIntegerAvg": {"\$avg": "\$genericInteger"}}}""",
                                """{"\$project": {"_id": 0, "plainString2": "\$_id.plainString2", "genericIntegerAvg": 1}}"""
                        ],
                        ["plainString2", "genericInteger"]
                ],
                [
                        "edm/edm8_apply_aggregate.xml",
                        "examples2",
                        "groupby((plainString2),aggregate(smallInteger1 with countdistinct as smallInteger1Countdistinct))",
                        [
                                """{"\$group": {"_id": {"plainString2": "\$plainString2"}, "smallInteger1Countdistinct_distinctArray": {"\$addToSet": "\$smallInteger1"}}}""",
                                """{"\$project": {"_id": 0, "plainString2": "\$_id.plainString2", "smallInteger1Countdistinct": {"\$size": "\$smallInteger1Countdistinct_distinctArray"}}}"""
                        ],
                        ["plainString2", "smallInteger1"]
                ],
                [
                        "edm/edm8_apply_aggregate.xml",
                        "examples2",
                        "groupby((plainString2),aggregate(\$count as plainString2Count))",
                        [
                                """{"\$group": {"_id": {"plainString2": "\$plainString2"}, "plainString2Count": {"\$sum": 1}}}""",
                                """{"\$project": {"_id": 0, "plainString2": "\$_id.plainString2", "plainString2Count": 1}}"""
                        ],
                        ["plainString2"]
                ],
                [
                        "edm/edm8_apply_aggregate.xml",
                        "examples2",
                        "groupby((plainString2),aggregate(genericInteger with average as genericIntegerAvg,smallInteger1 with countdistinct as smallInteger1Countdistinct))",
                        [
                                """{"\$group": {"_id": {"plainString2": "\$plainString2"}, "genericIntegerAvg": {"\$avg": "\$genericInteger"}, "smallInteger1Countdistinct_distinctArray": {"\$addToSet": "\$smallInteger1"}}}""",
                                """{"\$project": {"_id": 0, "plainString2": "\$_id.plainString2", "genericIntegerAvg": 1, "smallInteger1Countdistinct": {"\$size": "\$smallInteger1Countdistinct_distinctArray"}}}"""
                        ],
                        ["plainString2", "genericInteger", "smallInteger1"]
                ],
                [
                        "edm/edm8_apply_aggregate.xml",
                        "examples2",
                        "compute(genericInteger add 10 as genericIntegerPlus10)",
                        [
                                """{"\$addFields": {"genericIntegerPlus10": {"\$add": ["\$genericInteger", 10]}}}"""
                        ],
                        ["genericInteger"]
                ],
                [
                        "edm/edm8_apply_aggregate.xml",
                        "examples2",
                        "compute(genericInteger mul 2 add 5 as calcValue)",
                        [
                                """{"\$addFields": {"calcValue": {"\$add": [{"\$multiply": ["\$genericInteger", 2]}, 5]}}}"""
                        ],
                        ["genericInteger"]
                ],
                [
                        "edm/edm8_apply_aggregate.xml",
                        "examples2",
                        "filter(plainString2 eq 'Electronics')/compute(genericInteger sub 5 as calcValue)/groupby((plainString2),aggregate(calcValue with sum as calcValueSum))",
                        [
                                """{"\$match": {"\$and": [{"plainString2": "Electronics"}]}}""",
                                """{"\$addFields": {"calcValue": {"\$subtract": ["\$genericInteger", 5]}}}""",
                                """{"\$group": {"_id": {"plainString2": "\$plainString2"}, "calcValueSum": {"\$sum": "\$calcValue"}}}""",
                                """{"\$project": {"_id": 0, "plainString2": "\$_id.plainString2", "calcValueSum": 1}}"""
                        ],
                        ["plainString2", "genericInteger", "calcValue"]
                ],
                [
                        "edm/edm8_apply_aggregate.xml",
                        "examples2",
                        "concat(filter(plainString1 eq 'Laptop'),filter(plainString1 eq 'Tablet'))",
                        [
                                """{"\$facet": {"concat_0": [{"\$match": {"\$and": [{"plainString1": "Laptop"}]}}], "concat_1": [{"\$match": {"\$and": [{"plainString1": "Tablet"}]}}]}}""",
                                """{"\$project": {"_combinedResult": {"\$concatArrays": ["\$concat_0", "\$concat_1"]}}}""",
                                """{"\$unwind": "\$_combinedResult"}""",
                                """{"\$replaceRoot": {"newRoot": "\$_combinedResult"}}"""
                        ],
                        ["plainString1"]
                ],
                [
                        "edm/edm8_apply_aggregate.xml",
                        "examples2",
                        "concat(filter(plainString1 eq 'Laptop'),groupby((plainString2),aggregate(genericInteger with sum as calcValueSum)))",
                        [
                                """{"\$facet": {"concat_0": [{"\$match": {"\$and": [{"plainString1": "Laptop"}]}}], "concat_1": [{"\$group": {"_id": {"plainString2": "\$plainString2"}, "calcValueSum": {"\$sum": "\$genericInteger"}}}, {"\$project": {"_id": 0, "plainString2": "\$_id.plainString2", "calcValueSum": 1}}]}}""",
                                """{"\$project": {"_combinedResult": {"\$concatArrays": ["\$concat_0", "\$concat_1"]}}}""",
                                """{"\$unwind": "\$_combinedResult"}""",
                                """{"\$replaceRoot": {"newRoot": "\$_combinedResult"}}"""
                        ],
                        ["plainString1", "plainString2", "genericInteger"]
                ],
                [
                        "edm/edm_tree.xml",
                        "treeType1s",
                        "expand(children,expand(treeType2s))",
                        [
                                """{"\$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"\$lookup": {"from": "MyService.TreeType2", "localField": "_id", "foreignField": "treeType1Id", "as": "treeType2s"}}], "as": "children"}}"""
                        ],
                        ["_id"]
                ],
                [
                        "edm/edm_tree.xml",
                        "treeType1s",
                        "expand(treeType2s)",
                        [
                                """{"\$lookup": {"from": "MyService.TreeType2", "localField": "_id", "foreignField": "treeType1Id", "as": "treeType2s"}}"""
                        ],
                        ["_id"]
                ],
                [
                        "edm/edm_tree.xml",
                        "treeType1s",
                        "expand(treeType2s,filter(index gt 5))",
                        [
                                """{"\$lookup": {"from": "MyService.TreeType2", "localField": "_id", "foreignField": "treeType1Id", "pipeline": [{"\$match": {"\$and": [{"index": {"\$gt": 5}}]}}], "as": "treeType2s"}}"""
                        ],
                        ["_id"]
                ]

        ]
    }

}
