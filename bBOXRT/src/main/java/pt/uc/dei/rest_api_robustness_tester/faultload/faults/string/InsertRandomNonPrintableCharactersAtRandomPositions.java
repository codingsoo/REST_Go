package pt.uc.dei.rest_api_robustness_tester.faultload.faults.string;

import org.apache.commons.lang3.RandomUtils;
import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;
import pt.uc.dei.rest_api_robustness_tester.utils.Utils;

import java.util.Arrays;

//TODO: if defined, consider the maximum length of string to avoid overflowing
//      otherwise simply add a maximum number of X characters, X being a global constant
public class InsertRandomNonPrintableCharactersAtRandomPositions implements Fault
{
    @Override
    public String FaultName()
    {
        return "Insert random non-printable characters at random positions";
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
        {
            String r = value;
            for(int i = 0; i < maximum - value.length(); i++)
            {
                int j = RandomUtils.nextInt(0, r.length());
                r = r.substring(0, j) + Utils.RandomChar(Utils.AsciiNonPrintableCharacters()) + r.substring(j);
            }
            
            return r;
        }
        
        return value;
    }
}
