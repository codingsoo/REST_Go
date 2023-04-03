package pt.uc.dei.rest_api_robustness_tester.schema;

public abstract class NumericalSchema<T extends Number> extends PrimitiveSchema<T>
{
    public T minimum = DefaultMinimum();
    public T maximum = DefaultMaximum();
    public T multipleOf = null;
    
    NumericalSchema(SchemaBuilder builder)
    {
        super(builder);
        
    }



    protected abstract T DefaultMinimum();
    protected abstract T DefaultMaximum();
}
