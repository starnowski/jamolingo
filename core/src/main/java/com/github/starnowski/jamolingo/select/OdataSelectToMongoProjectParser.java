package com.github.starnowski.jamolingo.select;

import java.util.*;
import java.util.stream.Collectors;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.bson.Document;
import org.bson.conversions.Bson;

public class OdataSelectToMongoProjectParser {
  public SelectOperatorResult parse(SelectOption selectOption) {
    if (selectOption == null || selectOption.getSelectItems().isEmpty()) {
      // TODO check if _id is part of available register property
      return new DefaultSelectOperatorResult(new HashSet<>(), true, true);
    }

    List<String> fields = new ArrayList<>();
    for (SelectItem item : selectOption.getSelectItems()) {
      if (item.isStar()) {
        // TODO check if _id is part of available register property
        return new DefaultSelectOperatorResult(new HashSet<>(), true, true);
      }
      if (!item.getResourcePath().getUriResourceParts().stream()
          .allMatch(p -> p instanceof UriResourceProperty)) {
        throw new RuntimeException("Invalid select parameter " + item.getResourcePath());
      }
      String propertyName =
          item.getResourcePath().getUriResourceParts().stream()
              .map(p -> ((UriResourceProperty) p).getProperty().getName())
              .collect(Collectors.joining("."));
      fields.add(propertyName);
    }
    // TODO check if _id is part of available register property
    return new DefaultSelectOperatorResult(new HashSet<>(fields), true, false);
  }

  private static class DefaultSelectOperatorResult implements SelectOperatorResult {

    private final Set<String> selectedFields;
    private final boolean removeIdPropertyIfNotSpecified;
    private final boolean wildCard;

    private DefaultSelectOperatorResult(
        Set<String> selectedFields, boolean removeIdPropertyIfNotSpecified, boolean wildCard) {
      this.selectedFields = selectedFields;
      this.removeIdPropertyIfNotSpecified = removeIdPropertyIfNotSpecified;
      this.wildCard = wildCard;
    }

    @Override
    public Set<String> getSelectedFields() {
      return selectedFields;
    }

    @Override
    public boolean isWildCard() {
      return wildCard;
    }

    @Override
    public Bson getProjectObject() {
      Document document =
          new Document(
              Map.ofEntries(
                  getSelectedFields().stream()
                      .distinct()
                      .map(field -> Map.entry(field, 1))
                      .toList()
                      .toArray(new Map.Entry[0])));
      if (removeIdPropertyIfNotSpecified && !document.containsKey("_id")) {
        document.append("_id", 0);
      }
      return document;
    }

    @Override
    public Bson getStageObject() {
      return new Document("$project", getProjectObject());
    }

    @Override
    public List<Bson> getStagesObjects() {
      return Collections.singletonList(getStageObject());
    }

    @Override
    public List<String> getUsedMongoDocumentProperties() {
      return new ArrayList<>(selectedFields);
    }

    @Override
    public List<String> getProducedMongoDocumentProperties() {
      return new ArrayList<>(selectedFields);
    }
  }
}
