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

    @Override
    public String toString() {
        return "DefaultEdmPathContextSearch{" +
                "mongoPathMaxDepth=" + mongoPathMaxDepth +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer mongoPathMaxDepth;

        public Builder withMongoPathMaxDepth(Integer mongoPathMaxDepth) {
            this.mongoPathMaxDepth = mongoPathMaxDepth;
            return this;
        }

        public DefaultEdmPathContextSearch build() {
            return new DefaultEdmPathContextSearch(mongoPathMaxDepth);
        }
    }
}
