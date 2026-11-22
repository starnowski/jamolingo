package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.operators.UsedNavigationProperty;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.bson.conversions.Bson;

/** DefaultApplyOperatorResult type. */
public class DefaultApplyOperatorResult implements ApplyOperatorResult {

  private final List<Bson> stageObjects;
  private final List<String> usedMongoDocumentProperties;
  private final List<String> writtenMongoDocumentProperties;
  private final List<String> addedMongoDocumentProperties;
  private final List<String> removedMongoDocumentProperties;
  private final boolean documentShapeRedefined;
  private final List<UsedNavigationProperty> usedNavigationProperties;

  /** DefaultApplyOperatorResult constructor. */
  public DefaultApplyOperatorResult(
      List<Bson> stageObjects,
      List<String> usedMongoDocumentProperties,
      List<String> writtenMongoDocumentProperties,
      List<String> addedMongoDocumentProperties,
      List<String> removedMongoDocumentProperties,
      boolean documentShapeRedefined) {
    this(
        stageObjects,
        usedMongoDocumentProperties,
        writtenMongoDocumentProperties,
        addedMongoDocumentProperties,
        removedMongoDocumentProperties,
        documentShapeRedefined,
        Collections.emptyList());
  }

  /** DefaultApplyOperatorResult constructor with used navigation properties. */
  public DefaultApplyOperatorResult(
      List<Bson> stageObjects,
      List<String> usedMongoDocumentProperties,
      List<String> writtenMongoDocumentProperties,
      List<String> addedMongoDocumentProperties,
      List<String> removedMongoDocumentProperties,
      boolean documentShapeRedefined,
      List<UsedNavigationProperty> usedNavigationProperties) {
    this.stageObjects =
        stageObjects != null ? Collections.unmodifiableList(stageObjects) : Collections.emptyList();
    this.usedMongoDocumentProperties =
        usedMongoDocumentProperties != null
            ? Collections.unmodifiableList(usedMongoDocumentProperties)
            : Collections.emptyList();
    this.writtenMongoDocumentProperties =
        writtenMongoDocumentProperties != null
            ? Collections.unmodifiableList(writtenMongoDocumentProperties)
            : Collections.emptyList();
    this.addedMongoDocumentProperties =
        addedMongoDocumentProperties != null
            ? Collections.unmodifiableList(addedMongoDocumentProperties)
            : Collections.emptyList();
    this.removedMongoDocumentProperties =
        removedMongoDocumentProperties != null
            ? Collections.unmodifiableList(removedMongoDocumentProperties)
            : Collections.emptyList();
    this.documentShapeRedefined = documentShapeRedefined;
    this.usedNavigationProperties =
        usedNavigationProperties != null
            ? Collections.unmodifiableList(usedNavigationProperties)
            : Collections.emptyList();
  }

  @Override
  /** getStageObjects method. */
  public List<Bson> getStageObjects() {
    return stageObjects;
  }

  @Override
  /** getUsedMongoDocumentProperties method. */
  public List<String> getUsedMongoDocumentProperties() {
    return usedMongoDocumentProperties;
  }

  @Override
  /** getWrittenMongoDocumentProperties method. */
  public List<String> getWrittenMongoDocumentProperties() {
    return writtenMongoDocumentProperties;
  }

  @Override
  /** getAddedMongoDocumentProperties method. */
  public List<String> getAddedMongoDocumentProperties() {
    return addedMongoDocumentProperties;
  }

  @Override
  /** getRemovedMongoDocumentProperties method. */
  public List<String> getRemovedMongoDocumentProperties() {
    return removedMongoDocumentProperties;
  }

  @Override
  /** isDocumentShapeRedefined method. */
  public boolean isDocumentShapeRedefined() {
    return documentShapeRedefined;
  }

  @Override
  /** getUsedNavigationProperties method. */
  public List<UsedNavigationProperty> getUsedNavigationProperties() {
    return usedNavigationProperties;
  }

  @Override
  /** equals method. */
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DefaultApplyOperatorResult that = (DefaultApplyOperatorResult) o;
    return documentShapeRedefined == that.documentShapeRedefined
        && Objects.equals(stageObjects, that.stageObjects)
        && Objects.equals(usedMongoDocumentProperties, that.usedMongoDocumentProperties)
        && Objects.equals(writtenMongoDocumentProperties, that.writtenMongoDocumentProperties)
        && Objects.equals(addedMongoDocumentProperties, that.addedMongoDocumentProperties)
        && Objects.equals(removedMongoDocumentProperties, that.removedMongoDocumentProperties)
        && Objects.equals(usedNavigationProperties, that.usedNavigationProperties);
  }

  @Override
  /** hashCode method. */
  public int hashCode() {
    return Objects.hash(
        stageObjects,
        usedMongoDocumentProperties,
        writtenMongoDocumentProperties,
        addedMongoDocumentProperties,
        removedMongoDocumentProperties,
        documentShapeRedefined,
        usedNavigationProperties);
  }

  @Override
  /** toString method. */
  public String toString() {
    return "DefaultApplyOperatorResult{"
        + "stageObjects="
        + stageObjects
        + ", usedMongoDocumentProperties="
        + usedMongoDocumentProperties
        + ", writtenMongoDocumentProperties="
        + writtenMongoDocumentProperties
        + ", addedMongoDocumentProperties="
        + addedMongoDocumentProperties
        + ", removedMongoDocumentProperties="
        + removedMongoDocumentProperties
        + ", documentShapeRedefined="
        + documentShapeRedefined
        + ", usedNavigationProperties="
        + usedNavigationProperties
        + '}';
  }

  /** builder method. */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private List<Bson> stageObjects = Collections.emptyList();
    private List<String> usedMongoDocumentProperties = Collections.emptyList();
    private List<String> writtenMongoDocumentProperties = Collections.emptyList();
    private List<String> addedMongoDocumentProperties = Collections.emptyList();
    private List<String> removedMongoDocumentProperties = Collections.emptyList();
    private boolean documentShapeRedefined = false;
    private List<UsedNavigationProperty> usedNavigationProperties = Collections.emptyList();

    /** Copies all properties from an existing DefaultApplyOperatorResult. */
    public Builder withDefaultApplyOperatorResult(
        DefaultApplyOperatorResult defaultApplyOperatorResult) {
      this.stageObjects = defaultApplyOperatorResult.stageObjects;
      this.usedMongoDocumentProperties = defaultApplyOperatorResult.usedMongoDocumentProperties;
      this.writtenMongoDocumentProperties =
          defaultApplyOperatorResult.writtenMongoDocumentProperties;
      this.addedMongoDocumentProperties = defaultApplyOperatorResult.addedMongoDocumentProperties;
      this.removedMongoDocumentProperties =
          defaultApplyOperatorResult.removedMongoDocumentProperties;
      this.documentShapeRedefined = defaultApplyOperatorResult.documentShapeRedefined;
      this.usedNavigationProperties = defaultApplyOperatorResult.usedNavigationProperties;
      return this;
    }

    /** withStageObjects method. */
    public Builder withStageObjects(List<Bson> stageObjects) {
      this.stageObjects = stageObjects;
      return this;
    }

    public Builder withUsedMongoDocumentProperties(List<String> usedMongoDocumentProperties) {
      this.usedMongoDocumentProperties = usedMongoDocumentProperties;
      return this;
    }

    public Builder withWrittenMongoDocumentProperties(List<String> writtenMongoDocumentProperties) {
      this.writtenMongoDocumentProperties = writtenMongoDocumentProperties;
      return this;
    }

    public Builder withAddedMongoDocumentProperties(List<String> addedMongoDocumentProperties) {
      this.addedMongoDocumentProperties = addedMongoDocumentProperties;
      return this;
    }

    public Builder withRemovedMongoDocumentProperties(List<String> removedMongoDocumentProperties) {
      this.removedMongoDocumentProperties = removedMongoDocumentProperties;
      return this;
    }

    public Builder withDocumentShapeRedefined(boolean documentShapeRedefined) {
      this.documentShapeRedefined = documentShapeRedefined;
      return this;
    }

    public Builder withUsedNavigationProperties(
        List<UsedNavigationProperty> usedNavigationProperties) {
      this.usedNavigationProperties = usedNavigationProperties;
      return this;
    }

    /** build method. */
    public DefaultApplyOperatorResult build() {
      return new DefaultApplyOperatorResult(
          stageObjects,
          usedMongoDocumentProperties,
          writtenMongoDocumentProperties,
          addedMongoDocumentProperties,
          removedMongoDocumentProperties,
          documentShapeRedefined,
          usedNavigationProperties);
    }
  }
}
