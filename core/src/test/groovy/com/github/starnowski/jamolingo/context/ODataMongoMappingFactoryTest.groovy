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
                                        .withCircularReferenceMapping(CircularReferenceMapping.builder()
                                                .withStrategy(CircularStrategy.EMBED_LIMITED)
                                                .withAnchorEdmPath("Addresses").build())
                            )
                    ) ))))
            "edm/edm3_complextype_with_circular_reference_collection.xml"  | "Demo"    ||
                new ODataMongoMapping().withEntities(Map.of("Item", new EntityMapping().withCollection("Item").withProperties(Map.of("plainString", new PropertyMapping().withType("Edm.String"), "Name", new PropertyMapping().withType("Edm.String"), "Addresses", new PropertyMapping().withType("Demo.Address").withProperties(
                        Map.of(
                                "Street", new PropertyMapping().withType("Edm.String"),
                                "City", new PropertyMapping().withType("Edm.String"),
                                "ZipCode", new PropertyMapping().withType("Edm.String"),
                                "BackUpAddresses", new PropertyMapping().withType("Demo.Address")
                                .withCircularReferenceMapping(CircularReferenceMapping.builder()
                                        .withStrategy(CircularStrategy.EMBED_LIMITED)
                                        .withAnchorEdmPath("Addresses").build())
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
                                            .withCircularReferenceMapping(CircularReferenceMapping.builder()
                                                    .withStrategy(CircularStrategy.EMBED_LIMITED)
                                                    .withAnchorEdmPath("Definition").build()
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
                                                .withCircularReferenceMapping(CircularReferenceMapping.builder()
                                                        .withStrategy(CircularStrategy.EMBED_LIMITED)
                                                        .withAnchorEdmPath("Configuration").build()
                                                )
                                        )
                                        )
                                )))))
                )

            // Complex Types circular reference, where one type is from different schema Entity -> Type A -> Type B -> Type A
        "edm/edm4_complextype_with_long_nested_circular_reference_different_schema.xml" | "Policy.Model" ||
                new ODataMongoMapping()
                        .withEntities(
                                Map.of(
                                        "InsurancePolicy",
                                        new EntityMapping()
                                                .withCollection("InsurancePolicy")
                                                .withProperties(
                                                        Map.of(
                                                                "PolicyNumber",
                                                                new PropertyMapping()
                                                                        .withType("Edm.String")
                                                                        .withKey(true),

                                                                "CoverageSnapshot",
                                                                new PropertyMapping()
                                                                        .withType("Policy.Model.CoverageSnapshot")
                                                                        .withProperties(
                                                                                Map.of(
                                                                                        "CoverageCode",
                                                                                        new PropertyMapping()
                                                                                                .withType("Edm.String"),

                                                                                        "EligibilityRule",
                                                                                        new PropertyMapping()
                                                                                                .withType("Rules.Model.EligibilityRule")
                                                                                                .withProperties(
                                                                                                        Map.of(
                                                                                                                "RuleCode",
                                                                                                                new PropertyMapping()
                                                                                                                        .withType("Edm.String"),

                                                                                                                "Description",
                                                                                                                new PropertyMapping()
                                                                                                                        .withType("Edm.String"),

                                                                                                                "EvaluationContext",
                                                                                                                new PropertyMapping()
                                                                                                                        .withType("Rules.Model.RuleEvaluationContext")
                                                                                                                        .withProperties(
                                                                                                                                Map.of(
                                                                                                                                        "EvaluatedAtUtc",
                                                                                                                                        new PropertyMapping()
                                                                                                                                                .withType("Edm.DateTimeOffset"),

                                                                                                                                        "Result",
                                                                                                                                        new PropertyMapping()
                                                                                                                                                .withType("Edm.String"),

                                                                                                                                        "EvaluatedRule",
                                                                                                                                        new PropertyMapping()
                                                                                                                                                .withType("Rules.Model.EligibilityRule")
                                                                                                                                                .withCircularReferenceMapping(
                                                                                                                                                        CircularReferenceMapping.builder()
                                                                                                                                                                .withStrategy(CircularStrategy.EMBED_LIMITED)
                                                                                                                                                                .withAnchorEdmPath(
                                                                                                                                                                        "CoverageSnapshot/EligibilityRule"
                                                                                                                                                                ).build()
                                                                                                                                                )
                                                                                                                                )
                                                                                                                        )
                                                                                                        )
                                                                                                )
                                                                                )
                                                                        )
                                                        )
                                                )
                                )
                        )
        "edm/edm5_multiple_circula_references.xml" | "Demo.Model" ||
                new ODataMongoMapping()
                        .withEntities(
                                Map.of(
                                        "RootEntity",
                                        new EntityMapping()
                                                .withCollection("RootEntity")
                                                .withProperties(
                                                        Map.of(
                                                                "Id",
                                                                new PropertyMapping()
                                                                        .withType("Edm.String")
                                                                        .withKey(true),

                                                                // ---------------------------
                                                                // PropC (ComplexTypeC)
                                                                // ---------------------------
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
                                                                                                                        .withProperties(
                                                                                                                                Map.of(
                                                                                                                                        "StringProperty",
                                                                                                                                        new PropertyMapping()
                                                                                                                                                .withType("Edm.String"),

                                                                                                                                        "PropB",
                                                                                                                                        new PropertyMapping()
                                                                                                                                                .withType("Demo.Model.ComplexTypeB")
                                                                                                                                                .withCircularReferenceMapping(
                                                                                                                                                        CircularReferenceMapping.builder()
                                                                                                                                                                .withStrategy(CircularStrategy.EMBED_LIMITED)
                                                                                                                                                                .withAnchorEdmPath("PropC/PropB").build()
                                                                                                                                                )
                                                                                                                                )
                                                                                                                        ),

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
                                                                                                                        .withCircularReferenceMapping(
                                                                                                                                CircularReferenceMapping.builder()
                                                                                                                                        .withStrategy(CircularStrategy.EMBED_LIMITED)
                                                                                                                                        .withAnchorEdmPath("PropC/PropB").build()
                                                                                                                        )
                                                                                                        )
                                                                                                )
                                                                                )
                                                                        ),

                                                                // ---------------------------
                                                                // PropA (ComplexTypeA)
                                                                // ---------------------------
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

                                                                                                                "PropA",
                                                                                                                new PropertyMapping()
                                                                                                                        .withType("Demo.Model.ComplexTypeA")
                                                                                                                        .withCircularReferenceMapping(
                                                                                                                                CircularReferenceMapping.builder()
                                                                                                                                        .withStrategy(CircularStrategy.EMBED_LIMITED)
                                                                                                                                        .withAnchorEdmPath("PropA").build()
                                                                                                                        ),

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
                                                )
                                )
                        )

    }
}
