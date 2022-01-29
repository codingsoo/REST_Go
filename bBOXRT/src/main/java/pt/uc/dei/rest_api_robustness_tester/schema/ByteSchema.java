package pt.uc.dei.rest_api_robustness_tester.schema;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomUtils;
import pt.uc.dei.rest_api_robustness_tester.utils.Utils;

public class ByteSchema extends StringSchema
{
    ByteSchema(SchemaBuilder builder)
    {
        super(builder);
    }
    
    @Override
    public Schema<String> New() throws Exception
    {
        String v;
        if(defaultValue != null && RandomUtils.nextFloat(0, 1) <= DEFAULT_PROBABILITY)
            v = defaultValue;
        else
            v = super.New().value;
    
        value = Base64.encodeBase64String(v.getBytes());
        return this;
    }


    @Override
    public Schema<String> mutation(int iteration, int totalIterations, int mutationProb) throws Exception{
        String newValue = super.New().value;
        //operate mutation here
        newValue = Base64.encodeBase64String(newValue.getBytes());
        this.value = newValue;
        return this;
    }

    @Override
    public Schema<String> randomizeWithCurrentValue(int iteration, int totalIterations) throws Exception{

        String newValue = super.New().value;
        newValue = Base64.encodeBase64String(newValue.getBytes());
        this.value = newValue;
        return this;
    }

    @Override
    public void New(String value) throws Exception{
        value = Base64.encodeBase64String(value.getBytes());
        this.value = value;
    }

    @Override
    public Schema<String> copySchema(SchemaBuilder builderInput/*, Schema<String> newSchema*/) throws Exception{
        ByteSchema newSchema = new ByteSchema(builderInput);
        newSchema.value = this.value;
        return newSchema;
    }
    
//    @Override
//    protected String New(String value, MediaTypeFormatter formatter) throws Exception
//    {
//        String v = formatter.Deserialize(super.New(value, formatter), String.class);
//        return formatter.Serialize(Base64.encodeBase64String(v.getBytes()));
//    }
}
