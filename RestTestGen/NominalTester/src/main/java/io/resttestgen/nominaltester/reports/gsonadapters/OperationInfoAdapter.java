package io.resttestgen.nominaltester.reports.gsonadapters;

import com.google.gson.*;
import io.resttestgen.nominaltester.helper.ReflectionHelper;
import io.resttestgen.nominaltester.models.HTTPMethod;
import io.resttestgen.nominaltester.models.OperationInfo;
import io.resttestgen.swaggerschema.SchemaExtractor;
import io.resttestgen.swaggerschema.models.SwaggerOperation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Gson Adapter for serialization/deserialization of object of class OperationInfo
 */
public class OperationInfoAdapter implements JsonSerializer<OperationInfo>, JsonDeserializer<OperationInfo> {

    private final OpenAPI openAPI;
    private final HashMap<String, SwaggerOperation> operationsMap;

    public OperationInfoAdapter(OpenAPI openAPI) {
        this.openAPI = openAPI;
        this.operationsMap = SchemaExtractor.getOperationsMap(openAPI);
    }

    @Override
    public OperationInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject asJsonObject = json.getAsJsonObject();
        OperationInfo operationInfo = new OperationInfo(null, asJsonObject.get("operationId").getAsString());
        operationInfo.setHttpMethod(HTTPMethod.valueOf(asJsonObject.get("httpMethod").getAsString()));
        operationInfo.setOperationPath(asJsonObject.get("operationPath").getAsString());

        if (asJsonObject.get("invocationClassName") != null && asJsonObject.get("invocationMethodName") != null) {
            String invocationClassName = asJsonObject.get("invocationClassName").getAsString();
            String invocationMethodName = asJsonObject.get("invocationMethodName").getAsString();
            operationInfo.setInvocationClassName(invocationClassName);

            try {
                Class<?> invocationClass = Class.forName(invocationClassName);
                Method invocationMethod = ReflectionHelper.getMethodByName(invocationClass, invocationMethodName);
                operationInfo.setInvocationMethod(invocationMethod);
            } catch (ClassNotFoundException e) {
                // no class found during deserialization
            }
        }

        Operation operationSchema = this.operationsMap.get(operationInfo.getOperationId()).getOperationSchema();
        operationInfo.setOperationSchema(operationSchema);
        return operationInfo;
    }

    @Override
    public JsonElement serialize(OperationInfo src, Type typeOfSrc, JsonSerializationContext context) {
        String operationId = src.getOperationId();
        String httpMethod = src.getHttpMethod().toString();
        String operationPath = src.getOperationPath();
        Map<String, Object> map = new TreeMap<>();
        map.put("operationId", operationId);
        map.put("httpMethod", httpMethod);

        if (src.getInvocationMethod() != null && src.getInvocationClassName() != null) {
            String invocationClassName = src.getInvocationClassName();
            String invocationMethodName = src.getInvocationMethod().getName();
            map.put("invocationClassName", invocationClassName);
            map.put("invocationMethodName", invocationMethodName);
        }

        map.put("operationPath", operationPath);
        return context.serialize(map);
    }
}
