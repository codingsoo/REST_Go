package io.resttestgen.requestbuilder;

import io.resttestgen.requestbuilder.parameters.*;
import io.resttestgen.swaggerschema.SchemaExtractor;
import io.resttestgen.swaggerschema.models.ParameterType;
import io.resttestgen.swaggerschema.models.SwaggerOperation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequestParameterExtractor {

    private OpenAPI openAPI;
    private Map<String, SwaggerOperation> operationMap;

    public RequestParameterExtractor(OpenAPI openAPI) {
        this.openAPI = openAPI;
        this.operationMap = SchemaExtractor.getOperationsMap(openAPI);
    }

    /**
     * Returns a list of parameters extracted from the swagger
     * @param operationId target operation Id
     * @return list of requested parameters
     */
    public List<RequestParameter> getRequestParametersFromOperation(String operationId) {
        final SwaggerOperation operation = this.operationMap.get(operationId);
        final Operation operationSchema = operation.getOperationSchema();

        List<RequestParameter> requestParameters = new ArrayList<>();

        // Path and Query parameters
        List<Parameter> parameters = operationSchema.getParameters();
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                String in = parameter.getIn();

                // Factory
                RequestParameter requestParameter;
                switch (in) {
                    case "query":
                        requestParameter = new QueryParameter();
                        requestParameter.setParameterType(ParameterType.QUERY);
                        break;
                    case "header":
                        requestParameter = new HeaderParameter();
                        requestParameter.setParameterType(ParameterType.HEADER);
                        break;
                    default:
                        requestParameter = new PathParameter();
                        requestParameter.setParameterType(ParameterType.PATH);
                }

                requestParameter.setRequired(parameter.getRequired());
                requestParameter.setParameterIn(in);
                requestParameter.setParameterSchema(parameter.getSchema());
                requestParameter.setParameterName(parameter.getName());
                requestParameters.add(requestParameter);
            }
        }

        // Request Body
        RequestBody requestBody = operationSchema.getRequestBody();
        if (requestBody != null) {
            BodyParameter bodyParameter = new BodyParameter();
            bodyParameter.setParameterType(ParameterType.BODY);
            bodyParameter.setRequired(requestBody.getRequired());
            bodyParameter.setContent(requestBody.getContent());
            bodyParameter.setParameterIn("body");
            bodyParameter.setParameterName("body");
            requestParameters.add(bodyParameter);
        }

        return requestParameters;
    }
}
