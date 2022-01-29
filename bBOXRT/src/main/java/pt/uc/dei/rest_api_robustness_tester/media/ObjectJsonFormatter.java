package pt.uc.dei.rest_api_robustness_tester.media;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pt.uc.dei.rest_api_robustness_tester.schema.ObjectSchema;
import pt.uc.dei.rest_api_robustness_tester.schema.Schema;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;

import java.util.*;

public class ObjectJsonFormatter<K extends String, V extends Schema> implements SchemaFormatter<ObjectSchema<K, V>>
{
    @Override
    public String MediaType()
    {
        return FormatterManager.MediaType.JSON.Value();
    }
    
    @Override
    public Class<? extends Schema> SchemaType()
    {
        return ObjectSchema.class;
    }
    
    @Override
    public String Serialize(ObjectSchema<K, V> schema)
    {
        Map<String, String> formattedMap = new LinkedHashMap<>();
        for(K k : schema.value.keySet())
        {
            V v = schema.value.get(k);
            Class<? extends Schema> c;
            if(TypeManager.Instance().HasFormat(v.format))
                c = TypeManager.Instance().GetFormat(v.format);
            else
                c = TypeManager.Instance().GetType(v.type);
            
            SchemaFormatter f = FormatterManager.Instance().GetFormatter(MediaType(), c);
            formattedMap.put(k, f.Serialize(v));
        }
        List<String> elemList = new ArrayList<>();
        formattedMap.forEach((k, v) -> {elemList.add('"' + k + '"' + ":" + v);});
        StringJoiner joiner = new StringJoiner(",");
        elemList.forEach(joiner::add);
        return "{" + joiner.toString() + "}";
    }
    
    @Override
    public String GetElementValue(String value, String... nameHierarchy) throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(value);
    
        for(String name : nameHierarchy)
            json = json.path(name);
    
        String jsonStr = json.toString();
        if(jsonStr.startsWith("\"") && jsonStr.endsWith("\""))
            jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
        return jsonStr;
    }
    
    @Override
    public String SetElementValue(String value, String newValue, String... nameHierarchy) throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(value);
    
        JsonNode json = root;
        if(nameHierarchy.length > 1)
            for(int i = 0; i < nameHierarchy.length - 1; i++)
                json = json.path(nameHierarchy[i]);
    
        //FIXME: replace with null
        if(newValue.equals("ReplaceWithNull"))
        {
            ((ObjectNode) json).remove(nameHierarchy[nameHierarchy.length - 1]);
            return root.toString();
        }
        
        if(json.path(nameHierarchy[nameHierarchy.length - 1]).isArray())
        {
            JsonNode arrayNode = mapper.readTree(newValue);
            ((ObjectNode)json).replace(nameHierarchy[nameHierarchy.length - 1], arrayNode);
        }
        else
            ((ObjectNode) json).put(nameHierarchy[nameHierarchy.length - 1], newValue);
        
        return root.toString();
    }
}

