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

//    // Manually insert data to be sure it is there
//    try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27018")) {
//      MongoDatabase database = mongoClient.getDatabase("demos");
//      MongoCollection<Document> collection = database.getCollection("items");
//      collection.insertMany(
//          List.of(
//              Document.parse(
//                  "{ \"plainString\": \"eOMtThyhVNLWUZNRcBaQKxI\", \"tags\": [ \"word wide web\", \"spider trap\" ], \"active\": true }"),
//              Document.parse(
//                  "{ \"plainString\": \"Some text\", \"tags\": [ \"JxkyvRnL\", \"spiderweb\" ], \"active\": true }"),
//              Document.parse(
//                  "{ \"plainString\": \"Poem\", \"tags\": [ \"developer\", \"founder\", \"visioner\", \"focus\", \"llm agent\", \"LLM\" ], \"active\": true }"),
//              Document.parse(
//                  "{ \"plainString\": \"Mario\", \"tags\": [ \"star trek\", \"star wars\" ] }"),
//              Document.parse(
//                  "{ \"plainString\": \"Oleksa\", \"tags\": [ \"star trek\", \"star wars\", \"starlord\" ] }"),
//              Document.parse("{ \"plainString\": \"example1\" }"),
//              Document.parse("{ \"plainString\": \"example2\" }")));
//    }
  }

  @PreDestroy
  public void stop() {
    if (mongodExecutable != null) {
      mongodExecutable.stop();
    }
  }
}
