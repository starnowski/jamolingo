package com.github.starnowski.jamolingo;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Collections;
import java.util.Map;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class MongoAtlasResource implements QuarkusTestResourceLifecycleManager {

  private FixedPortMongoAtlasContainer mongoAtlasContainer;

  @Override
  public Map<String, String> start() {
    mongoAtlasContainer = new FixedPortMongoAtlasContainer("mongodb/mongodb-atlas-local:7.0.9");
    mongoAtlasContainer.bindFixedPort(27019, 27017);
    mongoAtlasContainer.waitingFor(Wait.forListeningPort());

    mongoAtlasContainer.start();

    return Collections.singletonMap(
        "quarkus.mongodb.connection-string", "mongodb://localhost:27019");
  }

  @Override
  public void stop() {
    if (mongoAtlasContainer != null) {
      mongoAtlasContainer.stop();
    }
  }

  private static class FixedPortMongoAtlasContainer
      extends GenericContainer<FixedPortMongoAtlasContainer> {
    public FixedPortMongoAtlasContainer(String image) {
      super(image);
    }

    public void bindFixedPort(int hostPort, int containerPort) {
      super.addFixedExposedPort(hostPort, containerPort);
    }
  }
}
