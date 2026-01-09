package com.github.starnowski.jamolingo.junit5;

public @interface MongoDocument {
  String collection();

  String database();

  String bsonFilePath();
}
