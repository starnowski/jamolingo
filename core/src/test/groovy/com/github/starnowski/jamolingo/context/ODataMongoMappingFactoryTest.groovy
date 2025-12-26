package com.github.starnowski.jamolingo.context

import com.github.starnowski.jamolingo.AbstractSpecification
import org.apache.olingo.commons.api.edm.Edm
import spock.lang.Unroll


class ODataMongoMappingFactoryTest extends AbstractSpecification {

    @Unroll
    def "should return expected stage bson object"(){
        given:
            def tested = new ODataMongoMappingFactory()
            Edm edm = loadEmdProvider(edmConfigFile)

        when:
            def result = tested.build(edm, schema)

        then:
            result == expectedODataMongoMapping

        where:
            edmConfigFile   | schema    || expectedODataMongoMapping
            "edm/edm1.xml"  | "Demo"    || new ODataMongoMapping().withEntities(Map.of("Item", new EntityMapping().withCollection("Item").withProperties(Map.of("plainString", new PropertyMapping().withType("Edm.String")))))
            "edm/edm2_with_nested_collections.xml"  | "Demo"    || new ODataMongoMapping().withEntities(Map.of("Item", new EntityMapping().withCollection("Item").withProperties(Map.of("plainString", new PropertyMapping().withType("Edm.String"), "Name", new PropertyMapping().withType("Edm.String"), "Addresses", new PropertyMapping().withType("Demo.Address").withProperties(Map.of("Street", new PropertyMapping().withType("Edm.String"), "City", new PropertyMapping().withType("Edm.String"), "ZipCode", new PropertyMapping().withType("Edm.String"))) ))))
            "edm/edm2_complextype_with_circular_reference.xml"  | "Demo"    ||
                    new ODataMongoMapping().withEntities(Map.of("Item", new EntityMapping().withCollection("Item").withProperties(Map.of("plainString", new PropertyMapping().withType("Edm.String"), "Name", new PropertyMapping().withType("Edm.String"), "Addresses", new PropertyMapping().withType("Demo.Address").withProperties(
                            Map.of(
                                    "Street", new PropertyMapping().withType("Edm.String"),
                                    "City", new PropertyMapping().withType("Edm.String"),
                                    "ZipCode", new PropertyMapping().withType("Edm.String"),
                                    "BackUpAddresses", new PropertyMapping().withType("Demo.Address")
                                        .withCircularReferenceMapping(new CircularReferenceMapping()
                                                .withStrategy(CircularStrategy.EMBED_LIMITED)
                                                .withAnchorEdmPath("Addresses"))
                            )
                    ) ))))
            "edm/edm3_complextype_with_circular_reference_collection.xml"  | "Demo"    ||
                new ODataMongoMapping().withEntities(Map.of("Item", new EntityMapping().withCollection("Item").withProperties(Map.of("plainString", new PropertyMapping().withType("Edm.String"), "Name", new PropertyMapping().withType("Edm.String"), "Addresses", new PropertyMapping().withType("Demo.Address").withProperties(
                        Map.of(
                                "Street", new PropertyMapping().withType("Edm.String"),
                                "City", new PropertyMapping().withType("Edm.String"),
                                "ZipCode", new PropertyMapping().withType("Edm.String"),
                                "BackUpAddresses", new PropertyMapping().withType("Demo.Address")
                                .withCircularReferenceMapping(new CircularReferenceMapping()
                                        .withStrategy(CircularStrategy.EMBED_LIMITED)
                                        .withAnchorEdmPath("Addresses"))
                        )
                ) ))))
            //TODO Test case with circular types
    }
}
