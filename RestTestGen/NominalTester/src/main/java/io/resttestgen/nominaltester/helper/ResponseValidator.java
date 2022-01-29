package io.resttestgen.nominaltester.helper;

import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import io.resttestgen.nominaltester.models.ExecutionResult;
import io.resttestgen.nominaltester.models.TestStep;
import io.resttestgen.swaggerschema.SchemaEditor;
import io.resttestgen.swaggerschema.SchemaExtractor;
import io.resttestgen.swaggerschema.SchemaValidatorAdapter;
import io.resttestgen.swaggerschema.models.SwaggerOperation;
import io.resttestgen.swaggerschema.models.ValidationError;
import io.resttestgen.swaggerschema.models.exceptions.SchemaValidationException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ResponseValidator {

    static final Logger logger = LogManager.getLogger(ApiResponseParser.class);
    static SchemaValidatorAdapter schemaValidator;
    private HashMap<String, SwaggerOperation> operationsMap;

    public ResponseValidator(OpenAPI openAPI) throws SchemaValidationException {
        try {
            schemaValidator = SchemaValidatorAdapter.fromString(SchemaEditor.toJSONSchema(openAPI));
            operationsMap = SchemaExtractor.getOperationsMap(openAPI);
        } catch (IOException e) {
            throw new SchemaValidationException("Cannot create schema validator object", null, e);
        }
    }
    /**
     * Get the matching response Schema given the Operation Schema and the response status code
     * @param operationId string representing operationId
     * @param operationSchema target operation schema
     * @param responseStatusCode response status code
     * @param responseMediaType response MediaType
     * @return response schema
     * @throws SchemaValidationException if no matching schema is found
     */
    private Schema getMatchingResponseSchema(String operationId, Operation operationSchema, int responseStatusCode, com.squareup.okhttp.MediaType responseMediaType) throws SchemaValidationException {

        // Get Response Schema
        ApiResponses documentedResponses = operationSchema.getResponses();
        io.swagger.v3.oas.models.responses.ApiResponse response = documentedResponses.get(String.valueOf(responseStatusCode));
        if (response == null) {
            io.swagger.v3.oas.models.responses.ApiResponse defaultResponse = documentedResponses.get("default");
            if (defaultResponse != null) {
                logger.debug("Using 'default' response for validation");
                response = defaultResponse;
            }
            else {
                throw new SchemaValidationException("undocumented response schema", responseStatusCode);
            }
        }

        if (response.getContent() == null || response.getContent().values().size() == 0) {
            String msg = String.format("Cannot validate response for %s with status code %d: no response schema defined",
                    operationId, responseStatusCode);
            throw new SchemaValidationException(msg, response);
        }

        Collection<MediaType> values = response.getContent().values();
        Schema responseSchema = values.iterator().next().getSchema();
        for (MediaType value : values) {
            if (responseMediaType.toString().contains(value.toString())) {
                responseSchema = value.getSchema();
                break;
            }
        }

        if (responseSchema == null) {
            String msg = String.format("Cannot validate response for %s with status code %d: no response schema defined",
                    operationId, responseStatusCode);
            throw new SchemaValidationException(msg, response);
        }
        return responseSchema;
    }

    public List<ValidationError> checkResponseSchemaValidity(String operationId, Response okHttpResponse) throws SchemaValidationException {
        // Get Operation Schema
        SwaggerOperation swaggerOperation = operationsMap.get(operationId);
        if (swaggerOperation == null)  throw new SchemaValidationException("No operation " + operationId + " found", null);
        Operation operationSchema = swaggerOperation.getOperationSchema();

        // Get the response body, status code, and mediatype
        int actualResponseStatusCode = okHttpResponse.code();
        ResponseBody body = okHttpResponse.body();
        String actualResponseBody;
        com.squareup.okhttp.MediaType mediaType = com.squareup.okhttp.MediaType.parse("application/json");
        try {
            actualResponseBody = body.string();
            if (body.contentType() != null) mediaType = body.contentType();
        } catch (IOException e) {
            throw new SchemaValidationException("No body content to validate", okHttpResponse, e);
        }

        // Get Response Schema
        Schema responseSchema;
        try {
            responseSchema = getMatchingResponseSchema(operationId, operationSchema, actualResponseStatusCode, mediaType);
        } catch (SchemaValidationException e) {
            ValidationError validationError = new ValidationError();
            validationError.setLevel("error");
            validationError.setKeyword("undocumented status code");
            validationError.setMessage(actualResponseStatusCode + " not in swagger specification");
            ArrayList<ValidationError> objects = new ArrayList<>();
            objects.add(validationError);
            return objects;

        }

        // Check that exists a matching response schema
        return schemaValidator.checkSwaggerResponseValidationErrors(actualResponseBody, responseSchema);
    }

    public List<ValidationError> checkResponseSchemaValidity(TestStep testStep) throws SchemaValidationException {
        ExecutionResult executionResult = testStep.getExecutionResult();
        Operation operationSchema = testStep.getTargetOperation().getOperationSchema();
        String operationId = testStep.getTargetOperation().getOperationId();

        // Get the response body, status code, and mediatype
        int actualResponseStatusCode = executionResult.getStatusCode();
        String actualResponseBody = executionResult.getResponseBody();

        if (actualResponseBody != null) {
            com.squareup.okhttp.MediaType mediaType = com.squareup.okhttp.MediaType.parse("application/json");

            Schema responseSchema;
            try {
                // Get Response Schema
                responseSchema = getMatchingResponseSchema(operationId, operationSchema, actualResponseStatusCode, mediaType);
            } catch (SchemaValidationException e) {
                ValidationError validationError = new ValidationError();
                validationError.setLevel("error");
                validationError.setKeyword(e.getMessage());
                validationError.setMessage(actualResponseStatusCode + " not in swagger specification");
                ArrayList<ValidationError> objects = new ArrayList<>();
                objects.add(validationError);
                return objects;
            }

            // Validate
            return schemaValidator.checkSwaggerResponseValidationErrors(actualResponseBody, responseSchema);
        }

        return new ArrayList<>();
    }
}
