package io.resttestgen.errortester;//package malformedmutator;
//
//import io.swagger.models.properties.RefProperty;
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.Operation;
//import io.swagger.v3.oas.models.media.ArraySchema;
//import io.swagger.v3.oas.models.media.MediaType;
//import io.swagger.v3.oas.models.media.Schema;
//import io.swagger.v3.oas.models.parameters.RequestBody;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
///**
// * SchemaExtractor contains several methods to extract information from swagger schema
// */
//public class BodyParameterSchemaSanitiser {
//
//    private Map<String, Schema> components;
//
//    public BodyParameterSchemaSanitiser(OpenAPI openAPI) {
//
//        // Initialise components map
//        this.components = new HashMap<>();
//        if (openAPI.getComponents() != null)
//            this.components = openAPI.getComponents().getSchemas();
//    }
//
//    /**
//     * Extract the list of input parameters from operation schema
//     * @param operationSchema target operation schema
//     * @return Set of operation's input parameters
//     */
//    public void sanitiseBodyParameters(Operation operationSchema) {
//        // Extract body input parameter
//        RequestBody requestBody = operationSchema.getRequestBody();
//        if (requestBody == null) {
//            return;
//        }
//
//        // Iterate over
//        Set<Map.Entry<String, MediaType>> entries = requestBody.getContent().entrySet();
//        entries.stream().map(entry -> entry.getValue().getSchema()).forEachOrdered(
//                schema -> sanitiseSchemaRecursively(schema, new HashSet<>()));
//    }
//
//    /**
//     * Given a schema, it sanitises it so that it is easier to visit it
//     *
//     * @param rootSchema Schema object to sanitise
//     * @param visited List of strings with visited schemas (to avoid cycles)
//     */
//    private void sanitiseSchemaRecursively(Schema rootSchema, Set<String> visited) {
//        // If the root schema is null, just return
//        if (rootSchema == null)
//            return;
//
//        // Try and fetch the current schema properties
//        Map<String, Schema> properties = rootSchema.getProperties();
//
//        // If there are properties, iterate over them and save them properly in the current schema
//        if (properties != null && properties.size() > 0) {
//            for (Map.Entry<String, Schema> entry : properties.entrySet()) {
//                String name = entry.getKey();
//                Schema propertySchema = entry.getValue();
//
//                // Fix missing names for the properties
//                if (propertySchema.getName() == null || propertySchema.getName().isEmpty()) {
//                    propertySchema.setName(name);
//                }
//
//                // Check each property
//                sanitiseSchemaRecursively(propertySchema, visited);
//            }
//        }
//
//        // If there are no properties inside the schema, check if there is a referenced schema. This is just
//        // exploring the referenced schema and thus the exploring depth counter should not be increased
//        if (properties == null) {
//            String nestedRef = rootSchema.get$ref();
//            if (nestedRef == null && "array".equals(rootSchema.getType())) {
//                ArraySchema arraySchema = (ArraySchema) rootSchema;
//                nestedRef = arraySchema.getItems().get$ref();
//            }
//
//            if (nestedRef != null) {
//                RefProperty refProperty = new RefProperty(nestedRef);
//                String componentName = refProperty.getSimpleRef();
//                Schema componentSchema = this.components.get(componentName);
//                if (componentSchema != null && !visited.contains(componentName)) {
//                    rootSchema.setName(componentName);
//                    rootSchema.setProperties(componentSchema.getProperties());
//                    rootSchema.setRequired(componentSchema.getRequired());
//
//                    // Mark schema reference as visited to avoid cycles
//                    visited.add(componentName);
//                    // Call this same function on the discovered component schema
//                    sanitiseSchemaRecursively(componentSchema, visited);
//                }
//            }
//        }
//    }
//
//}
