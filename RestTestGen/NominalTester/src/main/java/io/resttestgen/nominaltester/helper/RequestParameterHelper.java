package io.resttestgen.nominaltester.helper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.okhttp.MediaType;
import io.resttestgen.requestbuilder.RequestParameterExtractor;
import io.resttestgen.requestbuilder.parameters.*;
import io.resttestgen.nominaltester.models.ExecutionParameter;
import io.resttestgen.nominaltester.reports.ReportCustomGson;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestParameterHelper {

    OpenAPI openAPI;
    Gson customGson;

    public RequestParameterHelper(OpenAPI openAPI) {
        this.openAPI = openAPI;
        customGson = ReportCustomGson.getCustomGson(this.openAPI);
    }

    public List<RequestParameter> createRequestParameters(String operationId,
                                                          List<ExecutionParameter> executionParameters) {

        ArrayList<ExecutionParameter> simplifiedExecutionParameters = new ArrayList<>();
        for (ExecutionParameter executionParameter : executionParameters) {
            String serializedExecutionParameterJSON = customGson.toJson(executionParameter);
            ExecutionParameter simplifiedExecutionParameter = customGson.fromJson(serializedExecutionParameterJSON, ExecutionParameter.class);
            simplifiedExecutionParameters.add(simplifiedExecutionParameter);
        }

        Map<String, ExecutionParameter> executionParametersMap = simplifiedExecutionParameters.stream().collect(
                Collectors.toMap(ExecutionParameter::getSanitizedName, executionParameter -> executionParameter));

        // Initialise the RequestBuilder
        RequestParameterExtractor requestBuilder = new RequestParameterExtractor(openAPI);
        // Get the request parameters for the current operation
        List<RequestParameter> requestParameters = requestBuilder.getRequestParametersFromOperation(operationId);
        List<RequestParameter> actualRequestParameters = new ArrayList<>();

        // Fill the request parameters' values by iterating over the operation info's execution parameters
        for (RequestParameter requestParameter : requestParameters) {
            // An optional parameter was not used: just skip it
            if (executionParametersMap.get(requestParameter.getParameterName()) == null)
                continue;

            actualRequestParameters.add(requestParameter);

            switch (requestParameter.getParameterType()) {
                case BODY:
                    // SwaggerGen use application/json by default
                    // https://github.com/swagger-api/swagger-codegen-generators/blob/master/src/main/resources/mustache/Java/libraries/okhttp-gson/ApiClient.mustache#L994
                    String contentType = "application/json";
                    ExecutionParameter contentTypeHeaderParameter = executionParametersMap.get("Content-Type");
                    if (contentTypeHeaderParameter != null) {
                        contentType = (String) contentTypeHeaderParameter.getValue();
                    }
                    BodyParameter bodyParameter = (BodyParameter) requestParameter;
                    bodyParameter.setParameterValue(
                            MediaType.parse(contentType),
                            executionParametersMap.get(bodyParameter.getParameterName()).getValue());
                    break;
                case HEADER:
                    HeaderParameter headerParameter = (HeaderParameter) requestParameter;
                    String value =
                            (String) executionParametersMap.get(headerParameter.getParameterName()).getValue();
                    headerParameter.setParameterValue(value);
                    break;
                case QUERY:
                    QueryParameter queryParameter = (QueryParameter) requestParameter;

                    Object queryParameterValue =
                            executionParametersMap.get(queryParameter.getParameterName()).getValue();
                    ArrayList<String> valuesList = new ArrayList<>();
                    if (queryParameterValue instanceof String) {
                        valuesList.add((String) queryParameterValue);
                    } else if (queryParameterValue instanceof JsonArray) {
                        JsonArray rawValue = (JsonArray) queryParameterValue;

                        // TODO: review this thing
                        if (rawValue.size() > 0) {
                            if (rawValue.get(0).isJsonPrimitive())
                                valuesList.addAll(Arrays.asList(new Gson().fromJson(rawValue, String[].class)));
                            else if (rawValue.get(0).isJsonObject()) {
                                Object[] objects = new Gson().fromJson(rawValue, Object[].class);
                                for (Object object : objects) {
                                    String s = new Gson().toJson(object);
                                    valuesList.add(s);
                                }
                            }
                        }
                    } else {
                        JsonObject rawValue = (JsonObject) queryParameterValue;
                        String s = new Gson().toJson(rawValue);
                        valuesList.add(s);
                    }

                    queryParameter.setParameterValues(valuesList);
                    break;
                case PATH:
                    PathParameter pathParameter = (PathParameter) requestParameter;
                    Object pathValue = executionParametersMap.get(pathParameter.getParameterName()).getValue();
                    String finalValue;
                    if (pathValue instanceof String) {
                        finalValue = (String) pathValue;
                    } else {
                        finalValue = new Gson().toJson(pathValue);
                    }

                    pathParameter.setParameterValue(finalValue);
                    break;
                default:
                    break;
            }
        }

        return actualRequestParameters;
    }
}
