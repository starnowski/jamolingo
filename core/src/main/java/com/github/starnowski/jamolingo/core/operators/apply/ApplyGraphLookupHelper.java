package com.github.starnowski.jamolingo.core.operators.apply;

import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;

/** Helper methods for handling $apply stages in nested $graphLookup unwind mode. */
final class ApplyGraphLookupHelper {

  private ApplyGraphLookupHelper() {}

  static String resolveRootMongoPath(
      EdmPropertyMongoPathResolver edmMongoContextFacade, ApplyParserContext context) {
    if (context != null && context.getRootMongoPath() != null) {
      return context.getRootMongoPath();
    }
    if (edmMongoContextFacade instanceof EdmMongoContextFacade) {
      return ((EdmMongoContextFacade) edmMongoContextFacade).getRootMongoPath();
    }
    return null;
  }

  static boolean isGraphLookupUnwindMode(
      EdmPropertyMongoPathResolver edmMongoContextFacade, ApplyParserContext context) {
    String rootMongoPath = resolveRootMongoPath(edmMongoContextFacade, context);
    return rootMongoPath != null && context != null && context.getDepthVariable() != null;
  }

  static String stripRootMongoPath(String mongoPath, String rootMongoPath) {
    if (mongoPath != null && rootMongoPath != null) {
      String prefix = rootMongoPath + ".";
      if (mongoPath.startsWith(prefix)) {
        return mongoPath.substring(prefix.length());
      }
    }
    return mongoPath;
  }

  static Document buildGroupIdDocument(ApplyParserContext context, String rootMongoPath) {
    Document idDoc = new Document();
    int idIndex = 0;
    if (context != null && context.getIdProperties() != null) {
      for (String idProp : context.getIdProperties()) {
        idDoc.put("id_" + (idIndex++), "$" + idProp);
      }
    }
    if (context != null && context.getMongoConnectTo() != null) {
      idDoc.put("parent_id", "$" + rootMongoPath + "." + context.getMongoConnectTo());
    }
    if (context != null && context.getDepthVariable() != null) {
      idDoc.put("depth", "$" + rootMongoPath + "." + context.getDepthVariable());
    }
    return idDoc;
  }

  static List<Bson> buildSlicedGroupStages(
      ApplyParserContext context, String rootMongoPath, Object sliceExpression) {
    Document idDoc = buildGroupIdDocument(context, rootMongoPath);
    Document groupStage =
        new Document(
            "$group",
            new Document("_id", idDoc).append("__jamolingo_docs", new Document("$push", "$$ROOT")));
    Document projectStage =
        new Document(
            "$project",
            new Document(
                "__jamolingo_docs",
                new Document(
                    "$cond",
                    new Document(
                            "if",
                            new Document(
                                "$eq",
                                Arrays.asList(
                                    new Document("$ifNull", Arrays.asList("$_id.depth", null)),
                                    null)))
                        .append("then", "$__jamolingo_docs")
                        .append("else", sliceExpression))));
    Document unwindStage = new Document("$unwind", "$__jamolingo_docs");
    Document replaceRootStage =
        new Document("$replaceRoot", new Document("newRoot", "$__jamolingo_docs"));
    return Arrays.asList(groupStage, projectStage, unwindStage, replaceRootStage);
  }

  static void putNestedProperty(Document target, String relativePath, Object value) {
    String[] parts = relativePath.split("\\.");
    Document current = target;
    for (int i = 0; i < parts.length - 1; i++) {
      Object existing = current.get(parts[i]);
      if (existing instanceof Document) {
        current = (Document) existing;
      } else {
        Document next = new Document();
        current.put(parts[i], next);
        current = next;
      }
    }
    current.put(parts[parts.length - 1], value);
  }
}
