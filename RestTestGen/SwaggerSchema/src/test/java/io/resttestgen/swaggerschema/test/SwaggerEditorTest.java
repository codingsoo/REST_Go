package io.resttestgen.swaggerschema.test;

import io.resttestgen.swaggerschema.SchemaEditor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class SwaggerEditorTest {

    private OpenAPI openAPI;

    @Before
    public void beforeFunction(){
        File petstore = new File("src/test/resources/petstore.json");
        this.openAPI = new OpenAPIV3Parser().read(petstore.getPath());
    }

    @Test
    public void createSchemaEditorTest() {
        SchemaEditor schemaEditor = new SchemaEditor(openAPI);
        assertNotNull(schemaEditor);
        assertNotNull(schemaEditor.getOpenAPI());
        assertNotNull(schemaEditor.getEntities());
    }

    @Test
    public void convertToJSONTest() {
        SchemaEditor schemaEditor = new SchemaEditor(openAPI);
        String outputJson = SchemaEditor.toJSONSchema(schemaEditor.getOpenAPI());
        assertTrue(outputJson.length() > 0);
        // Try to parse it back
        openAPI = new OpenAPIV3Parser().readContents(outputJson, null, null).getOpenAPI();
        assertNotNull(openAPI);
    }

    @Test
    public void setMinimumStringRequirementTest() throws InvocationTargetException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        SchemaEditor schemaEditor = new SchemaEditor(openAPI);
        schemaEditor.setDefaultStringMinLength(0);
        Map<String, Schema> schemas = this.openAPI.getComponents().getSchemas();
        List<Schema> stringSchemas = getAllSchemasWithType("string", schemas, null);
        for (Schema stringSchema : stringSchemas) {
            assertEquals(0, (int) stringSchema.getMinLength());
        }
    }

    @Test
    public void setAdditionalFieldRequirementTest() throws InvocationTargetException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        SchemaEditor schemaEditor = new SchemaEditor(openAPI);
        schemaEditor.setDefaultAdditionalPropertiesToAll(false);
        Map<String, Schema> schemas = this.openAPI.getComponents().getSchemas();
        List<Schema> stringSchemas = getAllSchemasWithType("object", schemas, null);
        for (Schema stringSchema : stringSchemas) {
            assertEquals(false, stringSchema.getAdditionalProperties());
        }
    }

    @Test
    public void resolveIdAmbiguityTest() {
        File openbankproject = new File("src/test/resources/openbankproject.json");
        openAPI = new OpenAPIV3Parser().read(openbankproject.getPath());
        SchemaEditor schemaEditor = new SchemaEditor(openAPI);
        schemaEditor.resolveAmbiguousIds();
        Map<String, Schema> schemas = this.openAPI.getComponents().getSchemas();
        Schema schema = schemas.get("BankJSONV220");
        List<String> required = schema.getRequired();
        Set<String> properties = schema.getProperties().keySet();
        assertFalse(required.contains("id"));
        assertTrue(required.contains("bankId"));
        assertFalse(properties.contains("id"));
        assertTrue(properties.contains("bankId"));
    }

    private List<Schema> getAllSchemasWithType(String type, Map<String, Schema> schemas, List<Schema> output) {
        if (output == null) {
            output = new ArrayList<>();
        }
        for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
            Schema schema = entry.getValue();

            if (schema == null || schema.getType() == null) continue;

            if (schema.getType().equals(type)) {
                output.add(schema);
            }

            Map<String, Schema> properties = schema.getProperties();
            if (properties != null && properties.size() > 0) {
                getAllSchemasWithType(type, properties, output);
            }
        }
        return output;
    }

}
