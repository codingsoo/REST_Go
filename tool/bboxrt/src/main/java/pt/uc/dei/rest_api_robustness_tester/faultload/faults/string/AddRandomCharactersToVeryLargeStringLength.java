package pt.uc.dei.rest_api_robustness_tester.faultload.faults.string;

import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;
import pt.uc.dei.rest_api_robustness_tester.utils.Utils;

import java.util.Arrays;

public class AddRandomCharactersToVeryLargeStringLength implements Fault
{
    //TODO: should be Global constant
    private static int MAX_LENGTH = Integer.MAX_VALUE/1000;
    
    @Override
    public String FaultName()
    {
        return "Add random characters to reach very large string length";
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
        StringBuilder r = new StringBuilder(value);
        for(int i = 0; i < MAX_LENGTH - value.length() + 1; i++)
            r.append(Utils.RandomChar(Utils.AsciiPrintableCharacters()));
        
        return r.toString();
    }
}
