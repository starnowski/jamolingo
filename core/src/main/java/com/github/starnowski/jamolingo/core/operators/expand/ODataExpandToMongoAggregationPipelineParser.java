package com.github.starnowski.jamolingo.core.operators.expand;

import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import java.util.*;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmReferentialConstraint;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.bson.Document;
import org.bson.conversions.Bson;

public class ODataExpandToMongoAggregationPipelineParser {

  public ExpandOperatorResult parse(ExpandOption expandOption) {
    return parse(expandOption, DefaultExpandParserContext.builder().build());
  }

  public ExpandOperatorResult parse(
      ExpandOption expandOption, ExpandParserContext expandParserContext) {
    List<Bson> stageObjects = new ArrayList<>();
    for (ExpandItem eOption : expandOption.getExpandItems()) {
      stageObjects.addAll(prepareStageObjectsForExpandItem(eOption, expandParserContext));
    }
    return new DefaultExpandOperatorResult(stageObjects);
  }

  private Collection<? extends Bson> prepareStageObjectsForExpandItem(
      ExpandItem eOption, ExpandParserContext expandParserContext) {
    UriResource lastResource =
        eOption
            .getResourcePath()
            .getUriResourceParts()
            .get(eOption.getResourcePath().getUriResourceParts().size() - 1);
    if (!(lastResource instanceof UriResourceNavigation)) {
      return List.of();
    }
    EdmNavigationProperty navProp = ((UriResourceNavigation) lastResource).getProperty();
    EdmEntityType targetEntityType = (EdmEntityType) navProp.getType();
    String targetFullTypeName = targetEntityType.getNamespace() + "." + targetEntityType.getName();

    EdmPropertyMongoPathResolver targetResolver =
        expandParserContext.getEDMTypeMapping() != null
            ? expandParserContext.getEDMTypeMapping().get(targetFullTypeName)
            : null;

    String edmStartWith = null;
    String edmConnectFrom = null;
    String edmConnectTo = null;

    List<EdmReferentialConstraint> referentialConstraints = navProp.getReferentialConstraints();
    if (referentialConstraints.isEmpty() && navProp.getPartner() != null) {
      referentialConstraints = navProp.getPartner().getReferentialConstraints();
      if (!referentialConstraints.isEmpty()) {
        EdmReferentialConstraint constraint = referentialConstraints.get(0);
        edmStartWith = constraint.getReferencedPropertyName();
        edmConnectFrom = constraint.getReferencedPropertyName();
        edmConnectTo = constraint.getPropertyName();
      }
    } else if (!referentialConstraints.isEmpty()) {
      EdmReferentialConstraint constraint = referentialConstraints.get(0);
      edmStartWith = constraint.getPropertyName();
      edmConnectFrom = constraint.getPropertyName();
      edmConnectTo = constraint.getReferencedPropertyName();
    }

    if (edmConnectFrom != null && edmConnectTo != null) {
      String mongoStartWith = edmStartWith;
      String mongoConnectFrom = edmConnectFrom;
      String mongoConnectTo = edmConnectTo;

      if (targetResolver != null) {
        mongoConnectTo = targetResolver.resolveMongoPathForEDMPath(edmConnectTo).getMongoPath();
      }

      String targetCollection =
          expandParserContext
              .getEDMTablesToMongoDBCollections()
              .get(new KeyValue(targetEntityType.getNamespace(), targetEntityType.getName()))
              .getValue();

      if (eOption.getLevelsOption() != null && eOption.getLevelsOption().getValue() > 1) {
        List<Bson> pipeline = new ArrayList<>();
        // Adding $graphLookup
        Document graphLookup = new Document();
        graphLookup.append(
            "$graphLookup",
            new Document()
                .append("from", targetCollection)
                .append("startWith", "$" + mongoStartWith)
                .append("connectFromField", mongoConnectFrom)
                .append("connectToField", mongoConnectTo)
                .append("as", navProp.getName()));
        pipeline.add(graphLookup);
        return pipeline;
      }
    }
    return List.of();
  }

  private static class DefaultExpandOperatorResult implements ExpandOperatorResult {

    private final List<Bson> stageObjects;

    private DefaultExpandOperatorResult(List<Bson> stageObjects) {
      this.stageObjects = stageObjects;
    }

    @Override
    public List<Bson> getStageObjects() {
      return stageObjects;
    }

    @Override
    public List<String> getUsedMongoDocumentProperties() {
      return List.of();
    }

    @Override
    public List<String> getWrittenMongoDocumentProperties() {
      return List.of();
    }

    @Override
    public List<String> getAddedMongoDocumentProperties() {
      // TODO top elements for $expand
      return List.of();
    }

    @Override
    public List<String> getRemovedMongoDocumentProperties() {
      return List.of();
    }

    @Override
    public boolean isDocumentShapeRedefined() {
      return false;
    }
  }

  public static class DefaultExpandParserContext implements ExpandParserContext {
    private final Map<String, EdmPropertyMongoPathResolver> edmTypeMapping;
    private final Map<KeyValue<String, String>, KeyValue<String, String>>
        edmTablesToMongoDBCollections;

    public DefaultExpandParserContext(
        Map<String, EdmPropertyMongoPathResolver> edmTypeMapping,
        Map<KeyValue<String, String>, KeyValue<String, String>> edmTablesToMongoDBCollections) {
      this.edmTypeMapping = edmTypeMapping;
      this.edmTablesToMongoDBCollections = edmTablesToMongoDBCollections;
    }

    @Override
    public Map<String, EdmPropertyMongoPathResolver> getEDMTypeMapping() {
      return edmTypeMapping;
    }

    @Override
    public Map<KeyValue<String, String>, KeyValue<String, String>>
        getEDMTablesToMongoDBCollections() {
      return edmTablesToMongoDBCollections;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DefaultExpandParserContext that = (DefaultExpandParserContext) o;
      return Objects.equals(edmTypeMapping, that.edmTypeMapping)
          && Objects.equals(edmTablesToMongoDBCollections, that.edmTablesToMongoDBCollections);
    }

    @Override
    public int hashCode() {
      return Objects.hash(edmTypeMapping, edmTablesToMongoDBCollections);
    }

    @Override
    public String toString() {
      return "DefaultExpandParserContext{"
          + "edmTypeMapping="
          + edmTypeMapping
          + ", edmTablesToMongoDBCollections="
          + edmTablesToMongoDBCollections
          + '}';
    }

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private Map<String, EdmPropertyMongoPathResolver> edmTypeMapping = new HashMap<>();
      private Map<KeyValue<String, String>, KeyValue<String, String>>
          edmTablesToMongoDBCollections = new HashMap<>();

      public Builder withEdmTypeMapping(Map<String, EdmPropertyMongoPathResolver> edmTypeMapping) {
        this.edmTypeMapping = edmTypeMapping;
        return this;
      }

      public Builder withEdmTablesToMongoDBCollections(
          Map<KeyValue<String, String>, KeyValue<String, String>> edmTablesToMongoDBCollections) {
        this.edmTablesToMongoDBCollections = edmTablesToMongoDBCollections;
        return this;
      }

      public Builder withDefaultExpandParserContext(
          DefaultExpandParserContext defaultExpandParserContext) {
        this.edmTypeMapping =
            defaultExpandParserContext.edmTypeMapping != null
                ? new HashMap<>(defaultExpandParserContext.edmTypeMapping)
                : null;
        this.edmTablesToMongoDBCollections =
            defaultExpandParserContext.edmTablesToMongoDBCollections != null
                ? new HashMap<>(defaultExpandParserContext.edmTablesToMongoDBCollections)
                : null;
        return this;
      }

      public DefaultExpandParserContext build() {
        return new DefaultExpandParserContext(
            edmTypeMapping != null
                ? Collections.unmodifiableMap(new HashMap<>(edmTypeMapping))
                : null,
            edmTablesToMongoDBCollections != null
                ? Collections.unmodifiableMap(new HashMap<>(edmTablesToMongoDBCollections))
                : null);
      }
    }
  }
}
