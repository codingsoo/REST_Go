package pt.uc.dei.rest_api_robustness_tester.faultload.faults.bool;

import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;
import pt.uc.dei.rest_api_robustness_tester.utils.Utils;

import java.util.Arrays;

public class OverflowBooleanValue implements Fault
{
    @Override
    public String FaultName()
    {
        return "Overflow boolean value";
    }
    
    @Override
    public String[] AcceptedTypes()
    {
        return new String[]
                {
                        TypeManager.Type.Boolean.Value()
                };
    }
    
    @Override
    public String[] AcceptedFormats()
    {
        return new String[0];
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
        int max = ("" + false).length();
    
        StringBuilder r = new StringBuilder(value);
        for(int i = 0; i < max; i++)
            r.append(Utils.RandomChar(Utils.AsciiPrintableCharacters()));
    
        return r.toString();
    }
}
