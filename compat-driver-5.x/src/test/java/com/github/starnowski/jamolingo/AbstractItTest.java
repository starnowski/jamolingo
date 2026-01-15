package com.github.starnowski.jamolingo;

import com.github.starnowski.jamolingo.junit5.QuarkusMongoDataLoaderExtension;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.xml.stream.XMLStreamException;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider;
import org.apache.olingo.commons.core.edm.EdmProviderImpl;
import org.apache.olingo.server.core.MetadataParser;
import org.bson.Document;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(QuarkusMongoDataLoaderExtension.class)
public abstract class AbstractItTest {

  protected Edm loadEmdProvider(String filePath) throws XMLStreamException {
    Reader reader =
        new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(filePath), StandardCharsets.UTF_8);
    // Parse it into a CsdlEdmProvider
    MetadataParser parser = new MetadataParser();
    CsdlEdmProvider provider = parser.buildEdmProvider(reader);

    // Build Edm model from provider
    return new EdmProviderImpl(provider);
  }

  protected Document loadDocument(String filePath) throws IOException {

    String json =
        Files.readString(
            Paths.get(
                new File(getClass().getClassLoader().getResource(filePath).getFile()).getPath()));
    return Document.parse(json);
  }
}
