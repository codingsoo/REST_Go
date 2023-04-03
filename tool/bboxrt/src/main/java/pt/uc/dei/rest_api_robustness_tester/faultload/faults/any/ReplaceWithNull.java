package pt.uc.dei.rest_api_robustness_tester.faultload.faults.any;

import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;

public class ReplaceWithNull implements Fault
{
    @Override
    public String FaultName()
    {
        return "Replace with Null";
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
        //FIXME: this is clearly the wrong way to do it
        return "ReplaceWithNull";
    }
}
