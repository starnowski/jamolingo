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
            null                    ||  null
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
            def mappings = prepareEdmToMongoPathOneToOneMappingWithCircularReferences()
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
            "PropC/PropB/PropA/PropB/StringProperty"       ||  "PropC.PropB.PropA.PropB.StringProperty"
            "PropC/PropB/PropA/PropB/PropC/PropA/StringProperty"       ||  "PropC.PropB.PropA.PropB.PropC.PropA.StringProperty"
    }

    @Unroll
    def "should find mongo path for edm path based on EDM (that contains circular references) mapping that is different with Mongo Document"() {
        given:
            def mappings = Map.ofEntries(
                    Map.entry("Id", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Id").withMongoPath("_id").withType("Edm.String").withKey(true).build()),
                    Map.entry("PropA/PropB/PropA", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA/PropB/PropA").withType("Demo.Model.ComplexTypeA").withMongoPath("a.ab.ba").withCircularReferenceMapping(CircularReferenceMapping.builder().withAnchorEdmPath("PropA").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
                    Map.entry("PropA/PropB/PropC", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA/PropB/PropC").withType("Demo.Model.ComplexTypeC").withMongoPath("a.ab.bc").withCircularReferenceMapping(CircularReferenceMapping.builder().withAnchorEdmPath("PropC").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
                    Map.entry("PropA/PropB/StringProperty", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA/PropB/StringProperty").withType("Edm.String").withMongoPath("a.ab.bString").build()),
                    Map.entry("PropA/PropB", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA/PropB").withType("Demo.Model.ComplexTypeB").withMongoPath("a.ab").build()),
                    Map.entry("PropA/StringProperty", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA/StringProperty").withType("Edm.String").withMongoPath("a.aString").build()),
                    Map.entry("PropA", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA").withMongoPath("a").withType("Demo.Model.ComplexTypeA").build()),
                    Map.entry("PropC/PropA", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/PropA").withType("Demo.Model.ComplexTypeA").withMongoPath("c.ca").withCircularReferenceMapping(CircularReferenceMapping.builder().withAnchorEdmPath("PropA").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
                    Map.entry("PropC/PropB/PropA", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/PropB/PropA").withType("Demo.Model.ComplexTypeA").withMongoPath("c.cb.ba").withCircularReferenceMapping(CircularReferenceMapping.builder().withAnchorEdmPath("PropA").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
                    Map.entry("PropC/PropB/PropC", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/PropB/PropC").withType("Demo.Model.ComplexTypeC").withMongoPath("c.cb.bc").withCircularReferenceMapping(CircularReferenceMapping.builder().withAnchorEdmPath("PropC").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
                    Map.entry("PropC/PropB/StringProperty", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/PropB/StringProperty").withType("Edm.String").withMongoPath("c.cb.bString").build()),
                    Map.entry("PropC/PropB", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/PropB").withType("Demo.Model.ComplexTypeB").withMongoPath("c.cb").build()),
                    Map.entry("PropC/StringProperty", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/StringProperty").withType("Edm.String").withMongoPath("c.cString").build()),
                    Map.entry("PropC", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC").withMongoPath("c").withType("Demo.Model.ComplexTypeC").build())
            )
            def tested = new EntityPropertiesMongoPathContext(mappings)

        when:
            def result = tested.resolveMongoPathForEDMPath(edmPath)

        then:
            result == expectedMongoPath

        where:
            edmPath                         ||  expectedMongoPath
            "Id"                            ||  "_id"
            "PropC/PropB/PropA"             ||  "c.cb.ba"
            "PropA/PropB/StringProperty"    ||  "a.ab.bString"
            "PropC/PropB/PropA/PropB/StringProperty"       ||  "c.cb.ba.ab.bString"
            "PropC/PropB/PropA/PropB/PropC/PropA/StringProperty"       ||  "c.cb.ba.ab.bc.ca.aString"
            "PropC/PropB/PropA/PropB/PropC/PropA/PropB/PropC/PropA/StringProperty"       ||  "c.cb.ba.ab.bc.ca.ab.bc.ca.aString"
            "PropC/PropB/PropA/PropB/PropC/PropA/PropB/PropC/PropA/PropB/PropC/StringProperty"       ||  "c.cb.ba.ab.bc.ca.ab.bc.ca.ab.bc.cString"
    }

    @Unroll
    def "should thrown an exception with expected message '#expectedExceptionMessage' when trying to find edm path that do not exists"() {
        given:
            def mappings = prepareEdmToMongoPathOneToOneMappingWithCircularReferences()
            def tested = new EntityPropertiesMongoPathContext(mappings)

        when:
            tested.resolveMongoPathForEDMPath(edmPath)

        then:
            def  ex = thrown(EntityPropertiesMongoPathContext.InvalidEDMPathException)
            ex.message == expectedExceptionMessage

        where:
            edmPath                                                     ||  expectedExceptionMessage
            "xxx"                                                       ||  "No 'xxx' EDM path found"
            "PropC/PropBD"                                              ||  "No 'PropC/PropBD' EDM path found"
            "PropA/PropX/StringProperty"                                ||  "No 'PropA/PropX/StringProperty' EDM path found"
            "PropC/PropB/PropA/PropB/StringField"                       ||  "No 'PropC/PropB/PropA/PropB/StringField' EDM path found"
            "PropC/PropB/PropA/PropB/PropC/PropA/StringField"           ||  "No 'PropC/PropB/PropA/PropB/PropC/PropA/StringField' EDM path found"
    }

//    @Unroll
//    def "should throw an exception for edm path based on EDM (that contains circular references) when the limit for circular reference was exceeded"() {
//        given:
//            def mappings = prepareEdmToMongoPathOneToOneMappingWithCircularReferences()
//            def tested = new EntityPropertiesMongoPathContext(mappings)
//
//        when:
//            def result = tested.resolveMongoPathForEDMPath(edmPath)
//
//        then:
//            def  ex = thrown(EntityPropertiesMongoPathContext.ExceededCircularReferenceDepthException)
//            ex.message == expectedExceptionMessage
//
//        where:
//            edmPath                                                     ||  expectedMaxLevel
//            "PropC/PropB/PropA/PropB/StringProperty"                    ||  1
//            "PropC/PropB/PropA/PropB/PropC/PropA/StringProperty"        ||  1
//            "PropC/PropB/PropA/PropB/PropC/PropA/StringProperty"        ||  2
//    }

    //TODO Maximal nested level for single property - maxCircularLimit
    //TODO Maximal circular level for all property total
    //TODO Maximal nested depth
    @Unroll
    def "should throw an exception for edm path based on EDM (that contains circular references) when the mongo path depth limit was exceeded"() {
        given:
            def mappings = prepareEdmToMongoPathOneToOneMappingWithCircularReferences()
            def tested = new EntityPropertiesMongoPathContext(mappings)
            def searchContext = DefaultEdmPathContextSearch.builder().withMongoPathMaxDepth(maxDepth).build()

        when:
            tested.resolveMongoPathForEDMPath(edmPath, searchContext)

        then:
            def  ex = thrown(EntityPropertiesMongoPathContext.MongoPathMaxDepthException)
            ex.message == "Mongo path '${mongoPath}' for '${edmPath}' edm path exceeded max depth ${maxDepth}"

        where:
            edmPath                                                     | maxDepth  |   mongoPath
            "PropC/PropB/PropA/PropB/StringProperty"                    | 0         |   "PropC.PropB.PropA"
            "PropC/PropB/PropA/PropB/StringProperty"                    | 1         |   "PropC.PropB.PropA"
            "PropC/PropB/PropA/PropB/PropC/PropA/StringProperty"        | 1         |   "PropC.PropB.PropA"
            "PropC/PropB/PropA/PropB/PropC/PropA/StringProperty"        | 2         |   "PropC.PropB.PropA"
    }


    //TODO Circular reference with max level exception

    //TODO Circular reference with max level exception for specific fields

    private static Map<String, MongoPathEntry> prepareEdmToMongoPathOneToOneMappingWithCircularReferences() {
        Map.ofEntries(
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
    }
}