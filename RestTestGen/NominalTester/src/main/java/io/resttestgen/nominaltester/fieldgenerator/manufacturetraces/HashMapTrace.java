package io.resttestgen.nominaltester.fieldgenerator.manufacturetraces;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HashMapTrace extends ManufactureTrace {
    public HashMapTrace(HashMap<String, Object> value) {
        this.value = value;
    }

    private final HashMap<String, Object> value;

    @Override
    public String constructionString() {
        String hashmapName = getObjectName(this.value);
        String repr = String.format("HashMap<String, Object> %s = new HashMap<>();", hashmapName);
        Set<Map.Entry<String, Object>> entries = this.value.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            String entryVariableName = getObjectName(entry.getValue());
            repr += String.format("\n\t%s.put(%s, %s);", hashmapName, entry.getKey(), entryVariableName);
        }
        return repr;
    }
}
