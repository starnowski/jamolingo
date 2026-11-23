package com.github.starnowski.jamolingo.core.utils;

import static com.github.starnowski.jamolingo.core.utils.Constants.ODATA_PATH_SEPARATOR_CHARACTER;

import com.github.starnowski.jamolingo.common.beans.KeyValue;
import com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade;
import com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContext;
import com.github.starnowski.jamolingo.core.context.MongoPathResolution;
import com.github.starnowski.jamolingo.core.operators.UsedNavigationProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Helper component that builds MongoDB $lookup and $unwind aggregation pipeline stages based on
 * UsedNavigationProperty metadata.
 */
public class UsedNavigationPropertyLookupStagesBuilder {

  /**
   * Builds MongoDB $lookup and $unwind stages for a single UsedNavigationProperty using default
   * context.
   *
   * @param usedNavigationProperty the used navigation property metadata
   * @return list of Bson stages
   */
  public List<Bson> build(UsedNavigationProperty usedNavigationProperty) {
    return build(usedNavigationProperty, DefaultNavigationPropertyLookupContext.builder().build());
  }

  /**
   * Builds MongoDB $lookup and $unwind stages for a single UsedNavigationProperty using the
   * provided context.
   *
   * @param usedNavigationProperty the used navigation property metadata
   * @param context the lookup context providing type and collection mappings
   * @return list of Bson stages
   */
  public List<Bson> build(
      UsedNavigationProperty usedNavigationProperty, NavigationPropertyLookupContext context) {
    if (usedNavigationProperty == null) {
      return Collections.emptyList();
    }
    return build(Collections.singletonList(usedNavigationProperty), context);
  }

  /**
   * Builds MongoDB $lookup and $unwind stages for a collection of UsedNavigationProperty items
   * using default context.
   *
   * @param usedNavigationProperties the collection of used navigation properties
   * @return list of Bson stages
   */
  public List<Bson> build(Collection<UsedNavigationProperty> usedNavigationProperties) {
    return build(
        usedNavigationProperties, DefaultNavigationPropertyLookupContext.builder().build());
  }

  /**
   * Builds MongoDB $lookup and $unwind stages for a collection of UsedNavigationProperty items
   * using the provided context.
   *
   * @param usedNavigationProperties the collection of used navigation properties
   * @param context the lookup context providing type and collection mappings
   * @return list of Bson stages
   */
  public List<Bson> build(
      Collection<UsedNavigationProperty> usedNavigationProperties,
      NavigationPropertyLookupContext context) {
    if (usedNavigationProperties == null || usedNavigationProperties.isEmpty()) {
      return Collections.emptyList();
    }
    NavigationPropertyLookupContext effectiveContext =
        context != null ? context : DefaultNavigationPropertyLookupContext.builder().build();

    Map<String, UsedNavigationProperty> uniqueByEdmPath = new LinkedHashMap<>();
    for (UsedNavigationProperty prop : usedNavigationProperties) {
      if (prop == null
          || prop.getEdmPath() == null
          || prop.getEdmPath().isEmpty()
          || prop.getLocalKeyProperty() == null
          || prop.getForeignKeyProperty() == null) {
        continue;
      }
      String normalizedEdmPath = prop.getEdmPath().replace(".", ODATA_PATH_SEPARATOR_CHARACTER);
      uniqueByEdmPath.putIfAbsent(normalizedEdmPath, prop);
    }

    if (uniqueByEdmPath.isEmpty()) {
      return Collections.emptyList();
    }

    List<UsedNavigationProperty> orderedProps = new ArrayList<>(uniqueByEdmPath.size());
    Set<String> visitedEdmPaths = new HashSet<>();
    for (String edmPath : uniqueByEdmPath.keySet()) {
      addWithAncestors(edmPath, uniqueByEdmPath, visitedEdmPaths, orderedProps);
    }

    Set<String> consumedEdmPaths = new HashSet<>();
    Map<String, String> resolvedRelativeMongoPaths = new HashMap<>();
    return buildStagesForScope(
        orderedProps,
        uniqueByEdmPath,
        null,
        null,
        effectiveContext.getRootEdmEntityTypeName(),
        effectiveContext,
        consumedEdmPaths,
        resolvedRelativeMongoPaths);
  }

  private void addWithAncestors(
      String edmPath,
      Map<String, UsedNavigationProperty> uniqueByEdmPath,
      Set<String> visitedEdmPaths,
      List<UsedNavigationProperty> orderedProps) {
    if (visitedEdmPaths.contains(edmPath)) {
      return;
    }
    int slashIndex = edmPath.indexOf(ODATA_PATH_SEPARATOR_CHARACTER);
    while (slashIndex != -1) {
      String ancestorPath = edmPath.substring(0, slashIndex);
      if (uniqueByEdmPath.containsKey(ancestorPath) && !visitedEdmPaths.contains(ancestorPath)) {
        addWithAncestors(ancestorPath, uniqueByEdmPath, visitedEdmPaths, orderedProps);
      }
      slashIndex = edmPath.indexOf(ODATA_PATH_SEPARATOR_CHARACTER, slashIndex + 1);
    }
    visitedEdmPaths.add(edmPath);
    orderedProps.add(uniqueByEdmPath.get(edmPath));
  }

  private List<Bson> buildStagesForScope(
      List<UsedNavigationProperty> scopeProps,
      Map<String, UsedNavigationProperty> allPropsByEdmPath,
      String scopeEdmPrefix,
      String scopeMongoPrefix,
      String scopeRootEntityTypeName,
      NavigationPropertyLookupContext context,
      Set<String> consumedEdmPaths,
      Map<String, String> resolvedRelativeMongoPaths) {
    List<Bson> stages = new ArrayList<>();

    for (UsedNavigationProperty prop : scopeProps) {
      String normalizedEdmPath = prop.getEdmPath().replace(".", ODATA_PATH_SEPARATOR_CHARACTER);
      if (consumedEdmPaths.contains(normalizedEdmPath)) {
        continue;
      }
      consumedEdmPaths.add(normalizedEdmPath);

      UsedNavigationProperty parentNavProp =
          findNearestParentInScope(normalizedEdmPath, scopeEdmPrefix, allPropsByEdmPath);

      EdmMongoContextFacade sourceResolver =
          resolveSourceFacade(parentNavProp, scopeEdmPrefix, scopeRootEntityTypeName, context);
      EdmMongoContextFacade targetResolver =
          resolveEntityFacade(prop.getEdmEntityFullName(), context);

      String declaringRelativeEdmPath =
          extractDeclaringRelativeEdmPath(normalizedEdmPath, parentNavProp, scopeEdmPrefix);

      String relativeMongoPath =
          resolveRelativeMongoPath(
              prop,
              normalizedEdmPath,
              declaringRelativeEdmPath,
              parentNavProp,
              scopeMongoPrefix,
              sourceResolver,
              resolvedRelativeMongoPaths);
      resolvedRelativeMongoPaths.put(normalizedEdmPath, relativeMongoPath);

      String localField =
          resolveLocalField(
              prop,
              declaringRelativeEdmPath,
              relativeMongoPath,
              parentNavProp,
              sourceResolver,
              resolvedRelativeMongoPaths);

      String foreignField = resolveForeignField(prop.getForeignKeyProperty(), targetResolver);
      String targetCollection = resolveTargetCollection(prop.getEdmEntityFullName(), context);

      Document lookupInner =
          new Document()
              .append("from", targetCollection)
              .append("localField", localField)
              .append("foreignField", foreignField);

      if (prop.isCollection()) {
        String descendantPrefix = normalizedEdmPath + ODATA_PATH_SEPARATOR_CHARACTER;
        List<UsedNavigationProperty> descendants = new ArrayList<>();
        for (UsedNavigationProperty candidate : scopeProps) {
          String candidateEdmPath =
              candidate.getEdmPath().replace(".", ODATA_PATH_SEPARATOR_CHARACTER);
          if (!consumedEdmPaths.contains(candidateEdmPath)
              && candidateEdmPath.startsWith(descendantPrefix)) {
            descendants.add(candidate);
          }
        }
        if (!descendants.isEmpty()) {
          String nextScopeMongoPrefix =
              scopeMongoPrefix == null || scopeMongoPrefix.isEmpty()
                  ? relativeMongoPath
                  : scopeMongoPrefix + "." + relativeMongoPath;
          List<Bson> subPipeline =
              buildStagesForScope(
                  descendants,
                  allPropsByEdmPath,
                  normalizedEdmPath,
                  nextScopeMongoPrefix,
                  prop.getEdmEntityFullName(),
                  context,
                  consumedEdmPaths,
                  new HashMap<>());
          if (!subPipeline.isEmpty()) {
            lookupInner.append("pipeline", subPipeline);
          }
        }
      }

      lookupInner.append("as", relativeMongoPath);
      stages.add(new Document("$lookup", lookupInner));

      if (!prop.isCollection()) {
        stages.add(
            new Document(
                "$unwind",
                new Document("path", "$" + relativeMongoPath)
                    .append("preserveNullAndEmptyArrays", true)));
      }
    }

    return stages;
  }

  private UsedNavigationProperty findNearestParentInScope(
      String normalizedEdmPath,
      String scopeEdmPrefix,
      Map<String, UsedNavigationProperty> allPropsByEdmPath) {
    int minPrefixLength =
        scopeEdmPrefix == null || scopeEdmPrefix.isEmpty() ? 0 : scopeEdmPrefix.length();
    int lastSlash = normalizedEdmPath.lastIndexOf(ODATA_PATH_SEPARATOR_CHARACTER);
    while (lastSlash > minPrefixLength) {
      String candidateParentPath = normalizedEdmPath.substring(0, lastSlash);
      UsedNavigationProperty parent = allPropsByEdmPath.get(candidateParentPath);
      if (parent != null) {
        return parent;
      }
      lastSlash = candidateParentPath.lastIndexOf(ODATA_PATH_SEPARATOR_CHARACTER);
    }
    return null;
  }

  private EdmMongoContextFacade resolveSourceFacade(
      UsedNavigationProperty parentNavProp,
      String scopeEdmPrefix,
      String scopeRootEntityTypeName,
      NavigationPropertyLookupContext context) {
    if (parentNavProp != null) {
      return resolveEntityFacade(parentNavProp.getEdmEntityFullName(), context);
    }
    if (scopeEdmPrefix != null && !scopeEdmPrefix.isEmpty()) {
      return resolveEntityFacade(scopeRootEntityTypeName, context);
    }
    EdmMongoContextFacade resolver = resolveEntityFacade(scopeRootEntityTypeName, context);
    if (resolver == null) {
      resolver = context.getRootEdmMongoContextFacade();
    }
    return resolver;
  }

  private EdmMongoContextFacade resolveEntityFacade(
      String entityFullName, NavigationPropertyLookupContext context) {
    if (entityFullName == null) {
      return null;
    }
    if (context.getEdmTypeMapping() != null
        && context.getEdmTypeMapping().containsKey(entityFullName)) {
      return context.getEdmTypeMapping().get(entityFullName);
    }
    if (entityFullName.equals(context.getRootEdmEntityTypeName())) {
      return context.getRootEdmMongoContextFacade();
    }
    return null;
  }

  private String extractDeclaringRelativeEdmPath(
      String normalizedEdmPath, UsedNavigationProperty parentNavProp, String scopeEdmPrefix) {
    if (parentNavProp != null) {
      String parentEdmPath =
          parentNavProp.getEdmPath().replace(".", ODATA_PATH_SEPARATOR_CHARACTER);
      return normalizedEdmPath.substring(
          parentEdmPath.length() + ODATA_PATH_SEPARATOR_CHARACTER.length());
    }
    if (scopeEdmPrefix != null && !scopeEdmPrefix.isEmpty()) {
      return normalizedEdmPath.substring(
          scopeEdmPrefix.length() + ODATA_PATH_SEPARATOR_CHARACTER.length());
    }
    return normalizedEdmPath;
  }

  private String resolveRelativeMongoPath(
      UsedNavigationProperty prop,
      String normalizedEdmPath,
      String declaringRelativeEdmPath,
      UsedNavigationProperty parentNavProp,
      String scopeMongoPrefix,
      EdmMongoContextFacade sourceResolver,
      Map<String, String> resolvedRelativeMongoPaths) {
    String resolvedOnDeclaring = tryResolvePath(sourceResolver, declaringRelativeEdmPath);

    if (parentNavProp != null) {
      String parentEdmPath =
          parentNavProp.getEdmPath().replace(".", ODATA_PATH_SEPARATOR_CHARACTER);
      String parentRelativeMongoPath = resolvedRelativeMongoPaths.get(parentEdmPath);
      if (parentRelativeMongoPath == null) {
        parentRelativeMongoPath =
            parentNavProp.getMongoPath() != null
                ? parentNavProp.getMongoPath()
                : parentEdmPath.replace(ODATA_PATH_SEPARATOR_CHARACTER, ".");
      }
      if (resolvedOnDeclaring != null) {
        return parentRelativeMongoPath + "." + resolvedOnDeclaring;
      }
      if (prop.getMongoPath() != null
          && parentNavProp.getMongoPath() != null
          && prop.getMongoPath().startsWith(parentNavProp.getMongoPath() + ".")) {
        String childSuffix =
            prop.getMongoPath().substring(parentNavProp.getMongoPath().length() + 1);
        return parentRelativeMongoPath + "." + childSuffix;
      }
      return parentRelativeMongoPath
          + "."
          + declaringRelativeEdmPath.replace(ODATA_PATH_SEPARATOR_CHARACTER, ".");
    }

    if (scopeMongoPrefix != null && !scopeMongoPrefix.isEmpty()) {
      if (resolvedOnDeclaring != null) {
        return resolvedOnDeclaring;
      }
      if (prop.getMongoPath() != null && prop.getMongoPath().startsWith(scopeMongoPrefix + ".")) {
        return prop.getMongoPath().substring(scopeMongoPrefix.length() + 1);
      }
      return declaringRelativeEdmPath.replace(ODATA_PATH_SEPARATOR_CHARACTER, ".");
    }

    String defaultMongoPath = normalizedEdmPath.replace(ODATA_PATH_SEPARATOR_CHARACTER, ".");
    if (prop.getMongoPath() != null && !prop.getMongoPath().equals(defaultMongoPath)) {
      return prop.getMongoPath();
    }
    if (resolvedOnDeclaring != null) {
      return resolvedOnDeclaring;
    }
    return prop.getMongoPath() != null ? prop.getMongoPath() : defaultMongoPath;
  }

  private String resolveLocalField(
      UsedNavigationProperty prop,
      String declaringRelativeEdmPath,
      String relativeMongoPath,
      UsedNavigationProperty parentNavProp,
      EdmMongoContextFacade sourceResolver,
      Map<String, String> resolvedRelativeMongoPaths) {
    String localKeyProperty = prop.getLocalKeyProperty();
    int lastSlashInDeclaring = declaringRelativeEdmPath.lastIndexOf(ODATA_PATH_SEPARATOR_CHARACTER);
    String declaringComplexEdmPrefix =
        lastSlashInDeclaring != -1
            ? declaringRelativeEdmPath.substring(0, lastSlashInDeclaring)
            : null;
    String localKeyEdmPathOnDeclaring =
        declaringComplexEdmPrefix != null
            ? declaringComplexEdmPrefix + ODATA_PATH_SEPARATOR_CHARACTER + localKeyProperty
            : localKeyProperty;

    String resolvedLocalKeyOnDeclaring =
        tryResolvePath(sourceResolver, localKeyEdmPathOnDeclaring);
    if (resolvedLocalKeyOnDeclaring == null) {
      if (declaringComplexEdmPrefix != null) {
        String complexMongoPrefix =
            extractComplexMongoPrefix(
                relativeMongoPath, parentNavProp, resolvedRelativeMongoPaths);
        resolvedLocalKeyOnDeclaring =
            complexMongoPrefix != null
                ? complexMongoPrefix + "." + localKeyProperty
                : localKeyEdmPathOnDeclaring.replace(ODATA_PATH_SEPARATOR_CHARACTER, ".");
      } else {
        resolvedLocalKeyOnDeclaring = localKeyProperty;
      }
    }

    if (parentNavProp != null) {
      String parentEdmPath =
          parentNavProp.getEdmPath().replace(".", ODATA_PATH_SEPARATOR_CHARACTER);
      String parentRelativeMongoPath = resolvedRelativeMongoPaths.get(parentEdmPath);
      if (parentRelativeMongoPath == null) {
        parentRelativeMongoPath =
            parentNavProp.getMongoPath() != null
                ? parentNavProp.getMongoPath()
                : parentEdmPath.replace(ODATA_PATH_SEPARATOR_CHARACTER, ".");
      }
      return parentRelativeMongoPath + "." + resolvedLocalKeyOnDeclaring;
    }

    return resolvedLocalKeyOnDeclaring;
  }

  private String extractComplexMongoPrefix(
      String relativeMongoPath,
      UsedNavigationProperty parentNavProp,
      Map<String, String> resolvedRelativeMongoPaths) {
    String pathWithinDeclaring = relativeMongoPath;
    if (parentNavProp != null) {
      String parentEdmPath =
          parentNavProp.getEdmPath().replace(".", ODATA_PATH_SEPARATOR_CHARACTER);
      String parentRelativeMongoPath = resolvedRelativeMongoPaths.get(parentEdmPath);
      if (parentRelativeMongoPath != null
          && relativeMongoPath.startsWith(parentRelativeMongoPath + ".")) {
        pathWithinDeclaring = relativeMongoPath.substring(parentRelativeMongoPath.length() + 1);
      }
    }
    int lastDot = pathWithinDeclaring.lastIndexOf('.');
    return lastDot != -1 ? pathWithinDeclaring.substring(0, lastDot) : null;
  }

  private String resolveForeignField(
      String foreignKeyProperty, EdmMongoContextFacade targetResolver) {
    String resolved = tryResolvePath(targetResolver, foreignKeyProperty);
    return resolved != null ? resolved : foreignKeyProperty;
  }

  private String tryResolvePath(EdmMongoContextFacade resolver, String edmPath) {
    if (resolver == null || edmPath == null || edmPath.isEmpty()) {
      return null;
    }
    try {
      MongoPathResolution resolution = resolver.resolveMongoPathForEDMPath(edmPath);
      if (resolution != null && resolution.getMongoPath() != null) {
        return resolution.getMongoPath();
      }
    } catch (EntityPropertiesMongoPathContext.InvalidEDMPathException ignored) {
      // Fallback to default path when the property is not explicitly present in the mapping context
    }
    return null;
  }

  private String resolveTargetCollection(
      String edmEntityFullName, NavigationPropertyLookupContext context) {
    if (edmEntityFullName == null) {
      return null;
    }
    if (context.getEdmTablesToMongoDBCollections() != null
        && !context.getEdmTablesToMongoDBCollections().isEmpty()) {
      int lastDot = edmEntityFullName.lastIndexOf('.');
      if (lastDot != -1) {
        String namespace = edmEntityFullName.substring(0, lastDot);
        String name = edmEntityFullName.substring(lastDot + 1);
        String mappedCollection =
            context.getEdmTablesToMongoDBCollections().get(new KeyValue<>(namespace, name));
        if (mappedCollection != null) {
          return mappedCollection;
        }
      }
    }
    return edmEntityFullName;
  }
}
