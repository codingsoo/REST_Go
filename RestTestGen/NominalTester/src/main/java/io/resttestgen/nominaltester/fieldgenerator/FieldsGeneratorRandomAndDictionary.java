package io.resttestgen.nominaltester.fieldgenerator;

import io.resttestgen.nominaltester.fieldgenerator.manufacturetraces.ManufactureTraces;
import io.resttestgen.nominaltester.models.ResponseDictionary;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class extends FieldsGenerator to randomly choose if use, for each field, an existing field's value
 * in the dictionary, or create a new one using the random generator.
 */
public class FieldsGeneratorRandomAndDictionary extends FieldsGenerator {

    static final Logger logger = LogManager.getLogger(FieldsGeneratorRandomAndDictionary.class);

    private FieldsGeneratorDictionary fieldsGeneratorDictionary;

    public FieldsGeneratorRandomAndDictionary(OpenAPI openAPI, ResponseDictionary responseDictionary) {
        super(openAPI);
        fieldsGeneratorDictionary = new FieldsGeneratorDictionary(openAPI, responseDictionary);
    }

    public FieldsGeneratorRandomAndDictionary(OpenAPI openAPI, ResponseDictionary responseDictionary, int seed) {
        super(openAPI, seed);
        fieldsGeneratorDictionary = new FieldsGeneratorDictionary(openAPI, responseDictionary);
    }

    @Override
    protected <T> T createValueFromSchema(Class<T> pojoClass, Schema schema, ManufactureTraces manufactureTraces, String propertyFieldName){
        int generationChoice = randomGenerator.getRandomInteger(1, 6); // 1..5
        if (generationChoice > 1) {  // 1/5 -> will create from schema
            return super.createValueFromSchema(pojoClass, schema, manufactureTraces, propertyFieldName);
        } else {
            // 4/5 -> will get from dictionary
            return fieldsGeneratorDictionary.createValueFromSchema(pojoClass, schema, manufactureTraces, propertyFieldName);
        }
    }
}
