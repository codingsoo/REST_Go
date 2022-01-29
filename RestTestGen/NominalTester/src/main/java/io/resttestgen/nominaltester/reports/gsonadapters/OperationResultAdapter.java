package io.resttestgen.nominaltester.reports.gsonadapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.resttestgen.nominaltester.models.OperationResult;
import io.resttestgen.nominaltester.models.summaries.ResponseCoverageSummary;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

/**
 * Gson Adapter for serialization/deserialization of object of class OperationInfo
 */
public class OperationResultAdapter implements JsonSerializer<OperationResult> {

    @Override
    public JsonElement serialize(OperationResult src, Type typeOfSrc, JsonSerializationContext context) {
        String operationId = src.getOperationId();
        ResponseCoverageSummary responseCoverageSummary = src.getResponseCoverage().getSummary();
        Map<String, Object> map = new TreeMap<>();
        map.put("operationId", operationId);
        map.put("responseCoverage", responseCoverageSummary);
        map.put("exceptions", src.getExceptions());
        return context.serialize(map);
    }
}
