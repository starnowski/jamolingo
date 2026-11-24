package com.github.starnowski.jamolingo.core.operators.apply

import com.github.starnowski.jamolingo.core.operators.UsedNavigationProperty
import org.bson.Document
import spock.lang.Specification

class DefaultApplyOperatorResultTest extends Specification {

    def "should build DefaultApplyOperatorResult with usedNavigationProperties and verify getters, equals, hashCode, and toString"() {
        given:
        def stage = new Document('$match', new Document('name', 'Test'))
        def navProp = UsedNavigationProperty.builder()
                .withEdmPath("category")
                .withMongoPath("category")
                .withEdmPropertyName("category")
                .withEdmEntityFullName("MyService.Category")
                .withCollection(false)
                .withLocalKeyProperty("categoryId")
                .withForeignKeyProperty("_id")
                .build()

        when:
        def result1 = DefaultApplyOperatorResult.builder()
                .withStageObjects([stage])
                .withUsedMongoDocumentProperties(["category.name"])
                .withWrittenMongoDocumentProperties(["writtenProp"])
                .withAddedMongoDocumentProperties(["addedProp"])
                .withRemovedMongoDocumentProperties(["removedProp"])
                .withDocumentShapeRedefined(true)
                .withUsedNavigationProperties([navProp])
                .build()
        def resultCopy = DefaultApplyOperatorResult.builder()
                .withDefaultApplyOperatorResult(result1)
                .build()
        def resultDifferent = DefaultApplyOperatorResult.builder()
                .withDefaultApplyOperatorResult(result1)
                .withUsedNavigationProperties([])
                .build()

        then:
        result1.getStageObjects() == [stage]
        result1.getUsedMongoDocumentProperties() == ["category.name"]
        result1.getWrittenMongoDocumentProperties() == ["writtenProp"]
        result1.getAddedMongoDocumentProperties() == ["addedProp"]
        result1.getRemovedMongoDocumentProperties() == ["removedProp"]
        result1.isDocumentShapeRedefined()
        result1.getUsedNavigationProperties() == [navProp]

        result1 == result1
        result1 == resultCopy
        result1.hashCode() == resultCopy.hashCode()
        result1 != resultDifferent
        result1 != null

        result1.toString().contains("usedNavigationProperties=")
        result1.toString().contains("edmPath='category'")
    }
}
