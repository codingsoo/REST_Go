package pt.uc.dei.rest_api_robustness_tester.media;

import pt.uc.dei.rest_api_robustness_tester.schema.StringSchema;

public class StringJsonFormatter extends GenericPrimitiveFormatter<StringSchema>
{
    public StringJsonFormatter()
    {
        super(FormatterManager.MediaType.JSON.Value(), StringSchema.class);
    }
    
    @Override
    public String Serialize(StringSchema schema)
    {
        return '"' + schema.value + '"';
    }
    
    @Override
    public String GetElementValue(String value, String... nameHierarchy) throws Exception
    {
        if(value.startsWith("\"") && value.endsWith("\""))
            return value.substring(1, value.length() - 1);
        return value;
    }
    
    @Override
    public String SetElementValue(String value, String newValue, String... nameHierarchy) throws Exception
    {
        if(newValue.startsWith("\"") && newValue.endsWith("\""))
            return '"' + newValue + '"';
        return newValue;
    }
}
