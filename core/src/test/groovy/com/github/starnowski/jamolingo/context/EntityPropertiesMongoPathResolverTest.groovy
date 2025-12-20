package com.github.starnowski.jamolingo.context

import org.apache.olingo.commons.api.edm.Edm
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
            || Map.of("Item.plainString", "plainString")
    }
}
