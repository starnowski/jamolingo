package com.github.starnowski.jamolingo.core.operators.expand;

import com.github.starnowski.jamolingo.core.operators.select.SelectOperatorResult;
import org.bson.Document;

/**
 * Component that converts selected fields from SelectOperatorResult into a BSON Document that can
 * be used in a $map aggregation stage.
 */
public class SelectOperatorResultToBsonDocumentConverter {

  /**
   * Converts the selected fields from the given SelectOperatorResult into a BSON Document.
   *
   * @param selectOperatorResult the result of the select operator containing selected fields
   * @param itemName the name of the variable representing the current item in the $map stage
   * @return a BSON Document representing the projection for the $map stage
   */
  public Document convert(SelectOperatorResult selectOperatorResult, String itemName) {
    // TODO Pass object with selected fields, item name, and fields that represent the collection
    Document root = new Document();
    if (selectOperatorResult == null) {
      return root;
    }
    java.util.Set<String> selectedFields = selectOperatorResult.getSelectedFields();
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
