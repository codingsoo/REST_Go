package pt.uc.dei.rest_api_robustness_tester.schema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SchemaBuilder
{
    SchemaBuilder() {}
    
    public String type = null;
    public String format = null;
    
    public String GetTypeOrFormat()
    {
        if(format != null)
            return format;
        return type;
    }
    
    //Base
    public boolean nullable = false;
    public boolean readOnly = false;
    public boolean writeOnly = false;
    
    //Primitive
    public String defaultValue = null;
    public List<String> enumValues = new ArrayList<>();
    
    //Numerical
    public String minimum = null;
    public String maximum = null;
    public String multipleOf = null;
    
    //String
//    public Integer minLength = null;
//    public Integer maxLength = null;
    public String pattern = null;
    
    //Array
//    public String minItems = null;
//    public String maxItems = null;
    public SchemaBuilder items = null;
    
    //Object
    public List<String> required = new ArrayList<>();
    public Map<String, SchemaBuilder> properties = new LinkedHashMap<>();
    
    public static final String NAME_SEP = "#SEP#";
    public Map<String, SchemaBuilder> GetAllElements()
    {
        Map<String, SchemaBuilder> elements = new LinkedHashMap<>();
        if(!properties.isEmpty())
        {
            for(String schemaName : properties.keySet())
            {
                Map<String, SchemaBuilder> subSchemaElements = properties.get(schemaName).GetAllElements();
                for(String subSchemaName : subSchemaElements.keySet())
                {
                    if(subSchemaName.equals(NAME_SEP))
                        elements.put(schemaName, subSchemaElements.get(subSchemaName));
                    else
                        elements.put(schemaName + NAME_SEP + subSchemaName, subSchemaElements.get(subSchemaName));
                }
            }
        }
        else if(items != null)
            elements.put(NAME_SEP, this);
        else
            elements.put(NAME_SEP, this);
        
        return elements;
    }
    
    public Schema Build() throws Exception
    {
        Class<? extends Schema> c;
        TypeManager typeManager = TypeManager.Instance();
        if(this.format != null && typeManager.HasFormat(this.format))
            c = typeManager.GetFormat(this.format);
        else if(this.type != null && typeManager.HasType(this.type))
            c = typeManager.GetType(this.type);
        else
            throw new IllegalStateException("Schema may not have both type and format set to null");
    
        return c.getDeclaredConstructor(SchemaBuilder.class).newInstance(this);
    }
}
