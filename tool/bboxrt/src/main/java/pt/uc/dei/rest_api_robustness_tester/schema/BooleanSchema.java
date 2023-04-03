package pt.uc.dei.rest_api_robustness_tester.schema;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomUtils;

import java.util.Random;

public class BooleanSchema extends PrimitiveSchema<Boolean>
{
    BooleanSchema(SchemaBuilder builder)
    {
        super(builder);
        try
        {
            if (builder.defaultValue != null)
                this.defaultValue = Boolean.parseBoolean(builder.defaultValue);
            if(!builder.enumValues.isEmpty())
                for(String s : builder.enumValues)
                    this.enumValues.add(Boolean.parseBoolean(s));
        }
        catch(Exception ignored) {}
    }
    
    @Override
    public Schema<Boolean> New() throws Exception
    {
        value = RandomUtils.nextBoolean();
        return this;
    }

    @Override
    public Schema<Boolean> randomizeWithCurrentValue(int iteration, int totalIterations) throws Exception{
        int aux = 1;
        if((double)(iteration)/totalIterations  > 0.4)
            aux = 2;
        int cont = (int) Math.round(new Random().nextGaussian() * aux);
        // if cont != 0 then value gets a new random boolean
        if(cont == 0)
            value =  RandomUtils.nextBoolean();
        return this;
    }


    @Override
    public Schema<Boolean> mutation(int iteration, int totalIterations, int mutationProb) throws Exception{
        if(new Random().nextDouble() <= mutationProb)
            this.value = !this.value;
        return this;
    }

    @Override
    public void New(String value) throws Exception{
        this.value = Boolean.parseBoolean(value);
    }

    @Override
    public Schema<Boolean> copySchema(SchemaBuilder builderInput/*, Schema<Boolean> newSchema*/) throws Exception{
        BooleanSchema newSchema = new BooleanSchema(builderInput);
        newSchema.value = this.value;
        return newSchema;
    }
    
//    @Override
//    protected String New(String value, MediaTypeFormatter formatter) throws Exception
//    {
//        return value;
//    }
}