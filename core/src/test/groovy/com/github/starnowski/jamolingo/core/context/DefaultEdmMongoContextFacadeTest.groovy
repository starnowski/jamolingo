package com.github.starnowski.jamolingo.core.context

import spock.lang.Specification

class DefaultEdmMongoContextFacadeTest extends Specification {

    def "should return rootMongoPath when getRootMongoPath is called"() {
        given:
        def rootPath = "some.root.path"
        def facade = DefaultEdmMongoContextFacade.builder()
                .withRootMongoPath(rootPath)
                .build()

        expect:
        facade.getRootMongoPath() == rootPath
    }

    def "should return null when rootMongoPath is not set"() {
        given:
        def facade = DefaultEdmMongoContextFacade.builder()
                .build()

        expect:
        facade.getRootMongoPath() == null
    }

    def "should correctly copy rootMongoPath from another facade using builder"() {
        given:
        def rootPath = "another.root.path"
        def originalFacade = DefaultEdmMongoContextFacade.builder()
                .withRootMongoPath(rootPath)
                .build()

        when:
        def newFacade = DefaultEdmMongoContextFacade.builder()
                .withDefaultEdmMongoContextFacade(originalFacade)
                .build()

        then:
        newFacade.getRootMongoPath() == rootPath
    }
}
