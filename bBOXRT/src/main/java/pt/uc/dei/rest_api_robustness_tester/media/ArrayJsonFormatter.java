package pt.uc.dei.rest_api_robustness_tester.media;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pt.uc.dei.rest_api_robustness_tester.schema.ArraySchema;
import pt.uc.dei.rest_api_robustness_tester.schema.Schema;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ArrayJsonFormatter<T extends Schema> implements SchemaFormatter<ArraySchema<T>>
{
    @Override
    public String MediaType()
    {
        return FormatterManager.MediaType.JSON.Value();
    }
    
    @Override
    public Class<? extends Schema> SchemaType()
    {
        return ArraySchema.class;
    }
    
    @Override
    public String Serialize(ArraySchema<T> schema)
    {
        List<String> formattedList = new ArrayList<>();
        for(T t : schema.value)
        {
            Class<? extends Schema> c;
            if(TypeManager.Instance().HasFormat(t.format))
                c = TypeManager.Instance().GetFormat(t.format);
            else
                c = TypeManager.Instance().GetType(t.type);
    
            SchemaFormatter f = FormatterManager.Instance().GetFormatter(MediaType(), c);
            formattedList.add(f.Serialize(t));
        }
        StringJoiner joiner = new StringJoiner(",");
        formattedList.forEach(joiner::add);
        return "[" + joiner.toString() + "]";
    }
    
    @Override
    public String GetElementValue(String value, String... nameHierarchy) throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(value);
    
        for(String name : nameHierarchy)
            json = json.path(name);
    
        return json.toString();
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
            ((ObjectNode)json).put(nameHierarchy[nameHierarchy.length - 1], newValue);
    
        return root.toString();
    }
}
