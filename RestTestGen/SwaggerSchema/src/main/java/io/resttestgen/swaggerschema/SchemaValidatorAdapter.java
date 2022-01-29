package io.resttestgen.swaggerschema;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bjansen.ssv.SwaggerV20Library;
import com.github.bjansen.ssv.SwaggerValidator;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.gson.JsonSyntaxException;
import io.resttestgen.swaggerschema.models.ValidationError;
import io.resttestgen.swaggerschema.models.exceptions.SchemaValidationException;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class SchemaValidatorAdapter {
    private final SwaggerValidator validator;

    private SchemaValidatorAdapter(SwaggerValidator validator) {
        this.validator = validator;
    }

    private static boolean isJSONValid(final String json) {
        boolean valid = false;
        try {
            final JsonParser parser = new ObjectMapper().getFactory().createParser(json);
            while (parser.nextToken() != null) {
            }
            valid = true;
        } catch (JsonParseException jpe) {
            jpe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return valid;
    }

    /**
     * Create an instance of SchemaValidatorAdapter from file
     * @param filepath path of the swagger specification file (json or yaml)
     */
    public static SchemaValidatorAdapter fromFile(String filepath) throws IOException {
        Reader reader = new FileReader(filepath);
        if (filepath.endsWith(".json")) {
            return new SchemaValidatorAdapter(SwaggerValidator.forJsonSchema(reader));
        } else {
            return new SchemaValidatorAdapter(SwaggerValidator.forYamlSchema(reader));
        }
    }

    /**
     * Create an instance of SchemaValidatorAdapter from OpenAPI Pojo
     * @param openAPI openAPI
     */
    public static SchemaValidatorAdapter fromOpenAPI(OpenAPI openAPI) throws IOException {
        String openAPIStr = SchemaEditor.toJSONSchema(openAPI);
        return SchemaValidatorAdapter.fromString(openAPIStr);
    }

    /**
     * Create an instance of SchemaValidatorAdapter from file
     * @param jsonSchemaString string representing the swagger specification schema (json or yaml)
     *
     */
    public static SchemaValidatorAdapter fromString(String jsonSchemaString) throws IOException {
        Reader reader = new StringReader(jsonSchemaString);
        boolean isJson = isJSONValid(jsonSchemaString);
        if (isJson) {
            return new SchemaValidatorAdapter(SwaggerValidator.forJsonSchema(reader));
        }
        return new SchemaValidatorAdapter(SwaggerValidator.forYamlSchema(reader));
    }

    /**
     * @param processingReport report generated from the validation
     * @return List of jackson.JsonNode representing the error messages
     */
    private List<ValidationError> getErrorProcessingMessages(ProcessingReport processingReport) throws IOException {
        Iterator<ProcessingMessage> iterator = processingReport.iterator();
        List<ValidationError> processingMessages = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        while (iterator.hasNext()) {
            ProcessingMessage processingMessage = iterator.next();
            JsonNode jsonNode = processingMessage.asJson();
            String level = jsonNode.get("level").asText();
            if (level.equals("error")) {
                ValidationError validationError = mapper.readValue(jsonNode.toString(), ValidationError.class);
                processingMessages.add(validationError);
            }
        }
        return processingMessages;
    }

    /**
     * @param json string representing the json to validate
     * @param definitionPointer string representing a point to the reference model definition
     * @return List of jackson.JsonNode representing the error messages
     * @throws IOException
     * @throws ProcessingException
     */
    public List<ValidationError> validate(String json, String definitionPointer) throws IOException, ProcessingException {
        ProcessingReport report = this.validator.validate(json, definitionPointer);
        return getErrorProcessingMessages(report);
    }

    public List<ValidationError> validate(JsonNode data, JsonNode schema) throws ProcessingException, IOException {
        JsonSchemaFactory jsonSchemaFactory = SwaggerV20Library.schemaFactory(LogLevel.INFO, LogLevel.FATAL);
        ProcessingReport report = jsonSchemaFactory.getJsonSchema(schema).validate(data);
        return getErrorProcessingMessages(report);
    }

    /**
     * @param jsonData json content to validate
     * @param schema self-contained json schema
     * @return list of validation errors
     * @throws ProcessingException
     * @throws IOException
     */
    public List<ValidationError> validate(String jsonData, Schema schema) throws ProcessingException, IOException {
        JsonNode jsonNodeSchema = Json.mapper().convertValue(schema, JsonNode.class);
        JsonNode jsonNodeData = Json.mapper().readTree(jsonData);
        JsonSchemaFactory jsonSchemaFactory = SwaggerV20Library.schemaFactory(LogLevel.INFO, LogLevel.FATAL);
        JsonSchema jsonSchema = jsonSchemaFactory.getJsonSchema(jsonNodeSchema);
        ProcessingReport report = jsonSchema.validate(jsonNodeData);
        return getErrorProcessingMessages(report);
    }

    /**
     * Validate the responseBody with the matching responseSchema
     * @param responseBody JSON string representing the responseBody to validate
     * @param responseSchema response schema detail
     * @return list of validation errors
     */
    public List<ValidationError> checkSwaggerResponseValidationErrors(String responseBody, Schema responseSchema) throws SchemaValidationException {
        List<ValidationError> errors = new ArrayList<>();

        // Get responseSchemaType and schemaRef
        String responseSchemaType = responseSchema.getType();
        String schemaRef = responseSchema.get$ref();

        if (responseSchemaType == null && responseSchema.get$ref() != null) {
            responseSchemaType = "object";
        }

        if ("array".equals(responseSchemaType)) {
            schemaRef = ((ArraySchema) responseSchema).getItems().get$ref();
        }

        try {
            if (schemaRef == null)  {
                return validate(responseBody, responseSchema);
            } else {
                schemaRef = schemaRef.replace("#", "");
                if (responseSchemaType != null) {
                    switch (responseSchemaType) {
                        case "object":
                            return validate(responseBody, schemaRef);
                        case "array":
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode root = mapper.readTree(responseBody);
                            if (root.isArray()) {
                                for (final JsonNode objNode : root) {
                                    List<ValidationError> error = validate(objNode.toString(), schemaRef);
                                    errors.addAll(error);
                                }
                            }
                            break;
                        default:
                            errors.addAll(validate(responseBody, responseSchema));
                    }
                }
            }
        } catch (JsonSyntaxException | ProcessingException e) {
            ValidationError validationError = new ValidationError();
            validationError.setMessage("Not valid JSON content");
            errors.add(validationError);
        } catch (IOException e) {
            throw new SchemaValidationException("Error during validation: " + e.getMessage(),
                    responseBody, e);
        }

        return errors;
    }

}
