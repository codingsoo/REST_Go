package io.resttestgen.nominaltester.helper;

import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import io.resttestgen.nominaltester.helper.exceptions.ClassLoaderNotInitializedException;
import io.resttestgen.nominaltester.helper.exceptions.CodegenParserException;
import io.resttestgen.nominaltester.models.HTTPMethod;
import io.resttestgen.nominaltester.models.OperationInfo;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.generators.java.JavaClientCodegen;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Helper class for extract Operation for swagger codegen classes and matching the corresponding swagger schema
 */
public class SwaggerCodegenHelper {

    static final Logger logger = LogManager.getLogger(SwaggerCodegenHelper.class);

    private static Map<String, CodegenOperation> getCodegenOperations(OpenAPI openAPI) throws NoSuchFieldException, IllegalAccessException {
        DefaultGenerator defaultGenerator = new DefaultGenerator();
        Field openApiField = DefaultGenerator.class.getDeclaredField("openAPI");
        openApiField.setAccessible(true);
        openApiField.set(defaultGenerator, openAPI);

        Field config = DefaultGenerator.class.getDeclaredField("config");
        config.setAccessible(true);
        config.set(defaultGenerator, new JavaClientCodegen());

        Map<String, List<CodegenOperation>> mapClassCodeGenOperations = defaultGenerator.processPaths(openAPI.getPaths());
        List<CodegenOperation> codegenOperations = mapClassCodeGenOperations.values().
                stream().flatMap(Collection::stream).collect(Collectors.toList());

        Map<String, CodegenOperation> mapIdCodegenOperation = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (CodegenOperation codegenOperation : codegenOperations) {
            mapIdCodegenOperation.put(codegenOperation.getOperationId(), codegenOperation);
        }
        return mapIdCodegenOperation;
    }

    /**
     * Given a OpenAPI pojo class, extract all the operations linking the corresponding swagger-code gen class
     * NOTE. swagger code-gen classes must be imported in the classpath io.swagger.client.api
     *
     * @param openAPI Swagger POJO class
     * @return Map<className, list of rest-gen operations>
     */
    public static Map<String, List<OperationInfo>> getOperationsFromSwagger(OpenAPI openAPI) throws CodegenParserException, ClassLoaderNotInitializedException {

        Map<String, CodegenOperation> swaggerCodegenOperations;

        // Get Information of codegen operations
        try {
            swaggerCodegenOperations = getCodegenOperations(openAPI);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new CodegenParserException("Cannot extract codegen operations", e);
        }

        // Get codegen methods
        Map<String, Method> swaggerCodegenMethods = getSwaggerCodegenMethods();
        Map<String, List<OperationInfo>> invocationClassOperationMap = new HashMap<>();

        for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {

            List<OperationInfo> operations = createRestGenOperations(entry.getKey(), entry.getValue(),
                    swaggerCodegenMethods, swaggerCodegenOperations);

            for (OperationInfo operation : operations) {
                String key = operation.getInvocationClassName();
                if (invocationClassOperationMap.containsKey(key)) {
                    invocationClassOperationMap.get(key).add(operation);
                }
                else {
                    ArrayList<OperationInfo> operationInfos = new ArrayList<>();
                    operationInfos.add(operation);
                    invocationClassOperationMap.put(key, operationInfos);
                }
            }
        }

        return invocationClassOperationMap;
    }

    /**
     * Use the Guava class ClassPath to read all the methods inside the  swagger generated classes
     * from the classpath io.swagger.client.api.
     *
     * @return Map<operationId, Method>
     */
    private static Map<String, Method> getSwaggerCodegenMethods() throws ClassLoaderNotInitializedException {
        Map<String, Method> nameMethodMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        URLClassLoader urlClassLoader = SessionClassLoader.getInstance().getUrlClassLoader();
        ClassPath classpath; // scans the class path used by classloader
        try {
            classpath = ClassPath.from(urlClassLoader);
            for (ClassPath.ClassInfo classInfo : classpath.getTopLevelClasses("io.swagger.client.api")) {
                List<Method> methods = ReflectionHelper.getMethodsThatContain(classInfo.load(), "WithHttpInfo");
                for (Method method : methods) {
                    logger.debug("Found Operation: " + classInfo + ":" + method.getName());
                    String simplifiedName = method.getName().replace("WithHttpInfo", "");
                    if (!nameMethodMap.containsKey(simplifiedName)) {
                        nameMethodMap.put(simplifiedName, method);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nameMethodMap;
    }

    /**
     * From an object of type PathItem, extract all the operations and covert them
     * in a list of OperationInfo objects.
     *
     * @param path path of the end-point
     * @param pathItem Swagger POJO class PathItem (class representing an endpoint)
     * @param swaggerCodegenMethods Map<operationId, method> from swagger code-gen
     * @param swaggerCodegenOperations
     * @return List of RestGenOperations for the endpoint PathItem
     */
    private static List<OperationInfo> createRestGenOperations(String path, PathItem pathItem, Map<String, Method> swaggerCodegenMethods, Map<String, CodegenOperation> swaggerCodegenOperations) {
        List<OperationInfo> operations = new ArrayList<>();

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
            OperationInfo operationInfo = new OperationInfo(operation, sanitizedOperationId);
            operationInfo.setOperationPath(path);
            operationInfo.setHttpMethod(httpMethod);

            CodegenOperation codegenOperation = swaggerCodegenOperations.get(sanitizedOperationId);
            operationInfo.setCodegenOperation(codegenOperation);
            operationInfo.setInvocationClassName(codegenOperation.getBaseName() + "Api");

            Method swaggerGenMethod = swaggerCodegenMethods.get(operationInfo.getOperationId());
            if (swaggerGenMethod == null) {
                logger.warn("No swagger code-gen method found for operation: " + operationInfo.getOperationId());
            }

            operationInfo.setInvocationMethod(swaggerGenMethod);
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
