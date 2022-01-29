package io.resttestgen.swaggerschema.test;

import io.resttestgen.swaggerschema.SchemaExtractor;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SchemaExtractorTest {

    private OpenAPI openAPI;
    private static File petStoreSwagger;

    @Before
    public void beforeFunction(){
        petStoreSwagger = new File("src/test/resources/petstore.json");
        this.openAPI = new OpenAPIV3Parser().read(petStoreSwagger.getPath());
    }

    @Test
    public void extractInputParameters() {
        SchemaExtractor schemaExtractor = new SchemaExtractor(openAPI);
        Operation addPetOperation = this.openAPI.getPaths().get("/pet").getPost(); // add pet operation
        Set<String> inputs = schemaExtractor.extractInputParameters(addPetOperation);

        assertTrue(inputs.size() > 0);
        assertTrue(inputs.contains("pet"));
        assertTrue(inputs.contains("pet:id"));
        assertTrue(inputs.contains("pet:name"));
        assertTrue(inputs.contains("pet:category"));
        assertTrue(inputs.contains("category"));
        assertTrue(inputs.contains("tag"));
        assertTrue(inputs.contains("category:id"));
    }

    @Test
    public void extractInputParameters_2() {
        SchemaExtractor schemaExtractor = new SchemaExtractor(openAPI);
        Operation getPetsByTags = this.openAPI.getPaths().get("/pet/findByTags").getGet();
        Set<String> inputs = schemaExtractor.extractInputParameters(getPetsByTags);

        assertTrue(inputs.size() > 0);
        assertTrue(inputs.contains("tags"));
    }

    @Test
    public void extractInputParameters_3() {
        SchemaExtractor schemaExtractor = new SchemaExtractor(openAPI);
        Operation findPetById = this.openAPI.getPaths().get("/pet/{petId}").getGet();
        Set<String> inputs = schemaExtractor.extractInputParameters(findPetById);

        assertTrue(inputs.size() > 0);
        assertTrue(inputs.contains("petId"));
    }

    @Test
    public void extractOutputParameters() {
        SchemaExtractor schemaExtractor = new SchemaExtractor(openAPI);
        Operation getPetById = this.openAPI.getPaths().get("/pet/{petId}").getGet(); // add pet operation
        Set<String> output = schemaExtractor.extractOutputParameters(getPetById);

        assertTrue(output.size() > 0);
        assertTrue(output.contains("pet"));
        assertTrue(output.contains("pet:id"));
        assertTrue(output.contains("pet:name"));
        assertTrue(output.contains("pet:category"));
        assertTrue(output.contains("category"));
        assertTrue(output.contains("category:id"));
    }

    @Test
    public void getParametersRecursivelyWithNoGoDeepTest() {
        final Schema petSchema = this.openAPI.getComponents().getSchemas().get("Pet");

        SchemaExtractor schemaExtractor = new SchemaExtractor(openAPI, 0);
        Set<String> parameters = schemaExtractor.getParametersRecursively(petSchema, new HashSet<>(), 0);

        assertTrue(parameters.contains("name"));
        assertTrue(parameters.contains("id"));
        assertTrue(parameters.contains("name"));
        assertFalse(parameters.contains("pet"));
        assertFalse(parameters.contains("pet:category"));
        assertFalse(parameters.contains("category:id"));
    }

    @Test
    public void getParametersRecursivelyWithGoDeepTest() {
        final Schema petSchema = this.openAPI.getComponents().getSchemas().get("Pet");

        SchemaExtractor schemaExtractor = new SchemaExtractor(openAPI, 1);
        Set<String> parameters = schemaExtractor.getParametersRecursively(petSchema, new HashSet<>(), 0);

        assertTrue(parameters.contains("name"));
        assertTrue(parameters.contains("id"));
        assertTrue(parameters.contains("name"));
        assertFalse(parameters.contains("pet"));
        assertFalse(parameters.contains("pet:category"));
        assertTrue(parameters.contains("category:id"));
    }

    @Test
    public void getParametersRecursivelyWithCycleSchema() {
        OpenAPI openAPI = new OpenAPIV3Parser().read(petStoreSwagger.getPath());

        // create a schema cycle
        // change tags to ArraySchema with items: Pet
        Components components = openAPI.getComponents();
        Map<String, Schema> componentsSchemas = components.getSchemas();
        ObjectSchema petSchema = (ObjectSchema) componentsSchemas.get("Pet");
        Map<String, Schema> properties = petSchema.getProperties();
        ArraySchema tagsSchema = (ArraySchema) properties.get("tags");
        Schema tagsArrayItemSchema = tagsSchema.getItems();
        tagsArrayItemSchema.set$ref("#/components/schemas/Pet");

        SchemaExtractor schemaExtractor = new SchemaExtractor(openAPI, 10);
        Set<String> parameters = schemaExtractor.getParametersRecursively(petSchema, new HashSet<>(), 0);
        assertTrue(parameters.size() > 0);
    }
}