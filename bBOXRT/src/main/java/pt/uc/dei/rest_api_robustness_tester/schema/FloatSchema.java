package pt.uc.dei.rest_api_robustness_tester.schema;

import org.apache.commons.lang3.RandomUtils;
import pt.uc.dei.rest_api_robustness_tester.utils.Utils;

import java.util.Random;

public class FloatSchema extends NumericalSchema<Float>
{
    FloatSchema(SchemaBuilder builder)
    {
        super(builder);
        try
        {
            if(builder.maximum != null)
                this.maximum = Float.parseFloat(builder.maximum);
            if(builder.minimum != null)
                this.minimum = Float.parseFloat(builder.minimum);
            if(builder.multipleOf != null)
                this.multipleOf = Float.parseFloat(builder.multipleOf);
            if (builder.defaultValue != null)
                this.defaultValue = Float.parseFloat(builder.defaultValue);
            if(!builder.enumValues.isEmpty())
                for(String s : builder.enumValues)
                    this.enumValues.add(Float.parseFloat(s));
        }
        catch(Exception ignored) {}
    }
    
    @Override
    public Schema<Float> New() throws Exception
    {
        try
        {
            if (defaultValue != null && RandomUtils.nextFloat(0, 1) <= DEFAULT_PROBABILITY)
            {
                float avg = defaultValue;
                float std = avg / 2;
                value = (float) (new Random().nextGaussian() * std + avg);
                return this;
            }
        }
        catch(Exception ignored) {}
    
        if(enumValues.isEmpty())
        {
            Float min = minimum;
            Float max = maximum;
        
            //min = min == Float.MIN_VALUE? min + 1 : min;
            max = max == Float.MAX_VALUE? max : max + 1;
            min = min == (-Float.MAX_VALUE)? min + 1 : min;
        
            if(min < 0)
                value = RandomUtils.nextFloat(0, max) - RandomUtils.nextFloat(0, Math.abs(min));
            else
                value = RandomUtils.nextFloat(min, max);
        }
        else
            value = enumValues.get(RandomUtils.nextInt(0, enumValues.size()));
        return this;
    }

    @Override
    public Schema<Float> randomizeWithCurrentValue(int iteration, int totalIterations) throws Exception{

        if(enumValues.isEmpty()){
            Float nextValue;
            Float min = minimum;
            Float max = maximum;

            max = max == Float.MAX_VALUE? max : max + 1;
            //min = min == (Float.MIN_VALUE)? min + 1 : min;
            min = min == (-Float.MAX_VALUE)? min + 1 : min;

            Random random = new Random();
            do{
                double midPoint = (double)(max + min) / 2;
                double scale = max - midPoint;
  /*  if cols(population size) = 2000 then totalIterations = 1999 because first individual doesnt count
      first individual will have std = midPoint last one will have std = 0 and for that reason
      + 1.0 was add to this function, this way the last individual will have a std = 1
   */
                scale = ( scale * ((double)(totalIterations - iteration)/(double)totalIterations));
                nextValue = (float)(value + random.nextGaussian() * scale);

            }while(!(min < nextValue && max > nextValue));
            value = nextValue;
        }
        else{
            value = enumValues.get(RandomUtils.nextInt(0, enumValues.size()));
        }
        return this;
    }



    @Override
    public Schema<Float> mutation(int iteration, int totalIterations, int mutationProb) throws Exception{
        Float nextValue;
        StringBuilder binaryString;
        Random rand = new Random();
        if(enumValues.isEmpty()){

            Float min = minimum;
            Float max = maximum;
            max = max == Float.MAX_VALUE? max : max + 1;
            min = min == (-Float.MAX_VALUE)? min + 1 : min;

            do{
                nextValue = value;
                int intBits = Float.floatToIntBits(nextValue);
                binaryString = new StringBuilder(Integer.toBinaryString(intBits));
                for(int k = 0; k < binaryString.length(); k++){
                    if(rand.nextDouble() <= mutationProb){
                        if (binaryString.charAt(k) == '0')
                            binaryString.setCharAt(k, '1');
                        else
                            binaryString.setCharAt(k, '0');
                    }
                }

                if (binaryString.length() == 32){ // this type of treatment is necessary to work with negative numbers
                    if (binaryString.charAt(0) == '1'){
                        String negBinStr = binaryString.substring(1);
                        intBits = Integer.parseInt(negBinStr, 2);
                        nextValue = -1 * Float.intBitsToFloat(intBits);
                    }
                    else if (binaryString.charAt(0) == '0'){
                        intBits = Integer.parseInt(binaryString.toString(), 2);
                        nextValue = Float.intBitsToFloat(intBits);
                    }
                }
                else if(binaryString.length() < 32) {
                    intBits = Integer.parseInt(binaryString.toString(), 2);
                    nextValue = Float.intBitsToFloat(intBits);
                }
                else{
                    throw new Exception("While mutating a float resulted in a size bigger than 32 bits");
                }
            }while(!(min < nextValue && max > nextValue));
            value = nextValue;
        }
        else
            value = enumValues.get(RandomUtils.nextInt(0, enumValues.size()));
        return this;
    }

    @Override
    public Schema<Float> copySchema(SchemaBuilder builderInput/*,  Schema<Float> newSchema*/) throws Exception{
        //Schema<Float> newSchema = new FloatSchema(builderInput);
        FloatSchema newSchema = new FloatSchema(builderInput);
        newSchema.value = this.value;
        return newSchema;
    }


    @Override
    public void New(String value) throws Exception{
        this.value = Float.parseFloat( value );
    }

//    @Override
//    protected String New(String value, MediaTypeFormatter formatter) throws Exception
//    {
//        return value;
//    }
    
    @Override
    protected Float DefaultMinimum()
    {
        //return Float.MIN_VALUE;
        return -Float.MAX_VALUE;
    }
    
    @Override
    protected Float DefaultMaximum()
    {
        return Float.MAX_VALUE;
    }
}