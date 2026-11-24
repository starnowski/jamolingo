package com.github.starnowski.jamolingo.core.operators

import spock.lang.Specification

class UsedNavigationPropertyTest extends Specification {

    def "should build UsedNavigationProperty and verify getters, equals, hashCode, and toString"() {
        when:
        def prop1 = UsedNavigationProperty.builder()
                .withEdmPath("Order/Customer")
                .withMongoPath("Order.Customer")
                .withEdmPropertyName("Customer")
                .withEdmEntityFullName("MyService.Customer")
                .withCollection(false)
                .withLocalKeyProperty("customerId")
                .withForeignKeyProperty("_id")
                .build()
        def propCopy = UsedNavigationProperty.builder()
                .withUsedNavigationProperty(prop1)
                .build()
        def propDifferent = UsedNavigationProperty.builder()
                .withUsedNavigationProperty(prop1)
                .withCollection(true)
                .build()

        then:
        prop1.getEdmPath() == "Order/Customer"
        prop1.getMongoPath() == "Order.Customer"
        prop1.getEdmPropertyName() == "Customer"
        prop1.getEdmEntityFullName() == "MyService.Customer"
        !prop1.isCollection()
        prop1.getLocalKeyProperty() == "customerId"
        prop1.getForeignKeyProperty() == "_id"

        prop1 == prop1
        prop1 == propCopy
        prop1.hashCode() == propCopy.hashCode()
        prop1 != propDifferent
        prop1 != null
        prop1 != "other"

        prop1.toString().contains("edmPath='Order/Customer'")
        prop1.toString().contains("mongoPath='Order.Customer'")
        prop1.toString().contains("edmPropertyName='Customer'")
        prop1.toString().contains("edmEntityFullName='MyService.Customer'")
        prop1.toString().contains("collection=false")
        prop1.toString().contains("localKeyProperty='customerId'")
        prop1.toString().contains("foreignKeyProperty='_id'")
    }
}
