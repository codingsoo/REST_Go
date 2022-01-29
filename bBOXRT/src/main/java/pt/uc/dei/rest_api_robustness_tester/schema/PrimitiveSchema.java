package pt.uc.dei.rest_api_robustness_tester.schema;

import java.util.ArrayList;
import java.util.List;

public abstract class PrimitiveSchema<T> extends Schema<T>
{
    public T defaultValue = null;
    
    public List<T> enumValues = new ArrayList<>();

    
    PrimitiveSchema(SchemaBuilder builder)
    {
        super(builder);
    }
}
