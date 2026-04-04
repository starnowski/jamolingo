package com.github.starnowski.jamolingo.core.operators.expand;

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
    Document root = new Document();
    if (selectedFields == null) {
      return root;
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
