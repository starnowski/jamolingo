package com.github.starnowski.jamolingo;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongoCmdOptions;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.HashMap;
import java.util.Map;

public class EmbeddedMongoResource implements QuarkusTestResourceLifecycleManager {

  private MongodExecutable mongodExecutable;
  private MongodProcess mongodProcess;

  @Override
  public Map<String, String> start() {
    try {
      MongodStarter starter = MongodStarter.getDefaultInstance();
      int port = 27018; // You can choose any available port
      MongodConfig mongodConfig =
          MongodConfig.builder()
              .version(Version.Main.V6_0)
              .net(new de.flapdoodle.embed.mongo.config.Net(port, Network.localhostIsIPv6()))
              .replication(new Storage(null, null, 0)) // Enable journaling
              .cmdOptions(MongoCmdOptions.builder().useNoJournal(false).build())
              .build();
      mongodExecutable = starter.prepare(mongodConfig);
      mongodProcess = mongodExecutable.start();

      Map<String, String> config = new HashMap<>();
      config.put("quarkus.mongodb.connection-string", "mongodb://localhost:" + port);
      return config;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void stop() {
    if (mongodProcess != null) {
      mongodProcess.stop();
    }
    if (mongodExecutable != null) {
      mongodExecutable.stop();
    }
  }
}
