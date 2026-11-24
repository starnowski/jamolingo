package com.github.starnowski.jamolingo.core.utils

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

class UsedNavigationPropertyUnsetStagesBuilderTest extends AbstractSpecification {

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
        def builder = new UsedNavigationPropertyUnsetStagesBuilder()
        def missingEdmAndMongoPath = UsedNavigationProperty.builder()
                .withEdmEntityFullName("MyService.Category")
                .withLocalKeyProperty("categoryId")
                .withForeignKeyProperty("_id")
                .build()
        def emptyEdmAndMongoPath = UsedNavigationProperty.builder()
                .withEdmPath("")
                .withMongoPath("")
                .withEdmEntityFullName("MyService.Category")
                .withLocalKeyProperty("categoryId")
                .withForeignKeyProperty("_id")
                .build()

        expect:
        builder.build((UsedNavigationProperty) null) == []
        builder.build((UsedNavigationProperty) null, null) == []
        builder.build((Collection<UsedNavigationProperty>) null) == []
        builder.build((Collection<UsedNavigationProperty>) null, null) == []
        builder.build([]) == []
        builder.build([null, missingEdmAndMongoPath, emptyEdmAndMongoPath]) == []
    }

    def "should build unset stage for single non-collection UsedNavigationProperty"() {
        given:
        def builder = new UsedNavigationPropertyUnsetStagesBuilder()
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
                '''{"$unset": ["category"]}'''
        ])
    }

    def "should build unset stage for single collection UsedNavigationProperty"() {
        given:
        def builder = new UsedNavigationPropertyUnsetStagesBuilder()
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
                '''{"$unset": ["children"]}'''
        ])
    }

    @Unroll
    def "should build expected unset stage for scenario: #scenario"() {
        given:
        def builder = new UsedNavigationPropertyUnsetStagesBuilder()

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
        def treeType3Nav = UsedNavigationProperty.builder()
                .withEdmPath("treeType3")
                .withMongoPath("treeType3")
                .withEdmPropertyName("treeType3")
                .withEdmEntityFullName("MyService.TreeType3")
                .withCollection(false)
                .withLocalKeyProperty("treeType3Id")
                .withForeignKeyProperty("_id")
                .build()
        def treeType2Nav = UsedNavigationProperty.builder()
                .withEdmPath("treeType3/treeType2")
                .withMongoPath("treeType3.treeType2")
                .withEdmPropertyName("treeType2")
                .withEdmEntityFullName("MyService.TreeType2")
                .withCollection(false)
                .withLocalKeyProperty("treeType2Id")
                .withForeignKeyProperty("_id")
                .build()
        def treeType1Nav = UsedNavigationProperty.builder()
                .withEdmPath("treeType3/treeType2/treeType1")
                .withMongoPath("treeType3.treeType2.treeType1")
                .withEdmPropertyName("treeType1")
                .withEdmEntityFullName("MyService.TreeType1")
                .withCollection(false)
                .withLocalKeyProperty("treeType1Id")
                .withForeignKeyProperty("_id")
                .build()
        def deepCategoryNav = UsedNavigationProperty.builder()
                .withEdmPath("treeType3/treeType2/treeType1/category")
                .withMongoPath("treeType3.treeType2.treeType1.category")
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
                                '''{"$unset": ["category", "children", "parent"]}'''
                        ]
                ],
                [
                        "multi-hop single-valued navigation chain (parent -> parent/category) collapsed to parent",
                        [parentNav, parentCategoryNav],
                        [
                                '''{"$unset": ["parent"]}'''
                        ]
                ],
                [
                        "multi-hop single-valued parent to collection child (parent -> parent/treeType2s) collapsed to parent",
                        [parentNav, parentTreeType2sNav],
                        [
                                '''{"$unset": ["parent"]}'''
                        ]
                ],
                [
                        "out-of-order multi-hop single-valued chain ([parent/category, parent]) collapsed to parent",
                        [parentCategoryNav, parentNav],
                        [
                                '''{"$unset": ["parent"]}'''
                        ]
                ],
                [
                        "deep multi-hop chain (treeType3 -> treeType2 -> treeType1 -> category) collapsed to treeType3",
                        [treeType3Nav, treeType2Nav, treeType1Nav, deepCategoryNav],
                        [
                                '''{"$unset": ["treeType3"]}'''
                        ]
                ],
                [
                        "multi-hop collection to single-valued navigation property (children -> children/category) collapsed to children",
                        [childrenNav, childrenCategoryNav],
                        [
                                '''{"$unset": ["children"]}'''
                        ]
                ],
                [
                        "multi-hop collection to collection to single-valued (children -> children/treeType2s -> children/treeType2s/category) collapsed to children",
                        [childrenNav, childrenTreeType2sNav, childrenTreeType2sCategoryNav],
                        [
                                '''{"$unset": ["children"]}'''
                        ]
                ],
                [
                        "partial nested hop only (parent/category without parent in unset list)",
                        [parentCategoryNav],
                        [
                                '''{"$unset": ["parent.category"]}'''
                        ]
                ],
                [
                        "complex-prefixed navigation property (details/manufacturer)",
                        [complexManufacturerNav],
                        [
                                '''{"$unset": ["custom_details.manufacturer"]}'''
                        ]
                ]
        ]
    }

    def "should resolve custom navigation property paths from NavigationPropertyLookupContext"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        ODataMongoMappingFactory factory = new ODataMongoMappingFactory()
        ODataMongoMapping odataMapping = factory.build(edm.getSchema("MyService"))
        EntityPropertiesMongoPathContextBuilder pathContextBuilder = new EntityPropertiesMongoPathContextBuilder()

        EntityMapping t1Mapping = odataMapping.getEntities().get("TreeType1")
        t1Mapping.getProperties().get("parent").setMongoPath("custom_parent")
        t1Mapping.getProperties().get("category").setMongoPath("custom_category")
        EdmMongoContextFacade t1Facade = DefaultEdmMongoContextFacade.builder()
                .withEntityPropertiesMongoPathContext(pathContextBuilder.build(t1Mapping))
                .build()

        NavigationPropertyLookupContext lookupContext = DefaultNavigationPropertyLookupContext.builder()
                .withRootEdmEntityTypeName("MyService.TreeType1")
                .withRootEdmMongoContextFacade(t1Facade)
                .withEdmTypeMapping([
                        "MyService.TreeType1": t1Facade
                ])
                .build()

        def parentNav = UsedNavigationProperty.builder()
                .withEdmPath("parent")
                .withMongoPath("parent")
                .withEdmPropertyName("parent")
                .withEdmEntityFullName("MyService.TreeType1")
                .withCollection(false)
                .build()
        def parentCategoryNav = UsedNavigationProperty.builder()
                .withEdmPath("parent/category")
                .withMongoPath("parent.category")
                .withEdmPropertyName("category")
                .withEdmEntityFullName("MyService.Category")
                .withCollection(false)
                .build()
        def categoryNav = UsedNavigationProperty.builder()
                .withEdmPath("category")
                .withMongoPath("category")
                .withEdmPropertyName("category")
                .withEdmEntityFullName("MyService.Category")
                .withCollection(false)
                .build()

        def builder = new UsedNavigationPropertyUnsetStagesBuilder()

        when:
        def stages = builder.build([parentNav, parentCategoryNav, categoryNav], lookupContext)

        then:
        toNormalizedJsonList(stages) == parseExpectedJsonList([
                '''{"$unset": ["custom_parent", "custom_category"]}'''
        ])
    }

    def "should build unset stages directly from ODataFilterToMongoMatchParser, OdataOrderByToMongoSortParser, and ODataApplyToMongoAggregationPipelineParser results"() {
        given:
        Edm edm = loadEmdProvider("edm/edm_tree.xml")
        def unsetBuilder = new UsedNavigationPropertyUnsetStagesBuilder()
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

        def filterUnsetStages = unsetBuilder.build(filterResult.getUsedNavigationProperties())
        def orderByUnsetStages = unsetBuilder.build(orderByResult.getUsedNavigationProperties())
        def applyUnsetStages = unsetBuilder.build(applyResult.getUsedNavigationProperties())

        then:
        toNormalizedJsonList(filterUnsetStages) == parseExpectedJsonList([
                '''{"$unset": ["treeType3"]}'''
        ])
        toNormalizedJsonList(orderByUnsetStages) == parseExpectedJsonList([
                '''{"$unset": ["parent"]}'''
        ])
        toNormalizedJsonList(applyUnsetStages) == parseExpectedJsonList([
                '''{"$unset": ["children"]}'''
        ])
    }
}
