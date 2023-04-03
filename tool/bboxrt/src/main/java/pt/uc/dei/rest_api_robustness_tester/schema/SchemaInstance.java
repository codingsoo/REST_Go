package pt.uc.dei.rest_api_robustness_tester.schema;

public class SchemaInstance
{
    private String value;
    private Schema schema;
    
    public SchemaInstance(String value, Schema schema)
    {
        this.value = value;
        this.schema = schema;
    }

    public SchemaInstance(SchemaInstance newSchema)
    {
        this.value = newSchema.value;
        this.schema = newSchema.schema;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public String Value()
    {
        return this.value;
    }
    
    public Schema Schema()
    {
        return this.schema;
    }
}
