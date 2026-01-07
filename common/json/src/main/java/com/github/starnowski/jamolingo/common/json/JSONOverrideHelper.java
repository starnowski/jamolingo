package com.github.starnowski.jamolingo.common.json;

import jakarta.json.Json;
import jakarta.json.JsonMergePatch;
import jakarta.json.JsonPatch;
import jakarta.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;

/** Helper class to apply JSON changes (patches) to Java objects using different strategies. */
public class JSONOverrideHelper {

  private final PatchHelper patchHelper;

  /** Constructs a new JSONOverrideHelper with a default PatchHelper. */
  public JSONOverrideHelper() {
    this(new PatchHelper());
  }

  /**
   * Constructs a new JSONOverrideHelper with the specified PatchHelper.
   *
   * @param patchHelper The PatchHelper to use.
   */
  public JSONOverrideHelper(PatchHelper patchHelper) {
    this.patchHelper = patchHelper;
  }

  /** Enumeration of supported patch types. */
  public enum PatchType {
    /** JSON Patch (RFC 6902) */
    JSON_PATCH,
    /** JSON Merge Patch (RFC 7396) */
    MERGE
  }

  /**
   * Applies changes to a JSON object (represented as a Java object) based on the provided patch
   * payload and type.
   *
   * @param original The original object to apply changes to.
   * @param patchPayload The JSON payload representing the patch.
   * @param clazz The class of the object.
   * @param pathType The type of patch to apply.
   * @param <T> The type of the object.
   * @return The modified object, or null if the patch type is not supported.
   * @throws IOException If an I/O error occurs.
   */
  public <T> T applyChangesToJson(
      T original, String patchPayload, Class<T> clazz, PatchType pathType) throws IOException {
    switch (pathType) {
      case MERGE:
        JsonReader mergeReader = Json.createReader(new StringReader(patchPayload));
        JsonMergePatch merge = Json.createMergePatch(mergeReader.readValue());
        return patchHelper.applyMergePatch(merge, original, clazz);
      case JSON_PATCH:
        JsonReader patchReader = Json.createReader(new StringReader(patchPayload));
        JsonPatch patch = Json.createPatch(patchReader.readArray());
        return patchHelper.applyJsonPatch(patch, original, clazz);
      default:
        return null;
    }
  }
}
