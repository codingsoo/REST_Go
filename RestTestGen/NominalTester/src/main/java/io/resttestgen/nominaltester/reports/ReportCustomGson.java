package io.resttestgen.nominaltester.reports;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.resttestgen.nominaltester.models.*;
import io.resttestgen.nominaltester.reports.gsonadapters.*;
import io.swagger.v3.oas.models.OpenAPI;

public class ReportCustomGson {

    /**
     * Returns custom Gson with adapters to handle serialization/deserialization of OperationCoverage object
     * @return gson object
     */
    public static Gson getCustomGson(OpenAPI openAPI) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.registerTypeHierarchyAdapter(OperationInfo.class, new OperationInfoAdapter(openAPI));
        builder.registerTypeHierarchyAdapter(Authentication.class, new AuthenticationTypeAdapter());
        builder.registerTypeHierarchyAdapter(TestStep.class, new TestStepAdapter(openAPI));
        builder.registerTypeHierarchyAdapter(ExecutionResult.class, new ExecutionResultTypeAdapter());
        builder.registerTypeHierarchyAdapter(ExecutionParameter.class, new ExecutionParameterAdapter());
        builder.registerTypeHierarchyAdapter(OperationResult.class, new OperationResultAdapter());
        builder.serializeSpecialFloatingPointValues();
        return builder.create();
    }
}
