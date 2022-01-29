package io.resttestgen.nominaltester.reports.gsonadapters;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.resttestgen.nominaltester.models.ExecutionResult;
import io.resttestgen.swaggerschema.models.ValidationError;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Gson Adapter for serialization/deserialization of object of class ExecutionResult
 */
public class ExecutionResultTypeAdapter implements JsonSerializer<ExecutionResult>, JsonDeserializer<ExecutionResult> {

    @Override
    public ExecutionResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject asJsonObject = json.getAsJsonObject();
        int statusCode = asJsonObject.get("statusCode").getAsInt();

        Type headersMapType = new TypeToken<Map<String, List<String>>>(){}.getType();
        Map<String, List<String>> headers = context.deserialize(asJsonObject.get("responseHeaders"), headersMapType);

        Type validationErrorsType = new TypeToken<ArrayList<ValidationError>>(){}.getType();
        List<ValidationError> validationErrors = context.deserialize(asJsonObject.get("validationErrors"), validationErrorsType);

        ExecutionResult executionResult = new ExecutionResult();
        executionResult.setResponseHeaders(headers);
        executionResult.setStatusCode(statusCode);
        executionResult.setValidationErrors(validationErrors);

        if (asJsonObject.get("responseBody") != null) {
            String responseBody = asJsonObject.get("responseBody").getAsString();
            executionResult.setResponseBody(responseBody);
        }

        return executionResult;
    }

    @Override
    public JsonElement serialize(ExecutionResult src, Type typeOfSrc, JsonSerializationContext context) {
        int statusCode = src.getStatusCode();
        Map<String, List<String>> headers = src.getResponseHeaders();
        String responseBody = src.getResponseBody();
        List<ValidationError> validationErrors = src.getValidationErrors();
        Map<String, Object> map = new TreeMap<>();
        Map<String, List<String>> requestHeaders = new HashMap<>();
        try {
            requestHeaders = src.getRequest().headers().toMultimap();
            map.put("requestUrl", src.getRequest().httpUrl().toString());
        } catch (NullPointerException ignored) {
        }
        map.put("requestHeaders", requestHeaders);
        map.put("responseHeaders", headers);
        map.put("statusCode", statusCode);
        map.put("responseBody", responseBody);
        map.put("validationErrors", validationErrors);
        return context.serialize(map);
    }
}
