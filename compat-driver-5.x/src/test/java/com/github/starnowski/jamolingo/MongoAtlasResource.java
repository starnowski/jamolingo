package com.github.starnowski.jamolingo;

import com.mongodb.client.MongoCollection;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import org.bson.Document;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class MongoAtlasResource implements QuarkusTestResourceLifecycleManager {

  private GenericContainer<?> mongoAtlasContainer;

  @Override
  public Map<String, String> start() {
    mongoAtlasContainer =
        new GenericContainer<>("mongodb/mongodb-atlas-local:7.0.11")
            .withPrivilegedMode(true)
            .withCreateContainerCmdModifier(
                cmd -> {
                  cmd.getHostConfig().withMemory(4 * 1024 * 1024 * 1024L);
                  cmd.getHostConfig().withShmSize(2 * 1024 * 1024 * 1024L);
                })
            .withExposedPorts(27017, 27027)
            .withEnv("MONGOT_LOG_FILE", "/dev/stdout")
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)));

    mongoAtlasContainer.start();
    Wait.forLogMessage(".*Starting TCP server.*", 2)
        .withStartupTimeout(Duration.ofSeconds(30))
        .waitUntilReady(mongoAtlasContainer);
    Wait.forLogMessage(".*Starting message server.*", 2)
        .withStartupTimeout(Duration.ofSeconds(30))
        .waitUntilReady(mongoAtlasContainer);
    String connectionString =
        String.format(
            "mongodb://%s:%d/?directConnection=true",
            mongoAtlasContainer.getHost(), mongoAtlasContainer.getMappedPort(27017));
    return Collections.singletonMap("quarkus.mongodb.connection-string", connectionString);
  }

  private void ensureSearchIndex(MongoCollection<Document> collection) {
    try {
      collection.createSearchIndex(
          "test_index", new Document("mappings", new Document("dynamic", true)));
      // Wait for index to be ready
      while (true) {
        boolean ready = false;
        for (Document index : collection.listSearchIndexes()) {
          if ("test_index".equals(index.getString("name"))
              && "READY".equals(index.getString("status"))) {
            ready = true;
            break;
          }
        }
        if (ready) break;
        Thread.sleep(500);
      }
    } catch (Exception e) {
      // Index might already exist
    }
  }

  @Override
  public void stop() {
    if (mongoAtlasContainer != null) {
      mongoAtlasContainer.stop();
    }
  }
}
