package com.github.starnowski.jamolingo.common.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.*;
import java.io.IOException;
import java.io.StringReader;

/**
 * Helper class for applying JSON patches (RFC 6902) and JSON Merge Patches (RFC 7396) to Java
 * objects.
 */
public class PatchHelper {

  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * Applies a JSON Patch (RFC 6902) to a target object.
   *
   * @param patch The JSON Patch to apply.
   * @param target The target object to apply the patch to.
   * @param type The class of the target object.
   * @param <T> The type of the target object.
   * @return The modified object.
   * @throws IOException If there is an issue reading or writing JSON.
   * @throws JsonProcessingException If there is an issue processing JSON with Jackson.
   */
  public <T> T applyJsonPatch(JsonPatch patch, T target, Class<T> type)
      throws IOException, JsonProcessingException {
    // Convert POJO -> JSON string
    String targetJson = mapper.writeValueAsString(target);

    // Parse string into JSON-P structure
    JsonReader reader = Json.createReader(new StringReader(targetJson));
    JsonStructure targetStructure = reader.read();
    JsonValue patchedJson = patch.apply(targetStructure);
    return mapper.readValue(patchedJson.toString(), type);
  }

  /**
   * Applies a JSON Merge Patch (RFC 7396) to a target object.
   *
   * @param mergePatch The JSON Merge Patch to apply.
   * @param target The target object to apply the patch to.
   * @param type The class of the target object.
   * @param <T> The type of the target object.
   * @return The modified object.
   * @throws IOException If there is an issue reading or writing JSON.
   */
  public <T> T applyMergePatch(JsonMergePatch mergePatch, T target, Class<T> type)
      throws IOException {
    // Convert POJO -> JSON string
    String targetJson = mapper.writeValueAsString(target);

    // Parse string into JSON-P structure
    JsonReader reader = Json.createReader(new StringReader(targetJson));
    JsonStructure targetStructure = reader.read();
    JsonValue patchedJson = mergePatch.apply(targetStructure);
    return mapper.readValue(patchedJson.toString(), type);
  }
}
