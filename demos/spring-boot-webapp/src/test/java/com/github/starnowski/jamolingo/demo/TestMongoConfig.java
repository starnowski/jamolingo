package com.github.starnowski.jamolingo.demo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import org.bson.Document;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
public class TestMongoConfig {

  private MongodExecutable mongodExecutable;

  @PostConstruct
  public void start() throws IOException {
    MongodStarter starter = MongodStarter.getDefaultInstance();
    int port = 27018;
    MongodConfig mongodConfig =
        MongodConfig.builder()
            .version(Version.Main.V4_4)
            .net(new Net(port, Network.localhostIsIPv6()))
            .build();

    mongodExecutable = starter.prepare(mongodConfig);
    mongodExecutable.start();
  }

  @PreDestroy
  public void stop() {
    if (mongodExecutable != null) {
      mongodExecutable.stop();
    }
  }
}
