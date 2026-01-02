package com.github.starnowski.jamolingo.context;

public class DefaultEdmPathContextSearch implements EdmPathContextSearch{
    public DefaultEdmPathContextSearch(Integer mongoPathMaxDepth) {
        this.mongoPathMaxDepth = mongoPathMaxDepth;
    }

    private final Integer mongoPathMaxDepth;

    @Override
    public Integer getMongoPathMaxDepth() {
        return mongoPathMaxDepth;
    }


}
