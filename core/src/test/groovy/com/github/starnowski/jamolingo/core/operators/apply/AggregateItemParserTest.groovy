package com.github.starnowski.jamolingo.core.operators.apply

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver
import org.apache.olingo.server.api.uri.queryoption.ApplyItem
import spock.lang.Specification

class AggregateItemParserTest extends Specification {
    def "should throw IllegalArgumentException when non-Aggregate item is passed"() {
        given:
        def parser = new AggregateItemParser()

        when:
        parser.parse(Mock(ApplyItem), Mock(EdmPropertyMongoPathResolver))

        then:
        thrown(IllegalArgumentException)
    }
}
