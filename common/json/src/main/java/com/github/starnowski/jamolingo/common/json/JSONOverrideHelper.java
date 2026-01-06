package com.github.starnowski.jamolingo.common.json;

import jakarta.json.Json;
import jakarta.json.JsonMergePatch;
import jakarta.json.JsonPatch;
import jakarta.json.JsonReader;

import java.io.IOException;
import java.io.StringReader;

/**
 * TODO add tests cases
 */
public class JSONOverrideHelper {

    private final PatchHelper patchHelper;

    public JSONOverrideHelper() {
        this(new PatchHelper());
    }

    public JSONOverrideHelper(PatchHelper patchHelper) {
        this.patchHelper = patchHelper;
    }

    public enum PatchType  {
        /**
         * JSON Patch (RFC 6902)
         */
        JSON_PATCH,
        /**
         * JSON Merge Patch (RFC 7396)
         */
        MERGE
    }

    public <T> T applyChangesToJson(T original, String patchPayload, Class<T> clazz, PatchType pathType) throws IOException {
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
        //TODO
    }

}
