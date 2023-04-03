package pt.uc.dei.rest_api_robustness_tester.schema;

import org.apache.commons.lang3.RandomUtils;
import pt.uc.dei.rest_api_robustness_tester.utils.Utils;

import java.util.LinkedHashMap;
import java.util.Random;

public class LongSchema extends NumericalSchema<Long>
{
    LongSchema(SchemaBuilder builder)
    {
        super(builder);
        try
        {
            if(builder.maximum != null)
                this.maximum = Long.parseLong(builder.maximum);
            if(builder.minimum != null)
                this.minimum = Long.parseLong(builder.minimum);
            if(builder.multipleOf != null)
                this.multipleOf = Long.parseLong(builder.multipleOf);
            if (builder.defaultValue != null)
                this.defaultValue = Long.parseLong(builder.defaultValue);
            if(!builder.enumValues.isEmpty())
                for(String s : builder.enumValues)
                    this.enumValues.add(Long.parseLong(s));
        }
        catch(Exception ignored) {}
    }
    
    @Override
    public Schema<Long> New() throws Exception
    {
        try
        {
            if (defaultValue != null && RandomUtils.nextFloat(0, 1) <= DEFAULT_PROBABILITY)
            {
                long avg = defaultValue;
                long std = avg / 2;
                value = (long) (new Random().nextGaussian() * std + avg);
                return this;
            }
        }
        catch(Exception ignored) {}
    
        if(enumValues.isEmpty())
        {
            Long min = minimum;
            Long max = maximum;
        
            max = max == Long.MAX_VALUE? max : max + 1;
            min = min == Long.MIN_VALUE? min + 1 : min;
        
            if(min < 0)
                value = RandomUtils.nextLong(0, max) - RandomUtils.nextLong(0, Math.abs(min));
            else
                value = RandomUtils.nextLong(min, max);
        }
        else
            value = enumValues.get(RandomUtils.nextInt(0, enumValues.size()));
        return this;
    }
    
//    @Override
//    protected String New(String value, MediaTypeFormatter formatter) throws Exception
//    {
//        return value;
//    }

    @Override
    public Schema<Long> randomizeWithCurrentValue(int iteration, int totalIterations) throws Exception{

        boolean positiveIncrement;
        long nextValue;
        Long min = minimum;
        Long max = maximum;

        max = max == Long.MAX_VALUE? max : max + 1;
        min = min == Long.MIN_VALUE? min + 1 : min;

        Random random = new Random();

        if(enumValues.isEmpty()) {
            do {
                double midPoint = (double)(max + min) / 2;
                double scale = max - midPoint;
                scale = ( scale * ((double)(totalIterations - iteration)/(double)totalIterations));
                nextValue = (long)(value + random.nextGaussian() * scale);
            } while (!(min < nextValue && max > nextValue));
            value = nextValue;
        }
        else
            value = enumValues.get(RandomUtils.nextInt(0, enumValues.size()));


        return this;
    }


    @Override
    public Schema<Long> mutation(int iteration, int totalIterations, int mutationProb) throws Exception{
        Long nextValue;
        StringBuilder binaryString;
        Random rand = new Random();
        if(enumValues.isEmpty()){
            Long min = minimum;
            Long max = maximum;

            max = max == Long.MAX_VALUE? max : max + 1;
            min = min == Long.MIN_VALUE? min + 1 : min;

            do{
                nextValue = value;
                binaryString = new StringBuilder(Long.toBinaryString(nextValue));
                for(int k = 0; k < binaryString.length(); k++){
                    if(rand.nextDouble() <= mutationProb){
                        if (binaryString.charAt(k) == '0')
                            binaryString.setCharAt(k, '1');
                        else
                            binaryString.setCharAt(k, '0');
                    }
                }
                nextValue = Long.parseUnsignedLong(binaryString.toString(), 2);
            }while(!(min < nextValue && max > nextValue));
            value = nextValue;
        }
        else
            value = enumValues.get(RandomUtils.nextInt(0, enumValues.size()));
        return this;
    }


    @Override
    public Schema<Long> copySchema(SchemaBuilder builderInput/*, Schema<Long> newSchema*/) throws Exception{
        //Schema<Long> newSchema = new LongSchema(builderInput);
        LongSchema newSchema = new LongSchema(builderInput);
        newSchema.value = this.value;
        return newSchema;
    }

    @Override
    public void New(String value) throws Exception{
        this.value = Long.parseLong( value );
    }
    
    @Override
    protected Long DefaultMinimum()
    {
        return Long.MIN_VALUE;
    }
    
    @Override
    protected Long DefaultMaximum()
    {
        return Long.MAX_VALUE;
    }
}