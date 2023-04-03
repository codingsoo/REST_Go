package pt.uc.dei.rest_api_robustness_tester.schema;

import org.apache.commons.lang3.RandomUtils;
import pt.uc.dei.rest_api_robustness_tester.utils.Utils;

import java.util.Random;

public class DoubleSchema extends NumericalSchema<Double>
{
    DoubleSchema(SchemaBuilder builder)
    {
        super(builder);
        try
        {
            if(builder.maximum != null)
                this.maximum = Double.parseDouble(builder.maximum);
            if(builder.minimum != null)
                this.minimum = Double.parseDouble(builder.minimum);
            if(builder.multipleOf != null)
                this.multipleOf = Double.parseDouble(builder.multipleOf);
            if (builder.defaultValue != null)
                this.defaultValue = Double.parseDouble(builder.defaultValue);
            if(!builder.enumValues.isEmpty())
                for(String s : builder.enumValues)
                    this.enumValues.add(Double.parseDouble(s));
        }
        catch(Exception ignored) {}
    }
    
    @Override
    public Schema<Double> New() throws Exception
    {
        try
        {
            if (defaultValue != null && RandomUtils.nextFloat(0, 1) <= DEFAULT_PROBABILITY)
            {
                double avg = defaultValue;
                double std = avg / 2;
                value = new Random().nextGaussian() * std + avg;
                return this;
            }
        }
        catch(Exception ignored) {}
    
        if(enumValues.isEmpty())
        {
            Double min = minimum;
            Double max = maximum;
        
            //min = min == Double.MIN_VALUE? min + 1 : min;
            min = min == (-Double.MAX_VALUE)? min + 1 : min;
        
            if(min < 0)
                value = RandomUtils.nextDouble(0, max) - RandomUtils.nextDouble(0, Math.abs(min));
            else
                value = RandomUtils.nextDouble(min, max);
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
    public Schema<Double>  randomizeWithCurrentValue(int iteration, int totalIterations) throws Exception{

        if(enumValues.isEmpty()){
            boolean positiveIncrement;
            Double nextValue;
            Double min = minimum;
            Double max = maximum;

            max = max == Double.MAX_VALUE? max : max + 1;
            //min = min == Double.MIN_VALUE? min + 1 : min;
            min = min == (-Double.MAX_VALUE)? min + 1 : min;
            Random random = new Random();

            do{
                double midPoint = (max + min) / 2;
                double scale = max - midPoint;
	    /*  if cols(population size) = 2000 then totalIterations = 1999 because first individual doesnt count
	        first individual will have std = midPoint last one will have std = 0 and for that reason
	        + 1.0 was add to this function, this way the last individual will have a std = 1
	     */
                scale = ( scale * ((double)(totalIterations - iteration)/(double)totalIterations));
                nextValue = value + random.nextGaussian() * scale;
            }while(!(min < nextValue && max > nextValue));
            value = nextValue;
        }
        else{
            value = enumValues.get(RandomUtils.nextInt(0, enumValues.size()));
        }

        return this;
    }

    @Override
    public Schema<Double> copySchema(SchemaBuilder builderInput/*, Schema<Double> newSchema*/) throws Exception{
        //Schema<Double> newSchema = new DoubleSchema(builderInput);
        DoubleSchema newSchema = new DoubleSchema(builderInput);
        newSchema.value = this.value;
        return newSchema;
    }

    @Override
    public void New(String value) throws Exception{
        this.value = Double.parseDouble( value );
    }


    @Override
    public Schema<Double> mutation(int iteration, int totalIterations, int mutationProb) throws Exception{

        // we can create a mutation probability according to the iteration number in the population
        Double nextValue;
        StringBuilder binaryString;
        Random rand = new Random();
        if(enumValues.isEmpty()){
            Double min = minimum;
            Double max = maximum;
            //min = min == Double.MIN_VALUE? min + 1 : min;
            min = min == (-Double.MAX_VALUE)? min + 1 : min;

            do{
                nextValue = value;
                binaryString = new StringBuilder(Long.toBinaryString(Double.doubleToRawLongBits(nextValue)));
                for(int k = 0; k < binaryString.length(); k++){
                    if(rand.nextDouble() <= mutationProb){
                        if (binaryString.charAt(k) == '0')
                            binaryString.setCharAt(k, '1');
                        else
                            binaryString.setCharAt(k, '0');
                    }
                }

                if (binaryString.length() == 64){ // this type of treatment is necessary to work with negative numbers
                    if (binaryString.charAt(0) == '1'){
                        String negBinStr = binaryString.substring(1);
                        nextValue = -1 * Double.longBitsToDouble(Long.parseLong(negBinStr, 2));
                    }
                    else if (binaryString.charAt(0) == '0'){
                        nextValue = Double.longBitsToDouble(Long.parseLong(binaryString.toString(), 2));
                    }
                }
                else if(binaryString.length() < 64) {
                    nextValue = Double.longBitsToDouble(Long.parseLong(binaryString.toString(), 2));
                }
                else{
                    throw new Exception("While mutating a double resulted in a size bigger than 64 bits");
                }
            }while(!(min < nextValue && max > nextValue));
            value = nextValue;
        }
        else
            value = enumValues.get(RandomUtils.nextInt(0, enumValues.size()));
        return this;
    }
    
    @Override
    protected Double DefaultMinimum()
    {
        //return Double.MIN_VALUE;
        return -Double.MAX_VALUE;
    }
    
    @Override
    protected Double DefaultMaximum()
    {
        return Double.MAX_VALUE;
    }
}