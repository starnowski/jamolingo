package com.github.starnowski.jamolingo.core.utils

import com.github.starnowski.jamolingo.common.beans.KeyValue
import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade
import com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade
import spock.lang.Specification

class DefaultNavigationPropertyLookupContextTest extends Specification {

    def "should provide default values in NavigationPropertyLookupContext interface"() {
        given:
        NavigationPropertyLookupContext context = new NavigationPropertyLookupContext() {}

        expect:
        context.getRootEdmEntityTypeName() == null
        context.getRootEdmMongoContextFacade() == null
        context.getEdmTypeMapping() == [:]
        context.getEdmTablesToMongoDBCollections() == [:]
    }

    def "should build DefaultNavigationPropertyLookupContext and verify getters, copy builder, equals, hashCode, and toString"() {
        given:
        EdmMongoContextFacade rootFacade = DefaultEdmMongoContextFacade.builder().withRootMongoPath("root").build()
        EdmMongoContextFacade categoryFacade = DefaultEdmMongoContextFacade.builder().withRootMongoPath("cat").build()
        Map<String, EdmMongoContextFacade> typeMapping = ["MyService.Category": categoryFacade]
        Map<KeyValue<String, String>, String> collectionMapping = [(new KeyValue<>("MyService", "Category")): "categories_col"]

        when:
        def defaultContext = DefaultNavigationPropertyLookupContext.builder().build()
        def context1 = DefaultNavigationPropertyLookupContext.builder()
                .withRootEdmEntityTypeName("MyService.TreeType1")
                .withRootEdmMongoContextFacade(rootFacade)
                .withEdmTypeMapping(typeMapping)
                .withEdmTablesToMongoDBCollections(collectionMapping)
                .build()
        def contextCopy = DefaultNavigationPropertyLookupContext.builder()
                .withDefaultNavigationPropertyLookupContext(context1)
                .build()
        def diffRootName = DefaultNavigationPropertyLookupContext.builder()
                .withDefaultNavigationPropertyLookupContext(context1)
                .withRootEdmEntityTypeName("MyService.TreeType2")
                .build()
        def diffRootFacade = DefaultNavigationPropertyLookupContext.builder()
                .withDefaultNavigationPropertyLookupContext(context1)
                .withRootEdmMongoContextFacade(categoryFacade)
                .build()
        def diffTypeMapping = DefaultNavigationPropertyLookupContext.builder()
                .withDefaultNavigationPropertyLookupContext(context1)
                .withEdmTypeMapping([:])
                .build()
        def diffCollectionMapping = DefaultNavigationPropertyLookupContext.builder()
                .withDefaultNavigationPropertyLookupContext(context1)
                .withEdmTablesToMongoDBCollections([:])
                .build()

        then:
        defaultContext.getRootEdmEntityTypeName() == null
        defaultContext.getRootEdmMongoContextFacade() == null
        defaultContext.getEdmTypeMapping() == [:]
        defaultContext.getEdmTablesToMongoDBCollections() == [:]

        context1.getRootEdmEntityTypeName() == "MyService.TreeType1"
        context1.getRootEdmMongoContextFacade().is(rootFacade)
        context1.getEdmTypeMapping() == typeMapping
        context1.getEdmTablesToMongoDBCollections() == collectionMapping

        context1 == context1
        context1 == contextCopy
        context1.hashCode() == contextCopy.hashCode()
        context1 != diffRootName
        context1 != diffRootFacade
        context1 != diffTypeMapping
        context1 != diffCollectionMapping
        context1 != null
        context1 != "other"

        context1.toString().contains("rootEdmEntityTypeName='MyService.TreeType1'")
        context1.toString().contains("rootEdmMongoContextFacade=")
        context1.toString().contains("edmTypeMapping=")
        context1.toString().contains("edmTablesToMongoDBCollections=")
    }
}
