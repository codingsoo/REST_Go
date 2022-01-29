package pt.uc.dei.rest_api_robustness_tester.schema;

public abstract class CompositeSchema<T> extends Schema<T>
{
    CompositeSchema(SchemaBuilder builder)
    {
        super(builder);
    }
}
