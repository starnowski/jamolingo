package com.github.starnowski.jamolingo.common.beans

import spock.lang.Specification

class KeyValueSpec extends Specification {

    def "should create KeyValue and verify properties"() {
        given:
        def key = "testKey"
        def value = 123

        when:
        def keyValue = new KeyValue(key, value)

        then:
        keyValue.key == key
        keyValue.value == value
    }

    def "should use builder to create KeyValue"() {
        given:
        def key = "testKey"
        def value = 123

        when:
        def keyValue = KeyValue.builder()
                .withKey(key)
                .withValue(value)
                .build()

        then:
        keyValue.key == key
        keyValue.value == value
    }

    def "should verify equals and hashCode"() {
        given:
        def kv1 = new KeyValue("k1", "v1")
        def kv2 = new KeyValue("k1", "v1")
        def kv3 = new KeyValue("k1", "v2")
        def kv4 = new KeyValue("k2", "v1")

        expect:
        kv1 == kv2
        kv1.hashCode() == kv2.hashCode()
        kv1 != kv3
        kv1 != kv4
    }

    def "should verify toString"() {
        given:
        def keyValue = new KeyValue("myKey", "myValue")

        expect:
        keyValue.toString() == "KeyValue{key=myKey, value=myValue}"
    }

    def "should use withKeyValue in builder"() {
        given:
        def original = new KeyValue("originalKey", "originalValue")

        when:
        def copy = KeyValue.builder()
                .withKeyValue(original)
                .build()

        then:
        copy == original
        copy.key == original.key
        copy.value == original.value
    }
}
