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

    @Unroll
    def "should find mongo path for edm path based on EDM mapping that is different with Mongo Document"() {
        given:
            def mappings = Map.ofEntries(
                    Map.entry("plainString", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("plainString").withMongoPath("stringProperty").build()),
                    Map.entry("Name", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Name").withMongoPath("name").build()),
                    Map.entry("Addresses", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses").withMongoPath("userAddress").build()),
                    Map.entry("Addresses/Street", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses/Street").withMongoPath("street").build()),
                    Map.entry("Addresses/City", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses/City").withMongoPath("Addresses.City").build()),
                    Map.entry("Addresses/ZipCode", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses/ZipCode").withMongoPath("userAddress.zip.fullCode").build()))
            def tested = new EntityPropertiesMongoPathContext(mappings)

        when:
            def result = tested.resolveMongoPathForEDMPath(edmPath)

        then:
            result == expectedMongoPath

        where:
            edmPath                 ||  expectedMongoPath
            "plainString"           ||  "stringProperty"
            "Name"                  ||  "name"
            "Addresses"             ||  "userAddress"
            "Addresses/Street"      ||  "street"
            "Addresses/City"        ||  "Addresses.City"
            "Addresses/ZipCode"     ||  "userAddress.zip.fullCode"
    }

    @Unroll
    def "should find mongo path for edm path based on EDM (that contains circular references) mapping one-to-one with Mongo Document"() {
        given:
            def mappings = Map.ofEntries(
                    Map.entry("Id", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Id").withMongoPath("Id").withType("Edm.String").withKey(true).build()),
                    Map.entry("PropA/PropB/PropA", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA/PropB/PropA").withType("Demo.Model.ComplexTypeA").withMongoPath("PropA.PropB.PropA").withCircularReferenceMapping(CircularReferenceMapping.builder().withAnchorEdmPath("PropA").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
                    Map.entry("PropA/PropB/PropC", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA/PropB/PropC").withType("Demo.Model.ComplexTypeC").withMongoPath("PropA.PropB.PropC").withCircularReferenceMapping(CircularReferenceMapping.builder().withAnchorEdmPath("PropC").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
                    Map.entry("PropA/PropB/StringProperty", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA/PropB/StringProperty").withType("Edm.String").withMongoPath("PropA.PropB.StringProperty").build()),
                    Map.entry("PropA/PropB", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA/PropB").withType("Demo.Model.ComplexTypeB").withMongoPath("PropA.PropB").build()),
                    Map.entry("PropA/StringProperty", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA/StringProperty").withType("Edm.String").withMongoPath("PropA.StringProperty").build()),
                    Map.entry("PropA", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA").withMongoPath("PropA").withType("Demo.Model.ComplexTypeA").build()),
                    Map.entry("PropC/PropA", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/PropA").withType("Demo.Model.ComplexTypeA").withMongoPath("PropC.PropA").withCircularReferenceMapping(CircularReferenceMapping.builder().withAnchorEdmPath("PropA").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
                    Map.entry("PropC/PropB/PropA", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/PropB/PropA").withType("Demo.Model.ComplexTypeA").withMongoPath("PropC.PropB.PropA").withCircularReferenceMapping(CircularReferenceMapping.builder().withAnchorEdmPath("PropA").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
                    Map.entry("PropC/PropB/PropC", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/PropB/PropC").withType("Demo.Model.ComplexTypeC").withMongoPath("PropC.PropB.PropC").withCircularReferenceMapping(CircularReferenceMapping.builder().withAnchorEdmPath("PropC").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
                    Map.entry("PropC/PropB/StringProperty", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/PropB/StringProperty").withType("Edm.String").withMongoPath("PropC.PropB.StringProperty").build()),
                    Map.entry("PropC/PropB", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/PropB").withType("Demo.Model.ComplexTypeB").withMongoPath("PropC.PropB").build()),
                    Map.entry("PropC/StringProperty", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/StringProperty").withType("Edm.String").withMongoPath("PropC.StringProperty").build()),
                    Map.entry("PropC", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC").withMongoPath("PropC").withType("Demo.Model.ComplexTypeC").build())
            )
            def tested = new EntityPropertiesMongoPathContext(mappings)

        when:
            def result = tested.resolveMongoPathForEDMPath(edmPath)

        then:
            result == expectedMongoPath

        where:
            edmPath                         ||  expectedMongoPath
            "Id"                            ||  "Id"
            "PropC/PropB/PropA"             ||  "PropC.PropB.PropA"
            "PropA/PropB/StringProperty"    ||  "PropA.PropB.StringProperty"
    }

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