package com.github.starnowski.jamolingo.core.operators.expand;

import java.util.Arrays;
import java.util.Collections;
import org.bson.Document;

/**
 * Component that converts selected fields into a BSON Document that can be used in a $map
 * aggregation stage.
 */
public class SelectOperatorResultToBsonDocumentConverter {

  /**
   * Converts the selected fields into a BSON Document.
   *
   * @param selectedFields the collection of selected fields
   * @param itemName the name of the variable representing the current item in the $map stage
   * @return a BSON Document representing the projection for the $map stage
   */
  public Document convert(java.util.Collection<String> selectedFields, String itemName) {
    return convert(selectedFields, itemName, Collections.emptyList());
  }

  /**
   * Converts the selected fields into a BSON Document.
   *
   * @param selectedFields the collection of selected fields
   * @param itemName the name of the variable representing the current item in the $map stage
   * @param arraysFields the collection of fields that are arrays
   * @return a BSON Document representing the projection for the $map stage
   */
  public Document convert(
      java.util.Collection<String> selectedFields,
      String itemName,
      java.util.Collection<String> arraysFields) {
    arraysFields = arraysFields == null ? Collections.emptyList() : arraysFields;
    Document root = new Document();
    if (selectedFields == null || selectedFields.isEmpty()) {
      return new Document("$mergeObjects", Arrays.asList("$$" + itemName, new Document()));
    }

    for (String fieldPath : selectedFields) {
      if (isInsideArrayButNotArrayItself(fieldPath, arraysFields)) {
        String[] parts = fieldPath.split("\\.");
        Document current = root;
        StringBuilder currentPath = new StringBuilder();
        StringBuilder valuePath = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
          String part = parts[i];
          if (i > 0) {
            currentPath.append(".");
            valuePath.append(".");
          }
          currentPath.append(part);
          valuePath.append(part);
          boolean isArray = arraysFields.contains(currentPath.toString());
          if (isArray) {
            if (!current.containsKey(part)) {
              current.put(
                  part,
                  new Document(
                      "$map",
                      new Document()
                          .append("input", "$$" + itemName + "." + part)
                          .append("as", itemName)
                          .append("in", new Document())));
            }
            current =
                current
                    .get(part, Document.class)
                    .get("$map", Document.class)
                    .get("in", Document.class);
            valuePath = new StringBuilder();
          } else {
            if (!current.containsKey(part)) {
              current.put(part, new Document());
            }
            current = (Document) current.get(part);
          }
        }

        String lastPart = parts[parts.length - 1];
        String valuePathString = valuePath.isEmpty() ? lastPart : valuePath + "." + lastPart;
        current.put(
            lastPart,
            "$$"
                + itemName
                + (valuePathString.startsWith(".") ? valuePathString : "." + valuePathString));
      } else {
        if (isArrayAndOtherThereAreOtherSelectedFieldsInsideThisArray(
            fieldPath, arraysFields, selectedFields)) {
          continue;
        }
        String[] parts = fieldPath.split("\\.");
        Document current = root;
        for (int i = 0; i < parts.length - 1; i++) {
          String part = parts[i];
          if (!current.containsKey(part)) {
            current.put(part, new Document());
          }
          current = (Document) current.get(part);
        }

        String lastPart = parts[parts.length - 1];
        current.put(lastPart, "$$" + itemName + "." + fieldPath);
      }
    }

    return root;
  }

  private boolean isArrayAndOtherThereAreOtherSelectedFieldsInsideThisArray(
      String fieldPath,
      java.util.Collection<String> arraysFields,
      java.util.Collection<String> selectedFields) {
    if (!arraysFields.contains(fieldPath)) {
      return false;
    }
    for (String field : selectedFields) {
      if (!field.equals(fieldPath) && field.startsWith(fieldPath)) {
        return true;
      }
    }
    return false;
  }

  private boolean isInsideArrayButNotArrayItself(
      String fieldPath, java.util.Collection<String> arraysFields) {
    if (arraysFields.contains(fieldPath)) {
      return false;
    }
    for (String array : arraysFields) {
      if (fieldPath.startsWith(array)) {
        return true;
      }
    }
    return false;
  }
}
