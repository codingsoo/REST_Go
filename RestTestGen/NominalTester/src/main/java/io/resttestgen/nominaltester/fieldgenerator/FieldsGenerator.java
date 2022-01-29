package io.resttestgen.nominaltester.fieldgenerator;

import com.google.common.base.CaseFormat;
import io.resttestgen.nominaltester.fieldgenerator.exceptions.FieldGenerationException;
import io.resttestgen.nominaltester.fieldgenerator.exceptions.TypeNotHandledException;
import io.resttestgen.nominaltester.fieldgenerator.manufacturetraces.*;
import io.resttestgen.nominaltester.helper.ReflectionHelper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.FileSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.ClassUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetDateTime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This class should create, given a class with constraints defined in json schema
 * The corresponding Java Objects.
 *
 * The fields inside the Java Objects should be generated using some random-fieldgenerator
 */
public class FieldsGenerator {

    OpenAPI openAPI;
    SwaggerRandomGenerator randomGenerator;

    public FieldsGenerator(OpenAPI openAPI) {
        this.openAPI = openAPI;
        this.randomGenerator = new SwaggerRandomGenerator();
    }

    public FieldsGenerator(OpenAPI openAPI, int seed) {
        this.openAPI = openAPI;
        this.randomGenerator = new SwaggerRandomGenerator(seed);
    }

    public <T> T manufacturePojo(Class<T> pojoClass, Schema schema, ManufactureTraces manufactureTraces) throws FieldGenerationException, TypeNotHandledException {
        return factory(pojoClass, schema, manufactureTraces, null);
    }

    public <T> T manufacturePojo(Class<T> pojoClass, Schema schema, ManufactureTraces manufactureTraces, String propertyFieldName) throws TypeNotHandledException, FieldGenerationException {
        return factory(pojoClass, schema, manufactureTraces, propertyFieldName);
    }

    /**
     * Method to create a new object pojoClass, with all the fields initialized
     *
     * @param pojoClass reference of the class of the object to create; in case of an array, it is the item's class
     * @param schema swagger schema of the object to create; should be an arrayschema in case of array
     * @param <T> template parameter for the object to create
     * @return returns an object of class pojoClass, with all the field initialized
     * @throws Exception
     */
    public <T> T factory(Class<T> pojoClass, Schema schema, ManufactureTraces manufactureTraces, String propertyFieldName) throws FieldGenerationException, TypeNotHandledException {

        // If Schema is null, try to infer schema from class
        // If the schema is null the generation cannot continue
        if (schema == null){
            schema = getSchemaFromClass(pojoClass);
            if (schema == null) throw new TypeNotHandledException(pojoClass.toString());
        }

        // if schema contains just a reference
        // follow the reference
        if (schema.getType() == null && schema.get$ref() != null) {
            schema = getSchemaFromRef(schema.get$ref());
        }

        if (schema instanceof ComposedSchema || schema instanceof FileSchema) {
            throw new FieldGenerationException("Generation of " + schema.getClass().getName() + " is not implemented");
        }

        // If schema has enum
        List enums = schema.getEnum();
        if (enums != null && enums.size() > 0) {
            return getRandomEnum(pojoClass, enums, manufactureTraces);
        }

        if (schema.getType() == null) {
            if (pojoClass.getName().startsWith("io.swagger.client.model")){
                schema.setType("object");
            } else {
                // anonymous object, create hashmap with its parameters
                return createAnonymousObjectFromProperties(schema, manufactureTraces);
            }
        }

        // Switch based on types
        switch (schema.getType()) {
            case "object":
                T instance;
                if (Object.class.equals(pojoClass)) {
                    // There is no specific class for this object
                    // Handle it using HashMap of this properties
                    instance = createAnonymousObjectFromProperties(schema, manufactureTraces);
                } else {
                    instance = createObjectFromClass(pojoClass, schema, manufactureTraces);
                }
                return instance;
            case "array":
                int minLength = schema.getMinLength() != null ? schema.getMinLength() : 0 ;
                int maxLength = schema.getMaxLength() != null ? schema.getMaxLength() : minLength + 10;
                int randomSize = randomGenerator.getRandomInteger(minLength, maxLength);
                Schema itemSchema = ((ArraySchema)schema).getItems();
                String itemName = itemSchema.getName();
                if (itemName == null) itemName = propertyFieldName;
                List<Object> value = new ArrayList<>();
                for (int i = 0; i < randomSize; i++) {
                    Object arrayElement = manufacturePojo(pojoClass, itemSchema, manufactureTraces, itemName);
                    if (arrayElement != null) value.add(arrayElement);
                }
                ArrayTrace arrayTrace = new ArrayTrace(randomSize, Object.class, value);
                manufactureTraces.addTrace(arrayTrace);
                return (T) value;
            default:
                // It is a primitive
                T basicObjectValue = createValueFromSchema(pojoClass, schema, manufactureTraces, propertyFieldName);
                return basicObjectValue;
        }
    }

    /**
     * Generate a new primitive value based on property field name and its schema
     * @param propertyFieldName property name
     * @param schema property schema
     * @param pojoClass property object type
     * @param manufactureTraces traces to replicate the construction
     * @return new property value
     */
    protected <T> T createValueFromSchema(Class<T> pojoClass, Schema schema, ManufactureTraces manufactureTraces, String propertyFieldName){
        Object defaultValue = schema.getDefault();
        Object exampleValue = schema.getExample();
        Boolean nullable = schema.getNullable();
        int choice = randomGenerator.getRandomInteger(0, 24); // Original (0, 3)
        switch (choice) {
            case 0:
                if (nullable != null && nullable) {
                    PrimitiveObjectTrace primitiveObjectTrace = new PrimitiveObjectTrace(pojoClass, null);
                    manufactureTraces.addTrace(primitiveObjectTrace);
                    return null;
                }
            case 1:
                if (defaultValue != null) {
                    PrimitiveObjectTrace primitiveObjectTrace = new PrimitiveObjectTrace(pojoClass, defaultValue);
                    manufactureTraces.addTrace(primitiveObjectTrace);
                    return (T) defaultValue;
                }
            case 2:
                if (exampleValue != null) {
                    PrimitiveObjectTrace primitiveObjectTrace = new PrimitiveObjectTrace(pojoClass, exampleValue);
                    manufactureTraces.addTrace(primitiveObjectTrace);
                    return (T) exampleValue;
                }
            default:
                T valueFromSchema =  (T) randomGenerator.createRandomObjectFromSchema(schema);
                ManufactureTrace manufactureTrace;
                if (valueFromSchema instanceof LocalDate) {
                    manufactureTrace = new LocalDateTrace(valueFromSchema);
                } else if (valueFromSchema instanceof OffsetDateTime) {
                    manufactureTrace = new OffsetDateTimeTrace(valueFromSchema, propertyFieldName);
                } else {
                    manufactureTrace = new PrimitiveObjectTrace(pojoClass, valueFromSchema);
                }
                manufactureTraces.addTrace(manufactureTrace);
                return valueFromSchema;
        }
    }

    /**
     * @param pojoClass target class for enum (e.g. Enum (PetStatus), Integer, String)
     * @param enums collection of available  values
     * @param manufactureTraces list of manufacture traces to add new one
     * @param <T> returned type
     * @return return a random enum among the one available
     * @throws FieldGenerationException error during casts of generated value to enum
     */
    protected <T> T getRandomEnum(Class<T> pojoClass, List enums, ManufactureTraces manufactureTraces) throws FieldGenerationException {
        Object value = randomGenerator.getRandomElementFromCollection(enums);
        Method fromValue = ReflectionHelper.getMethodByName( pojoClass,"fromValue");

        if (fromValue == null) {
            T enumValue = (T) value;
            if (pojoClass == null) {
                pojoClass = (Class<T>) String.class;
            }
            EnumTrace enumTrace = new EnumTrace(pojoClass, enumValue);
            manufactureTraces.addTrace(enumTrace);
            return enumValue;
        }

        try {
            T enumValue =  (T) fromValue.invoke(pojoClass, value);
            EnumTrace enumTrace = new EnumTrace(pojoClass, enumValue);
            manufactureTraces.addTrace(enumTrace);
            return enumValue;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new FieldGenerationException("Cannot create enum object", e);
        }
    }

    /**
     * Creates an hashmap representing the object described in the target schema
     * keys in the hashmap are the property names, values are generated from the schema only
     * @param schema target schema
     * @param manufactureTraces list of traces in which the function add new traces for object creation
     * @param <T> Object to be created
     * @return HashMap
     * @throws TypeNotHandledException error during the object creation
     * @throws FieldGenerationException error during object creation
     */
    protected <T> T createAnonymousObjectFromProperties(Schema schema, ManufactureTraces manufactureTraces) throws FieldGenerationException, TypeNotHandledException {
        HashMap myInstance = new HashMap<String, Object>();

        // Iterate over schema's properties
        if (schema != null && schema.getProperties() != null) {
            Set properties = schema.getProperties().keySet();
            for (Object propertyName : properties) {
                Schema propertySchema = (Schema) schema.getProperties().get(propertyName);

                // Create element only if property is a primitive
                if (propertySchema.get$ref() == null)
                {
                    Object value = manufacturePojo(Object.class, propertySchema, manufactureTraces, (String) propertyName);
                    myInstance.put(propertyName, value);
                }
            }
        }

        HashMapTrace hashMapTrace = new HashMapTrace(myInstance);
        manufactureTraces.addTrace(hashMapTrace);

        return (T) myInstance;
    }

    /**
     * Creates and fills a new instance of a given target class
     * @param pojoClass target class of the obnject to be created or array items class
     * @param schema swagger schema of the object to create
     * @param manufactureTraces list of traces in which the function add new traces for object creation
     * @param <T> type of element to be return
     * @return new object of class pojoClass or array of element of class pojoClass
     * @throws TypeNotHandledException error during the object creation
     * @throws FieldGenerationException error during object creation
     */
    protected <T> T createObjectFromClass(Class<T> pojoClass, Schema schema, ManufactureTraces manufactureTraces) throws TypeNotHandledException, FieldGenerationException {

        // Assertions
        if (pojoClass == null) {
            throw new FieldGenerationException("Target class is null");
        }
        if (ReflectionHelper.isPrimitiveOrWrapped(pojoClass)) {
            throw new FieldGenerationException(pojoClass.toString() + " is not a complex class");
        }

        // Create a new Instance (no constructor parameter needed)
        T myInstance = null;
        try {
            myInstance = pojoClass.getConstructor().newInstance();
            ConstructorTrace constructorCallTrace = new ConstructorTrace(pojoClass, new Object[]{}, myInstance);
            manufactureTraces.addTrace(constructorCallTrace);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new FieldGenerationException("Error during object instantiation " + pojoClass.getSimpleName(), e);
        }

        // Iterate over schema's properties
        // for each property: find the matching setter
        // 1. find the matching property setter
        // 2. create a new property value
        // 3. use the class setter to set the property value
        if (schema != null && schema.getProperties() != null) {
            Set propertiesNames = schema.getProperties().keySet();
            for (Object propertyName : propertiesNames) {
                Schema propertySchema = (Schema) schema.getProperties().get(propertyName);
                Method propertySetter = getPropertySetter(pojoClass, (String)propertyName);

                if (propertySetter == null) continue; // no setter found

                // Get setter parameter type
                Parameter setterParameter = propertySetter.getParameters()[0];
                Class<?> propertyParameterClass = setterParameter.getType();

                // If property is array, pass the item class instead
                if ("array".equals(propertySchema.getType())) {
                    ParameterizedType parameterizedType = (ParameterizedType)setterParameter.getParameterizedType();
                    propertyParameterClass = (Class<?>)(parameterizedType.getActualTypeArguments()[0]);
                }

                // Recursive call
                String propertyFieldName = propertySetter.getDeclaringClass().getSimpleName().toLowerCase();
                propertyFieldName += ":" + ((String) propertyName).toLowerCase();

                // Set the value to the property
                try {
                    Object value = manufacturePojo(propertyParameterClass, propertySchema, manufactureTraces, propertyFieldName);
                    Object invoke = propertySetter.invoke(myInstance, value);
                    SetterTrace setterCallTrace = new SetterTrace(myInstance, propertySetter, new Object[]{value});
                    manufactureTraces.addTrace(setterCallTrace);
                } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException | StackOverflowError e) {
                    // throw new FieldGenerationException("Cannot set the field value using setter", e);
                }
            }
        }

        return myInstance;
    }

    /**
     * Search for a swagger schema with the same name of the class
     *
     * @param propertyParameterClass Java class
     * @return matching swagger Schema, or null if not found
     */
    protected Schema getSchemaFromClass(Class<?> propertyParameterClass) {
        String className = propertyParameterClass.getSimpleName();
        Schema schema = this.openAPI.getComponents().getSchemas().get(className);
        if (schema != null) return schema;

        // Check if class is primitive, if yes, create a new schema
        // with the primitive type name as primitive field

        boolean isPrimitiveOrWrapped =
                ClassUtils.isPrimitiveOrWrapper(propertyParameterClass);

        if (isPrimitiveOrWrapped) {
            schema = new Schema();
            schema.setType(className.toLowerCase());
        }

        // Not handled, return null schema

        return schema;
    }

    /**
     * Get schema from JSON schema reference
     *
     * @param ref Reference string inside swagger schema
     * @return Matching reference schema
     */
    protected Schema getSchemaFromRef(String ref){
        String[] parts = ref.split("/");
        String name = parts[parts.length - 1];
        return this.openAPI.getComponents().getSchemas().get(name);
    }

    /**
     * Get the setter method for a given Property in the target class
     *
     * @param pojoClass class that should contain the setter method
     * @param propertyName property
     * @param <T> Class
     * @return setter method for the property
     */
    protected  <T> Method getPropertySetter(Class<T> pojoClass, String propertyName) {
        String setterName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, "set" + propertyName);
        return ReflectionHelper.getMethodByName(pojoClass, setterName);
    }
}
