package com.github.starnowski.jamolingo.core.operators.apply;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.bson.conversions.Bson;

public class DefaultApplyOperatorResult implements ApplyOperatorResult {

  private final List<Bson> stageObjects;

  public DefaultApplyOperatorResult(List<Bson> stageObjects) {
    this.stageObjects =
        stageObjects != null ? Collections.unmodifiableList(stageObjects) : Collections.emptyList();
  }

  @Override
  public List<Bson> getStageObjects() {
    return stageObjects;
  }

  @Override
  public List<String> getUsedMongoDocumentProperties() {
    return Collections.emptyList();
  }

  @Override
  public List<String> getWrittenMongoDocumentProperties() {
    return Collections.emptyList();
  }

  @Override
  public List<String> getAddedMongoDocumentProperties() {
    return Collections.emptyList();
  }

  @Override
  public List<String> getRemovedMongoDocumentProperties() {
    return Collections.emptyList();
  }

  @Override
  public boolean isDocumentShapeRedefined() {
    return true; // Apply typically redefines document shape (e.g., groupBy, aggregate)
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DefaultApplyOperatorResult that = (DefaultApplyOperatorResult) o;
    return Objects.equals(stageObjects, that.stageObjects);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stageObjects);
  }

  @Override
  public String toString() {
    return "DefaultApplyOperatorResult{" + "stageObjects=" + stageObjects + '}';
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private List<Bson> stageObjects = Collections.emptyList();

    public Builder withStageObjects(List<Bson> stageObjects) {
      this.stageObjects = stageObjects;
      return this;
    }

    public DefaultApplyOperatorResult build() {
      return new DefaultApplyOperatorResult(stageObjects);
    }
  }
}
