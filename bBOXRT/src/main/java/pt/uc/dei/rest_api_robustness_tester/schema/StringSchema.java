package pt.uc.dei.rest_api_robustness_tester.schema;

import com.github.curiousoddman.rgxgen.RgxGen;
import com.mifmif.common.regex.Generex;
import org.apache.commons.lang3.RandomUtils;
import pt.uc.dei.rest_api_robustness_tester.utils.Utils;

import java.util.Random;

public class StringSchema extends PrimitiveSchema<String>
{
    //TODO: maybe by default use String.MIN and String.MAX?
    //      or
    //      some MIN and MAX Global constants?
    public Integer minLength = 1;
    public Integer maxLength = Utils.AsciiAlphanumericCharacters().length() - 1;
    private boolean hasMin = false;
    private boolean hasMax = false;
    
    public String pattern = null;
    
    StringSchema(SchemaBuilder builder)
    {
        super(builder);
        try
        {
            if(builder.maximum != null){
                this.maxLength = Integer.parseInt(builder.maximum);
                this.hasMax = true;
            }
            if(builder.minimum != null){
                this.minLength = Integer.parseInt(builder.minimum);
                this.hasMin = true;
            }
            if(builder.pattern != null)
                this.pattern = builder.pattern;
            if (builder.defaultValue != null)
                this.defaultValue = builder.defaultValue;
            if(!builder.enumValues.isEmpty())
                this.enumValues.addAll(builder.enumValues);
        }
        catch(Exception ignored) {}
    }

    private void generatePattern(){
        try
        {
            boolean done = false;
            String checkValue = new RgxGen(pattern).generate();
            if(hasMin || hasMax) {
                while (true) {
                    if (!hasMax && hasMin && checkValue.length() >= minLength)
                        done = true;
                    else if (hasMax && !hasMin && checkValue.length() <= maxLength)
                        done = true;
                    else if (hasMax && hasMin && checkValue.length() >= minLength && checkValue.length() <= maxLength){
                        int indexStart = Utils.getRandomNumber(minLength, checkValue.length() - 1);
                        int indexLast = Utils.getRandomNumber(indexStart + 1, checkValue.length());
                        checkValue = checkValue.substring(indexStart,indexLast);
                        done = true;
                    }
                    else if (hasMax && hasMin && checkValue.length() >= minLength && checkValue.length() > maxLength) {
                        int indexStart = Utils.getRandomNumber(minLength, maxLength - 1);
                        int indexLast = Utils.getRandomNumber(indexStart + 1, maxLength);
                        checkValue = checkValue.substring(indexStart,indexLast);
                        done = true;
                    }

                    if (done)
                        break;

                    checkValue = new RgxGen(pattern).generate();

                }
            }
            value = checkValue;
        }
        catch(Exception e)
        {
            System.err.println("Failed to generate string using RgxGen library (pattern: " + pattern +")");
            value = new Generex(pattern).random();
        }
    }

    @Override
    public Schema<String> New() throws Exception
    {
        //TODO: some APIs represent booleans or numbers as Strings, and the default value (if provided)
        //      is the only way of telling that
        //      Therefore, it should be useful to infer the Schema's real type from its default value
        //      and generate a new value accordingly
        if(defaultValue != null && RandomUtils.nextFloat(0, 1) <= DEFAULT_PROBABILITY)
        {
            value = defaultValue;
            return this;
        }
    
        if(pattern != null)
        {
            this.generatePattern();
            return this;
        }
        
        if(enumValues.isEmpty())
        {
            Integer min = minLength;
            Integer max = maxLength;
        
            max = max == Integer.MAX_VALUE? max : max + 1;
        
            StringBuilder s = new StringBuilder();
        
            for(int i = 0; i < RandomUtils.nextInt(min, max); i++)
                s.append(Utils.AsciiAlphanumericCharacters().
                        charAt(RandomUtils.nextInt(0, Utils.AsciiAlphanumericCharacters().length())));
    
            value = s.toString();
        }
        else
            value = enumValues.get(RandomUtils.nextInt(0, enumValues.size()));
        return this;
    }

    @Override
    public Schema<String> randomizeWithCurrentValue(int iteration, int totalIterations) throws Exception{

        String currentValue = value;
        String newValue = New().value;
        if(pattern == null && enumValues.isEmpty()){
            char[] charNewValue = newValue.toCharArray();
            char[] charCurrentValue = currentValue.toCharArray();

            StringBuilder s = new StringBuilder();
            int shorter = newValue.length() <= currentValue.length()? newValue.length() : currentValue.length();
            int  i = 0;

            for( ;  i < shorter ; i++){
                int pos1 = Utils.AsciiAlphanumericCharacters().indexOf(charNewValue[i]);
                int pos2 = Utils.AsciiAlphanumericCharacters().indexOf(charCurrentValue[i]);
                int posf = (pos1 + pos2) %  (Utils.AsciiAlphanumericCharacters().length());

                s.append(Utils.AsciiAlphanumericCharacters().charAt(posf));
            }

            if(currentValue.length() >= newValue.length() ){
                for(; i < currentValue.length(); i++){
                    s.append(charCurrentValue[i]);
                }
            }
            else {
                for(; i < newValue.length(); i++){
                    s.append(charNewValue[i]);
                }
            }

            this.value = s.toString();
        }
        else{
            this.value = newValue;
        }
        return this;
    }

    @Override
    public Schema<String> mutation(int iteration, int totalIterations, int mutationProb) throws Exception{
        Random rand = new Random();
        if(pattern != null)
        {
            this.generatePattern();
            return this;
        }

        if(enumValues.isEmpty())
        {
            String nextValue = value;
            String result = convertStringToBinary(nextValue);
            StringBuilder binaryString = new StringBuilder(result);

            for(int k = 0; k < binaryString.length(); k++){ /* validation in ASCII table is necessary for every 8bits*/
                if(rand.nextDouble() <= mutationProb){
                    if (binaryString.charAt(k) == '0')
                        binaryString.setCharAt(k, '1');
                    else
                        binaryString.setCharAt(k, '0');
                }
            }
            value = binaryToText(binaryString.toString());
        }
        else
            value = enumValues.get(RandomUtils.nextInt(0, enumValues.size()));
        return this;
    }

    private String convertStringToBinary(String input) {
        StringBuilder result = new StringBuilder();
        char[] chars = input.toCharArray();
        for (char aChar : chars) {
            result.append(
                    String.format("%8s", Integer.toBinaryString(aChar))
                            .replaceAll(" ", "0")
            );
        }
        return result.toString();
    }

    private String binaryToText(String binaryString) {
        StringBuilder stringBuilder = new StringBuilder();
        int charCode;
        for (int i = 0; i < binaryString.length(); i += 8) {
            charCode = Integer.parseInt(binaryString.substring(i, i + 8), 2);
            String returnChar = Character.toString((char) charCode);
            stringBuilder.append(returnChar);
        }
        return stringBuilder.toString();
    }

    @Override
    public Schema<String> copySchema(SchemaBuilder builderInput/*,Schema<String> newSchema */) throws Exception{
        StringSchema newSchema = new StringSchema(builderInput);
        newSchema.value = this.value;
        return newSchema;
    }

    @Override
    public void New(String value) throws Exception{
        this.value = value;
    }
    
//    @Override
//    protected String New(String value, MediaTypeFormatter formatter) throws Exception
//    {
//        return value;
//    }
}