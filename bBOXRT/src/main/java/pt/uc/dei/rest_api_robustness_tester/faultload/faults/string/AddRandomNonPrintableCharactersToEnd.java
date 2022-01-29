package pt.uc.dei.rest_api_robustness_tester.faultload.faults.string;

import org.apache.commons.lang3.RandomUtils;
import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;
import pt.uc.dei.rest_api_robustness_tester.utils.Utils;

import java.util.Arrays;

//TODO: if defined, consider the maximum length of string to avoid overflowing
//      otherwise simply add a maximum number of X characters, X being a global constant
public class AddRandomNonPrintableCharactersToEnd implements Fault
{
    @Override
    public String FaultName()
    {
        return "Add random non-printable characters to the end";
    }
    
    @Override
    public String[] AcceptedTypes()
    {
        return new String[]
                {
                        TypeManager.Type.String.Value()
                };
    }
    
    @Override
    public String[] AcceptedFormats()
    {
        return new String[]
                {
                        TypeManager.Format.Password.Value()
                };
    }
    
    @Override
    public boolean IsPreconditionRespected(SchemaBuilder schema)
    {
        return Arrays.asList(AcceptedTypes()).contains(schema.type) ||
                Arrays.asList(AcceptedFormats()).contains(schema.format);
    }
    
    @Override
    public String Inject(String value, SchemaBuilder schema)
    {
        int maximum = value.length() + 5;
        if(schema.maximum != null)
            maximum = Integer.parseInt(schema.maximum);
        
        if(maximum > value.length())
            return value + Utils.RandomString(Utils.AsciiNonPrintableCharacters(),
                    RandomUtils.nextInt(1, maximum - value.length()));
        
        return value;
    }
}
