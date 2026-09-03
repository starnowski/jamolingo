package com.github.starnowski.jamolingo.core.operators.apply

import org.apache.olingo.server.api.uri.queryoption.apply.BottomTop
import spock.lang.Specification

class BottomTopReflectionTest extends Specification {
    def "print BottomTop methods"() {
        expect:
        println "BottomTop methods:"
        BottomTop.class.getMethods().each { println it }
        println "BottomTop.Method enum:"
        BottomTop.Method.values().each { println it }
    }
}
