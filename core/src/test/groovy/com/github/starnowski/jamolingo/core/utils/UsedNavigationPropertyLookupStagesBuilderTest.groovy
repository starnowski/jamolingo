package com.github.starnowski.jamolingo.core.utils

import com.github.starnowski.jamolingo.common.beans.KeyValue
import com.github.starnowski.jamolingo.core.AbstractSpecification
import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade
import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade
import com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContextBuilder
import com.github.starnowski.jamolingo.core.mapping.EntityMapping
import com.github.starnowski.jamolingo.core.mapping.ODataMongoMapping
import com.github.starnowski.jamolingo.core.mapping.ODataMongoMappingFactory
import com.github.starnowski.jamolingo.core.operators.UsedNavigationProperty
import com.github.starnowski.jamolingo.core.operators.apply.ODataApplyToMongoAggregationPipelineParser
import com.github.starnowski.jamolingo.core.operators.filter.ODataFilterToMongoMatchParser
import com.github.starnowski.jamolingo.core.operators.orderby.OdataOrderByToMongoSortParser
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
import org.bson.conversions.Bson
import org.bson.json.JsonWriterSettings
import spock.lang.Unroll

class UsedNavigationPropertyLookupStagesBuilderTest extends AbstractSpecification {

    private static List<String> toNormalizedJsonList(List<Bson> stages) {
        JsonWriterSettings settings = JsonWriterSettings.builder().build()
        CodecRegistry registry = CodecRegistries.fromRegistries(
                CodecRegistries.fromProviders(new UuidCodecProvider(UuidRepresentation.STANDARD)),
                MongoClientSettings.getDefaultCodecRegistry()
        )
        DocumentCodec codec = new DocumentCodec(registry)
        return stages.collect {
            Document.parse(it.toBsonDocument(Document.class, registry).toJson(settings)).toJson(settings, codec)
        }
    }

    private static List<String> parseExpectedJsonList(List<String> expectedJsonStages) {
        JsonWriterSettings settings = JsonWriterSettings.builder().build()
        CodecRegistry registry = CodecRegistries.fromRegistries(
                CodecRegistries.fromProviders(new UuidCodecProvider(UuidRepresentation.STANDARD)),
                MongoClientSettings.getDefaultCodecRegistry()
        )
        DocumentCodec codec = new DocumentCodec(registry)
        return expectedJsonStages.collect { Document.parse(it).toJson(settings, codec) }
    }

    def "should return empty list for null, empty, or incomplete UsedNavigationProperty inputs"() {
        given:
        def builder = new UsedNavigationPropertyLookupStagesBuilder()
        def missingEdmPath = UsedNavigationProperty.builder()
                .withMongoPath("category")
                .withEdmEntityFullName("MyService.Category")
                .withLocalKeyProperty("categoryId")
                .withForeignKeyProperty("_id")
                .build()
        def emptyEdmPath = UsedNavigationProperty.builder()
                .withEdmPath("")
                .withMongoPath("category")
                .withEdmEntityFullName("MyService.Category")
                .withLocalKeyProperty("categoryId")
                .withForeignKeyProperty("_id")
                .build()
        def missingLocalKey = UsedNavigationProperty.builder()
                .withEdmPath("category")
                .withMongoPath("category")
                .withEdmEntityFullName("MyService.Category")
                .withForeignKeyProperty("_id")
                .build()
        def missingForeignKey = UsedNavigationProperty.builder()
                .withEdmPath("category")
                .withMongoPath("category")
                .withEdmEntityFullName("MyService.Category")
                .withLocalKeyProperty("categoryId")
                .build()

        expect:
        builder.build((UsedNavigationProperty) null) == []
        builder.build((UsedNavigationProperty) null, null) == []
        builder.build((Collection<UsedNavigationProperty>) null) == []
        builder.build((Collection<UsedNavigationProperty>) null, null) == []
        builder.build([]) == []
        builder.build([null, missingEdmPath, emptyEdmPath, missingLocalKey, missingForeignKey]) == []
    }

    def "should build lookup and unwind stages for single non-collection UsedNavigationProperty"() {
        given:
        def builder = new UsedNavigationPropertyLookupStagesBuilder()
        def categoryNav = UsedNavigationProperty.builder()
                .withEdmPath("category")
                .withMongoPath("category")
                .withEdmPropertyName("category")
                .withEdmEntityFullName("MyService.Category")
                .withCollection(false)
                .withLocalKeyProperty("categoryId")
                .withForeignKeyProperty("_id")
                .build()

        when:
        def stages = builder.build(categoryNav)

        then:
        toNormalizedJsonList(stages) == parseExpectedJsonList([
                '''{"$lookup": {"from": "MyService.Category", "localField": "categoryId", "foreignField": "_id", "as": "category"}}''',
                '''{"$unwind": {"path": "$category", "preserveNullAndEmptyArrays": true}}'''
        ])
    }

    def "should build only lookup stage without unwind for single collection UsedNavigationProperty"() {
        given:
        def builder = new UsedNavigationPropertyLookupStagesBuilder()
        def childrenNav = UsedNavigationProperty.builder()
                .withEdmPath("children")
                .withMongoPath("children")
                .withEdmPropertyName("children")
                .withEdmEntityFullName("MyService.TreeType1")
                .withCollection(true)
                .withLocalKeyProperty("_id")
                .withForeignKeyProperty("parentId")
                .build()

        when:
        def stages = builder.build(childrenNav)

        then:
        toNormalizedJsonList(stages) == parseExpectedJsonList([
                '''{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "as": "children"}}'''
        ])
    }

    @Unroll
    def "should build expected lookup and unwind stages for scenario: #scenario"() {
        given:
        def builder = new UsedNavigationPropertyLookupStagesBuilder()

        when:
        def stages = builder.build(inputProps)

        then:
        toNormalizedJsonList(stages) == parseExpectedJsonList(expectedStages)

        where:
        [scenario, inputProps, expectedStages] << stageGenerationScenarios()
    }

    static stageGenerationScenarios() {
        def categoryNav = UsedNavigationProperty.builder()
                .withEdmPath("category")
                .withMongoPath("category")
                .withEdmPropertyName("category")
                .withEdmEntityFullName("MyService.Category")
                .withCollection(false)
                .withLocalKeyProperty("categoryId")
                .withForeignKeyProperty("_id")
                .build()
        def parentNav = UsedNavigationProperty.builder()
                .withEdmPath("parent")
                .withMongoPath("parent")
                .withEdmPropertyName("parent")
                .withEdmEntityFullName("MyService.TreeType1")
                .withCollection(false)
                .withLocalKeyProperty("parentId")
                .withForeignKeyProperty("_id")
                .build()
        def parentCategoryNav = UsedNavigationProperty.builder()
                .withEdmPath("parent/category")
                .withMongoPath("parent.category")
                .withEdmPropertyName("category")
                .withEdmEntityFullName("MyService.Category")
                .withCollection(false)
                .withLocalKeyProperty("categoryId")
                .withForeignKeyProperty("_id")
                .build()
        def parentTreeType2sNav = UsedNavigationProperty.builder()
                .withEdmPath("parent/treeType2s")
                .withMongoPath("parent.treeType2s")
                .withEdmPropertyName("treeType2s")
                .withEdmEntityFullName("MyService.TreeType2")
                .withCollection(true)
                .withLocalKeyProperty("_id")
                .withForeignKeyProperty("treeType1Id")
                .build()
        def childrenNav = UsedNavigationProperty.builder()
                .withEdmPath("children")
                .withMongoPath("children")
                .withEdmPropertyName("children")
                .withEdmEntityFullName("MyService.TreeType1")
                .withCollection(true)
                .withLocalKeyProperty("_id")
                .withForeignKeyProperty("parentId")
                .build()
        def childrenCategoryNav = UsedNavigationProperty.builder()
                .withEdmPath("children/category")
                .withMongoPath("children.category")
                .withEdmPropertyName("category")
                .withEdmEntityFullName("MyService.Category")
                .withCollection(false)
                .withLocalKeyProperty("categoryId")
                .withForeignKeyProperty("_id")
                .build()
        def childrenTreeType2sNav = UsedNavigationProperty.builder()
                .withEdmPath("children/treeType2s")
                .withMongoPath("children.treeType2s")
                .withEdmPropertyName("treeType2s")
                .withEdmEntityFullName("MyService.TreeType2")
                .withCollection(true)
                .withLocalKeyProperty("_id")
                .withForeignKeyProperty("treeType1Id")
                .build()
        def childrenTreeType2sCategoryNav = UsedNavigationProperty.builder()
                .withEdmPath("children/treeType2s/category")
                .withMongoPath("children.treeType2s.category")
                .withEdmPropertyName("category")
                .withEdmEntityFullName("MyService.Category")
                .withCollection(false)
                .withLocalKeyProperty("categoryId")
                .withForeignKeyProperty("_id")
                .build()
        def childrenParentNav = UsedNavigationProperty.builder()
                .withEdmPath("children/parent")
                .withMongoPath("children.parent")
                .withEdmPropertyName("parent")
                .withEdmEntityFullName("MyService.TreeType1")
                .withCollection(false)
                .withLocalKeyProperty("parentId")
                .withForeignKeyProperty("_id")
                .build()
        def childrenParentCategoryNav = UsedNavigationProperty.builder()
                .withEdmPath("children/parent/category")
                .withMongoPath("children.parent.category")
                .withEdmPropertyName("category")
                .withEdmEntityFullName("MyService.Category")
                .withCollection(false)
                .withLocalKeyProperty("categoryId")
                .withForeignKeyProperty("_id")
                .build()
        def complexManufacturerNav = UsedNavigationProperty.builder()
                .withEdmPath("details/manufacturer")
                .withMongoPath("custom_details.manufacturer")
                .withEdmPropertyName("manufacturer")
                .withEdmEntityFullName("MyService.Manufacturer")
                .withCollection(false)
                .withLocalKeyProperty("manufacturerId")
                .withForeignKeyProperty("_id")
                .build()

        [
                [
                        "multiple independent navigation properties with deduplication",
                        [categoryNav, childrenNav, categoryNav, parentNav, childrenNav],
                        [
                                '''{"$lookup": {"from": "MyService.Category", "localField": "categoryId", "foreignField": "_id", "as": "category"}}''',
                                '''{"$unwind": {"path": "$category", "preserveNullAndEmptyArrays": true}}''',
                                '''{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "as": "children"}}''',
                                '''{"$lookup": {"from": "MyService.TreeType1", "localField": "parentId", "foreignField": "_id", "as": "parent"}}''',
                                '''{"$unwind": {"path": "$parent", "preserveNullAndEmptyArrays": true}}'''
                        ]
                ],
                [
                        "multi-hop single-valued navigation chain (parent -> parent/category)",
                        [parentNav, parentCategoryNav],
                        [
                                '''{"$lookup": {"from": "MyService.TreeType1", "localField": "parentId", "foreignField": "_id", "as": "parent"}}''',
                                '''{"$unwind": {"path": "$parent", "preserveNullAndEmptyArrays": true}}''',
                                '''{"$lookup": {"from": "MyService.Category", "localField": "parent.categoryId", "foreignField": "_id", "as": "parent.category"}}''',
                                '''{"$unwind": {"path": "$parent.category", "preserveNullAndEmptyArrays": true}}'''
                        ]
                ],
                [
                        "multi-hop single-valued parent to collection child (parent -> parent/treeType2s)",
                        [parentNav, parentTreeType2sNav],
                        [
                                '''{"$lookup": {"from": "MyService.TreeType1", "localField": "parentId", "foreignField": "_id", "as": "parent"}}''',
                                '''{"$unwind": {"path": "$parent", "preserveNullAndEmptyArrays": true}}''',
                                '''{"$lookup": {"from": "MyService.TreeType2", "localField": "parent._id", "foreignField": "treeType1Id", "as": "parent.treeType2s"}}'''
                        ]
                ],
                [
                        "out-of-order multi-hop single-valued chain ([parent/category, parent])",
                        [parentCategoryNav, parentNav],
                        [
                                '''{"$lookup": {"from": "MyService.TreeType1", "localField": "parentId", "foreignField": "_id", "as": "parent"}}''',
                                '''{"$unwind": {"path": "$parent", "preserveNullAndEmptyArrays": true}}''',
                                '''{"$lookup": {"from": "MyService.Category", "localField": "parent.categoryId", "foreignField": "_id", "as": "parent.category"}}''',
                                '''{"$unwind": {"path": "$parent.category", "preserveNullAndEmptyArrays": true}}'''
                        ]
                ],
                [
                        "multi-hop collection to single-valued navigation property (children -> children/category)",
                        [childrenNav, childrenCategoryNav],
                        [
                                '''{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$lookup": {"from": "MyService.Category", "localField": "categoryId", "foreignField": "_id", "as": "category"}}, {"$unwind": {"path": "$category", "preserveNullAndEmptyArrays": true}}], "as": "children"}}'''
                        ]
                ],
                [
                        "multi-hop collection to collection to single-valued (children -> children/treeType2s -> children/treeType2s/category)",
                        [childrenNav, childrenTreeType2sNav, childrenTreeType2sCategoryNav],
                        [
                                '''{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$lookup": {"from": "MyService.TreeType2", "localField": "_id", "foreignField": "treeType1Id", "pipeline": [{"$lookup": {"from": "MyService.Category", "localField": "categoryId", "foreignField": "_id", "as": "category"}}, {"$unwind": {"path": "$category", "preserveNullAndEmptyArrays": true}}], "as": "treeType2s"}}], "as": "children"}}'''
                        ]
                ],
                [
                        "multi-hop collection to two-hop single-valued chain (children -> children/parent -> children/parent/category) out-of-order",
                        [childrenParentCategoryNav, childrenParentNav, childrenNav],
                        [
                                '''{"$lookup": {"from": "MyService.TreeType1", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$lookup": {"from": "MyService.TreeType1", "localField": "parentId", "foreignField": "_id", "as": "parent"}}, {"$unwind": {"path": "$parent", "preserveNullAndEmptyArrays": true}}, {"$lookup": {"from": "MyService.Category", "localField": "parent.categoryId", "foreignField": "_id", "as": "parent.category"}}, {"$unwind": {"path": "$parent.category", "preserveNullAndEmptyArrays": true}}], "as": "children"}}'''
                        ]
                ],
                [
                        "complex-prefixed navigation property (details/manufacturer)",
                        [complexManufacturerNav],
                        [
                                '''{"$lookup": {"from": "MyService.Manufacturer", "localField": "custom_details.manufacturerId", "foreignField": "_id", "as": "custom_details.manufacturer"}}''',
                                '''{"$unwind": {"path": "$custom_details.manufacturer", "preserveNullAndEmptyArrays": true}}'''
                        ]
                ]
        ]
    }

    def "should resolve custom collection names and custom property mappings from NavigationPropertyLookupContext"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        ODataMongoMappingFactory factory = new ODataMongoMappingFactory()
        ODataMongoMapping odataMapping = factory.build(edm.getSchema("MyService"))
        EntityPropertiesMongoPathContextBuilder pathContextBuilder = new EntityPropertiesMongoPathContextBuilder()

        EntityMapping categoryMapping = odataMapping.getEntities().get("Category")
        categoryMapping.getProperties().get("_id").setMongoName("cat_id")
        categoryMapping.getProperties().get("name").setMongoName("cat_name")
        EdmMongoContextFacade categoryFacade = DefaultEdmMongoContextFacade.builder()
                .withEntityPropertiesMongoPathContext(pathContextBuilder.build(categoryMapping))
                .build()

        EntityMapping t1Mapping = odataMapping.getEntities().get("TreeType1")
        t1Mapping.getProperties().get("_id").setMongoName("t1_id")
        t1Mapping.getProperties().get("parentId").setMongoName("t1_parentId")
        t1Mapping.getProperties().get("categoryId").setMongoName("t1_categoryId")
        EdmMongoContextFacade t1Facade = DefaultEdmMongoContextFacade.builder()
                .withEntityPropertiesMongoPathContext(pathContextBuilder.build(t1Mapping))
                .build()

        NavigationPropertyLookupContext lookupContext = DefaultNavigationPropertyLookupContext.builder()
                .withRootEdmEntityTypeName("MyService.TreeType1")
                .withRootEdmMongoContextFacade(t1Facade)
                .withEdmTypeMapping([
                        "MyService.TreeType1": t1Facade,
                        "MyService.Category": categoryFacade
                ])
                .withEdmTablesToMongoDBCollections([
                        (new KeyValue<>("MyService", "TreeType1")): "custom_t1_col",
                        (new KeyValue<>("MyService", "Category")): "custom_cat_col"
                ])
                .build()

        def parentNav = UsedNavigationProperty.builder()
                .withEdmPath("parent")
                .withMongoPath("parent")
                .withEdmPropertyName("parent")
                .withEdmEntityFullName("MyService.TreeType1")
                .withCollection(false)
                .withLocalKeyProperty("parentId")
                .withForeignKeyProperty("_id")
                .build()
        def parentCategoryNav = UsedNavigationProperty.builder()
                .withEdmPath("parent/category")
                .withMongoPath("parent.category")
                .withEdmPropertyName("category")
                .withEdmEntityFullName("MyService.Category")
                .withCollection(false)
                .withLocalKeyProperty("categoryId")
                .withForeignKeyProperty("_id")
                .build()
        def childrenNav = UsedNavigationProperty.builder()
                .withEdmPath("children")
                .withMongoPath("children")
                .withEdmPropertyName("children")
                .withEdmEntityFullName("MyService.TreeType1")
                .withCollection(true)
                .withLocalKeyProperty("_id")
                .withForeignKeyProperty("parentId")
                .build()
        def childrenCategoryNav = UsedNavigationProperty.builder()
                .withEdmPath("children/category")
                .withMongoPath("children.category")
                .withEdmPropertyName("category")
                .withEdmEntityFullName("MyService.Category")
                .withCollection(false)
                .withLocalKeyProperty("categoryId")
                .withForeignKeyProperty("_id")
                .build()

        def builder = new UsedNavigationPropertyLookupStagesBuilder()

        when:
        def stages = builder.build([parentNav, parentCategoryNav, childrenNav, childrenCategoryNav], lookupContext)

        then:
        toNormalizedJsonList(stages) == parseExpectedJsonList([
                '''{"$lookup": {"from": "custom_t1_col", "localField": "t1_parentId", "foreignField": "t1_id", "as": "parent"}}''',
                '''{"$unwind": {"path": "$parent", "preserveNullAndEmptyArrays": true}}''',
                '''{"$lookup": {"from": "custom_cat_col", "localField": "parent.t1_categoryId", "foreignField": "cat_id", "as": "parent.category"}}''',
                '''{"$unwind": {"path": "$parent.category", "preserveNullAndEmptyArrays": true}}''',
                '''{"$lookup": {"from": "custom_t1_col", "localField": "t1_id", "foreignField": "t1_parentId", "pipeline": [{"$lookup": {"from": "custom_cat_col", "localField": "t1_categoryId", "foreignField": "cat_id", "as": "category"}}, {"$unwind": {"path": "$category", "preserveNullAndEmptyArrays": true}}], "as": "children"}}'''
        ])
    }

    def "should build lookup stages directly from ODataFilterToMongoMatchParser, OdataOrderByToMongoSortParser, and ODataApplyToMongoAggregationPipelineParser results"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        def lookupBuilder = new UsedNavigationPropertyLookupStagesBuilder()
        def filterParser = new ODataFilterToMongoMatchParser()
        def orderByParser = new OdataOrderByToMongoSortParser()
        def applyParser = new ODataApplyToMongoAggregationPipelineParser()

        UriInfo uriInfo = new Parser(edm, OData.newInstance())
                .parseUri(
                        "treeType4s",
                        "\$filter=treeType3/treeType2/treeType1/category/name eq 'Root'&\$orderby=parent/index desc&\$apply=filter(children/any(c:c/category/name eq 'Leaf'))",
                        null,
                        null)

        when:
        def filterResult = filterParser.parse(uriInfo.getFilterOption())
        def orderByResult = orderByParser.parse(uriInfo.getOrderByOption())
        def applyResult = applyParser.parse(uriInfo.getApplyOption(), DefaultEdmMongoContextFacade.builder().build())

        def filterLookupStages = lookupBuilder.build(filterResult.getUsedNavigationProperties())
        def orderByLookupStages = lookupBuilder.build(orderByResult.getUsedNavigationProperties())
        def applyLookupStages = lookupBuilder.build(applyResult.getUsedNavigationProperties())

        then:
        toNormalizedJsonList(filterLookupStages) == parseExpectedJsonList([
                '''{"$lookup": {"from": "MyService.TreeType3", "localField": "treeType3Id", "foreignField": "_id", "as": "treeType3"}}''',
                '''{"$unwind": {"path": "$treeType3", "preserveNullAndEmptyArrays": true}}''',
                '''{"$lookup": {"from": "MyService.TreeType2", "localField": "treeType3.treeType2Id", "foreignField": "_id", "as": "treeType3.treeType2"}}''',
                '''{"$unwind": {"path": "$treeType3.treeType2", "preserveNullAndEmptyArrays": true}}''',
                '''{"$lookup": {"from": "MyService.TreeType1", "localField": "treeType3.treeType2.treeType1Id", "foreignField": "_id", "as": "treeType3.treeType2.treeType1"}}''',
                '''{"$unwind": {"path": "$treeType3.treeType2.treeType1", "preserveNullAndEmptyArrays": true}}''',
                '''{"$lookup": {"from": "MyService.Category", "localField": "treeType3.treeType2.treeType1.categoryId", "foreignField": "_id", "as": "treeType3.treeType2.treeType1.category"}}''',
                '''{"$unwind": {"path": "$treeType3.treeType2.treeType1.category", "preserveNullAndEmptyArrays": true}}'''
        ])
        toNormalizedJsonList(orderByLookupStages) == parseExpectedJsonList([
                '''{"$lookup": {"from": "MyService.TreeType4", "localField": "parentId", "foreignField": "_id", "as": "parent"}}''',
                '''{"$unwind": {"path": "$parent", "preserveNullAndEmptyArrays": true}}'''
        ])
        toNormalizedJsonList(applyLookupStages) == parseExpectedJsonList([
                '''{"$lookup": {"from": "MyService.TreeType4", "localField": "_id", "foreignField": "parentId", "pipeline": [{"$lookup": {"from": "MyService.Category", "localField": "categoryId", "foreignField": "_id", "as": "category"}}, {"$unwind": {"path": "$category", "preserveNullAndEmptyArrays": true}}], "as": "children"}}'''
        ])
    }
}
