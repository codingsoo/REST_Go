package pt.uc.dei.rest_api_robustness_tester.faultload.faults.string;

import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;
import pt.uc.dei.rest_api_robustness_tester.utils.Utils;

import java.util.Arrays;

public class DuplicateRandomElementsToOverflowMaximumLength implements Fault
{
    @Override
    public String FaultName()
    {
        return "Duplicate random elements to overflow maximum length";
    }
    
    @Override
    public String[] AcceptedTypes()
    {
        return new String[0];
    }
    
    @Override
    public String[] AcceptedFormats()
    {
        return new String[]
                {
                        TypeManager.Format.Byte.Value(),
                        TypeManager.Format.Binary.Value()
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
        int maximum = Integer.parseInt(schema.maximum);
        
        if(maximum > value.length())
        {
            StringBuilder r = new StringBuilder(value);
            for(int i = 0; i < maximum - value.length() + 1; i++)
                r.append(Utils.RandomChar(value));
            
            return r.toString();
        }
        
        return value;
    }
}
