package com.github.starnowski.jamolingo.common.json

import spock.lang.Specification
import spock.lang.Unroll

class JSONOverrideHelperTest extends Specification {

    def "should apply merge patch (RFC 7396) correctly"() {
        given:
        def helper = new JSONOverrideHelper()
        def original = new Person(
                name: "John",
                age: 30,
                address: new Address(city: "New York", street: "5th Ave"),
                tags: ["friend", "work"]
        )

        when:
        def result = helper.applyChangesToJson(original, patchJson, Person.class, JSONOverrideHelper.PatchType.MERGE)

        then:
        result.name == expectedName
        result.age == expectedAge
        result.address?.city == expectedCity
        result.address?.street == expectedStreet
        result.tags == expectedTags

        where:
        patchJson                                      || expectedName | expectedAge | expectedCity | expectedStreet | expectedTags
        '{"name": "Jane"}'                             || "Jane"       | 30          | "New York"   | "5th Ave"      | ["friend", "work"]
        '{"age": 31}'                                  || "John"       | 31          | "New York"   | "5th Ave"      | ["friend", "work"]
        '{"address": {"city": "Boston"}}'              || "John"       | 30          | "Boston"     | "5th Ave"      | ["friend", "work"]
        '{"name": "Jane", "age": 25}'                  || "Jane"       | 25          | "New York"   | "5th Ave"      | ["friend", "work"]
        '{"address": {"street": null}}'                || "John"       | 30          | "New York"   | null           | ["friend", "work"]
        '{"tags": ["home"]}'                           || "John"       | 30          | "New York"   | "5th Ave"      | ["home"]
        '{"tags": null}'                               || "John"       | 30          | "New York"   | "5th Ave"      | null
        '{"address": null}'                            || "John"       | 30          | null         | null           | ["friend", "work"]
    }

    def "should apply json patch (RFC 6902) correctly"() {
        given:
        def helper = new JSONOverrideHelper()
        def original = new Person(
                name: "John",
                age: 30,
                address: new Address(city: "New York", street: "5th Ave"),
                tags: ["friend", "work"]
        )

        when:
        def result = helper.applyChangesToJson(original, patchJson, Person.class, JSONOverrideHelper.PatchType.JSON_PATCH)

        then:
        result.name == expectedName
        result.age == expectedAge
        result.address?.city == expectedCity
        result.address?.street == expectedStreet
        result.tags == expectedTags

        where:
        patchJson                                                                                  || expectedName | expectedAge | expectedCity | expectedStreet | expectedTags
        '[{"op": "replace", "path": "/name", "value": "Jane"}]'                                    || "Jane"       | 30          | "New York"   | "5th Ave"      | ["friend", "work"]
        '[{"op": "replace", "path": "/age", "value": 31}]'                                         || "John"       | 31          | "New York"   | "5th Ave"      | ["friend", "work"]
        '[{"op": "replace", "path": "/address/city", "value": "Boston"}]'                          || "John"       | 30          | "Boston"     | "5th Ave"      | ["friend", "work"]
        '[{"op": "add", "path": "/tags/0", "value": "home"}]'                                      || "John"       | 30          | "New York"   | "5th Ave"      | ["home", "friend", "work"]
        '[{"op": "remove", "path": "/tags/1"}]'                                                    || "John"       | 30          | "New York"   | "5th Ave"      | ["friend"]
        '[{"op": "remove", "path": "/address/street"}]'                                            || "John"       | 30          | "New York"   | null           | ["friend", "work"]
        '[{"op": "move", "from": "/address/city", "path": "/address/street"}]'                     || "John"       | 30          | null         | "New York"     | ["friend", "work"]
        '[{"op": "copy", "from": "/name", "path": "/address/city"}]'                               || "John"       | 30          | "John"       | "5th Ave"      | ["friend", "work"]
        '[{"op": "test", "path": "/name", "value": "John"}, {"op": "replace", "path": "/name", "value": "Jane"}]' || "Jane" | 30 | "New York" | "5th Ave" | ["friend", "work"]
    }

    def "should throw JsonException when JSON Patch 'test' operation fails"() {
        given:
        def helper = new JSONOverrideHelper()
        def original = new Person(name: "John", age: 30)
        def patchJson = '[{"op": "test", "path": "/name", "value": "Jane"}, {"op": "replace", "path": "/name", "value": "Jane"}]'

        when:
        helper.applyChangesToJson(original, patchJson, Person.class, JSONOverrideHelper.PatchType.JSON_PATCH)

        then:
        thrown(jakarta.json.JsonException)
    }

    def "should return null for unsupported patch type"() {
        given:
        def helper = new JSONOverrideHelper()
        def original = new Person(name: "John", age: 30)

        when:
        def result = helper.applyChangesToJson(original, "{}", Person.class, null)

        then:
        thrown(NullPointerException)
    }

    static class Person {
        String name
        Integer age
        Address address
        List<String> tags

        // No-args constructor for Jackson
        Person() {}
    }

    static class Address {
        String city
        String street
        
        // No-args constructor for Jackson
        Address() {}
    }
}
