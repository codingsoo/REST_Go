package pt.uc.dei.rest_api_robustness_tester.faultload.faults.date;

import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;

import java.util.Arrays;

public class ReplaceWithInvalidDate2 implements Fault
{
    @Override
    public String FaultName()
    {
        return "Replace with invalid date 2";
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
                        TypeManager.Format.Date.Value(),
                        TypeManager.Format.DateTime.Value(),
                        TypeManager.Format.DateTimeV2.Value()
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
        int length = "yyyy-MM-dd".length();
        String remainder = "";
        
        if(value.contains("T"))
            remainder = value.substring(length);
        
        return "1998-4-31" + remainder;
    }
}
