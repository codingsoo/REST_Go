package io.resttestgen.nominaltester.reports.gsonadapters;

import com.google.gson.*;
import io.resttestgen.requestbuilder.Request;
import io.resttestgen.requestbuilder.parameters.RequestParameter;
import io.resttestgen.nominaltester.helper.RequestParameterHelper;
import io.resttestgen.nominaltester.models.ExecutionParameter;
import io.resttestgen.nominaltester.models.ExecutionResult;
import io.resttestgen.nominaltester.models.OperationInfo;
import io.resttestgen.nominaltester.models.TestStep;
import io.swagger.v3.oas.models.OpenAPI;

import java.lang.reflect.Type;
import java.util.*;

/**
 * This class creates a TestStep and generates a Request by analysing the JSON report.
 */
public class TestStepAdapter implements JsonSerializer<TestStep>, JsonDeserializer<TestStep> {
    private final OpenAPI openAPI;

    /**
     * Initialises the openAPI variable of the class
     * @param openAPI an OpenAPI object containing the result of the parsing of the Swagger file
     */
    public TestStepAdapter(OpenAPI openAPI) {
        this.openAPI = openAPI;
    }

    @Override
    public JsonElement serialize(TestStep src, Type typeOfSrc, JsonSerializationContext context) {
        OperationInfo targetOperation = src.getTargetOperation();
        int statusCode = src.getExecutionResult().getStatusCode();
        Map<String, Object> map = new TreeMap<>();
        map.put("targetOperation", context.serialize(targetOperation));
        map.put("statusCode", statusCode);
        map.put("parameters", src.getExecutionParameters());
        map.put("executionResult", src.getExecutionResult());
        return context.serialize(map);
    }

    @Override
    public TestStep deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject asJsonObject = json.getAsJsonObject();

        JsonElement executionResultAsJsonElement = asJsonObject.get("executionResult");
        JsonElement executionParametersAsJsonElement = asJsonObject.get("parameters");
        JsonElement targetOperation = asJsonObject.get("targetOperation");

        OperationInfo operationInfo = context.deserialize(targetOperation, OperationInfo.class);

        ExecutionParameter[] executionParameters = context.deserialize(
                executionParametersAsJsonElement, ExecutionParameter[].class);
        TestStep testStep = new TestStep(operationInfo, new ArrayList<>(Arrays.asList(executionParameters)));

        ExecutionResult executionResult = context.deserialize(executionResultAsJsonElement, ExecutionResult.class);
        RequestParameterHelper requestParameterHelper = new RequestParameterHelper(openAPI);
        List<RequestParameter> requestParameters = requestParameterHelper.createRequestParameters(operationInfo.getOperationId(), Arrays.asList(executionParameters));

        Request request = new Request.Builder(openAPI, operationInfo.getOperationId())
                .addRequestParameters(requestParameters).build();

        testStep.setRequest(request);
        testStep.setRequestParameters(requestParameters);
        testStep.setExecutionResult(executionResult);
        testStep.setStatusCode(executionResult.getStatusCode());

        return testStep;
    }


}
