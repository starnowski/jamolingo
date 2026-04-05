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

    return root;
  }
}
