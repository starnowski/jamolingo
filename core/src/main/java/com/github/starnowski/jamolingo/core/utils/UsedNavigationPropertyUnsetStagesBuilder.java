package com.github.starnowski.jamolingo.core.utils;

import static com.github.starnowski.jamolingo.core.utils.Constants.ODATA_PATH_SEPARATOR_CHARACTER;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Helper component that builds MongoDB $unset aggregation pipeline stages based on
 * UsedNavigationProperty metadata.
 */
public class UsedNavigationPropertyUnsetStagesBuilder {

  /**
   * Builds a MongoDB $unset stage for a single UsedNavigationProperty using default context.
   *
   * @param usedNavigationProperty the used navigation property metadata
   * @return list of Bson stages
   */
  public List<Bson> build(UsedNavigationProperty usedNavigationProperty) {
    return build(usedNavigationProperty, DefaultNavigationPropertyLookupContext.builder().build());
  }

  /**
   * Builds a MongoDB $unset stage for a single UsedNavigationProperty using the provided context.
   *
   * @param usedNavigationProperty the used navigation property metadata
   * @param context the lookup context providing type and property path mappings
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
   * Builds a MongoDB $unset stage for a collection of UsedNavigationProperty items using default
   * context.
   *
   * @param usedNavigationProperties the collection of used navigation properties
   * @return list of Bson stages
   */
  public List<Bson> build(Collection<UsedNavigationProperty> usedNavigationProperties) {
    return build(
        usedNavigationProperties, DefaultNavigationPropertyLookupContext.builder().build());
  }

  /**
   * Builds a MongoDB $unset stage for a collection of UsedNavigationProperty items using the
   * provided context.
   *
   * @param usedNavigationProperties the collection of used navigation properties
   * @param context the lookup context providing type and property path mappings
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
      String normalizedEdmPath = extractNormalizedEdmPath(prop);
      if (normalizedEdmPath == null) {
        continue;
      }
      uniqueByEdmPath.putIfAbsent(normalizedEdmPath, prop);
    }

    if (uniqueByEdmPath.isEmpty()) {
      return Collections.emptyList();
    }

    List<String> orderedEdmPaths = new ArrayList<>(uniqueByEdmPath.size());
    Set<String> visitedEdmPaths = new HashSet<>();
    for (String edmPath : uniqueByEdmPath.keySet()) {
      addWithAncestors(edmPath, uniqueByEdmPath, visitedEdmPaths, orderedEdmPaths);
    }

    Map<String, String> resolvedMongoPathsByEdmPath = new HashMap<>();
    Set<String> candidateMongoPaths = new LinkedHashSet<>();

    for (String normalizedEdmPath : orderedEdmPaths) {
      UsedNavigationProperty prop = uniqueByEdmPath.get(normalizedEdmPath);
      UsedNavigationProperty parentNavProp = findNearestParent(normalizedEdmPath, uniqueByEdmPath);
      EdmMongoContextFacade sourceResolver = resolveSourceFacade(parentNavProp, effectiveContext);
      String declaringRelativeEdmPath =
          extractDeclaringRelativeEdmPath(normalizedEdmPath, parentNavProp);

      String fullMongoPath =
          resolveFullMongoPath(
              prop,
              normalizedEdmPath,
              declaringRelativeEdmPath,
              parentNavProp,
              sourceResolver,
              resolvedMongoPathsByEdmPath);
      if (fullMongoPath != null && !fullMongoPath.isEmpty()) {
        resolvedMongoPathsByEdmPath.put(normalizedEdmPath, fullMongoPath);
        candidateMongoPaths.add(fullMongoPath);
      }
    }

    List<String> collapsedMongoPaths = collapseDescendantPaths(candidateMongoPaths);
    if (collapsedMongoPaths.isEmpty()) {
      return Collections.emptyList();
    }

    return Collections.singletonList(new Document("$unset", collapsedMongoPaths));
  }

  private String extractNormalizedEdmPath(UsedNavigationProperty prop) {
    if (prop == null) {
      return null;
    }
    if (prop.getEdmPath() != null && !prop.getEdmPath().isEmpty()) {
      return prop.getEdmPath().replace(".", ODATA_PATH_SEPARATOR_CHARACTER);
    }
    if (prop.getMongoPath() != null && !prop.getMongoPath().isEmpty()) {
      return prop.getMongoPath().replace(".", ODATA_PATH_SEPARATOR_CHARACTER);
    }
    return null;
  }

  private void addWithAncestors(
      String edmPath,
      Map<String, UsedNavigationProperty> uniqueByEdmPath,
      Set<String> visitedEdmPaths,
      List<String> orderedEdmPaths) {
    if (visitedEdmPaths.contains(edmPath)) {
      return;
    }
    int slashIndex = edmPath.indexOf(ODATA_PATH_SEPARATOR_CHARACTER);
    while (slashIndex != -1) {
      String ancestorPath = edmPath.substring(0, slashIndex);
      if (uniqueByEdmPath.containsKey(ancestorPath) && !visitedEdmPaths.contains(ancestorPath)) {
        addWithAncestors(ancestorPath, uniqueByEdmPath, visitedEdmPaths, orderedEdmPaths);
      }
      slashIndex = edmPath.indexOf(ODATA_PATH_SEPARATOR_CHARACTER, slashIndex + 1);
    }
    visitedEdmPaths.add(edmPath);
    orderedEdmPaths.add(edmPath);
  }

  private UsedNavigationProperty findNearestParent(
      String normalizedEdmPath, Map<String, UsedNavigationProperty> allPropsByEdmPath) {
    int lastSlash = normalizedEdmPath.lastIndexOf(ODATA_PATH_SEPARATOR_CHARACTER);
    while (lastSlash > 0) {
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
      UsedNavigationProperty parentNavProp, NavigationPropertyLookupContext context) {
    if (parentNavProp != null) {
      return resolveEntityFacade(parentNavProp.getEdmEntityFullName(), context);
    }
    EdmMongoContextFacade resolver =
        resolveEntityFacade(context.getRootEdmEntityTypeName(), context);
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
      String normalizedEdmPath, UsedNavigationProperty parentNavProp) {
    if (parentNavProp != null) {
      String parentEdmPath = extractNormalizedEdmPath(parentNavProp);
      return normalizedEdmPath.substring(
          parentEdmPath.length() + ODATA_PATH_SEPARATOR_CHARACTER.length());
    }
    return normalizedEdmPath;
  }

  private String resolveFullMongoPath(
      UsedNavigationProperty prop,
      String normalizedEdmPath,
      String declaringRelativeEdmPath,
      UsedNavigationProperty parentNavProp,
      EdmMongoContextFacade sourceResolver,
      Map<String, String> resolvedMongoPathsByEdmPath) {
    String resolvedOnDeclaring = tryResolvePath(sourceResolver, declaringRelativeEdmPath);

    if (parentNavProp != null) {
      String parentEdmPath = extractNormalizedEdmPath(parentNavProp);
      String parentMongoPath = resolvedMongoPathsByEdmPath.get(parentEdmPath);
      if (parentMongoPath == null) {
        parentMongoPath =
            parentNavProp.getMongoPath() != null && !parentNavProp.getMongoPath().isEmpty()
                ? parentNavProp.getMongoPath()
                : parentEdmPath.replace(ODATA_PATH_SEPARATOR_CHARACTER, ".");
      }
      if (resolvedOnDeclaring != null) {
        return parentMongoPath + "." + resolvedOnDeclaring;
      }
      if (prop.getMongoPath() != null
          && parentNavProp.getMongoPath() != null
          && prop.getMongoPath().startsWith(parentNavProp.getMongoPath() + ".")) {
        String childSuffix =
            prop.getMongoPath().substring(parentNavProp.getMongoPath().length() + 1);
        return parentMongoPath + "." + childSuffix;
      }
      return parentMongoPath
          + "."
          + declaringRelativeEdmPath.replace(ODATA_PATH_SEPARATOR_CHARACTER, ".");
    }

    String defaultMongoPath = normalizedEdmPath.replace(ODATA_PATH_SEPARATOR_CHARACTER, ".");
    if (prop.getMongoPath() != null
        && !prop.getMongoPath().isEmpty()
        && !prop.getMongoPath().equals(defaultMongoPath)) {
      return prop.getMongoPath();
    }
    if (resolvedOnDeclaring != null) {
      return resolvedOnDeclaring;
    }
    return prop.getMongoPath() != null && !prop.getMongoPath().isEmpty()
        ? prop.getMongoPath()
        : defaultMongoPath;
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

  private List<String> collapseDescendantPaths(Set<String> candidateMongoPaths) {
    List<String> collapsed = new ArrayList<>();
    for (String path : candidateMongoPaths) {
      boolean hasAncestor = false;
      for (String other : candidateMongoPaths) {
        if (!path.equals(other) && path.startsWith(other + ".")) {
          hasAncestor = true;
          break;
        }
      }
      if (!hasAncestor) {
        collapsed.add(path);
      }
    }
    return collapsed;
  }
}
