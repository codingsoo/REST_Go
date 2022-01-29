package io.resttestgen.nominaltester.fieldgenerator;

import io.resttestgen.nominaltester.fieldgenerator.manufacturetraces.GsonTrace;
import io.resttestgen.nominaltester.fieldgenerator.manufacturetraces.ManufactureTrace;
import io.resttestgen.nominaltester.fieldgenerator.manufacturetraces.ManufactureTraces;
import io.resttestgen.nominaltester.fieldgenerator.manufacturetraces.PrimitiveObjectTrace;
import io.resttestgen.nominaltester.helper.ReflectionHelper;
import io.resttestgen.nominaltester.models.ResponseDictionary;
import io.resttestgen.nominaltester.models.exceptions.ParametersMismatchException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.ClassUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * This class extends FieldsGenerator to randomly choose if use, for each field, an existing field's value
 * in the dictionary, or create a new one using the random generator.
 */
public class FieldsGeneratorDictionary extends FieldsGenerator {

    static final Logger logger = LogManager.getLogger(FieldsGeneratorDictionary.class);

    private ResponseDictionary responseDictionary;

    public FieldsGeneratorDictionary(OpenAPI openAPI, ResponseDictionary responseDictionary) {
        super(openAPI);
        this.responseDictionary = responseDictionary;
    }

    public FieldsGeneratorDictionary(OpenAPI openAPI, ResponseDictionary responseDictionary, int seed) {
        super(openAPI, seed);
        this.responseDictionary = responseDictionary;
    }

    /*
        @Override
        public <T> T manufacturePojo(Class<T> pojoClass, Schema schema, ManufactureTraces manufactureTraces, String fieldname) throws FieldGenerationException, TypeNotHandledException {
            if (fieldname != null && responseDictionary.containsField(fieldname)) {
                try {
                    return tryToGetObjectFromDictionary(pojoClass, schema, manufactureTraces, fieldname);
                } catch (ParametersMismatchException e) {
                    return factory(pojoClass, schema, manufactureTraces, fieldname);
                }
            }
            return factory(pojoClass, schema, manufactureTraces, fieldname);
        }
     */

    @Override
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
                // Try to get from Dictionary
                try {
                    T object = tryToGetObjectFromDictionary(pojoClass, schema, manufactureTraces, propertyFieldName);
                    if (object != null) {
                        addToStackTrace(pojoClass, manufactureTraces, object);
                        return object;
                    }
                } catch (ParametersMismatchException e) {
                    logger.debug("ParametersMismatchException: " + e.getMessage());
                }
                return super.createValueFromSchema(pojoClass, schema, manufactureTraces, propertyFieldName);
        }
    }

    private <T> void addToStackTrace(Class<T> pojoClass, ManufactureTraces manufactureTraces, T value) {
        boolean isPrimitiveOrWrapped =
                ClassUtils.isPrimitiveOrWrapper(pojoClass);
        ManufactureTrace manufactureTrace = (isPrimitiveOrWrapped || pojoClass.equals(String.class)) ?
                new PrimitiveObjectTrace(pojoClass, value) :
                new GsonTrace(value, pojoClass);
        manufactureTraces.addTrace(manufactureTrace);
    }

    /**
     * Lookup into the dictionary searching for a the closest field-name match.
     * If there is match, get a random parameter among the one available
     * Otherwise return null
     * @param propertyFieldName property name
     * @param schema property schema
     * @param pojoClass property object type
     * @param manufactureTraces traces to replicate the construction
     * @return object from the dictionary
     */
    private <T> T tryToGetObjectFromDictionary(Class<T> pojoClass, Schema schema, ManufactureTraces manufactureTraces, String propertyFieldName) throws ParametersMismatchException {
        String firstMatchingField = responseDictionary.getFirstMatchingField(propertyFieldName);

        // No matching field, return null
        if (firstMatchingField == null) return null;

        // There is a matching field, get one or more objects from the ones available
        List<Object> objectsByField = responseDictionary.getObjectsByField(firstMatchingField);

        T value;
        if ("array".equals(schema.getType())) {
            List<Object> subset = new ArrayList<>();
            int size = randomGenerator.getRandomInteger(Math.min(1, objectsByField.size()), objectsByField.size());
            for (int i = 0; i < size; i++) {
                // get random element among the ones available
                Object randomElementFromCollection = randomGenerator.getRandomElementFromCollection(objectsByField);
                Object castedElement = ReflectionHelper.tryToCastInstance(pojoClass, randomElementFromCollection);
                if (castedElement == null) continue;
                subset.add(castedElement);
            }
            value = (T) subset;
        } else {
            Object randomElementFromCollection = randomGenerator.getRandomElementFromCollection(objectsByField);
            value = (T)ReflectionHelper.tryToCastInstance(pojoClass, randomElementFromCollection);
            if (value == null) {
                String msg = "Cannot convert " + randomElementFromCollection.getClass().getName() + " to " + pojoClass.getName();
                throw new ParametersMismatchException(msg);
            }
        }
        return value;
    }

}
