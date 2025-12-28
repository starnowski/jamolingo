package com.github.starnowski.jamolingo.context

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
                    Map.entry("Addresses/BackUpAddresses", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses/BackUpAddresses").withMongoPath("Addresses.BackUpAddresses").withCircularReferenceMapping(CircularReferenceMapping.builder().withAnchorEdmPath("Addresses").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
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

                                                                                // 🔁 C → B → A (circle to RootEntity/PropA)
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
                Map.entry("plainString", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("plainString").withMongoPath("plainString").build()),
                Map.entry("Name", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Name").withMongoPath("Name").build()),
                Map.entry("Addresses", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses").withMongoPath("Addresses").build()),
                Map.entry("Addresses/BackUpAddresses", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses/BackUpAddresses").withMongoPath("Addresses.BackUpAddresses").withCircularReferenceMapping(CircularReferenceMapping.builder().withAnchorEdmPath("Addresses").withStrategy(CircularStrategy.EMBED_LIMITED).build()).build()),
                Map.entry("Addresses/Street", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses/Street").withMongoPath("Addresses.Street").build()),
                Map.entry("Addresses/City", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses/City").withMongoPath("Addresses.City").build()),
                Map.entry("Addresses/ZipCode", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("Addresses/ZipCode").withMongoPath("Addresses.ZipCode").build())
        )

    }
}
