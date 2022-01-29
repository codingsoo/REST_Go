package io.resttestgen.nominaltester.reports.gsonadapters;

import com.google.gson.*;
import io.resttestgen.nominaltester.models.ExecutionParameter;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

public class ExecutionParameterAdapter implements JsonSerializer<ExecutionParameter>, JsonDeserializer<ExecutionParameter> {

    @Override
    public ExecutionParameter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject asJsonObject = json.getAsJsonObject();

        String parameterName = asJsonObject.get("parameterName").getAsString();
        String sanitizedName = asJsonObject.get("sanitizedName").getAsString();

        JsonElement valueAsJsonElement = asJsonObject.get("value");
        Object value = null;

        // If value is a primitive type, return it as a String; otherwise, get it as appropriate Json type and
        // transform it into a String
        if (valueAsJsonElement == null || valueAsJsonElement.isJsonNull())
            value = "null";
        else if (valueAsJsonElement.isJsonPrimitive())
            value = valueAsJsonElement.getAsString();
        else if (valueAsJsonElement.isJsonObject())
            value = valueAsJsonElement.getAsJsonObject();
        else if (valueAsJsonElement.isJsonArray())
            value = valueAsJsonElement.getAsJsonArray();

        ExecutionParameter parameter = new ExecutionParameter(parameterName);
        parameter.setParameterName(parameterName);
        parameter.setSanitizedName(sanitizedName);
        parameter.setParameterClassName(parameterName);
        parameter.setValue(value);

        return parameter;
    }

    @Override
    public JsonElement serialize(ExecutionParameter src, Type typeOfSrc, JsonSerializationContext context) {
        String className = src.getValue().getClass().getName();
        JsonElement value = context.serialize(src.getValue());
        Map<String, Object> map = new TreeMap<>();
        map.put("sanitizedName", src.getSanitizedName());
        map.put("parameterName", src.getParameterName());
        map.put("className", className);
        map.put("value", value);
        return context.serialize(map);
    }
}