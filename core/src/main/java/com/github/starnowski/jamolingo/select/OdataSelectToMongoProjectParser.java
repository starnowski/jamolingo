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
      //            return new Document(); // no projection → all fields
      // TODO
      return null;
    }

    List<String> fields = new ArrayList<>();
    for (SelectItem item : selectOption.getSelectItems()) {
      if (item.isStar()) {
        //                return new Document(); // * means all fields
        // TODO
        return null;
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
    return new DefaultSelectOperatorResult(new HashSet<>(fields));
  }

  private class DefaultSelectOperatorResult implements SelectOperatorResult {

    private final Set<String> selectedFields;

    private DefaultSelectOperatorResult(Set<String> selectedFields) {
      this.selectedFields = selectedFields;
    }

    @Override
    public Set<String> getSelectedFields() {
      return selectedFields;
    }

    @Override
    public boolean isWildCard() {
      return false;
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
      return document;
    }

    @Override
    public Bson getStageObject() {
      return new Document("$project", getProjectObject());
    }
  }
}
