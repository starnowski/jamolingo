package com.github.starnowski.jamolingo.context

import spock.lang.Specification
import spock.lang.Unroll

class EntityPropertiesMongoPathResolverTest extends Specification {

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
            new EntityMapping()
                            .withCollection("Item")
                            .withProperties(Map.of("plainString", new PropertyMapping()))
            || Map.of("plainString", "plainString")
            new EntityMapping().withCollection("Item").withProperties(Map.of("plainString", new PropertyMapping(), "Name", new PropertyMapping(), "Addresses", new PropertyMapping().withProperties(Map.of("Street", new PropertyMapping(), "City", new PropertyMapping(), "ZipCode", new PropertyMapping())) ))
                    || Map.of("plainString", "plainString", "Addresses.City", "Addresses.City", "Addresses.ZipCode","Addresses.ZipCode", "Addresses.Street","Addresses.Street", "Name", "Name")
    }
}
