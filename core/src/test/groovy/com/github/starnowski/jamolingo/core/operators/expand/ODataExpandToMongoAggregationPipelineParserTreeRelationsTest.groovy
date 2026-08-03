package com.github.starnowski.jamolingo.core.operators.expand

import com.github.starnowski.jamolingo.core.AbstractSpecification
import com.github.starnowski.jamolingo.core.operators.expand.ODataExpandToMongoAggregationPipelineParser.DefaultExpandParserContext
import com.mongodb.MongoClientSettings
import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.core.uri.parser.Parser
import org.bson.Document
import org.bson.UuidRepresentation
import org.bson.codecs.DocumentCodec
import org.bson.codecs.UuidCodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.json.JsonWriterSettings
import spock.lang.Unroll
import org.bson.BsonArray
import org.bson.BsonDocument
import org.bson.codecs.BsonArrayCodec

class ODataExpandToMongoAggregationPipelineParserTreeRelationsTest extends AbstractSpecification {

    private String convertToFormattedJsonArray(List<Document> documents) {
        JsonWriterSettings settings = JsonWriterSettings.builder().build()
        CodecRegistry registry = CodecRegistries.fromRegistries(
                CodecRegistries.fromProviders(new UuidCodecProvider(UuidRepresentation.STANDARD)),
                MongoClientSettings.getDefaultCodecRegistry()
        )
        DocumentCodec codec = new DocumentCodec(registry)
        
        def jsonElements = documents.collect { it.toJson(settings, codec) }
        return "[" + jsonElements.join(", ") + "]"
    }

    private List<Document> parseJsonArray(String json) {
        if (json == null || json.trim().isEmpty()) {
            return []
        }
        // Wrapping the array into a document to parse it easily with Document.parse
        Document doc = Document.parse("{ \"arr\": " + json + "}")
        return doc.get("arr", List.class)
    }

    @Unroll
    def "should return expected pipeline stages for tree relations with graphLookup: #expandQuery"() {
        given:
        System.out.println("Testing graphLookup expand: " + expandQuery)
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("treeType1s", expandQuery, null, null)

        def context = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(false)
                .withMaxLevel(5)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        def result = parser.parse(uriInfo.getExpandOption(), context)
        def actualJson = convertToFormattedJsonArray(result.getStageObjects() as List<Document>)
        System.out.println("ACTUAL_JSON_GRAPH_LOOKUP: " + expandQuery + " : " + actualJson)

        then:
        def expectedList = parseJsonArray(expectedPipelineBsonArray)
        def actualList = result.getStageObjects() as List<Document>
        
        def actualJsonNormalized = convertToFormattedJsonArray(actualList)
        def expectedJsonNormalized = convertToFormattedJsonArray(expectedList)
        
        actualJsonNormalized == expectedJsonNormalized

        where:
        [expandQuery, expectedPipelineBsonArray] << graphLookupEdmPathsMappings()
    }

    @Unroll
    def "should return expected pipeline stages for tree relations with multi-level lookup: #expandQuery"() {
        given:
        System.out.println("Testing lookup expand: " + expandQuery)
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri("treeType1s", expandQuery, null, null)

        def context = DefaultExpandParserContext.builder()
                .withUseLookupForLevelGreaterThanOne(true)
                .withMaxLevel(5)
                .build()

        def parser = new ODataExpandToMongoAggregationPipelineParser()

        when:
        def result = parser.parse(uriInfo.getExpandOption(), context)
        def actualJson = convertToFormattedJsonArray(result.getStageObjects() as List<Document>)
        System.out.println("ACTUAL_JSON_LOOKUP: " + expandQuery + " : " + actualJson)

        then:
        def expectedList = parseJsonArray(expectedPipelineBsonArray)
        def actualList = result.getStageObjects() as List<Document>
        
        def actualJsonNormalized = convertToFormattedJsonArray(actualList)
        def expectedJsonNormalized = convertToFormattedJsonArray(expectedList)
        
        actualJsonNormalized == expectedJsonNormalized

        where:
        [expandQuery, expectedPipelineBsonArray] << lookupMultiLevelEdmPathsMappings()
    }

    static graphLookupEdmPathsMappings() {
        [
                ['$expand=category', '''[{"$lookup": {"from": "MyService.Category", "localField": "categoryId", "foreignField": "_id", "as": "category"}}, {"$unwind": {"path": "$category", "preserveNullAndEmptyArrays": true}}]'''],
                ['$expand=category,children', '''[{"$lookup": {"from": "MyService.Category", "localField": "categoryId", "foreignField": "_id", "as": "category"}}, {"$unwind": {"path": "$category", "preserveNullAndEmptyArrays": true}}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "as": "children"}}]'''],
                ['$expand=category,children,treeType2s', '''[{"$lookup": {"from": "MyService.Category", "localField": "categoryId", "foreignField": "_id", "as": "category"}}, {"$unwind": {"path": "$category", "preserveNullAndEmptyArrays": true}}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "as": "children"}}, {"$lookup": {"from": "MyService.TreeType2", "localField": "_id", "foreignField": "treeType1Id", "as": "treeType2s"}}]'''],
                ['$expand=children($levels=5)', '''[{"$graphLookup": {"from": "MyService.TreeType1", "startWith": "$_id", "connectFromField": "_id", "connectToField": "parentId", "maxDepth": 4, "as": "children", "depthField": "children_odata_graphlookup_depth_variable"}}, {"$unset": "children.children_odata_graphlookup_depth_variable"}]'''],
                ['$expand=children($levels=max)', '''[{"$graphLookup": {"from": "MyService.TreeType1", "startWith": "$_id", "connectFromField": "_id", "connectToField": "parentId", "maxDepth": 4, "as": "children", "depthField": "children_odata_graphlookup_depth_variable"}}, {"$unset": "children.children_odata_graphlookup_depth_variable"}]'''],
                ['$expand=treeType2s($levels=5;$filter=index in (1, 2))', '''[{"$graphLookup": {"from": "MyService.TreeType2", "startWith": "$_id", "connectFromField": "_id", "connectToField": "treeType1Id", "maxDepth": 4, "as": "treeType2s", "restrictSearchWithMatch": {"$and": [{"index": {"$in": [1, 2]}}]}, "depthField": "treeType2s_odata_graphlookup_depth_variable"}}, {"$set": {"treeType2s": {"$reduce": {"input": {"$sortArray": {"input": "$treeType2s", "sortBy": {"treeType2s_odata_graphlookup_depth_variable": 1}}}, "initialValue": [], "in": {"$let": {"vars": {"current": "$$this", "acc": "$$value"}, "in": {"$cond": [{"$or": [{"$eq": ["$$current.treeType2s_odata_graphlookup_depth_variable", 0]}, {"$in": ["$$current.treeType1Id", "$$acc._id"]}]}, {"$concatArrays": ["$$acc", ["$$current"]]}, "$$acc"]}}}}}}}, {"$unset": "treeType2s.treeType2s_odata_graphlookup_depth_variable"}]'''],
                ['$expand=children($levels=3;$filter=index in (2, 3, 4))', '''[{"$graphLookup": {"from": "MyService.TreeType1", "startWith": "$_id", "connectFromField": "_id", "connectToField": "parentId", "maxDepth": 2, "as": "children", "restrictSearchWithMatch": {"$and": [{"index": {"$in": [2, 3, 4]}}]}, "depthField": "children_odata_graphlookup_depth_variable"}}, {"$set": {"children": {"$reduce": {"input": {"$sortArray": {"input": "$children", "sortBy": {"children_odata_graphlookup_depth_variable": 1}}}, "initialValue": [], "in": {"$let": {"vars": {"current": "$$this", "acc": "$$value"}, "in": {"$cond": [{"$or": [{"$eq": ["$$current.children_odata_graphlookup_depth_variable", 0]}, {"$in": ["$$current.parentId", "$$acc._id"]}]}, {"$concatArrays": ["$$acc", ["$$current"]]}, "$$acc"]}}}}}}}, {"$unset": "children.children_odata_graphlookup_depth_variable"}]'''],
                ['$expand=children($levels=max;$orderby=index asc)', '''[{"$graphLookup": {"from": "MyService.TreeType1", "startWith": "$_id", "connectFromField": "_id", "connectToField": "parentId", "maxDepth": 4, "as": "children", "depthField": "children_odata_graphlookup_depth_variable"}}, {"$set": {"children": {"$sortArray": {"input": "$children", "sortBy": {"children_odata_graphlookup_depth_variable": 1, "index": 1}}}}}, {"$unset": "children.children_odata_graphlookup_depth_variable"}]'''],
                ['$expand=children($levels=max;$top=2;$orderby=index asc)', '''[{"$graphLookup": {"from": "MyService.TreeType1", "startWith": "$_id", "connectFromField": "_id", "connectToField": "parentId", "maxDepth": 4, "as": "children", "depthField": "children_odata_graphlookup_depth_variable"}}, {"$set": {"children": {"$sortArray": {"input": "$children", "sortBy": {"children_odata_graphlookup_depth_variable": 1, "index": 1}}}}}, {"$set": {"children_odata_graphlookup_tmp_array": {"$reduce": {"input": {"$ifNull": ["$children", []]}, "initialValue": [], "in": {"$let": {"vars": {"index": {"$indexOfArray": ["$$value.parentId", "$$this.parentId"]}}, "in": {"$cond": [{"$eq": ["$$index", -1]}, {"$concatArrays": ["$$value", [{"parentId": "$$this.parentId", "children_odata_graphlookup_depth_variable": "$$this.children_odata_graphlookup_depth_variable", "children": ["$$this"]}]]}, {"$map": {"input": "$$value", "as": "bucket", "in": {"$cond": [{"$eq": ["$$bucket.parentId", "$$this.parentId"]}, {"parentId": "$$bucket.parentId", "children": {"$concatArrays": ["$$bucket.children", ["$$this"]]}}, "$$bucket"]}}}]}}}}}}}, {"$set": {"children_odata_graphlookup_tmp_array": {"$map": {"input": {"$ifNull": ["$children_odata_graphlookup_tmp_array", []]}, "as": "item", "in": {"$mergeObjects": ["$$item", {"children": {"$sortArray": {"input": "$$item.children", "sortBy": {"children_odata_graphlookup_depth_variable": 1, "index": 1}}}}]}}}}}, {"$set": {"children_odata_graphlookup_tmp_array": {"$map": {"input": {"$ifNull": ["$children_odata_graphlookup_tmp_array", []]}, "as": "item", "in": {"$mergeObjects": ["$$item", {"children": {"$slice": ["$$item.children", 0, 2]}}]}}}}}, {"$set": {"children_odata_graphlookup_tmp_array": {"$reduce": {"input": "$children_odata_graphlookup_tmp_array", "initialValue": [], "in": {"$concatArrays": ["$$value", "$$this.children"]}}}}}, {"$set": {"children": "$children_odata_graphlookup_tmp_array"}}, {"$set": {"children": {"$reduce": {"input": {"$sortArray": {"input": "$children", "sortBy": {"children_odata_graphlookup_depth_variable": 1}}}, "initialValue": [], "in": {"$let": {"vars": {"current": "$$this", "acc": "$$value"}, "in": {"$cond": [{"$or": [{"$eq": ["$$current.children_odata_graphlookup_depth_variable", 0]}, {"$in": ["$$current.parentId", "$$acc._id"]}]}, {"$concatArrays": ["$$acc", ["$$current"]]}, "$$acc"]}}}}}}}, {"$unset": "children_odata_graphlookup_tmp_array"}, {"$unset": "children.children_odata_graphlookup_depth_variable"}]'''],
                ['$expand=children($levels=max;$skip=1;$top=2;$orderby=index asc)', '''[{"$graphLookup": {"from": "MyService.TreeType1", "startWith": "$_id", "connectFromField": "_id", "connectToField": "parentId", "maxDepth": 4, "as": "children", "depthField": "children_odata_graphlookup_depth_variable"}}, {"$set": {"children": {"$sortArray": {"input": "$children", "sortBy": {"children_odata_graphlookup_depth_variable": 1, "index": 1}}}}}, {"$set": {"children_odata_graphlookup_tmp_array": {"$reduce": {"input": {"$ifNull": ["$children", []]}, "initialValue": [], "in": {"$let": {"vars": {"index": {"$indexOfArray": ["$$value.parentId", "$$this.parentId"]}}, "in": {"$cond": [{"$eq": ["$$index", -1]}, {"$concatArrays": ["$$value", [{"parentId": "$$this.parentId", "children_odata_graphlookup_depth_variable": "$$this.children_odata_graphlookup_depth_variable", "children": ["$$this"]}]]}, {"$map": {"input": "$$value", "as": "bucket", "in": {"$cond": [{"$eq": ["$$bucket.parentId", "$$this.parentId"]}, {"parentId": "$$bucket.parentId", "children": {"$concatArrays": ["$$bucket.children", ["$$this"]]}}, "$$bucket"]}}}]}}}}}}}, {"$set": {"children_odata_graphlookup_tmp_array": {"$map": {"input": {"$ifNull": ["$children_odata_graphlookup_tmp_array", []]}, "as": "item", "in": {"$mergeObjects": ["$$item", {"children": {"$sortArray": {"input": "$$item.children", "sortBy": {"children_odata_graphlookup_depth_variable": 1, "index": 1}}}}]}}}}}, {"$set": {"children_odata_graphlookup_tmp_array": {"$map": {"input": {"$ifNull": ["$children_odata_graphlookup_tmp_array", []]}, "as": "item", "in": {"$mergeObjects": ["$$item", {"children": {"$slice": ["$$item.children", 1, 2]}}]}}}}}, {"$set": {"children_odata_graphlookup_tmp_array": {"$reduce": {"input": "$children_odata_graphlookup_tmp_array", "initialValue": [], "in": {"$concatArrays": ["$$value", "$$this.children"]}}}}}, {"$set": {"children": "$children_odata_graphlookup_tmp_array"}}, {"$set": {"children": {"$reduce": {"input": {"$sortArray": {"input": "$children", "sortBy": {"children_odata_graphlookup_depth_variable": 1}}}, "initialValue": [], "in": {"$let": {"vars": {"current": "$$this", "acc": "$$value"}, "in": {"$cond": [{"$or": [{"$eq": ["$$current.children_odata_graphlookup_depth_variable", 0]}, {"$in": ["$$current.parentId", "$$acc._id"]}]}, {"$concatArrays": ["$$acc", ["$$current"]]}, "$$acc"]}}}}}}}, {"$unset": "children_odata_graphlookup_tmp_array"}, {"$unset": "children.children_odata_graphlookup_depth_variable"}]'''],
                ['$expand=children($levels=max;$skip=1;$top=2;$orderby=index asc;$select=index)', '''[{"$graphLookup": {"from": "MyService.TreeType1", "startWith": "$_id", "connectFromField": "_id", "connectToField": "parentId", "maxDepth": 4, "as": "children", "depthField": "children_odata_graphlookup_depth_variable"}}, {"$set": {"children": {"$sortArray": {"input": "$children", "sortBy": {"children_odata_graphlookup_depth_variable": 1, "index": 1}}}}}, {"$set": {"children_odata_graphlookup_tmp_array": {"$reduce": {"input": {"$ifNull": ["$children", []]}, "initialValue": [], "in": {"$let": {"vars": {"index": {"$indexOfArray": ["$$value.parentId", "$$this.parentId"]}}, "in": {"$cond": [{"$eq": ["$$index", -1]}, {"$concatArrays": ["$$value", [{"parentId": "$$this.parentId", "children_odata_graphlookup_depth_variable": "$$this.children_odata_graphlookup_depth_variable", "children": ["$$this"]}]]}, {"$map": {"input": "$$value", "as": "bucket", "in": {"$cond": [{"$eq": ["$$bucket.parentId", "$$this.parentId"]}, {"parentId": "$$bucket.parentId", "children": {"$concatArrays": ["$$bucket.children", ["$$this"]]}}, "$$bucket"]}}}]}}}}}}}, {"$set": {"children_odata_graphlookup_tmp_array": {"$map": {"input": {"$ifNull": ["$children_odata_graphlookup_tmp_array", []]}, "as": "item", "in": {"$mergeObjects": ["$$item", {"children": {"$sortArray": {"input": "$$item.children", "sortBy": {"children_odata_graphlookup_depth_variable": 1, "index": 1}}}}]}}}}}, {"$set": {"children_odata_graphlookup_tmp_array": {"$map": {"input": {"$ifNull": ["$children_odata_graphlookup_tmp_array", []]}, "as": "item", "in": {"$mergeObjects": ["$$item", {"children": {"$slice": ["$$item.children", 1, 2]}}]}}}}}, {"$set": {"children_odata_graphlookup_tmp_array": {"$reduce": {"input": "$children_odata_graphlookup_tmp_array", "initialValue": [], "in": {"$concatArrays": ["$$value", "$$this.children"]}}}}}, {"$set": {"children": "$children_odata_graphlookup_tmp_array"}}, {"$set": {"children": {"$reduce": {"input": {"$sortArray": {"input": "$children", "sortBy": {"children_odata_graphlookup_depth_variable": 1}}}, "initialValue": [], "in": {"$let": {"vars": {"current": "$$this", "acc": "$$value"}, "in": {"$cond": [{"$or": [{"$eq": ["$$current.children_odata_graphlookup_depth_variable", 0]}, {"$in": ["$$current.parentId", "$$acc._id"]}]}, {"$concatArrays": ["$$acc", ["$$current"]]}, "$$acc"]}}}}}}}, {"$unset": "children_odata_graphlookup_tmp_array"}, {"$set": {"children": {"$map": {"input": {"$ifNull": ["$children", []]}, "as": "item", "in": {"index": "$$item.index"}}}}}, {"$unset": "children.children_odata_graphlookup_depth_variable"}]''']
        ]
    }

    static lookupMultiLevelEdmPathsMappings() {
        [
                ['$expand=category', '''[{"$lookup": {"from": "MyService.Category", "localField": "categoryId", "foreignField": "_id", "as": "category"}}, {"$unwind": {"path": "$category", "preserveNullAndEmptyArrays": true}}]'''],
                ['$expand=category,children', '''[{"$lookup": {"from": "MyService.Category", "localField": "categoryId", "foreignField": "_id", "as": "category"}}, {"$unwind": {"path": "$category", "preserveNullAndEmptyArrays": true}}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "as": "children"}}]'''],
                ['$expand=category,children,treeType2s', '''[{"$lookup": {"from": "MyService.Category", "localField": "categoryId", "foreignField": "_id", "as": "category"}}, {"$unwind": {"path": "$category", "preserveNullAndEmptyArrays": true}}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "as": "children"}}, {"$lookup": {"from": "MyService.TreeType2", "localField": "_id", "foreignField": "treeType1Id", "as": "treeType2s"}}]'''],
                ['$expand=children($levels=5)', '''[{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "as": "children"}}], "as": "children"}}], "as": "children"}}], "as": "children"}}], "as": "children"}}]'''],
                ['$expand=children($levels=max)', '''[{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "as": "children"}}], "as": "children"}}], "as": "children"}}], "as": "children"}}], "as": "children"}}]'''],
                ['$expand=treeType2s($levels=5;$filter=index in (1, 2))', '''[{"$lookup": {"from": "MyService.TreeType2", "localField": "_id", "foreignField": "treeType1Id", "pipeline": [{"$match": {"$and": [{"index": {"$in": [1, 2]}}]}}, {"$lookup": {"from": "MyService.TreeType2", "localField": "_id", "foreignField": "treeType1Id", "pipeline": [{"$match": {"$and": [{"index": {"$in": [1, 2]}}]}}, {"$lookup": {"from": "MyService.TreeType2", "localField": "_id", "foreignField": "treeType1Id", "pipeline": [{"$match": {"$and": [{"index": {"$in": [1, 2]}}]}}, {"$lookup": {"from": "MyService.TreeType2", "localField": "_id", "foreignField": "treeType1Id", "pipeline": [{"$match": {"$and": [{"index": {"$in": [1, 2]}}]}}, {"$lookup": {"from": "MyService.TreeType2", "localField": "_id", "foreignField": "treeType1Id", "pipeline": [{"$match": {"$and": [{"index": {"$in": [1, 2]}}]}}], "as": "treeType2s"}}], "as": "treeType2s"}}], "as": "treeType2s"}}], "as": "treeType2s"}}], "as": "treeType2s"}}]'''],
                ['$expand=children($levels=3;$filter=index in (2, 3, 4))', '''[{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$match": {"$and": [{"index": {"$in": [2, 3, 4]}}]}}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$match": {"$and": [{"index": {"$in": [2, 3, 4]}}]}}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$match": {"$and": [{"index": {"$in": [2, 3, 4]}}]}}], "as": "children"}}], "as": "children"}}], "as": "children"}}]'''],
                ['$expand=children($levels=max;$orderby=index asc)', '''[{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}], "as": "children"}}], "as": "children"}}], "as": "children"}}], "as": "children"}}], "as": "children"}}]'''],
                ['$expand=children($levels=max;$top=2;$orderby=index asc)', '''[{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$limit": 2}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$limit": 2}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$limit": 2}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$limit": 2}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$limit": 2}], "as": "children"}}], "as": "children"}}], "as": "children"}}], "as": "children"}}], "as": "children"}}]'''],
                ['$expand=children($levels=max;$skip=1;$top=2;$orderby=index asc)', '''[{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$skip": 1}, {"$limit": 2}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$skip": 1}, {"$limit": 2}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$skip": 1}, {"$limit": 2}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$skip": 1}, {"$limit": 2}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$skip": 1}, {"$limit": 2}], "as": "children"}}], "as": "children"}}], "as": "children"}}], "as": "children"}}], "as": "children"}}]'''],
                ['$expand=children($levels=max;$skip=1;$top=2;$orderby=index asc;$select=index)', '''[{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$skip": 1}, {"$limit": 2}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$skip": 1}, {"$limit": 2}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$skip": 1}, {"$limit": 2}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$skip": 1}, {"$limit": 2}, {"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$sort": {"index": 1}}, {"$skip": 1}, {"$limit": 2}, {"$project": {"children": 1, "index": 1, "_id": 0}}, {"$unset": "_id"}], "as": "children"}}, {"$project": {"_id": 1, "children": 1, "index": 1}}, {"$unset": "_id"}], "as": "children"}}, {"$project": {"_id": 1, "children": 1, "index": 1}}, {"$unset": "_id"}], "as": "children"}}, {"$project": {"_id": 1, "children": 1, "index": 1}}, {"$unset": "_id"}], "as": "children"}}, {"$project": {"_id": 1, "children": 1, "index": 1}}, {"$unset": "_id"}], "as": "children"}}]''']
        ]
    }
}
