package com.github.starnowski.jamolingo.select;

import com.github.starnowski.jamolingo.core.api.DefaultEdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import java.util.*;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Parses OData $select system query options and translates them into MongoDB $project aggregation
 * stages.
 */
public class OdataSelectToMongoProjectParser {

  /**
   * Parses the given SelectOption using a default context facade.
   *
   * @param selectOption the OData select option to parse
   * @return the result of the parsing containing the MongoDB projection
   */
  public SelectOperatorResult parse(SelectOption selectOption) {
    return parse(selectOption, DefaultEdmMongoContextFacade.builder().build());
  }

  /**
   * Parses the given SelectOption using the provided context facade.
   *
   * @param selectOption the OData select option to parse
   * @param edmMongoContextFacade the context facade for resolving paths
   * @return the result of the parsing containing the MongoDB projection
   */
  public SelectOperatorResult parse(
      SelectOption selectOption, EdmMongoContextFacade edmMongoContextFacade) {
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
          edmMongoContextFacade.resolveMongoPathForEDMPath(item.getResourcePath()).getMongoPath();
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
    public List<Bson> getStageObjects() {
      return Collections.singletonList(getStageObject());
    }

    @Override
    public List<String> getUsedMongoDocumentProperties() {
      return new ArrayList<>(selectedFields);
    }

    @Override
    public List<String> getWrittenMongoDocumentProperties() {
      return List.of();
    }

    @Override
    public List<String> getAddedMongoDocumentProperties() {
      return List.of();
    }

    @Override
    public List<String> getRemovedMongoDocumentProperties() {
      // TODO in case if the _id would be removed
      return List.of();
    }

    @Override
    public boolean isDocumentShapeRedefined() {
      return false;
    }
  }
}
