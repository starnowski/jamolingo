package com.github.starnowski.jamolingo.core.mapping;

/** Strategy for handling circular references in EDM mapping. */
public enum CircularStrategy {
  /** Embeds the referenced entity or complex type up to a specified maximum depth. */
  EMBED_LIMITED // embed up to maxDepth
}
