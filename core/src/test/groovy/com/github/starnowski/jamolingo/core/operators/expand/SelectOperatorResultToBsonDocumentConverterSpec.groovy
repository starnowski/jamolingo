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
            [itemName, selectedFields, bsonJson] << selectFieldsToBsonDocumentMappings()
    }

    @Unroll
    def "should convert SelectOperatorResult to BSON Document for \$map stage (#itemName, #selectedFields, #arraysFields)"() {
        given:
            def converter = new SelectOperatorResultToBsonDocumentConverter()
            def expectedBson = Document.parse(bsonJson)

        when:
            def result = converter.convert(selectedFields, itemName, arraysFields)

        then:
            result == expectedBson

        where:
            [itemName, selectedFields, arraysFields, bsonJson] << selectFieldsAndArraysFieldsToBsonDocumentMappings()
    }

    def "should handle empty selected fields"() {
        given:
            def converter = new SelectOperatorResultToBsonDocumentConverter()
            def itemName = "item"

        when:
            def result = converter.convert([], itemName)

        then:
            result == Document.parse("""
                {
                    "\$mergeObjects": [
                        "\$\$item",
                        {}
                    ]
                }
            """)
    }

    def "should handle null selected fields"() {
        given:
            def converter = new SelectOperatorResultToBsonDocumentConverter()

        when:
            def result = converter.convert(null, "item")

        then:
            result == Document.parse("""
                {
                    "\$mergeObjects": [
                        "\$\$item",
                        {}
                    ]
                }
            """)
    }

    static selectFieldsToBsonDocumentMappings() {
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

    static selectFieldsAndArraysFieldsToBsonDocumentMappings() {
        [
                [
                        "currentItem",
                        ["plainString", "Name", "Addresses.Street", "Addresses.ZipCode", "Addresses.BackUpAddresses.ZipCode"],
                        ["Addresses", "Addresses.BackUpAddresses"],
                        """
                            {
                                "plainString": "\$\$currentItem.plainString",
                                "Name": "\$\$currentItem.Name",
                                "Addresses": {
                                "\$map": {
                                "input": "\$\$currentItem.Addresses",
                                "as": "currentItem",
                                "in": {
                                    "Street": "\$\$currentItem.Street",
                                    "ZipCode": "\$\$currentItem.ZipCode",
                                    "BackUpAddresses": {
                                        "\$map": {
                                            "input": "\$\$currentItem.BackUpAddresses",
                                            "as": "currentItem",
                                            "in": {
                                                "ZipCode": "\$\$currentItem.ZipCode"
                                            }
                                        }
                                    }
                                    }
                                }
                                }
                            }
                        """
                ],
                // Array element contains complex type (Flat.number)
                [
                        "currentItem",
                        ["plainString", "Name", "Addresses.Street", "Addresses.Flat.number", "Addresses.ZipCode", "Addresses.BackUpAddresses.ZipCode"],
                        ["Addresses", "Addresses.BackUpAddresses"],
                        """
                            {
                                "plainString": "\$\$currentItem.plainString",
                                "Name": "\$\$currentItem.Name",
                                "Addresses": {
                                "\$map": {
                                "input": "\$\$currentItem.Addresses",
                                "as": "currentItem",
                                "in": {
                                    "Flat": { "number": "\$\$currentItem.Flat.number" },
                                    "Street": "\$\$currentItem.Street",
                                    "ZipCode": "\$\$currentItem.ZipCode",
                                    "BackUpAddresses": {
                                        "\$map": {
                                            "input": "\$\$currentItem.BackUpAddresses",
                                            "as": "currentItem",
                                            "in": {
                                                "ZipCode": "\$\$currentItem.ZipCode"
                                            }
                                        }
                                    }
                                    }
                                }
                                }
                            }
                        """
                ],
                // With single array property Addresses but with all properties from array
                [
                        "currentItem",
                        ["plainString", "Name", "Addresses"],
                        ["Addresses"],
                        """
                            {
                                "plainString": "\$\$currentItem.plainString",
                                "Name": "\$\$currentItem.Name",
                                "Addresses": "\$\$currentItem.Addresses"
                            }
                        """
                ]
        ]
    }
}
