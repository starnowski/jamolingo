package com.github.starnowski.jamolingo.core.operators.apply

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver
import org.apache.olingo.server.api.uri.queryoption.ApplyItem
import spock.lang.Specification

class TopItemParserTest extends Specification {
    def "should throw unsupported exception"() {
        given:
        def parser = new TopItemParser()

        when:
        parser.parse(Mock(ApplyItem), Mock(EdmPropertyMongoPathResolver))

        then:
        thrown(UnsupportedOperationException)
    }
}
