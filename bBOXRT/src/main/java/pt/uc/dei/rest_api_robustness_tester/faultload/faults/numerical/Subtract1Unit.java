package pt.uc.dei.rest_api_robustness_tester.faultload.faults.numerical;

import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;

import java.util.Arrays;

public class Subtract1Unit implements Fault
{
    @Override
    public String FaultName()
    {
        return "Subtract 1 unit";
    }
    
    @Override
    public String[] AcceptedTypes()
    {
        return new String[]
                {
                        TypeManager.Type.Integer.Value(),
                        TypeManager.Type.Number.Value()
                };
    }
    
    @Override
    public String[] AcceptedFormats()
    {
        return new String[]
                {
                        TypeManager.Format.Int32.Value(),
                        TypeManager.Format.Int64.Value(),
                        TypeManager.Format.Float.Value(),
                        TypeManager.Format.Double.Value()
                };
    }
    
    @Override
    public boolean IsPreconditionRespected(SchemaBuilder schema)
    {
        return Arrays.asList(AcceptedTypes()).contains(schema.type) ||
                Arrays.asList(AcceptedFormats()).contains(schema.format);
    }
    
    @Override
    public String Inject(String valueRepresentation, SchemaBuilder schema)
    {
        try
        {
            int value = Integer.parseInt(valueRepresentation);
            return "" + (value - 1);
        }
        catch(Exception ignored) {}
    
        try
        {
            float value = Float.parseFloat(valueRepresentation);
            return "" + (value - 1);
        }
        catch(Exception ignored) {}
    
        try
        {
            long value = Long.parseLong(valueRepresentation);
            return "" + (value - 1);
        }
        catch(Exception ignored) {}
    
        try
        {
            double value = Double.parseDouble(valueRepresentation);
            return "" + (value - 1);
        }
        catch(Exception ignored) {}
        
        return valueRepresentation;
    }
}
