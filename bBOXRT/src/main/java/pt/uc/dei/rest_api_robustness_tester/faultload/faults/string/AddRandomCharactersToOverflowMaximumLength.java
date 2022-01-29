package pt.uc.dei.rest_api_robustness_tester.faultload.faults.string;

import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;
import pt.uc.dei.rest_api_robustness_tester.utils.Utils;

import java.util.Arrays;

//TODO: if defined, consider the maximum length of string to avoid overflowing
//      otherwise simply add a maximum number of X characters, X being a global constant
public class AddRandomCharactersToOverflowMaximumLength implements Fault
{
    @Override
    public String FaultName()
    {
        return "Add random characters to overflow maximum string length";
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
        return (Arrays.asList(AcceptedTypes()).contains(schema.type) ||
                Arrays.asList(AcceptedFormats()).contains(schema.format)) &&
                schema.maximum != null;
    }
    
    @Override
    public String Inject(String value, SchemaBuilder schema)
    {

        //int maximum = Integer.parseInt(schema.maximum);
        //changed to double [carlos] because of overflowing
        double maximum = Double.parseDouble(schema.maximum);
        
        if(maximum > value.length())
        {
            StringBuilder r = new StringBuilder(value);
            for(int i = 0; i < maximum - value.length() + 1; i++)
                r.append(Utils.RandomChar(Utils.AsciiPrintableCharacters()));
            
            return r.toString();
        }
        
        return value;
    }
}
