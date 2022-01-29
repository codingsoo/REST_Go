package pt.uc.dei.rest_api_robustness_tester.request;

import pt.uc.dei.rest_api_robustness_tester.schema.SchemaInstance;

public class ParameterInstance
{
    private  String name;
    private  Parameter.Location location;
    private  String mediaType;
    private  SchemaInstance schema;
    private  Parameter base;
    
    public ParameterInstance(String name, Parameter.Location location, SchemaInstance schema, Parameter base)
    {
        this(name, location, null, schema, base);
    }
    public ParameterInstance(String name, Parameter.Location location, String mediaType, SchemaInstance schema, Parameter base)
    {
        this.name = name;
        this.location = location;
        this.mediaType = mediaType;
        this.schema = schema;
        this.base = base;
    }

    public ParameterInstance(ParameterInstance newParameter)
    {
        this.name = newParameter.name;
        this.location = newParameter.location;
        this.mediaType =    newParameter.mediaType;
        this.schema = (new SchemaInstance(newParameter.schema));
        this.base = newParameter.base;
    }
    
    public String Name()
    {
        return this.name;
    }
    
    public Parameter.Location Location()
    {
        return this.location;
    }
    
    public String MediaType()
    {
        return this.mediaType;
    }
    
    public SchemaInstance Schema()
    {
        return this.schema;
    }

    public void setSchema(SchemaInstance schema) {
        this.schema = schema;
    }

    public Parameter Base()
    {
        return this.base;
    }
}
