package com.github.starnowski.jamolingo.core.operators.apply;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.bson.conversions.Bson;

/** DefaultApplyOperatorResult type. */
public class DefaultApplyOperatorResult implements ApplyOperatorResult {

  private final List<Bson> stageObjects;

  /** DefaultApplyOperatorResult constructor. */
  public DefaultApplyOperatorResult(List<Bson> stageObjects) {
    this.stageObjects =
        stageObjects != null ? Collections.unmodifiableList(stageObjects) : Collections.emptyList();
  }

  @Override
  /** getStageObjects method. */
  public List<Bson> getStageObjects() {
    return stageObjects;
  }

  @Override
  /** getUsedMongoDocumentProperties method. */
  public List<String> getUsedMongoDocumentProperties() {
    return Collections.emptyList();
  }

  @Override
  /** getWrittenMongoDocumentProperties method. */
  public List<String> getWrittenMongoDocumentProperties() {
    return Collections.emptyList();
  }

  @Override
  /** getAddedMongoDocumentProperties method. */
  public List<String> getAddedMongoDocumentProperties() {
    return Collections.emptyList();
  }

  @Override
  /** getRemovedMongoDocumentProperties method. */
  public List<String> getRemovedMongoDocumentProperties() {
    return Collections.emptyList();
  }

  @Override
  /** isDocumentShapeRedefined method. */
  public boolean isDocumentShapeRedefined() {
    return true; // Apply typically redefines document shape (e.g., groupBy, aggregate)
  }

  @Override
  /** equals method. */
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DefaultApplyOperatorResult that = (DefaultApplyOperatorResult) o;
    return Objects.equals(stageObjects, that.stageObjects);
  }

  @Override
  /** hashCode method. */
  public int hashCode() {
    return Objects.hash(stageObjects);
  }

  @Override
  /** toString method. */
  public String toString() {
    return "DefaultApplyOperatorResult{" + "stageObjects=" + stageObjects + '}';
  }

  /** builder method. */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private List<Bson> stageObjects = Collections.emptyList();

    /** withStageObjects method. */
    public Builder withStageObjects(List<Bson> stageObjects) {
      this.stageObjects = stageObjects;
      return this;
    }

    /** build method. */
    public DefaultApplyOperatorResult build() {
      return new DefaultApplyOperatorResult(stageObjects);
    }
  }
}
