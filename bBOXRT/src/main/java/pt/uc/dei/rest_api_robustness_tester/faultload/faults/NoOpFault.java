package pt.uc.dei.rest_api_robustness_tester.faultload.faults;

import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;

public class NoOpFault implements Fault
{
    @Override
    public String FaultName()
    {
        return "No-op fault";
    }
    
    @Override
    public String[] AcceptedTypes()
    {
        return new String[]
                {
                        TypeManager.Type.Any.Value()
                };
    }
    
    @Override
    public String[] AcceptedFormats()
    {
        return new String[]
                {
                        TypeManager.Type.Any.Value()
                };
    }
    
    @Override
    public boolean IsPreconditionRespected(SchemaBuilder schema)
    {
        return true;
    }
    
    @Override
    public String Inject(String value, SchemaBuilder schema)
    {
        return value;
    }
}
