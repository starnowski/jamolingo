package com.github.starnowski.jamolingo

import org.apache.olingo.commons.api.edm.Edm
import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider
import org.apache.olingo.commons.core.edm.EdmProviderImpl
import org.apache.olingo.server.core.MetadataParser
import org.bson.Document
import org.bson.conversions.Bson
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

class AbstractSpecification extends Specification {

    def Bson loadBsonFromFile(String filePath) {
        String bson = Files.readString(Paths.get(new File(getClass().getClassLoader().getResource(filePath).getFile()).getPath()))
        Document.parse(bson)
    }

    def Edm  loadEmdProvider(String filePath){
        Reader reader = new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(filePath),
                StandardCharsets.UTF_8
        )
        // Parse it into a CsdlEdmProvider
        MetadataParser parser = new MetadataParser()
        CsdlEdmProvider provider = parser.buildEdmProvider(reader)

        // Build Edm model from provider
        return new EdmProviderImpl(provider)
    }
}
