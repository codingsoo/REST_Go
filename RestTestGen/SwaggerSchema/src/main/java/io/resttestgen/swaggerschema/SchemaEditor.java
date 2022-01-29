package io.resttestgen.swaggerschema;

import com.google.common.base.CaseFormat;
import io.swagger.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SchemaEditor class maps the swagger file in a POJO object and
 * comprises functions to easily modify the properties of the schema
 * and, in particular, to determine the default behaviour of the
 * validator in case of:
 * - empty string
 * - additional properties
 */
public class SchemaEditor {

    private OpenAPI openAPI;
    private Set<String> entities;

    public SchemaEditor(OpenAPI openAPI) {
        this.openAPI = openAPI;
        this.entities = extractEntities();
    }

    /**
     * @return POJO swagger file
     */
    public OpenAPI getOpenAPI() {
        return openAPI;
    }


    /**
     * @return string representing swagger file as a JSON
     */
    public static String toJSONSchema(OpenAPI openAPI) {
        return Json.pretty(openAPI);
    }


    /**
     * Tries to infer from the swagger specification file a list of entities
     *
     * How? If assume that the part of the string before an "_id" represents
     * an Entity.
     *
     * @return List of Inferred Entities
     */
    private Set<String> extractEntities() {
        String content = toJSONSchema(this.openAPI);
        Set<String> entities = new HashSet<>();

        Pattern p = Pattern.compile("([a-zA-z]+)(_id|_Id|_ID)");
        Matcher matcher = p.matcher(content);

        while (matcher.find()) {
            String entity = matcher.group(1).toLowerCase();
            if (!entity.isEmpty()) entities.add(entity);
        }

        return entities;
    }


    /**
     * Change the schema object field name if ambiguous (es. `id`)
     *
     */
    public void resolveAmbiguousIds(){
        Map<String, Schema> schemas = this.openAPI.getComponents().getSchemas();
        if (schemas != null) {
            for (Map.Entry<String, Schema> schemaEntry : schemas.entrySet()) {
                String schemaRef = schemaEntry.getKey();
                Schema schemaObj = schemaEntry.getValue();

                Map<String, Schema> parameters = schemaObj.getProperties();

                if (parameters == null) continue;

                Set<String> fields = new HashSet<>(parameters.keySet());

                for (String field : fields) {
                    if (field.equalsIgnoreCase("id")) {
                        Optional<String> matchingEntity = getMatchingEntity(schemaRef);
                        // WARNING: here I decided to put schemaRef lowercase anyway
                        // previously was "field"; so that the field remains "id".
                        String entity = matchingEntity.orElse(schemaRef.toLowerCase());
                        updateKeyString(parameters, "id", entity + "Id");
                        updateKeyString(parameters, "ID", entity + "Id");
                        updateKeyString(parameters, "Id", entity + "Id");
                        if (schemaObj.getRequired() != null) {
                            schemaObj.getRequired().remove("id");
                            schemaObj.getRequired().remove("ID");
                            schemaObj.getRequired().remove("Id");
                            schemaObj.getRequired().add(entity + "Id"); //camel-case
                        }
                    }
                }
            }
        }
    }

    /**
     * Rename a key
     *
     * @param map Source map
     * @param oldkey oldkey to rename
     * @param newKey new key
     */
    private void updateKeyString(Map<String, Schema> map, String oldkey, String newKey){
        if (map.containsKey(oldkey)) {
            Object obj = map.remove(oldkey);
            map.put(newKey, (Schema) obj);
        }
    }

    /**
     * Given the name of the schema, get the matching inferred entity
     * that is the subject of the operation,
     *
     * es. `createNewPaymentRequest` should match the entity: PaymentRequest
     *
     * In the case multiple matches (Payment, Request, PaymentRequest), the longest match will be taken.
     *
     * @param schemaRef name of the schema
     * @return Matching entity, null if there is no match
     */
    public Optional<String> getMatchingEntity(String schemaRef){
        List<String> matchingEntities = new ArrayList<>();
        String schemaRefName = schemaRef.toLowerCase();

        for (String entity : entities) {
            String entityRaw = entity.replace("_", "").toLowerCase();
            if (schemaRefName.contains(entityRaw)) matchingEntities.add(entity);
        }

        return matchingEntities.stream().max(Comparator.comparing(String::length));
    }

    public Set<String> getEntities() {
        return entities;
    }


    /**
     * Recursively set a default value to the property additionalProperties
     *
     * @param value default value of the property "additionalProperties"
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchFieldException
     */
    public void setDefaultAdditionalPropertiesToAll(boolean value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        if (this.openAPI.getComponents() != null) {
            Map<String, Schema> schemas = this.openAPI.getComponents().getSchemas();
            setFieldRecursively(schemas, "object", "additionalProperties", value);
        }
    }


    /**
     *
     * Recursively set a default value to the property minLength to all the
     * schemes with type 'string'.
     *
     * @param value default value of the property "minLength"
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchFieldException
     */
    public void setDefaultStringMinLength(int value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        if (this.openAPI.getComponents() != null) {
            Map<String, Schema> schemas = this.openAPI.getComponents().getSchemas();
            setFieldRecursively(schemas, "string", "minLength", value);
        }
    }


    /**
     * Helper function to call recursively a setter method for all those nodes with a matching node-type
     * and without an already set value for the target property to set.
     *
     * @param schemas map<SchemaName, Schema> identifying the starting point (roots) of the recursive search algorithm
     * @param targetType string representing the target schemas es. 'string' to target all the nodes of type string
     * @param methodName string representing the setter name of the target property es. 'minLength', 'additionalProperties'
     * @param param param of the setter method
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    public void setFieldRecursively(Map<String, Schema> schemas, String targetType, String methodName, Object param) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (schemas == null) return;
        for (Map.Entry<String, Schema> entry : schemas.entrySet()) {

            if (entry == null || entry.getValue() == null || entry.getValue().getType() == null) continue;

            String[] words = ("get_" + methodName).split("(?=[A-Z])");
            String getterMethodName =
                    CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, String.join("_", words));

            Object actualValue = entry.getValue().getClass().getMethod(getterMethodName).invoke(entry.getValue());

            if (entry.getValue().getType().equals(targetType) && actualValue == null) {
                Method[] methods = entry.getValue().getClass().getMethods();
                Method targetSetterMethod = Arrays.stream(methods)
                        .filter(method -> method.getName().equals(methodName))
                        .findFirst()
                        .get();
                targetSetterMethod.invoke(entry.getValue(), param);
            }

            Map<String, Schema> properties = entry.getValue().getProperties();
            if (properties != null && properties.size() > 0) {
                setFieldRecursively(properties,  targetType, methodName, param);
            }
        }
    }

    /**
     * Adds "http:" to the servers' URLs if not already specified and removes ending /
     */
    public void fixBasePaths() {
        if (this.openAPI.getServers() != null) {
            for (Server server : this.openAPI.getServers()) {
                if (server.getUrl().startsWith("//")) {
                    server.setUrl("http:" + server.getUrl());
                }
                if (server.getUrl().endsWith("/")) {
                    server.setUrl(server.getUrl().substring(0, server.getUrl().length() - 1));
                }
                System.out.println(server);
            }
        }
    }

}
