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
            def tested = new DefaultEntityPropertiesMongoPathContext(mappings)

        when:
            def result = tested.resolveMongoPathForEDMPath(edmPath)

        then:
            if (expectedMongoPath == null) {
                assert result == null
            } else {
                assert result.getMongoPath() == expectedMongoPath
            }

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
            def tested = new DefaultEntityPropertiesMongoPathContext(mappings)

        when:
            def result = tested.resolveMongoPathForEDMPath(edmPath)

        then:
            result.getMongoPath() == expectedMongoPath

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
            def tested = new DefaultEntityPropertiesMongoPathContext(mappings)

        when:
            def result = tested.resolveMongoPathForEDMPath(edmPath)

        then:
            result.getMongoPath() == expectedMongoPath

        where:
            edmPath                         ||  expectedMongoPath
            "Id"                            ||  "Id"
            "PropC/PropB/PropA"             ||  "PropC.PropB.PropA"
            "PropA/PropB/StringProperty"    ||  "PropA.PropB.StringProperty"
            "PropC/PropB/PropA/PropB/StringProperty"       ||  "PropC.PropB.PropA.PropB.StringProperty"
            "PropC/PropB/PropA/PropB/PropC/PropA/StringProperty"       ||  "PropC.PropB.PropA.PropB.PropC.PropA.StringProperty"
            "PropC/PropB/PropA/PropB/PropC/PropB/PropA/StringProperty"       ||  "PropC.PropB.PropA.PropB.PropC.PropB.PropA.StringProperty"
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
            def tested = new DefaultEntityPropertiesMongoPathContext(mappings)

        when:
            def result = tested.resolveMongoPathForEDMPath(edmPath)

        then:
            result.getMongoPath() == expectedMongoPath

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
            def tested = new DefaultEntityPropertiesMongoPathContext(mappings)

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

    @Unroll
    def "should throw an exception for edm path based on EDM (that contains circular references) when the mongo path depth limit was exceeded"() {
        given:
            def mappings = prepareEdmToMongoPathOneToOneMappingWithCircularReferences()
            mappings = new HashMap<String, MongoPathEntry>(mappings)
            // Adding one EDM property that has nested mongo path
            mappings.put("PropDNested", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropDNested").withMongoPath("somePropertyD.child.grandChild").build())
            mappings.put("PropC/PropCNested", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/PropCNested").withMongoPath("PropC.PropCNested").build())
            def tested = new DefaultEntityPropertiesMongoPathContext(mappings)
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
            "PropDNested"                                               | 2         |   "somePropertyD.child.grandChild"
            "PropC/PropB/PropC/PropCNested"                             | 2         |   "PropC.PropB.PropC"
    }

    @Unroll
    def "should throw an exception for edm path based on EDM (that contains circular references) when the circular limit (#circularLimit) was exceeded"() {
        given:
            def mappings = prepareEdmToMongoPathOneToOneMappingWithCircularReferences()
            def tested = new DefaultEntityPropertiesMongoPathContext(mappings)
            def searchContext = DefaultEdmPathContextSearch.builder().withMaxCircularLimitPerEdmPath(circularLimit).build()

        when:
            tested.resolveMongoPathForEDMPath(edmPath, searchContext)

        then:
            def  ex = thrown(EntityPropertiesMongoPathContext.ExceededCircularReferenceDepthException)
            ex.message == "Circular edm path '${exceptionCircularPath}' exceeded max depth ${circularLimit} in main edm path '${edmPath}'"

        where:
            edmPath                                                             | circularLimit     |   exceptionCircularPath
            "PropC/PropB/PropA/PropB/StringProperty"                            | 0                 |   "PropC/PropB/PropA"
            "PropC/PropB/PropA/PropB/PropC/PropB/PropA/StringProperty"          | 1                 |   "PropC/PropB/PropA"
    }

    @Unroll
    def "should throw ExceededCircularReferenceDepthException when property specific circular limit of #limit is exceeded for #edmPath"() {
        given:
            def mappings = prepareEdmToMongoPathOneToOneMappingWithCircularReferences()
            mappings = new HashMap<>(mappings)
            // Override global limit for PropA/PropB/PropA
            mappings.put("PropA/PropB/PropA", new MongoPathEntry.MongoPathEntryBuilder()
                    .withEdmPath("PropA/PropB/PropA")
                    .withType("Demo.Model.ComplexTypeA")
                    .withMongoPath("PropA.PropB.PropA")
                    .withCircularReferenceMapping(
                            CircularReferenceMapping.builder()
                                    .withAnchorEdmPath("PropA")
                                    .withStrategy(CircularStrategy.EMBED_LIMITED)
                                    .build())
                    .withMaxCircularLimitPerEdmPath(limit) // Property specific limit
                    .build())

            def tested = new DefaultEntityPropertiesMongoPathContext(mappings)
            def searchContext = DefaultEdmPathContextSearch.builder().withMaxCircularLimitPerEdmPath(10).build() // Global limit is higher

        when:
            tested.resolveMongoPathForEDMPath(edmPath, searchContext)

        then:
            def ex = thrown(EntityPropertiesMongoPathContext.ExceededCircularReferenceDepthException)
            ex.message == "Circular edm path '${exceptionCircularPath}' exceeded max depth ${limit} in main edm path '${edmPath}'"

        where:
            edmPath                                                             | limit | exceptionCircularPath
            "PropA/PropB/PropA/PropB/StringProperty"                            | 0     | "PropA/PropB/PropA"
            "PropA/PropB/PropA/PropB/PropA/PropB/StringProperty"                            | 1     | "PropA/PropB/PropA"
    }

    @Unroll
    def "should find mongo path when property specific circular limit of #limit is NOT exceeded for #edmPath"() {
        given:
            def mappings = prepareEdmToMongoPathOneToOneMappingWithCircularReferences()
            mappings = new HashMap<>(mappings)
            // Override global limit for PropA/PropB/PropA
            mappings.put("PropA/PropB/PropA", new MongoPathEntry.MongoPathEntryBuilder()
                    .withEdmPath("PropA/PropB/PropA")
                    .withType("Demo.Model.ComplexTypeA")
                    .withMongoPath("PropA.PropB.PropA")
                    .withCircularReferenceMapping(
                            CircularReferenceMapping.builder()
                                    .withAnchorEdmPath("PropA")
                                    .withStrategy(CircularStrategy.EMBED_LIMITED)
                                    .build())
                    .withMaxCircularLimitPerEdmPath(limit) // Property specific limit
                    .build())

            def tested = new DefaultEntityPropertiesMongoPathContext(mappings)
            def searchContext = DefaultEdmPathContextSearch.builder().withMaxCircularLimitPerEdmPath(1).build() // Global limit is lower

        when:
            def result = tested.resolveMongoPathForEDMPath(edmPath, searchContext)

        then:
            result.getMongoPath() == expectedMongoPath

        where:
            edmPath                                                             | limit | expectedMongoPath
            "PropA/PropB/PropA/PropB/StringProperty"                            | 1     | "PropA.PropB.PropA.PropB.StringProperty"
            "PropA/PropB/PropA/PropB/PropC/PropB/PropA/PropB/StringProperty"          | 5     | "PropA.PropB.PropA.PropB.PropC.PropB.PropA.PropB.StringProperty"
    }

    @Unroll
    def "should find mongo path for edm path based on EDM (that contains circular references) when the circular limit is NOT exceeded"() {
        given:
            def mappings = prepareEdmToMongoPathOneToOneMappingWithCircularReferences()
            def tested = new DefaultEntityPropertiesMongoPathContext(mappings)
            def searchContext = DefaultEdmPathContextSearch.builder().withMaxCircularLimitPerEdmPath(circularLimit).build()

        when:
            def result = tested.resolveMongoPathForEDMPath(edmPath, searchContext)

        then:
            result.getMongoPath() == expectedMongoPath

        where:
            edmPath                                                             | circularLimit | expectedMongoPath
            "PropC/PropB/PropA/PropB/StringProperty"                            | 1             | "PropC.PropB.PropA.PropB.StringProperty"
            "PropC/PropB/PropA/PropB/PropC/PropB/PropA/StringProperty"          | 2             | "PropC.PropB.PropA.PropB.PropC.PropB.PropA.StringProperty"
    }

    @Unroll
    def "should throw ExceededTotalCircularReferenceLimitException when total circular limit of #limit is exceeded"() {
        given:
            def mappings = prepareEdmToMongoPathOneToOneMappingWithCircularReferences()
            def tested = new DefaultEntityPropertiesMongoPathContext(mappings)
            def searchContext = DefaultEdmPathContextSearch.builder().withMaxCircularLimitForAllEdmPaths(limit).build()

        when:
            tested.resolveMongoPathForEDMPath(edmPath, searchContext)

        then:
            def ex = thrown(EntityPropertiesMongoPathContext.ExceededTotalCircularReferenceLimitException)
            ex.message == "Total circular reference limit of ${limit} exceeded."

        where:
            edmPath                                                             | limit
            "PropC/PropB/PropA/PropB/StringProperty"                            | 0
            "PropC/PropB/PropA/PropB/PropC/PropB/PropA/StringProperty"          | 0
            "PropC/PropB/PropA/PropB/PropC/PropB/PropA/StringProperty"          | 1
            "PropC/PropB/PropA/PropB/PropC/PropB/PropA/StringProperty"          | 2
    }

    @Unroll
    def "should find mongo path for edm path when total circular limit is NOT exceeded"() {
        given:
            def mappings = prepareEdmToMongoPathOneToOneMappingWithCircularReferences()
            def tested = new DefaultEntityPropertiesMongoPathContext(mappings)
            def searchContext = DefaultEdmPathContextSearch.builder().withMaxCircularLimitForAllEdmPaths(limit).build()

        when:
            def result = tested.resolveMongoPathForEDMPath(edmPath, searchContext)

        then:
            result.getMongoPath() == expectedMongoPath

        where:
            edmPath                                                             | limit | expectedMongoPath
            "PropC/PropB/PropA/PropB/StringProperty"                            | 1     | "PropC.PropB.PropA.PropB.StringProperty"
            "PropC/PropB/PropA/PropB/PropC/PropB/PropA/StringProperty"          | 3     | "PropC.PropB.PropA.PropB.PropC.PropB.PropA.StringProperty"
    }

    def "should throw InvalidAnchorPathException when anchor path is invalid"() {
        given:
        def mappings = new HashMap<>(prepareEdmToMongoPathOneToOneMappingWithCircularReferences())
        mappings.put("PropA/PropB/PropA", new MongoPathEntry.MongoPathEntryBuilder()
                .withEdmPath("PropA/PropB/PropA")
                .withType("Demo.Model.ComplexTypeA")
                .withMongoPath("PropA.PropB.PropA")
                .withCircularReferenceMapping(
                        CircularReferenceMapping.builder()
                                .withAnchorEdmPath("InvalidAnchor") // Invalid anchor path
                                .withStrategy(CircularStrategy.EMBED_LIMITED)
                                .build())
                .build())
        def tested = new DefaultEntityPropertiesMongoPathContext(mappings)
        def edmPath = "PropA/PropB/PropA/PropB/StringProperty"

        when:
        tested.resolveMongoPathForEDMPath(edmPath)

        then:
        def ex = thrown(EntityPropertiesMongoPathContext.InvalidAnchorPathException)
        ex.message == "The anchor path 'InvalidAnchor' defined in the circular reference mapping for 'PropA/PropB/PropA' is not a valid EDM path."
    }


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