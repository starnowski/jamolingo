package com.github.starnowski.jamolingo.core.operators.apply

import spock.lang.Specification

class DefaultApplyParserContextTest extends Specification {

    def "should correctly set and get all properties via builder"() {
        given:
        def rootMongoPath = "children"
        def idProperties = ["_id", "tenantId"] as Set
        def mongoConnectTo = "parentId"
        def depthVariable = "children___odata_depth"

        when:
        def context = DefaultApplyParserContext.builder()
                .withRootMongoPath(rootMongoPath)
                .withIdProperties(idProperties)
                .withMongoConnectTo(mongoConnectTo)
                .withDepthVariable(depthVariable)
                .build()

        then:
        context.getRootMongoPath() == rootMongoPath
        context.getIdProperties() == idProperties
        context.getMongoConnectTo() == mongoConnectTo
        context.getDepthVariable() == depthVariable
    }

    def "should correctly copy properties from another instance via withDefaultApplyParserContext"() {
        given:
        def original = DefaultApplyParserContext.builder()
                .withRootMongoPath("children")
                .withIdProperties(["_id"] as Set)
                .withMongoConnectTo("parentId")
                .withDepthVariable("children___odata_depth")
                .build()

        when:
        def copy = DefaultApplyParserContext.builder()
                .withDefaultApplyParserContext(original)
                .build()

        then:
        copy.getRootMongoPath() == original.getRootMongoPath()
        copy.getIdProperties() == original.getIdProperties()
        copy.getMongoConnectTo() == original.getMongoConnectTo()
        copy.getDepthVariable() == original.getDepthVariable()
        copy == original
        copy.hashCode() == original.hashCode()
    }

    def "should correctly implement equals, hashCode and toString"() {
        given:
        def context1 = DefaultApplyParserContext.builder()
                .withRootMongoPath("children")
                .withIdProperties(["_id"] as Set)
                .withMongoConnectTo("parentId")
                .withDepthVariable("children___odata_depth")
                .build()

        def context2 = DefaultApplyParserContext.builder()
                .withRootMongoPath("children")
                .withIdProperties(["_id"] as Set)
                .withMongoConnectTo("parentId")
                .withDepthVariable("children___odata_depth")
                .build()

        def different = DefaultApplyParserContext.builder()
                .withRootMongoPath("other")
                .build()

        expect:
        context1 == context2
        context1 == context1
        context1 != null
        context1 != "some string"
        context1 != different
        context1.hashCode() == context2.hashCode()
        context1.toString().contains("rootMongoPath='children'")
        context1.toString().contains("mongoConnectTo='parentId'")
        context1.toString().contains("depthVariable='children___odata_depth'")
    }

    def "should handle null idProperties gracefully and provide empty set"() {
        when:
        def context = new DefaultApplyParserContext("children", null, "parentId", "depth")

        then:
        context.getIdProperties() != null
        context.getIdProperties().isEmpty()
    }
}
