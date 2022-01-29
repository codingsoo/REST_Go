package pt.uc.dei.rest_api_robustness_tester.media;

import pt.uc.dei.rest_api_robustness_tester.schema.PrimitiveSchema;

public class GenericPrimitiveFormatter<T extends PrimitiveSchema> implements SchemaFormatter<T>
{
    private final String mediaType;
    private final Class<T> schemaClass;
    
    public GenericPrimitiveFormatter(String mediaType, Class<T> schemaClass)
    {
        this.mediaType = mediaType;
        this.schemaClass = schemaClass;
    }
    
    @Override
    public String MediaType()
    {
        return mediaType;
    }
    
    @Override
    public Class<T> SchemaType()
    {
        return schemaClass;
    }
    
    @Override
    public String Serialize(T schema)
    {
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
