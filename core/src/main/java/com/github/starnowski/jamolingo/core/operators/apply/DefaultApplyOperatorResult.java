package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.operators.UsedNavigationProperty;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.bson.conversions.Bson;

/** Default implementation of ApplyOperatorResult. */
public class DefaultApplyOperatorResult implements ApplyOperatorResult {

  private final List<Bson> stageObjects;
  private final List<String> usedMongoDocumentProperties;
  private final List<String> writtenMongoDocumentProperties;
  private final List<String> addedMongoDocumentProperties;
  private final List<String> removedMongoDocumentProperties;
  private final boolean documentShapeRedefined;
  private final List<UsedNavigationProperty> usedNavigationProperties;

  /**
   * Constructs a new DefaultApplyOperatorResult.
   *
   * @param stageObjects the list of MongoDB aggregation pipeline stages
   * @param usedMongoDocumentProperties the list of MongoDB document properties read by the operator
   * @param writtenMongoDocumentProperties the list of MongoDB document properties written by the
   *     operator
   * @param addedMongoDocumentProperties the list of MongoDB document properties added by the
   *     operator
   * @param removedMongoDocumentProperties the list of MongoDB document properties removed by the
   *     operator
   * @param documentShapeRedefined true if the document shape is redefined, false otherwise
   */
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

  /**
   * Constructs a new DefaultApplyOperatorResult including used navigation properties.
   *
   * @param stageObjects the list of MongoDB aggregation pipeline stages
   * @param usedMongoDocumentProperties the list of MongoDB document properties read by the operator
   * @param writtenMongoDocumentProperties the list of MongoDB document properties written by the
   *     operator
   * @param addedMongoDocumentProperties the list of MongoDB document properties added by the
   *     operator
   * @param removedMongoDocumentProperties the list of MongoDB document properties removed by the
   *     operator
   * @param documentShapeRedefined true if the document shape is redefined, false otherwise
   * @param usedNavigationProperties the list of navigation properties referenced by the operator
   */
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
  public List<Bson> getStageObjects() {
    return stageObjects;
  }

  @Override
  public List<String> getUsedMongoDocumentProperties() {
    return usedMongoDocumentProperties;
  }

  @Override
  public List<String> getWrittenMongoDocumentProperties() {
    return writtenMongoDocumentProperties;
  }

  @Override
  public List<String> getAddedMongoDocumentProperties() {
    return addedMongoDocumentProperties;
  }

  @Override
  public List<String> getRemovedMongoDocumentProperties() {
    return removedMongoDocumentProperties;
  }

  @Override
  public boolean isDocumentShapeRedefined() {
    return documentShapeRedefined;
  }

  @Override
  public List<UsedNavigationProperty> getUsedNavigationProperties() {
    return usedNavigationProperties;
  }

  @Override
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

  /**
   * Creates a new Builder for DefaultApplyOperatorResult.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for DefaultApplyOperatorResult. */
  public static class Builder {
    private List<Bson> stageObjects = Collections.emptyList();
    private List<String> usedMongoDocumentProperties = Collections.emptyList();
    private List<String> writtenMongoDocumentProperties = Collections.emptyList();
    private List<String> addedMongoDocumentProperties = Collections.emptyList();
    private List<String> removedMongoDocumentProperties = Collections.emptyList();
    private boolean documentShapeRedefined = false;
    private List<UsedNavigationProperty> usedNavigationProperties = Collections.emptyList();

    /**
     * Copies all properties from an existing DefaultApplyOperatorResult.
     *
     * @param defaultApplyOperatorResult the instance to copy from
     * @return this builder
     */
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

    /**
     * Sets the list of MongoDB aggregation pipeline stages.
     *
     * @param stageObjects the list of stage objects
     * @return this builder
     */
    public Builder withStageObjects(List<Bson> stageObjects) {
      this.stageObjects = stageObjects;
      return this;
    }

    /**
     * Sets the list of MongoDB document properties read by the operator.
     *
     * @param usedMongoDocumentProperties the list of used document properties
     * @return this builder
     */
    public Builder withUsedMongoDocumentProperties(List<String> usedMongoDocumentProperties) {
      this.usedMongoDocumentProperties = usedMongoDocumentProperties;
      return this;
    }

    /**
     * Sets the list of MongoDB document properties written by the operator.
     *
     * @param writtenMongoDocumentProperties the list of written document properties
     * @return this builder
     */
    public Builder withWrittenMongoDocumentProperties(List<String> writtenMongoDocumentProperties) {
      this.writtenMongoDocumentProperties = writtenMongoDocumentProperties;
      return this;
    }

    /**
     * Sets the list of MongoDB document properties added by the operator.
     *
     * @param addedMongoDocumentProperties the list of added document properties
     * @return this builder
     */
    public Builder withAddedMongoDocumentProperties(List<String> addedMongoDocumentProperties) {
      this.addedMongoDocumentProperties = addedMongoDocumentProperties;
      return this;
    }

    /**
     * Sets the list of MongoDB document properties removed by the operator.
     *
     * @param removedMongoDocumentProperties the list of removed document properties
     * @return this builder
     */
    public Builder withRemovedMongoDocumentProperties(List<String> removedMongoDocumentProperties) {
      this.removedMongoDocumentProperties = removedMongoDocumentProperties;
      return this;
    }

    /**
     * Sets whether the document shape is redefined by the operator.
     *
     * @param documentShapeRedefined true if the document shape is redefined, false otherwise
     * @return this builder
     */
    public Builder withDocumentShapeRedefined(boolean documentShapeRedefined) {
      this.documentShapeRedefined = documentShapeRedefined;
      return this;
    }

    /**
     * Sets the list of navigation properties referenced by the operator.
     *
     * @param usedNavigationProperties the list of used navigation properties
     * @return this builder
     */
    public Builder withUsedNavigationProperties(
        List<UsedNavigationProperty> usedNavigationProperties) {
      this.usedNavigationProperties = usedNavigationProperties;
      return this;
    }

    /**
     * Builds a new DefaultApplyOperatorResult instance.
     *
     * @return the created DefaultApplyOperatorResult
     */
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
