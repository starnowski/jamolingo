package com.github.starnowski.jamolingo.core.operators.expand

import com.github.starnowski.jamolingo.core.operators.select.SelectOperatorResult
import org.bson.Document
import spock.lang.Specification

class SelectOperatorResultToBsonDocumentConverterSpec extends Specification {

    def "should convert SelectOperatorResult to BSON Document for \$map stage"() {
        given:
        def converter = new SelectOperatorResultToBsonDocumentConverter()
        def selectOperatorResult = Mock(SelectOperatorResult)
        def itemName = "currentItem"
        def selectedFields = [
                "propertyAZB",
                "propertyB",
                "propertyC.name",
                "propertyC.nestedObject",
                "propertyC.nestedObject2._id",
                "propertyC.nestedObject2.index",
                "propertyC.nestedObject2.a"
        ] as Set

        when:
        def result = converter.convert(selectOperatorResult, itemName)

        then:
        1 * selectOperatorResult.getSelectedFields() >> selectedFields
        result != null
        result.get("propertyAZB") == '$$currentItem.propertyAZB'
        result.get("propertyB") == '$$currentItem.propertyB'
        
        def propertyC = result.get("propertyC") as Document
        propertyC != null
        propertyC.get("name") == '$$currentItem.name'
        propertyC.get("nestedObject") == '$$currentItem.nestedObject'
        
        def nestedObject2 = propertyC.get("nestedObject2") as Document
        nestedObject2 != null
        nestedObject2.get("_id") == '$$currentItem._id'
        nestedObject2.get("index") == '$$currentItem.index'
        nestedObject2.get("a") == '$$currentItem.a'
    }

    def "should handle empty selected fields"() {
        given:
        def converter = new SelectOperatorResultToBsonDocumentConverter()
        def selectOperatorResult = Mock(SelectOperatorResult)
        def itemName = "item"

        when:
        def result = converter.convert(selectOperatorResult, itemName)

        then:
        1 * selectOperatorResult.getSelectedFields() >> ([] as Set)
        result != null
        result.isEmpty()
    }

    def "should handle null SelectOperatorResult"() {
        given:
        def converter = new SelectOperatorResultToBsonDocumentConverter()

        when:
        def result = converter.convert(null, "item")

        then:
        result != null
        result.isEmpty()
    }
}
