package com.github.starnowski.jamolingo.core.operators.expand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;

public class GraphLookUpToLookUpStrategyResultsTransformer {

  public List<Document> transform(
      List<Document> documents, ExpandOperatorResult expandOperatorResult) {

    if (documents == null
        || documents.isEmpty()
        || expandOperatorResult == null
        || expandOperatorResult.getExpandElements() == null) {
      return documents;
    }

    for (Document document : documents) {
      for (ExpandElement expandElement : expandOperatorResult.getExpandElements().values()) {
        transformDocument(document, expandElement);
      }
    }
    return documents;
  }

  private void transformDocument(Document document, ExpandElement expandElement) {
    if (document == null) {
      return;
    }

    String mongoPath = expandElement.getMongoPath();
    Object value = document.get(mongoPath);

    if (value == null) {
      if (Boolean.TRUE.equals(expandElement.getCollection())) {
        document.put(mongoPath, new ArrayList<>());
      }
      return;
    }

    if (FetchType.GRAPHLOOKUP.equals(expandElement.getFetchType()) && value instanceof List) {
      List<Document> flatList = (List<Document>) value;
      if (flatList.isEmpty()) {
        if (expandElement.getCollection() != null && !expandElement.getCollection()) {
          document.remove(mongoPath);
        }
        return;
      }

      // 1. Recursively transform nested expand elements for all nodes in the flat list
      for (Document node : flatList) {
        for (ExpandElement nestedExpandElement : expandElement.getExpandElements().values()) {
          transformDocument(node, nestedExpandElement);
        }
      }

      // 2. Build the tree for this expand element
      Object rootId = document.get(expandElement.getLocalKeyProperty());

      Map<Object, List<Document>> childrenByParentId = new HashMap<>();
      for (Document node : flatList) {
        Object parentId = node.get(expandElement.getForeignKeyProperty());
        if (parentId != null) {
          childrenByParentId.computeIfAbsent(parentId, k -> new ArrayList<>()).add(node);
        }
      }

      List<Document> rootNodesOriginal = childrenByParentId.getOrDefault(rootId, new ArrayList<>());
      List<Document> rootNodes = new ArrayList<>();
      java.util.Set<Object> visited = new java.util.HashSet<>();

      buildTree(rootNodesOriginal, childrenByParentId, expandElement, visited, rootNodes);

      if (expandElement.getCollection() != null && !expandElement.getCollection()) {
        if (!rootNodes.isEmpty()) {
          document.put(mongoPath, rootNodes.get(0));
        } else {
          document.remove(mongoPath);
        }
      } else {
        document.put(mongoPath, rootNodes);
      }
    } else if (value instanceof List) {
      List<Document> list = (List<Document>) value;
      for (Document nestedDoc : list) {
        for (ExpandElement nestedExpandElement : expandElement.getExpandElements().values()) {
          transformDocument(nestedDoc, nestedExpandElement);
        }
      }
    } else if (value instanceof Document) {
      Document nestedDoc = (Document) value;
      for (ExpandElement nestedExpandElement : expandElement.getExpandElements().values()) {
        transformDocument(nestedDoc, nestedExpandElement);
      }
    }
  }

  private void buildTree(
      List<Document> currentNodesOriginal,
      Map<Object, List<Document>> childrenByParentId,
      ExpandElement expandElement,
      java.util.Set<Object> visited,
      List<Document> resultList) {
    GraphLookUpCleanUpInfo cleanUpInfo = expandElement.getGraphLookUpCleanUpInfo();
    String mongoPath = expandElement.getMongoPath();
    String depthVarName = expandElement.getDepthVariableName();
    Integer maxLevel = expandElement.getLevel();

    for (Document nodeOriginal : currentNodesOriginal) {
      Object nodeId = nodeOriginal.get(expandElement.getLocalKeyProperty());

      if (nodeId != null && !visited.add(nodeId)) {
        continue;
      }

      Document node = new Document(nodeOriginal);
      resultList.add(node);

      int depth = -1;
      if (depthVarName != null && node.get(depthVarName) != null) {
        depth = ((Number) node.get(depthVarName)).intValue();
      }

      boolean isMaxDepth = (maxLevel != null && depth == maxLevel - 1);

      if (!isMaxDepth) {
        List<Document> childrenOriginal =
            childrenByParentId.getOrDefault(nodeId, new ArrayList<>());
        List<Document> children = new ArrayList<>();
        buildTree(childrenOriginal, childrenByParentId, expandElement, visited, children);

        if (expandElement.getCollection() != null && !expandElement.getCollection()) {
          if (!children.isEmpty()) {
            node.put(mongoPath, children.get(0));
          }
        } else {
          node.put(mongoPath, children);
        }
      }

      if (depthVarName != null) {
        node.remove(depthVarName);
      }

      if (cleanUpInfo != null) {
        if (cleanUpInfo.isRemoveLocalKeyProperty()) {
          node.remove(expandElement.getLocalKeyProperty());
        }
        if (cleanUpInfo.isRemoveForeignKeyProperty()) {
          node.remove(expandElement.getForeignKeyProperty());
        }
      }

      if (nodeId != null) {
        visited.remove(nodeId);
      }
    }
  }
}
