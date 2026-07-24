package com.github.starnowski.jamolingo.core.operators.expand;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ExpandElement {
  private final String edmPath;
  private final String mongoPath;
  private final FetchType fetchType;
  private final Integer level;
  private final Boolean maxLevelRequest;
  private final Map<String, ExpandElement> expandElements;
  private final String localKeyProperty;
  private final String foreignKeyProperty;

  public ExpandElement(
      String edmPath,
      String mongoPath,
      FetchType fetchType,
      Integer level,
      Boolean maxLevelRequest,
      Map<String, ExpandElement> expandElements,
      String localKeyProperty,
      String foreignKeyProperty) {
    this.edmPath = edmPath;
    this.mongoPath = mongoPath;
    this.fetchType = fetchType;
    this.level = level;
    this.maxLevelRequest = maxLevelRequest;
    this.expandElements =
        expandElements == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new HashMap<>(expandElements));
    this.localKeyProperty = localKeyProperty;
    this.foreignKeyProperty = foreignKeyProperty;
  }

  public String getEdmPath() {
    return edmPath;
  }

  public String getMongoPath() {
    return mongoPath;
  }

  public FetchType getFetchType() {
    return fetchType;
  }

  public Integer getLevel() {
    return level;
  }

  public Boolean getMaxLevelRequest() {
    return maxLevelRequest;
  }

  public Map<String, ExpandElement> getExpandElements() {
    return expandElements;
  }

  public String getLocalKeyProperty() {
    return localKeyProperty;
  }

  public String getForeignKeyProperty() {
    return foreignKeyProperty;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExpandElement that = (ExpandElement) o;
    return Objects.equals(edmPath, that.edmPath)
        && Objects.equals(mongoPath, that.mongoPath)
        && fetchType == that.fetchType
        && Objects.equals(level, that.level)
        && Objects.equals(maxLevelRequest, that.maxLevelRequest)
        && Objects.equals(expandElements, that.expandElements)
        && Objects.equals(localKeyProperty, that.localKeyProperty)
        && Objects.equals(foreignKeyProperty, that.foreignKeyProperty);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        edmPath,
        mongoPath,
        fetchType,
        level,
        maxLevelRequest,
        expandElements,
        localKeyProperty,
        foreignKeyProperty);
  }

  @Override
  public String toString() {
    return "ExpandElement{"
        + "edmPath='"
        + edmPath
        + '\''
        + ", mongoPath='"
        + mongoPath
        + '\''
        + ", fetchType="
        + fetchType
        + ", level="
        + level
        + ", maxLevelRequest="
        + maxLevelRequest
        + ", expandElements="
        + expandElements
        + ", localKeyProperty='"
        + localKeyProperty
        + '\''
        + ", foreignKeyProperty='"
        + foreignKeyProperty
        + '\''
        + '}';
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String edmPath;
    private String mongoPath;
    private FetchType fetchType;
    private Integer level;
    private Boolean maxLevelRequest;
    private Map<String, ExpandElement> expandElements = new HashMap<>();
    private String localKeyProperty;
    private String foreignKeyProperty;

    public Builder withExpandElement(ExpandElement expandElement) {
      this.edmPath = expandElement.edmPath;
      this.mongoPath = expandElement.mongoPath;
      this.fetchType = expandElement.fetchType;
      this.level = expandElement.level;
      this.maxLevelRequest = expandElement.maxLevelRequest;
      this.expandElements = new HashMap<>(expandElement.expandElements);
      this.localKeyProperty = expandElement.localKeyProperty;
      this.foreignKeyProperty = expandElement.foreignKeyProperty;
      return this;
    }

    public Builder withEdmPath(String edmPath) {
      this.edmPath = edmPath;
      return this;
    }

    public Builder withMongoPath(String mongoPath) {
      this.mongoPath = mongoPath;
      return this;
    }

    public Builder withFetchType(FetchType fetchType) {
      this.fetchType = fetchType;
      return this;
    }

    public Builder withLevel(Integer level) {
      this.level = level;
      return this;
    }

    public Builder withMaxLevelRequest(Boolean maxLevelRequest) {
      this.maxLevelRequest = maxLevelRequest;
      return this;
    }

    public Builder withExpandElements(Map<String, ExpandElement> expandElements) {
      this.expandElements =
          expandElements != null ? new HashMap<>(expandElements) : new HashMap<>();
      return this;
    }

    public Builder withExpandElement(String key, ExpandElement expandElement) {
      this.expandElements.put(key, expandElement);
      return this;
    }

    public Builder withLocalKeyProperty(String localKeyProperty) {
      this.localKeyProperty = localKeyProperty;
      return this;
    }

    public Builder withForeignKeyProperty(String foreignKeyProperty) {
      this.foreignKeyProperty = foreignKeyProperty;
      return this;
    }

    public ExpandElement build() {
      return new ExpandElement(
          edmPath,
          mongoPath,
          fetchType,
          level,
          maxLevelRequest,
          expandElements,
          localKeyProperty,
          foreignKeyProperty);
    }
  }
}
