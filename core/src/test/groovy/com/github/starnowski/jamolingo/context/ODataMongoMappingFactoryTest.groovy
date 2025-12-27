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
            // Complex Types circular reference Entity -> Type A -> Type B -> Type A
            "edm/edm4_complextype_with_long_circular_reference.xml"  | "Workflow.Model"    ||
                new ODataMongoMapping().withEntities(Map.of("WorkflowInstance", new EntityMapping().withCollection("WorkflowInstance").withProperties(
                        Map.of("InstanceId", new PropertyMapping().withType("Edm.String").withKey(true),
                                "Definition", new PropertyMapping().withType("Workflow.Model.WorkflowDefinition").withProperties(
                        Map.of(
                                "WorkflowKey", new PropertyMapping().withType("Edm.String"),
                                "Version", new PropertyMapping().withType("Edm.Int32"),
                                "Steps", new PropertyMapping().withType("Edm.String"),
                                "ExecutionContext", new PropertyMapping().withType("Workflow.Model.ExecutionContext")
                                .withProperties(Map.of("TriggeredBy", new PropertyMapping().withType("Edm.String"),
                                        "ExecutionTimeUtc", new PropertyMapping().withType("Edm.DateTimeOffset"),
                                        "RuntimeVariables", new PropertyMapping().withType("Edm.String"),
                                        "EvaluatedDefinition", new PropertyMapping().withType("Workflow.Model.WorkflowDefinition")
                                            .withCircularReferenceMapping(new CircularReferenceMapping()
                                                    .withStrategy(CircularStrategy.EMBED_LIMITED)
                                                    .withAnchorEdmPath("Definition")
                                            )
                                )
                        )
                )))))
                )
            // Complex Types circular reference, where one type is from different schema Entity -> Type A -> Type B -> Type A
            "edm/edm4_complextype_with_long_circular_reference_different_schema.xml"  | "Sales.Model"    ||
                new ODataMongoMapping().withEntities(Map.of("SalesOrder", new EntityMapping().withCollection("SalesOrder").withProperties(
                        Map.of("OrderId", new PropertyMapping().withType("Edm.String").withKey(true),
                                "Configuration", new PropertyMapping().withType("Sales.Model.ProductConfiguration").withProperties(
                                Map.of(
                                        "ProductId", new PropertyMapping().withType("Edm.String"),
                                        "SelectedOptions", new PropertyMapping().withType("Edm.String"),
                                        "PricingContext", new PropertyMapping().withType("Pricing.Model.PricingContext")
                                        .withProperties(Map.of("Currency", new PropertyMapping().withType("Edm.String"),
                                                "CustomerGroup", new PropertyMapping().withType("Edm.String"),
                                                "EvaluatedConfiguration", new PropertyMapping().withType("Sales.Model.ProductConfiguration")
                                                .withCircularReferenceMapping(new CircularReferenceMapping()
                                                        .withStrategy(CircularStrategy.EMBED_LIMITED)
                                                        .withAnchorEdmPath("Configuration")
                                                )
                                        )
                                        )
                                )))))
                )

            //edm4_complextype_with_long_circular_reference
            //TODO Test case with circular types
    }
}
