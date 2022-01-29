package io.resttestgen.swaggerschema;

import com.google.common.collect.Lists;
import io.resttestgen.swaggerschema.models.HTTPMethod;
import io.resttestgen.swaggerschema.models.SwaggerOperation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SchemaExtractor contains several methods to extract information from swagger schema
 */
public class SchemaExtractor {

    private final int MAX_DEPTH;
    private OpenAPI openAPI;
    private Map<String, Schema> components;

    public SchemaExtractor(OpenAPI openAPI) {
        this(openAPI, 5);
    }

    public SchemaExtractor(OpenAPI openAPI, int maxDepth) {
        this.openAPI = openAPI;
        MAX_DEPTH = maxDepth;

        // initialize components map
        this.components = new HashMap<>();
        if (this.openAPI.getComponents() != null)
            this.components = this.openAPI.getComponents().getSchemas();
    }

    /**
     * Extract the list of input parameters from operation schema
     * @param operationSchema target operation schema
     * @return Set of operation's input parameters
     */
    public Set<String> extractInputParameters(Operation operationSchema){
        Set<String> parameters = new HashSet<>();

        // Extract path input parameters
        if (operationSchema.getParameters() != null) {
            // Path parameters
            parameters = operationSchema.getParameters().stream()
                    .map(Parameter::getName).collect(Collectors.toSet());
        }

        // Extract body input parameter
        RequestBody requestBody = operationSchema.getRequestBody();
        if (requestBody != null) {
            List<String> bodyParameters  = requestBody.getContent().entrySet().stream()
                    .flatMap(entry -> getParametersRecursively(entry.getValue().getSchema(), new HashSet<>(), 0).stream())
                    .collect(Collectors.toList());
            parameters.addAll(bodyParameters);
        }

        return parameters;
    }

    /**
     * Extract the list of output parameters
     * @param operationSchema target operation schema
     * @return Set of operation's output parameters
     */
    public Set<String> extractOutputParameters(Operation operationSchema){
        ArrayList<ApiResponse> apiResponses = new ArrayList<>(operationSchema.getResponses().values());
        return apiResponses.stream()
                .filter(p -> p.getContent() != null)
                .flatMap(p -> p.getContent().entrySet().stream())
                .flatMap(entry -> getParametersRecursively(entry.getValue().getSchema(), new HashSet<>(), 0).stream())
                .collect(Collectors.toSet());
    }

    /**
     * Given a schema, it returns the list of its parameters
     * If the flag `godeep` is true, it gets the parameters of
     * the referenced parameter schema recursively.
     *
     * @param rootSchema Schema object
     * @param visited List of strings with visited schemas (to avoid cycles)
     * @param currentCallDepth current call depth
     * @return List of parameter names
     */
    public Set<String> getParametersRecursively(Schema rootSchema, Set<String> visited, int currentCallDepth) {
        Set<String> fields = new HashSet<>();
        if (rootSchema == null) return fields;

        Map<String, Schema> properties = rootSchema.getProperties();

        // if there are properties, add the field names
        if (properties != null && properties.size() > 0) {
            fields.addAll(properties.keySet());
            if (currentCallDepth < MAX_DEPTH) {
                Collection<Schema> propertiesSchemas = properties.values();
                for (Schema propertySchema : propertiesSchemas) {
                    fields.addAll(getParametersRecursively(propertySchema, visited, currentCallDepth + 1));
                }
            }
        }

        // if there are no properties inside the schema, check if there is a referenced schema
        // this is just exploring the referenced schema and should not be increased the exploring
        // depth counter
        if (properties == null) {
            String nestedRef = rootSchema.get$ref();
            if (nestedRef == null && "array".equals(rootSchema.getType())) {
                ArraySchema arraySchema = (ArraySchema) rootSchema;
                nestedRef = arraySchema.getItems().get$ref();
            }

            if (nestedRef != null) {
                String[] schemaRefSplit = nestedRef.split("/");
                String componentName = schemaRefSplit[schemaRefSplit.length - 1];
                Schema componentSchema = this.components.get(componentName);
                componentName = componentName.toLowerCase();
                fields.add(componentName);
                if (componentSchema != null && !visited.contains(componentName)) {
                    visited.add(componentName); // mark schema reference as visited to avoid cycles
                    for (String s : getParametersRecursively(componentSchema, visited, currentCallDepth)) {
                        if (!s.contains(":")) {
                            fields.add(componentName + ":" + s);
                        } else {
                            fields.add(s);
                            fields.add(s.split(":")[0]);
                        }
                    }
                }
            }
        }

        return fields;
    }

    /**
     * Create a map (OperationId -> SwaggerOperation)
     * @return  Map linking OperationId with SwaggerOperation Object
     */
    public static HashMap<String, SwaggerOperation> getOperationsMap(OpenAPI openAPI) {
        List<SwaggerOperation> operations = new ArrayList<>();
        Paths paths = openAPI.getPaths();
        List<Map.Entry<String, PathItem>> pathsList = new ArrayList<>(paths.entrySet());
        for (Map.Entry<String, PathItem> entry : pathsList) {
            List<SwaggerOperation> pathOperations = extractOperationsFromPath(entry.getKey(), entry.getValue());
            operations.addAll(pathOperations);
        }

        HashMap<String, SwaggerOperation> operationHashMap = new HashMap<>();
        for (SwaggerOperation operation : operations) {
            String operationId = operation.getOperationId();
            operationHashMap.put(operationId, operation);
        }

        return operationHashMap;
    }

    /**
     * Returns the list of SwaggerOperations
     * @return  Map linking OperationId with SwaggerOperation Object
     */
    public static List<SwaggerOperation> getOperationsList(OpenAPI openAPI) {
        List<SwaggerOperation> operations = new ArrayList<>();
        Paths paths = openAPI.getPaths();
        List<Map.Entry<String, PathItem>> pathsList = new ArrayList<>(paths.entrySet());
        for (Map.Entry<String, PathItem> entry : pathsList) {
            List<SwaggerOperation> pathOperations = extractOperationsFromPath(entry.getKey(), entry.getValue());
            operations.addAll(pathOperations);
        }
        return operations;
    }

    /**
     * From an object of type PathItem, extract all the operations and covert them
     * in a list of OperationInfo objects.
     * @return List of RestGenOperations for the endpoint PathItem
     */
    public static List<SwaggerOperation> extractOperationsFromPath(String path, PathItem pathItem) {
        List<SwaggerOperation> operations = new ArrayList<>();

        HashMap<HTTPMethod, Operation> httpMethodOperationMap = new HashMap<>();

        httpMethodOperationMap.put(HTTPMethod.GET, pathItem.getGet());
        httpMethodOperationMap.put(HTTPMethod.DELETE, pathItem.getDelete());
        httpMethodOperationMap.put(HTTPMethod.PATCH, pathItem.getPatch());
        httpMethodOperationMap.put(HTTPMethod.POST, pathItem.getPost());
        httpMethodOperationMap.put(HTTPMethod.PUT, pathItem.getPut());
        httpMethodOperationMap.put(HTTPMethod.HEAD, pathItem.getHead());

        for (Map.Entry<HTTPMethod, Operation> httpMethodOperationEntry : httpMethodOperationMap.entrySet()) {
            Operation operation = httpMethodOperationEntry.getValue();
            if (operation == null) continue;

            HTTPMethod httpMethod = httpMethodOperationEntry.getKey();
            String sanitizedOperationId = getOrGenerateOperationId(operation, path, httpMethod.toString());
            SwaggerOperation operationInfo = new SwaggerOperation(operation, sanitizedOperationId, httpMethod);
            operationInfo.setPath(path);
            operations.add(operationInfo);
        }

        return operations;
    }

    /*
     * =====================================================================
     * FROM Swagger-code gen
     * Used to have a consistent operationId that matches the method's name
     * =====================================================================
     * */

    static boolean allowUnicodeIdentifiers = false;

    /**
     * Sanitize name (parameter, property, method, etc)
     *
     * @param name string to be sanitize
     * @return sanitized string
     */
    private static String sanitizeName(String name) {
        // NOTE: performance wise, we should have written with 2 replaceAll to replace desired
        // character with _ or empty character. Below aims to spell out different cases we've
        // encountered so far and hopefully make it easier for others to add more special
        // cases in the future.

        // better error handling when map/array type is invalid
        if (name == null) {
            return Object.class.getSimpleName();
        }

        // if the name is just '$', map it to 'value' for the time being.
        if ("$".equals(name)) {
            return "value";
        }

        // input[] => input
        name = name.replaceAll("\\[\\]", ""); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        // input[a][b] => input_a_b
        name = name.replaceAll("\\[", "_");
        name = name.replaceAll("\\]", "");

        // input(a)(b) => input_a_b
        name = name.replaceAll("\\(", "_");
        name = name.replaceAll("\\)", "");

        // input.name => input_name
        name = name.replaceAll("\\.", "_");

        // input-name => input_name
        name = name.replaceAll("-", "_");

        // input name and age => input_name_and_age
        name = name.replaceAll(" ", "_");

        // remove everything else other than word, number and _
        // $php_variable => php_variable
        if (allowUnicodeIdentifiers) { //could be converted to a single line with ?: operator
            name = Pattern.compile("\\W", Pattern.UNICODE_CHARACTER_CLASS).matcher(name).replaceAll("");
        }
        else {
            name = name.replaceAll("\\W", "");
        }

        return name;
    }

    /**
     * Get operationId from the operation object, and if it's blank, generate a new one from the given parameters.
     *
     * @param operation the operation object
     * @param path the path of the operation
     * @param httpMethod the HTTP method of the operation
     * @return the (generated) operationId
     */
    protected static String getOrGenerateOperationId(Operation operation, String path, String httpMethod) {
        httpMethod = WordUtils.capitalize(httpMethod.toLowerCase());
        String operationId = operation.getOperationId();
        if (StringUtils.isBlank(operationId)) {
            String tmpPath = path;
            tmpPath = tmpPath.replaceAll("\\{", "");
            tmpPath = tmpPath.replaceAll("\\}", "");
            String[] parts = (tmpPath + "/" + httpMethod).split("/");
            StringBuilder builder = new StringBuilder();
            if ("/".equals(tmpPath)) {
                // must be root tmpPath
                builder.append("root");
            }
            for (String part : parts) {
                if (part.length() > 0) {
                    if (builder.toString().length() == 0) {
                        part = Character.toLowerCase(part.charAt(0)) + part.substring(1);
                    } else {
                        part = StringUtils.capitalize(part);
                    }
                    builder.append(part);
                }
            }
            operationId = sanitizeName(builder.toString());
        }
        return removeNonNameElementToCamelCase(operationId, "[-_:;# {}]");
    }

    /**
     * Remove characters that is not good to be included in method name from the input and camelize it
     *
     * @param name string to be camelize
     * @param nonNameElementPattern a regex pattern of the characters that is not good to be included in name
     * @return camelized string
     */
    private static String removeNonNameElementToCamelCase(final String name, final String nonNameElementPattern) {
        String result = StringUtils.join(Lists.newArrayList(name.split(nonNameElementPattern)).stream().map(input -> StringUtils.capitalize(input)).collect(Collectors.toList()), "");
        if (result.length() > 0) {
            result = result.substring(0, 1).toLowerCase() + result.substring(1);
        }
        return result.replaceAll("\\.", "");
    }
}
