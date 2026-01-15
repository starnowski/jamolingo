package com.github.starnowski.jamolingo.context;

/** Strategy for handling circular references in EDM mapping. */
public enum CircularStrategy {
  /** Embeds the referenced entity or complex type up to a specified maximum depth. */
  EMBED_LIMITED // embed up to maxDepth
}
