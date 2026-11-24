package com.github.starnowski.jamolingo.core.operators;

import com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver;
import com.github.starnowski.jamolingo.core.context.MongoPathResolution;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmReferentialConstraint;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceCount;
import org.apache.olingo.server.api.uri.UriResourceLambdaAll;
import org.apache.olingo.server.api.uri.UriResourceLambdaAny;
import org.apache.olingo.server.api.uri.UriResourceLambdaVariable;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourceProperty;

/**
 * Utility class for extracting used navigation properties and EDM paths from Olingo URI resources.
 */
public final class NavigationPropertyExtractor {

  private NavigationPropertyExtractor() {}

  /**
   * Extracts all navigation property hops from the given UriInfoResource.
   *
   * @param uriInfoResource the URI info resource containing URI resource parts
   * @param resolver the resolver for mapping EDM paths to MongoDB paths
   * @return list of used navigation properties in order of traversal
   */
  public static List<UsedNavigationProperty> extract(
      UriInfoResource uriInfoResource, EdmPropertyMongoPathResolver resolver) {
    return extract(uriInfoResource, null, resolver);
  }

  /**
   * Extracts all navigation property hops from the given UriInfoResource using an optional base EDM
   * path prefix.
   *
   * @param uriInfoResource the URI info resource containing URI resource parts
   * @param baseEdmPathPrefix optional base EDM path prefix using slash separator
   * @param resolver the resolver for mapping EDM paths to MongoDB paths
   * @return list of used navigation properties in order of traversal
   */
  public static List<UsedNavigationProperty> extract(
      UriInfoResource uriInfoResource,
      String baseEdmPathPrefix,
      EdmPropertyMongoPathResolver resolver) {
    if (uriInfoResource == null || uriInfoResource.getUriResourceParts() == null) {
      return Collections.emptyList();
    }
    return extract(uriInfoResource.getUriResourceParts(), baseEdmPathPrefix, resolver, null);
  }

  /**
   * Extracts all navigation property hops from the given list of UriResource parts.
   *
   * @param uriResources the list of URI resource parts
   * @param resolver the resolver for mapping EDM paths to MongoDB paths
   * @return list of used navigation properties in order of traversal
   */
  public static List<UsedNavigationProperty> extract(
      List<UriResource> uriResources, EdmPropertyMongoPathResolver resolver) {
    return extract(uriResources, null, resolver, null);
  }

  /**
   * Extracts all navigation property hops from the given list of UriResource parts using an
   * optional base EDM path prefix.
   *
   * @param uriResources the list of URI resource parts
   * @param baseEdmPathPrefix optional base EDM path prefix using slash separator
   * @param resolver the resolver for mapping EDM paths to MongoDB paths
   * @return list of used navigation properties in order of traversal
   */
  public static List<UsedNavigationProperty> extract(
      List<UriResource> uriResources,
      String baseEdmPathPrefix,
      EdmPropertyMongoPathResolver resolver) {
    return extract(uriResources, baseEdmPathPrefix, resolver, null);
  }

  /**
   * Extracts all navigation property hops from the given list of UriResource parts using an
   * optional base EDM path prefix and lambda variable EDM path resolver.
   *
   * @param uriResources the list of URI resource parts
   * @param baseEdmPathPrefix optional base EDM path prefix using slash separator
   * @param resolver the resolver for mapping EDM paths to MongoDB paths
   * @param lambdaVariableEdmPathResolver optional function resolving a lambda variable name to its
   *     EDM path prefix
   * @return list of used navigation properties in order of traversal
   */
  public static List<UsedNavigationProperty> extract(
      List<UriResource> uriResources,
      String baseEdmPathPrefix,
      EdmPropertyMongoPathResolver resolver,
      Function<String, String> lambdaVariableEdmPathResolver) {
    if (uriResources == null || uriResources.isEmpty()) {
      return Collections.emptyList();
    }
    String currentEdmPath =
        baseEdmPathPrefix != null && !baseEdmPathPrefix.isEmpty()
            ? baseEdmPathPrefix.replace(".", "/")
            : null;
    Set<UsedNavigationProperty> result = new LinkedHashSet<>();

    for (UriResource resource : uriResources) {
      if (resource instanceof UriResourceLambdaVariable) {
        if (lambdaVariableEdmPathResolver != null) {
          String lambdaPath =
              lambdaVariableEdmPathResolver.apply(
                  ((UriResourceLambdaVariable) resource).getVariableName());
          if (lambdaPath != null && !lambdaPath.isEmpty()) {
            String normalizedLambdaPath = lambdaPath.replace(".", "/");
            currentEdmPath =
                currentEdmPath == null
                    ? normalizedLambdaPath
                    : currentEdmPath + "/" + normalizedLambdaPath;
          }
        }
      } else if (resource instanceof UriResourceProperty) {
        EdmProperty prop = ((UriResourceProperty) resource).getProperty();
        String segName = prop != null ? prop.getName() : resource.getSegmentValue();
        if (segName != null) {
          currentEdmPath = currentEdmPath == null ? segName : currentEdmPath + "/" + segName;
        }
      } else if (resource instanceof UriResourceNavigation) {
        EdmNavigationProperty navProp = ((UriResourceNavigation) resource).getProperty();
        String segName = navProp != null ? navProp.getName() : resource.getSegmentValue();
        if (segName != null) {
          currentEdmPath = currentEdmPath == null ? segName : currentEdmPath + "/" + segName;
        }
        if (navProp != null && currentEdmPath != null) {
          result.add(buildUsedNavigationProperty(navProp, currentEdmPath, resolver));
        }
      } else {
        break;
      }
    }
    return new ArrayList<>(result);
  }

  /**
   * Builds a UsedNavigationProperty instance from the given EdmNavigationProperty, EDM path, and
   * path resolver.
   *
   * @param navProp the EDM navigation property
   * @param edmPath the EDM path to the navigation property using slash separator
   * @param resolver the resolver for mapping EDM paths to MongoDB paths
   * @return the constructed UsedNavigationProperty
   */
  public static UsedNavigationProperty buildUsedNavigationProperty(
      EdmNavigationProperty navProp, String edmPath, EdmPropertyMongoPathResolver resolver) {
    String normalizedEdmPath = edmPath != null ? edmPath.replace(".", "/") : null;
    String mongoPath = normalizedEdmPath != null ? normalizedEdmPath.replace("/", ".") : null;
    if (resolver != null && normalizedEdmPath != null) {
      MongoPathResolution resolution = resolver.resolveMongoPathForEDMPath(normalizedEdmPath);
      if (resolution != null && resolution.getMongoPath() != null) {
        mongoPath = resolution.getMongoPath();
      }
    }
    return buildUsedNavigationProperty(navProp, normalizedEdmPath, mongoPath);
  }

  /**
   * Builds a UsedNavigationProperty instance from the given EdmNavigationProperty, EDM path, and
   * resolved MongoDB path.
   *
   * @param navProp the EDM navigation property
   * @param edmPath the EDM path to the navigation property using slash separator
   * @param mongoPath the resolved MongoDB path using dot notation
   * @return the constructed UsedNavigationProperty
   */
  public static UsedNavigationProperty buildUsedNavigationProperty(
      EdmNavigationProperty navProp, String edmPath, String mongoPath) {
    String edmPropertyName = navProp != null ? navProp.getName() : null;
    String edmEntityFullName = null;
    boolean collection = false;
    String localKeyProperty = null;
    String foreignKeyProperty = null;

    if (navProp != null) {
      collection = navProp.isCollection();
      if (navProp.getType() != null) {
        if (navProp.getType().getFullQualifiedName() != null) {
          edmEntityFullName =
              navProp.getType().getFullQualifiedName().getFullQualifiedNameAsString();
        } else if (navProp.getType().getNamespace() != null
            && navProp.getType().getName() != null) {
          edmEntityFullName = navProp.getType().getNamespace() + "." + navProp.getType().getName();
        }
      }

      List<EdmReferentialConstraint> referentialConstraints = navProp.getReferentialConstraints();
      if ((referentialConstraints == null || referentialConstraints.isEmpty())
          && navProp.getPartner() != null) {
        referentialConstraints = navProp.getPartner().getReferentialConstraints();
        if (referentialConstraints != null && !referentialConstraints.isEmpty()) {
          EdmReferentialConstraint constraint = referentialConstraints.get(0);
          localKeyProperty = constraint.getReferencedPropertyName();
          foreignKeyProperty = constraint.getPropertyName();
        }
      } else if (referentialConstraints != null && !referentialConstraints.isEmpty()) {
        EdmReferentialConstraint constraint = referentialConstraints.get(0);
        localKeyProperty = constraint.getPropertyName();
        foreignKeyProperty = constraint.getReferencedPropertyName();
      }
    }

    return UsedNavigationProperty.builder()
        .withEdmPath(edmPath)
        .withMongoPath(mongoPath)
        .withEdmPropertyName(edmPropertyName)
        .withEdmEntityFullName(edmEntityFullName)
        .withCollection(collection)
        .withLocalKeyProperty(localKeyProperty)
        .withForeignKeyProperty(foreignKeyProperty)
        .build();
  }

  /**
   * Extracts a path string from the given UriInfoResource using the specified delimiter.
   *
   * @param uriInfoResource the URI info resource
   * @param delimiter the segment delimiter (such as "/" or ".")
   * @return the joined path string
   */
  public static String extractPath(UriInfoResource uriInfoResource, String delimiter) {
    if (uriInfoResource == null || uriInfoResource.getUriResourceParts() == null) {
      return "";
    }
    return extractPath(uriInfoResource.getUriResourceParts(), delimiter);
  }

  /**
   * Extracts a path string from the given list of UriResource parts using the specified delimiter.
   *
   * @param uriResources the list of URI resource parts
   * @param delimiter the segment delimiter (such as "/" or ".")
   * @return the joined path string
   */
  public static String extractPath(List<UriResource> uriResources, String delimiter) {
    if (uriResources == null || uriResources.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (UriResource resource : uriResources) {
      String segName = null;
      if (resource instanceof UriResourceProperty) {
        EdmProperty prop = ((UriResourceProperty) resource).getProperty();
        segName = prop != null ? prop.getName() : resource.getSegmentValue();
      } else if (resource instanceof UriResourceNavigation) {
        EdmNavigationProperty navProp = ((UriResourceNavigation) resource).getProperty();
        segName = navProp != null ? navProp.getName() : resource.getSegmentValue();
      } else if (!(resource instanceof UriResourceCount)
          && !(resource instanceof UriResourceLambdaAny)
          && !(resource instanceof UriResourceLambdaAll)
          && !(resource instanceof UriResourceLambdaVariable)) {
        segName = resource.getSegmentValue();
      } else {
        break;
      }
      if (segName == null) {
        break;
      }
      if (sb.length() > 0) {
        sb.append(delimiter);
      }
      sb.append(segName);
    }
    return sb.toString();
  }
}
