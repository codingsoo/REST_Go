package pt.uc.dei.rest_api_robustness_tester.faultload.faults.string;

import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;
import pt.uc.dei.rest_api_robustness_tester.utils.Utils;

import java.util.Arrays;

public class ReplaceWithRandomPrintableCharacterString implements Fault
{
    @Override
    public String FaultName()
    {
        return "Replace with random printable character string of equal length";
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
        return Utils.RandomString(Utils.AsciiPrintableCharacters(), value.length());
    }
}
