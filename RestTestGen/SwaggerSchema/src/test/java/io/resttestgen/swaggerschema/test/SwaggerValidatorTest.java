package io.resttestgen.swaggerschema.test;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import io.resttestgen.swaggerschema.SchemaValidatorAdapter;
import io.resttestgen.swaggerschema.models.ValidationError;
import io.resttestgen.swaggerschema.models.exceptions.SchemaValidationException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SwaggerValidatorTest {

    private SchemaValidatorAdapter validatorAdapter;
    private OpenAPI openAPI;

    @Before
    public void beforeFunction() throws IOException {
        File petstore = new File("src/test/resources/petstore.json");
        this.openAPI = new OpenAPIV3Parser().read(petstore.getPath());
        this.validatorAdapter = SchemaValidatorAdapter.fromOpenAPI(openAPI);
    }

    @Test
    public void successfulValidationFromPointer() throws IOException, ProcessingException {
        // Create pet
        String petStr = "{\n" +
                "  \"id\": 0,\n" +
                "  \"category\": {\n" +
                "    \"id\": 0,\n" +
                "    \"name\": \"string\"\n" +
                "  },\n" +
                "  \"name\": \"doggie\",\n" +
                "  \"photoUrls\": [\n" +
                "    \"string\"\n" +
                "  ],\n" +
                "  \"tags\": [\n" +
                "    {\n" +
                "      \"id\": 0,\n" +
                "      \"name\": \"string\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"status\": \"available\"\n" +
                "}";

        List<ValidationError> validate = validatorAdapter.validate(petStr, "/components/schemas/Pet");
        assertEquals(0, validate.size());
    }

    @Test
    public void successfulValidationFromPointer_Array() throws SchemaValidationException {
        // Create pet
        String petStr = "[{\n" +
                "  \"id\": 0,\n" +
                "  \"category\": {\n" +
                "    \"id\": 0,\n" +
                "    \"name\": \"string\"\n" +
                "  },\n" +
                "  \"name\": \"doggie\",\n" +
                "  \"photoUrls\": [\n" +
                "    \"string\"\n" +
                "  ],\n" +
                "  \"tags\": [\n" +
                "    {\n" +
                "      \"name\": \"string\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"status\": \"available\"\n" +
                "}]";

        PathItem pathItem = this.openAPI.getPaths().get("/pet/findByStatus");
        ApiResponses responses = pathItem.getGet().getResponses();
        ApiResponse apiResponse = responses.get("200");

        MediaType mediaType = apiResponse.getContent().get("application/json");
        Schema schema = mediaType.getSchema();

        List<ValidationError> validate = validatorAdapter.checkSwaggerResponseValidationErrors(petStr, schema);
        assertEquals(0, validate.size());
    }

    @Test
    public void unsuccessfulValidationFromPointer_Array() throws SchemaValidationException {
        // Create pet
        String petStr = "[{\n" +
                "  \"id\": 0,\n" +
                "  \"category\": {\n" +
                "    \"id\": 0,\n" +
                "    \"name\": \"string\"\n" +
                "  },\n" +
                "  \"photoUrls\": [\n" +
                "    \"string\"\n" +
                "  ],\n" +
                "  \"tags\": [\n" +
                "    {\n" +
                "      \"name\": \"string\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"status\": \"available\"\n" +
                "}]";

        PathItem pathItem = this.openAPI.getPaths().get("/pet/findByStatus");
        ApiResponses responses = pathItem.getGet().getResponses();
        ApiResponse apiResponse = responses.get("200");

        MediaType mediaType = apiResponse.getContent().get("application/json");
        Schema schema = mediaType.getSchema();

        List<ValidationError> validate = validatorAdapter.checkSwaggerResponseValidationErrors(petStr, schema);
        assertEquals(1, validate.size());
    }

    @Test
    public void unsuccessfulValidationFromPointer() throws IOException, ProcessingException {
        // Create pet, without required field - name
        String petStr = "{\n" +
                "  \"id\": 0,\n" +
                "  \"category\": {\n" +
                "    \"id\": 0,\n" +
                "    \"name\": \"string\"\n" +
                "  },\n" +
                "  \"photoUrls\": [\n" +
                "    \"string\"\n" +
                "  ],\n" +
                "  \"tags\": [\n" +
                "    {\n" +
                "      \"id\": 0,\n" +
                "      \"name\": \"string\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"status\": \"available\"\n" +
                "}";

        List<ValidationError> validate = validatorAdapter.validate(petStr, "/components/schemas/Pet");
        assertEquals(1, validate.size());
    }

    @Test
    public void validationFromSchema() throws IOException, ProcessingException {
        // Create pet, without required field - name
        String petStr = "{\n" +
                "  \"id\": 0,\n" +
                "  \"category\": {\n" +
                "    \"id\": 0,\n" +
                "    \"name\": \"string\"\n" +
                "  },\n" +
                "  \"photoUrls\": [\n" +
                "    \"string\"\n" +
                "  ],\n" +
                "  \"tags\": [\n" +
                "    {\n" +
                "      \"id\": 0,\n" +
                "      \"name\": \"string\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"status\": \"available\"\n" +
                "}";

        Schema petSchema = this.openAPI.getComponents().getSchemas().get("Pet");
        List<ValidationError> validate = validatorAdapter.validate(petStr, petSchema);
        assertEquals(1, validate.size());
    }

}
