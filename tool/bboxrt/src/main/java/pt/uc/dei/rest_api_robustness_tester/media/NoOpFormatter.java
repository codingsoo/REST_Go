package pt.uc.dei.rest_api_robustness_tester.media;

import pt.uc.dei.rest_api_robustness_tester.schema.ArraySchema;
import pt.uc.dei.rest_api_robustness_tester.schema.Schema;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;

import java.util.StringJoiner;

public class NoOpFormatter implements SchemaFormatter
{
    @Override
    public String MediaType()
    {
        return FormatterManager.MediaType.WILDCARD.Value();
    }
    
    @Override
    public Class<? extends Schema> SchemaType()
    {
        return Schema.class;
    }
    
    @Override
    public String Serialize(Schema schema)
    {
        //FIXME: this should definitely be implemented in better way - VERY BAD PRACTICE
        if(schema.type.equals(TypeManager.Type.Array.Value()))
        {
            ArraySchema<? extends Schema> arr = (ArraySchema<? extends Schema>)schema;
            StringJoiner strJoiner = new StringJoiner(",");
            for(Schema s : arr.value)
            {
                if(s.type.equals(TypeManager.Type.String.Value()))
                    strJoiner.add("\"" + s.value + "\"");
                else
                    strJoiner.add(s.value.toString());
            }
            return "[" + strJoiner.toString() + "]";
        }
        return schema.value.toString();
    }
    
    @Override
    public String GetElementValue(String value, String... nameHierarchy) throws Exception
    {
        return value;
    }
    
    @Override
    public String SetElementValue(String value, String newValue, String... nameHierarchy) throws Exception
    {
        return newValue;
    }
}
