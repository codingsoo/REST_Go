package pt.uc.dei.rest_api_robustness_tester.schema;

import org.apache.commons.lang3.RandomUtils;
import pt.uc.dei.rest_api_robustness_tester.utils.Utils;

import java.util.Random;

public class IntegerSchema extends NumericalSchema<Integer>
{


    IntegerSchema(SchemaBuilder builder)
    {
        super(builder);
        try
        {
            if(builder.maximum != null)
                this.maximum = Integer.parseInt(builder.maximum);
            if(builder.minimum != null)
                this.minimum = Integer.parseInt(builder.minimum);
            if(builder.multipleOf != null)
                this.multipleOf = Integer.parseInt(builder.multipleOf);
            if (builder.defaultValue != null)
                this.defaultValue = Integer.parseInt(builder.defaultValue);
            if(!builder.enumValues.isEmpty())
                for(String s : builder.enumValues)
                    this.enumValues.add(Integer.parseInt(s));
        }
        catch(Exception ignored) {}
    }

    @Override
    public Schema<Integer> New() throws Exception
    {
        try
        {
            if (defaultValue != null && RandomUtils.nextFloat(0, 1) <= DEFAULT_PROBABILITY)
            {
                int avg = defaultValue;
                int std = avg / 2;
                value = (int) (new Random().nextGaussian() * std + avg);
                return this;
            }
        }
        catch(Exception ignored) {}

        if(enumValues.isEmpty())
        {
            Integer min = minimum;
            Integer max = maximum;

            max = max == Integer.MAX_VALUE? max : max + 1;
            min = min == Integer.MIN_VALUE? min + 1 : min;

            if(min < 0)
                value = RandomUtils.nextInt(0, max) - RandomUtils.nextInt(0, Math.abs(min));
            else
                value = RandomUtils.nextInt(min, max);
        }
        else
            value = enumValues.get(RandomUtils.nextInt(0, enumValues.size()));
        return this;
    }

    @Override
    public Schema<Integer> randomizeWithCurrentValue(int iteration, int totalIterations) throws Exception{

        if(enumValues.isEmpty()) {
            boolean positiveIncrement;
            int nextValue;
            int min = minimum;
            int max = maximum;

            max = max == Integer.MAX_VALUE? max : max + 1;
            min = min == Integer.MIN_VALUE? min + 1 : min;

            Random random = new Random();

            do{
                double midPoint = (double)(max + min) / 2;
                double scale = max - midPoint;
    /*  if cols(population size) = 2000 then totalIterations = 1999 because first individual doesnt count
        first individual will have std = midPoint last one will have std = 0 and for that reason
        + 1.0 was add to this function, this way the last individual will have a std = 1
     */
                scale = ( scale * ((double)(totalIterations - iteration)/(double)totalIterations));
                nextValue = (int)((double)value + random.nextGaussian() * scale);


            }while(!(min < nextValue && max > nextValue));
            value = nextValue;
        }
        else{
            value = enumValues.get(RandomUtils.nextInt(0, enumValues.size()));
        }
        return this;
    }

    @Override
    public Schema<Integer> mutation(int iteration, int totalIterations, int mutationProb) throws Exception{
        // operate mutation here, take in consideration the logic implemented in New()

        Integer nextValue;
        StringBuilder binaryString;
        Random rand = new Random();
        if(enumValues.isEmpty())
        {
            Integer min = minimum;
            Integer max = maximum;

            max = max == Integer.MAX_VALUE? max : max + 1;
            min = min == Integer.MIN_VALUE? min + 1 : min;

            do{
                nextValue = value;
                binaryString = new StringBuilder(Integer.toBinaryString(nextValue));
                for(int k = 0; k < binaryString.length(); k++){
                    if(rand.nextDouble() <= mutationProb){
                        if (binaryString.charAt(k) == '0')
                            binaryString.setCharAt(k, '1');
                        else
                            binaryString.setCharAt(k, '0');
                    }
                }
                nextValue =  Integer.parseUnsignedInt(binaryString.toString(), 2);
            }while(!(min < nextValue && max > nextValue));
            value = nextValue;
        }
        else
            value = enumValues.get(RandomUtils.nextInt(0, enumValues.size()));


        return this;
    }


    @Override
    public Schema<Integer> copySchema(SchemaBuilder builderInput/*, Schema<Integer> newSchema*/) throws Exception{
        //Schema<Integer> newSchema = new IntegerSchema(builderInput);
        IntegerSchema newSchema = new IntegerSchema(builderInput);
        newSchema.value = this.value;
        return newSchema;
    }

    @Override
    public void New(String value) throws Exception{
        this.value = Integer.parseInt( value );
    }

//    @Override
//    protected String New(String value, MediaTypeFormatter formatter) throws Exception
//    {
//        return value;
//    }

    @Override
    protected Integer DefaultMinimum()
    {
        return Integer.MIN_VALUE;
    }

    @Override
    protected Integer DefaultMaximum()
    {
        return Integer.MAX_VALUE;
    }
}