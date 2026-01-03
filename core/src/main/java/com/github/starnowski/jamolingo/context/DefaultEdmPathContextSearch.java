package com.github.starnowski.jamolingo.context;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultEdmPathContextSearch that = (DefaultEdmPathContextSearch) o;
        return Objects.equals(mongoPathMaxDepth, that.mongoPathMaxDepth);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mongoPathMaxDepth);
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
