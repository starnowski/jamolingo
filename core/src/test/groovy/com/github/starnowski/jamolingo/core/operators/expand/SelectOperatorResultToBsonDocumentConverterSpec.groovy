package com.github.starnowski.jamolingo.core.operators.expand

import org.bson.Document
import spock.lang.Specification
import spock.lang.Unroll

class SelectOperatorResultToBsonDocumentConverterSpec extends Specification {

    @Unroll
    def "should convert SelectOperatorResult to BSON Document for \$map stage (#itemName, #selectedFields)"() {
        given:
        def converter = new SelectOperatorResultToBsonDocumentConverter()
        def expectedBson = Document.parse(bsonJson)

        when:
        def result = converter.convert(selectedFields, itemName)

        then:
        result == expectedBson

        where:
        [itemName, selectedFields, bsonJson] << selectOperatorResultToBsonDocumentMappings()
    }

    def "should handle empty selected fields"() {
        given:
        def converter = new SelectOperatorResultToBsonDocumentConverter()
        def itemName = "item"

        when:
        def result = converter.convert([], itemName)

        then:
        result != null
        result.isEmpty()
    }

    def "should handle null selected fields"() {
        given:
        def converter = new SelectOperatorResultToBsonDocumentConverter()

        when:
        def result = converter.convert(null, "item")

        then:
        result != null
        result.isEmpty()
    }

    static selectOperatorResultToBsonDocumentMappings() {
        [
                [
                        "currentItem",
                        [
                                "propertyAZB",
                                "propertyB",
                                "propertyC.name",
                                "propertyC.nestedObject",
                                "propertyC.nestedObject2._id",
                                "propertyC.nestedObject2.index",
                                "propertyC.nestedObject2.a"
                        ],
                        '{"propertyAZB": "$$currentItem.propertyAZB", "propertyB": "$$currentItem.propertyB", "propertyC": {"name": "$$currentItem.propertyC.name", "nestedObject": "$$currentItem.propertyC.nestedObject", "nestedObject2": {"_id": "$$currentItem.propertyC.nestedObject2._id", "index": "$$currentItem.propertyC.nestedObject2.index", "a": "$$currentItem.propertyC.nestedObject2.a"}}}'
                ],
                [
                        "item",
                        ["simpleProp"],
                        '{"simpleProp": "$$item.simpleProp"}'
                ]
        ]
    }
}
