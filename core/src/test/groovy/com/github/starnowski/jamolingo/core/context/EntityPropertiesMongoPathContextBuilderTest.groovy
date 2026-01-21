package com.github.starnowski.jamolingo.core.context

import com.github.starnowski.jamolingo.core.mapping.CircularReferenceMapping
import com.github.starnowski.jamolingo.core.mapping.CircularReferenceMappingRecord
import com.github.starnowski.jamolingo.core.mapping.CircularStrategy
import com.github.starnowski.jamolingo.context.EntityMapping
import com.github.starnowski.jamolingo.context.MongoPathEntry
import com.github.starnowski.jamolingo.context.PropertyMapping
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

class EntityPropertiesMongoPathContextBuilderTest extends Specification {

    @Unroll
    def "should return correct mongo path based on entity configuration #entityMapping with only leafs"(){
        given:
            def tested = new EntityPropertiesMongoPathContextBuilder()

        when:
            def result = tested.build(entityMapping, new EntityPropertiesMongoPathContextBuilder.EntityPropertiesMongoPathResolverContext.EntityPropertiesMongoPathResolverContextBuilder().withGenerateOnlyLeafs(true).build()).getEdmToMongoPath().entrySet().stream().collect(
                    Collectors.toMap(
                            (entry) -> entry.getKey(), (entry) -> entry.getValue().getMongoPath()))

        then:
            result == expecteMongoPatsh

        where:
            entityMapping   ||  expecteMongoPatsh
            new EntityMapping()
                            .withCollection("Item")
                            .withProperties(Map.of("plainString", new PropertyMapping()))
            || Map.of("plainString", "plainString")
            new EntityMapping().withCollection("Item").withProperties(Map.of("plainString", new PropertyMapping(), "Name", new PropertyMapping(), "Addresses", new PropertyMapping().withProperties(Map.of("Street", new PropertyMapping(), "City", new PropertyMapping(), "ZipCode", new PropertyMapping())) ))
                    || Map.of("plainString", "plainString", "Addresses/City", "Addresses.City", "Addresses/ZipCode","Addresses.ZipCode", "Addresses/Street","Addresses.Street", "Name", "Name")
            // EDM flat property - Mongo nested object
            new EntityMapping()
                .withCollection("Item")
                .withProperties(Map.of("plainString", new PropertyMapping().withRelativeTo("nestedObject")))
                || Map.of("plainString", "nestedObject.plainString")
            // EDM nested object - Mongo flat property
            new EntityMapping()
                .withCollection("Item")
                .withProperties(Map.of("nestedObject", new PropertyMapping().withProperties(Map.of("plainString", new PropertyMapping().withFlattenedLevelUp(1)) )))
                || Map.of("nestedObject/plainString", "plainString")
    }

    @Unroll
    def "should return correct mongo path based on entity configuration #entityMapping"(){
        given:
        def tested = new EntityPropertiesMongoPathContextBuilder()

        when:
        def result = tested.build(entityMapping).getEdmToMongoPath().entrySet().stream().collect(
                Collectors.toMap(
                        (entry) -> entry.getKey(), (entry) -> entry.getValue().getMongoPath()))

        then:
        result == expecteMongoPatsh

        where:
        entityMapping   ||  expecteMongoPatsh
        new EntityMapping()
                .withCollection("Item")
                .withProperties(Map.of("plainString", new PropertyMapping()))
                        || Map.of("plainString", "plainString")
        new EntityMapping().withCollection("Item").withProperties(Map.of("plainString", new PropertyMapping(), "Name", new PropertyMapping(), "Addresses", new PropertyMapping().withProperties(Map.of("Street", new PropertyMapping(), "City", new PropertyMapping(), "ZipCode", new PropertyMapping())) ))
                        || Map.of("plainString", "plainString", "Addresses/City", "Addresses.City", "Addresses/ZipCode","Addresses.ZipCode", "Addresses/Street","Addresses.Street", "Name", "Name", "Addresses", "Addresses")
        // EDM flat property - Mongo nested object
        new EntityMapping()
                .withCollection("Item")
                .withProperties(Map.of("plainString", new PropertyMapping().withRelativeTo("nestedObject")))
                        || Map.of("plainString", "nestedObject.plainString")
        // EDM nested object - Mongo flat property
        new EntityMapping()
                .withCollection("Item")
                .withProperties(Map.of("nestedObject", new PropertyMapping().withProperties(Map.of("plainString", new PropertyMapping().withFlattenedLevelUp(1)) )))
                        || Map.of("nestedObject/plainString", "plainString", "nestedObject", "nestedObject")
        // EDM Circular complex type
        new EntityMapping().withCollection("Item").withProperties(Map.of("plainString", new PropertyMapping(), "Name", new PropertyMapping(), "Addresses", new PropertyMapping().withProperties(Map.of("BackUpAddresses", new PropertyMapping().withCircularReferenceMapping(CircularReferenceMapping.builder().withAnchorEdmPath("Addresses").withStrategy(CircularStrategy.EMBED_LIMITED).build()), "Street", new PropertyMapping(), "City", new PropertyMapping(), "ZipCode", new PropertyMapping())) ))
                || Map.ofEntries(Map.entry("plainString", "plainString"), Map.entry("Addresses/City", "Addresses.City"), Map.entry("Addresses/ZipCode","Addresses.ZipCode"), Map.entry("Addresses/Street","Addresses.Street"), Map.entry("Addresses", "Addresses"), Map.entry("Name", "Name"), Map.entry("Addresses/BackUpAddresses","Addresses.BackUpAddresses"))
    }

    @Unroll
    def "should throw exception for invalid anchor path for #testCaseName"() {
        given:
        def tested = new EntityPropertiesMongoPathContextBuilder()

        when:
        tested.build(entityMapping)

        then:
        def e = thrown(EntityPropertiesMongoPathContext.InvalidAnchorPathException)
        e.message == expectedMessage

        where:
        testCaseName | entityMapping || expectedMessage
        "simple case" | new EntityMapping()
                .withCollection("Item")
                .withProperties(Map.of(
                        "Addresses", new PropertyMapping().withProperties(Map.of(
                                "BackUpAddresses", new PropertyMapping().withCircularReferenceMapping(
                                        CircularReferenceMapping.builder()
                                                .withAnchorEdmPath("InvalidAnchor") // Invalid anchor path
                                                .withStrategy(CircularStrategy.EMBED_LIMITED)
                                                .build()
                                )
                        ))
                )) || "The anchor path 'InvalidAnchor' defined in the circular reference mapping for 'Addresses/BackUpAddresses' is not a valid EDM path."
        "nested case" | new EntityMapping()
                .withCollection("Item")
                .withProperties(Map.of(
                        "Person", new PropertyMapping().withProperties(Map.of(
                                "Addresses", new PropertyMapping().withProperties(Map.of(
                                        "BackUpAddresses", new PropertyMapping().withCircularReferenceMapping(
                                                CircularReferenceMapping.builder()
                                                        .withAnchorEdmPath("Person/InvalidAnchor") // Invalid anchor path
                                                        .withStrategy(CircularStrategy.EMBED_LIMITED)
                                                        .build()
                                        )
                                ))
                        ))
                )) || "The anchor path 'Person/InvalidAnchor' defined in the circular reference mapping for 'Person/Addresses/BackUpAddresses' is not a valid EDM path."
    }

    @Unroll
    def "should return correct mongo properties mapping based on entity configuration #entityMapping"(){
        given:
            def tested = new EntityPropertiesMongoPathContextBuilder()

        when:
            def result = tested.build(entityMapping)

        then:
            result.getEdmToMongoPath() == expecteMongoPatsh

        where:
            entityMapping   ||  expecteMongoPatsh

            // EDM Circular complex type
            new EntityMapping().withCollection("Item").withProperties(Map.of("plainString", new PropertyMapping(), "Name", new PropertyMapping(), "Addresses", new PropertyMapping().withProperties(Map.of("BackUpAddresses", new PropertyMapping().withCircularReferenceMapping(CircularReferenceMapping.builder().withAnchorEdmPath("Addresses").withStrategy(CircularStrategy.EMBED_LIMITED).build()), "Street", new PropertyMapping(), "City", new PropertyMapping(), "ZipCode", new PropertyMapping())) ))
                        || Map.ofEntries(
                    Map.entry("plainString", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("plainString").withMongoPath("plainString").build()),
                    Map.entry("Name", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Name").withMongoPath("Name").build()),
                    Map.entry("Addresses", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses").withMongoPath("Addresses").build()),
                    Map.entry("Addresses/BackUpAddresses", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses/BackUpAddresses").withMongoPath("Addresses.BackUpAddresses").withCircularReferenceMapping(CircularReferenceMappingRecord.builder().withAnchorEdmPath("Addresses").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
                    Map.entry("Addresses/Street", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses/Street").withMongoPath("Addresses.Street").build()),
                    Map.entry("Addresses/City", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses/City").withMongoPath("Addresses.City").build()),
                    Map.entry("Addresses/ZipCode", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses/ZipCode").withMongoPath("Addresses.ZipCode").build())
            )
            /*
            Entity → C → B → C	Anchor path: PropC
            Entity → C → B → A	Anchor path: PropA
            Entity → A → B → A	Anchor path: PropA
            Entity → A → B → C	Anchor path: PropC
            */

            new EntityMapping()
                .withCollection("RootEntity")
                .withProperties(
                        Map.of(
                                "Id",
                                new PropertyMapping()
                                        .withType("Edm.String")
                                        .withKey(true),

                                // =======================
                                // PropC (ComplexTypeC)
                                // =======================
                                "PropC",
                                new PropertyMapping()
                                        .withType("Demo.Model.ComplexTypeC")
                                        .withProperties(
                                                Map.of(
                                                        "StringProperty",
                                                        new PropertyMapping()
                                                                .withType("Edm.String"),

                                                        "PropB",
                                                        new PropertyMapping()
                                                                .withType("Demo.Model.ComplexTypeB")
                                                                .withProperties(
                                                                        Map.of(
                                                                                "StringProperty",
                                                                                new PropertyMapping()
                                                                                        .withType("Edm.String"),

                                                                                "PropA",
                                                                                new PropertyMapping()
                                                                                        .withType("Demo.Model.ComplexTypeA")
                                                                                        .withCircularReferenceMapping(
                                                                                                CircularReferenceMapping.builder()
                                                                                                        .withStrategy(CircularStrategy.EMBED_LIMITED)
                                                                                                        .withAnchorEdmPath("PropA").build()
                                                                                        ),

                                                                                // 🔁 C → B → C
                                                                                "PropC",
                                                                                new PropertyMapping()
                                                                                        .withType("Demo.Model.ComplexTypeC")
                                                                                        .withCircularReferenceMapping(
                                                                                                CircularReferenceMapping.builder()
                                                                                                        .withStrategy(CircularStrategy.EMBED_LIMITED)
                                                                                                        .withAnchorEdmPath("PropC").build()
                                                                                        )
                                                                        )
                                                                ),

                                                        // 🔁 C → A (circle to RootEntity/PropA)
                                                        "PropA",
                                                        new PropertyMapping()
                                                                .withType("Demo.Model.ComplexTypeA")
                                                                .withCircularReferenceMapping(
                                                                        CircularReferenceMapping.builder()
                                                                                .withStrategy(CircularStrategy.EMBED_LIMITED)
                                                                                .withAnchorEdmPath("PropA").build()
                                                                )
                                                )
                                        ),

                                // =======================
                                // PropA (ComplexTypeA)
                                // =======================
                                "PropA",
                                new PropertyMapping()
                                        .withType("Demo.Model.ComplexTypeA")
                                        .withProperties(
                                                Map.of(
                                                        "StringProperty",
                                                        new PropertyMapping()
                                                                .withType("Edm.String"),

                                                        "PropB",
                                                        new PropertyMapping()
                                                                .withType("Demo.Model.ComplexTypeB")
                                                                .withProperties(
                                                                        Map.of(
                                                                                "StringProperty",
                                                                                new PropertyMapping()
                                                                                        .withType("Edm.String"),

                                                                                // 🔁 A → B → A
                                                                                "PropA",
                                                                                new PropertyMapping()
                                                                                        .withType("Demo.Model.ComplexTypeA")
                                                                                        .withCircularReferenceMapping(
                                                                                                CircularReferenceMapping.builder()
                                                                                                        .withStrategy(CircularStrategy.EMBED_LIMITED)
                                                                                                        .withAnchorEdmPath("PropA").build()
                                                                                        ),

                                                                                // 🔁 A → B → C
                                                                                "PropC",
                                                                                new PropertyMapping()
                                                                                        .withType("Demo.Model.ComplexTypeC")
                                                                                        .withCircularReferenceMapping(
                                                                                                CircularReferenceMapping.builder()
                                                                                                        .withStrategy(CircularStrategy.EMBED_LIMITED)
                                                                                                        .withAnchorEdmPath("PropC").build()
                                                                                        )
                                                                        )
                                                                )
                                                )
                                        )
                        )
                )   || Map.ofEntries(
                Map.entry("Id", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Id").withMongoPath("Id").withType("Edm.String").withKey(true).build()),
                Map.entry("PropA/PropB/PropA", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA/PropB/PropA").withType("Demo.Model.ComplexTypeA").withMongoPath("PropA.PropB.PropA").withCircularReferenceMapping(CircularReferenceMappingRecord.builder().withAnchorEdmPath("PropA").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
                Map.entry("PropA/PropB/PropC", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA/PropB/PropC").withType("Demo.Model.ComplexTypeC").withMongoPath("PropA.PropB.PropC").withCircularReferenceMapping(CircularReferenceMappingRecord.builder().withAnchorEdmPath("PropC").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
                Map.entry("PropA/PropB/StringProperty", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA/PropB/StringProperty").withType("Edm.String").withMongoPath("PropA.PropB.StringProperty").build()),
                Map.entry("PropA/PropB", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA/PropB").withType("Demo.Model.ComplexTypeB").withMongoPath("PropA.PropB").build()),
                Map.entry("PropA/StringProperty", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA/StringProperty").withType("Edm.String").withMongoPath("PropA.StringProperty").build()),
                Map.entry("PropA", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropA").withMongoPath("PropA").withType("Demo.Model.ComplexTypeA").build()),
                Map.entry("PropC/PropA", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/PropA").withType("Demo.Model.ComplexTypeA").withMongoPath("PropC.PropA").withCircularReferenceMapping(CircularReferenceMappingRecord.builder().withAnchorEdmPath("PropA").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
                Map.entry("PropC/PropB/PropA", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/PropB/PropA").withType("Demo.Model.ComplexTypeA").withMongoPath("PropC.PropB.PropA").withCircularReferenceMapping(CircularReferenceMappingRecord.builder().withAnchorEdmPath("PropA").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
                Map.entry("PropC/PropB/PropC", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/PropB/PropC").withType("Demo.Model.ComplexTypeC").withMongoPath("PropC.PropB.PropC").withCircularReferenceMapping(CircularReferenceMappingRecord.builder().withAnchorEdmPath("PropC").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
                Map.entry("PropC/PropB/StringProperty", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/PropB/StringProperty").withType("Edm.String").withMongoPath("PropC.PropB.StringProperty").build()),
                Map.entry("PropC/PropB", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/PropB").withType("Demo.Model.ComplexTypeB").withMongoPath("PropC.PropB").build()),
                Map.entry("PropC/StringProperty", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC/StringProperty").withType("Edm.String").withMongoPath("PropC.StringProperty").build()),
                Map.entry("PropC", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("PropC").withMongoPath("PropC").withType("Demo.Model.ComplexTypeC").build())
        )

    }
}
