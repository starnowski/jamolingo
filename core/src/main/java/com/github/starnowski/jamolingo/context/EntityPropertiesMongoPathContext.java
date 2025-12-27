package com.github.starnowski.jamolingo.context;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class EntityPropertiesMongoPathContext {
    public EntityPropertiesMongoPathContext(
            Map<String, MongoPathEntry> edmToMongoPath, Map<String, MongoPathEntry> circularEdmPaths) {
        this.edmToMongoPath = Collections.unmodifiableMap(edmToMongoPath);
        this.circularEdmPaths = Collections.unmodifiableMap(circularEdmPaths);
    }

    public Map<String, MongoPathEntry> getEdmToMongoPath() {
        return edmToMongoPath;
    }

    public Map<String, MongoPathEntry> getCircularEdmPaths() {
        return circularEdmPaths;
    }

    @Override
    public String toString() {
        return "EntityPropertiesMongoPathContext{"
                + "edmToMongoPath="
                + edmToMongoPath
                + ", circularEdmPaths="
                + circularEdmPaths
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EntityPropertiesMongoPathContext that = (EntityPropertiesMongoPathContext) o;
        return Objects.equals(edmToMongoPath, that.edmToMongoPath)
                && Objects.equals(circularEdmPaths, that.circularEdmPaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edmToMongoPath, circularEdmPaths);
    }

    private final Map<String, MongoPathEntry> edmToMongoPath;
    private final Map<String, MongoPathEntry> circularEdmPaths;
}