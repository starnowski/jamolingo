package com.github.starnowski.jamolingo.context

import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

import static java.util.Map.entry

class EntityPropertiesMongoPathResolverTest extends Specification {

    @Unroll
    def "should return correct mongo path based on entity configuration #entityMapping with only leafs"(){
        given:
            def tested = new EntityPropertiesMongoPathResolver()

        when:
            def result = tested.resolve(entityMapping, new EntityPropertiesMongoPathResolver.EntityPropertiesMongoPathResolverContext.EntityPropertiesMongoPathResolverContextBuilder().withGenerateOnlyLeafs(true).build()).entrySet().stream().collect(
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
                    || Map.of("plainString", "plainString", "Addresses.City", "Addresses.City", "Addresses.ZipCode","Addresses.ZipCode", "Addresses.Street","Addresses.Street", "Name", "Name")
            // EDM flat property - Mongo nested object
            new EntityMapping()
                .withCollection("Item")
                .withProperties(Map.of("plainString", new PropertyMapping().withRelativeTo("nestedObject")))
                || Map.of("plainString", "nestedObject.plainString")
            // EDM nested object - Mongo flat property
            new EntityMapping()
                .withCollection("Item")
                .withProperties(Map.of("nestedObject", new PropertyMapping().withProperties(Map.of("plainString", new PropertyMapping().withFlattenedLevelUp(1)) )))
                || Map.of("nestedObject.plainString", "plainString")
    }

    @Unroll
    def "should return correct mongo path based on entity configuration #entityMapping"(){
        given:
        def tested = new EntityPropertiesMongoPathResolver()

        when:
        def result = tested.resolve(entityMapping).entrySet().stream().collect(
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
                        || Map.of("plainString", "plainString", "Addresses.City", "Addresses.City", "Addresses.ZipCode","Addresses.ZipCode", "Addresses.Street","Addresses.Street", "Name", "Name", "Addresses", "Addresses")
        // EDM flat property - Mongo nested object
        new EntityMapping()
                .withCollection("Item")
                .withProperties(Map.of("plainString", new PropertyMapping().withRelativeTo("nestedObject")))
                        || Map.of("plainString", "nestedObject.plainString")
        // EDM nested object - Mongo flat property
        new EntityMapping()
                .withCollection("Item")
                .withProperties(Map.of("nestedObject", new PropertyMapping().withProperties(Map.of("plainString", new PropertyMapping().withFlattenedLevelUp(1)) )))
                        || Map.of("nestedObject.plainString", "plainString", "nestedObject", "nestedObject")
        // EDM Circular complex type
        new EntityMapping().withCollection("Item").withProperties(Map.of("plainString", new PropertyMapping(), "Name", new PropertyMapping(), "Addresses", new PropertyMapping().withProperties(Map.of("BackUpAddresses", new PropertyMapping().withCircularReferenceMapping(new CircularReferenceMapping().withAnchorEdmPath("Addresses").withStrategy(CircularStrategy.EMBED_LIMITED)), "Street", new PropertyMapping(), "City", new PropertyMapping(), "ZipCode", new PropertyMapping())) ))
                || Map.ofEntries(Map.entry("plainString", "plainString"), Map.entry("Addresses.City", "Addresses.City"), Map.entry("Addresses.ZipCode","Addresses.ZipCode"), Map.entry("Addresses.Street","Addresses.Street"), Map.entry("Addresses", "Addresses"), Map.entry("Name", "Name"), Map.entry("Addresses.BackUpAddresses","Addresses.BackUpAddresses"))
        // TODO Add mappings for object itself
        // TODO flatted
        //TODO Circular
    }

    @Unroll
    def "should return correct mongo properties mapping based on entity configuration #entityMapping"(){
        given:
            def tested = new EntityPropertiesMongoPathResolver()

        when:
            def result = tested.resolve(entityMapping)

        then:
            result == expecteMongoPatsh

        where:
        entityMapping   ||  expecteMongoPatsh

        // EDM Circular complex type
            new EntityMapping().withCollection("Item").withProperties(Map.of("plainString", new PropertyMapping(), "Name", new PropertyMapping(), "Addresses", new PropertyMapping().withProperties(Map.of("BackUpAddresses", new PropertyMapping().withCircularReferenceMapping(new CircularReferenceMapping().withAnchorEdmPath("Addresses").withStrategy(CircularStrategy.EMBED_LIMITED)), "Street", new PropertyMapping(), "City", new PropertyMapping(), "ZipCode", new PropertyMapping())) ))
                        || Map.ofEntries(Map.entry("plainString", new MongoPathEntry.MongoPathEntryBuilder().withEdmPath("plainString").withMongoPath("plainString").build()))
        // TODO Add mappings for object itself
        // TODO flatted
        //TODO Circular
    }
}
