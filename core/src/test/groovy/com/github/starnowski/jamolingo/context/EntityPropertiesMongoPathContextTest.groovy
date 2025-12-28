package com.github.starnowski.jamolingo.context

import spock.lang.Specification
import spock.lang.Unroll

class EntityPropertiesMongoPathContextTest extends Specification {

    @Unroll
    def "should find mongo path for edm path based on EDM mapping one-to-one with Mongo Document"() {
        given:
            def mappings = Map.ofEntries(
                    Map.entry("plainString", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("plainString").withMongoPath("plainString").build()),
                    Map.entry("Name", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Name").withMongoPath("Name").build()),
                    Map.entry("Addresses", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses").withMongoPath("Addresses").build()),
                    Map.entry("Addresses/Street", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses/Street").withMongoPath("Addresses.Street").build()),
                    Map.entry("Addresses/City", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses/City").withMongoPath("Addresses.City").build()),
                    Map.entry("Addresses/ZipCode", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses/ZipCode").withMongoPath("Addresses.ZipCode").build()))
            def tested = new EntityPropertiesMongoPathContext(mappings)

        when:
            def result = tested.resolveMongoPathForEDMPath(edmPath)

        then:
            result == expectedMongoPath

        where:
            edmPath                 ||  expectedMongoPath
            "plainString"           ||  "plainString"
            "Name"                  ||  "Name"
            "Addresses"             ||  "Addresses"
            "Addresses/Street"      ||  "Addresses.Street"
            "Addresses/City"        ||  "Addresses.City"
            "Addresses/ZipCode"     ||  "Addresses.ZipCode"
    }

    //TODO flatted properties (properties have different names and mongo paths levels) mapping
    //TODO Complex types (one-to-one) mapping
    //TODO Complex types (properties have different names and mongo paths levels) mapping
    //TODO Circular reference (one-to-one) mapping with nested levels (no max level)
    //TODO Circular reference (properties have different names and mongo paths levels) mapping with nested levels (no max level)
    //TODO Circular reference with max level exception
    //TODO Missing edmPath

    //TODO Circular reference A -> B -> A -> B -> C -> B -> A -> B -> B.pro
    /*
     * TODO Circular reference
     *  ENTITY - C -> B -> A (edm - A)
     *  ENTITY - C -> B -> C (edm - C)
     *  ENTITY - C -> A -> B
     *  ENTITY - A -> B -> A (edm - A)
     */
    /*
     * edmPath - used to resolve type definition, not path
     */
}