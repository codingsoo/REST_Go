package io.resttestgen.nominaltester.models;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import io.resttestgen.nominaltester.fieldgenerator.RandomGenerator;
import io.resttestgen.nominaltester.helper.ReflectionHelper;
import opennlp.tools.stemmer.PorterStemmer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * ResponseDictionary manages a map String -> List< Object >
 * containing, for each fieldname, a list of values from operation's execution responses.
 */
public class ResponseDictionary {

    static final Logger logger = LogManager.getLogger(ResponseDictionary.class);


    private Map<String, List<Object>> dictionary;
    private RandomGenerator randomGenerator;
    public ResponseDictionary() {
        dictionary = new HashMap<>();
        randomGenerator = new RandomGenerator();
    }

    public Map<String, List<Object>> getDictionary() {
        return dictionary;
    }

    /**
     * Import content of JSON file into the dictionary
     * @param jsonPath path to json file
     */
    public void addFromJSONFile(String jsonPath) {
        try {
            String content = new String ( Files.readAllBytes( Paths.get(jsonPath) ) );
            addJSONFieldsToDictionary(content);
        } catch (IOException e) {
            logger.warn("Cannot read the file " + jsonPath);
        }
    }

    /**
     * Add each key value pair into the dictionary
     * It will just map primitive values, but traversing all the JSON tree
     * @param jsonContent JSON as a string
     */
    public void addJSONFieldsToDictionary(String jsonContent) {
        Gson gson = new Gson();
        try {
            Object root = gson.fromJson(jsonContent, Object.class);
            addPrimitiveFieldsToDictionary("", root);
        } catch (JsonParseException e ) {
            logger.debug("Cannot parse response JSON during dictionary insertion");
        }
    }

    private void addPrimitiveFieldsToDictionary(String parentKey, Object root) {
        try {
            if (root instanceof Map) {
                addMapPrimitiveFieldsToDictionary(parentKey, (Map)root);
            } else if (root instanceof List) {
                List rootList = (List) root;
                if (rootList.size() > 0) {
                    boolean areItemsPrimitive = ReflectionHelper.isPrimitiveOrWrapped(rootList.get(0));
                    if (areItemsPrimitive && !parentKey.isEmpty()) {
                        rootList.forEach(item -> addFieldToDictionary(parentKey, item));
                    } else if (!areItemsPrimitive) {
                        rootList.forEach(item -> addMapPrimitiveFieldsToDictionary(parentKey, (Map) item));
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Unable to add " + parentKey + " to Dictionary due to error: " + e.getMessage());
        }
    }

    /**
     * Goes through nested deserialized data-structure create with JSON
     * and adds to the dictionary all the key-object pairs where value is a primitive
     * @param root JSON deserialized object
    */
    private void addMapPrimitiveFieldsToDictionary(String parentKey, Map<String, Object> root) {
        Set<Map.Entry<String, Object>> entries = root.entrySet();
        for (Map.Entry<String, Object> entry : entries ) {
            Object value = entry.getValue();
            if (ReflectionHelper.isPrimitiveOrWrapped(value)) {
                String key = entry.getKey();
                if (!parentKey.isEmpty()) {
                    key = parentKey + ":" + key;
                }
                addFieldToDictionary(key, entry.getValue());
            } else {
                addPrimitiveFieldsToDictionary(entry.getKey(), value);
            }
        }
    }

    /**
     * Adds a new element to the dictionary
     * @param field key of the element, will create a new one if does not exist
     * @param object new object to be added
     */
    private void addFieldToDictionary(String field, Object object) {
        if (object != null) {
            if (object instanceof String && ((String) object).isEmpty()) return;
            if (object instanceof Collection<?> && ((Collection) object).isEmpty()) return;
            List<Object> dictionaryItems = dictionary.getOrDefault(field, new ArrayList<>());
            if (dictionaryItems.contains(object)) return;
            if (object instanceof Collection<?>) {
                dictionaryItems.addAll((Collection<?>) object);
            } else {
                dictionaryItems.add(object);
            }
            this.dictionary.put(field, dictionaryItems);
        }
    }

    public List<Object> getObjectsByField(String field) {
        return dictionary.getOrDefault(field, new ArrayList<>());
    }

    /**
     * Check if a field can be matched with some fields in the dictionary
     * 1) check for exact match
     * 2) check for object-parameter-notation matches
     * 3) check for inferred fields
     * @param field field to look at inside the dictionary
     * @return true if the target field can be matched, false otherwise
     */
    public boolean containsField(String field) {
        String matchingField = getFirstMatchingField(field);
        if (matchingField == null) return false;
        return dictionary.get(matchingField).size() > 0;
    }

    /**
     * Get the first matching field (key) inside the dictionary according to the following rules:
     * 1) looks for key with exact match (e.g "petId")
     * 2) looks for key with exact match with the field object-parameter-notation (e.g pet:id)
     * 3) s
     * @param field target field to search inside the dictionary
     * @return String representing first matching key
     */
    public String getFirstMatchingField(String field) {
        boolean containsField = dictionary.containsKey(field);
        if (containsField) return field;

        // try object notation
        String objectNotationField = convertToObjectParameterNotation(field);
        boolean containsObjectNotationField = dictionary.containsKey(objectNotationField);
        if (containsObjectNotationField) return objectNotationField;

        // search closest match for the field
        String closestMatch = searchFirstMatchingKeyWithDistance(field, 1);
        if (closestMatch != null) return closestMatch;

        // seatch for closest match for the field in object notation
        closestMatch = searchFirstMatchingKeyWithDistance(objectNotationField, 1);
        if (closestMatch != null) return closestMatch;

        // try last part of object notation
        String[] objectNotationPart = objectNotationField.split(":"); // pet:id
        String lastNotationPart = objectNotationPart[objectNotationPart.length - 1]; // "id"
        boolean containsLastPartObjectNotation = dictionary.containsKey(lastNotationPart); // checks for "id"
        if (containsLastPartObjectNotation) return lastNotationPart;

        // check if fieldname is contained in some keys
        String keysContainingFieldName = getKeysContaining(field);
        boolean containsKeyContainingFieldName = keysContainingFieldName != null;
        if (containsKeyContainingFieldName) return keysContainingFieldName;

        // check if lastNotationPart is contained in some keys
        // TODO: remove
        String keysContainingLastObjectNotationPart = getKeysContaining(lastNotationPart);
        boolean containsKeyContainingLastPartObjectNotation = keysContainingLastObjectNotationPart != null;
        if (containsKeyContainingLastPartObjectNotation) return keysContainingLastObjectNotationPart;

        // last chance, try using stemming
        // TODO: remove
        PorterStemmer porterStemmer = new PorterStemmer();
        String stemmedKey = porterStemmer.stem(field); // tags -> tag or tag:names -> tag:nam
        boolean containsStemmedKey = dictionary.containsKey(stemmedKey);
        if (containsStemmedKey) return stemmedKey;

        return null;
    }

    /**
     Uses the LevenshteinDistance to find a key with distance *targetDistance* from *field*
     * @param field target fields
     * @param targetDistance maximum allowed distance
     * @return return the first key with a distance <= of target distance
     */
    private String searchFirstMatchingKeyWithDistance(String field, int targetDistance) {
        // Try looking at the key with the minimum levenshtein distance
        Set<String> keys = dictionary.keySet();
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        for (String key : keys) {
            Integer distance = levenshteinDistance.apply(key, field);
            if (distance <= targetDistance) {
                return key;
            }
        }
        return null;
    }

    /**
     * Converts a string to object parameter notation
     * Camel case or '_' are converted to ':'
     * do nothing if input string contains already ":"
     * @param parameter String to convert to object parameter notation
     * @return string in object parameter notation
     */
    private String convertToObjectParameterNotation(String parameter) {
        if (parameter.contains(":")) return parameter;
        parameter = StringUtils.join(
                StringUtils.splitByCharacterTypeCamelCase(parameter),
                ':'
        );
        parameter = parameter.replace(":_:", ":");
        return parameter.toLowerCase();
    }

    /**
     * Iterate over dictionary keys, getting random keys containing parameter string
     * @param innerKeys string to be contained inside keys
     * @return random key containing string innerKeys
     */
    private String getKeysContaining(String innerKeys) {
        Set<String> keys = dictionary.keySet();
        List<String> matchingKeys = new ArrayList<>();
        for (String key : keys) {
            if (key.contains(innerKeys)) {
                matchingKeys.add(key);
            }
        }
        if (matchingKeys.size() == 0) return null;
        return randomGenerator.getRandomElementFromCollection(matchingKeys);
    }
}
