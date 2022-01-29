package pt.uc.dei.rest_api_robustness_tester.faultload.faults.datetime;

import org.joda.time.DateTime;
import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;

import java.util.Arrays;

public class Subtract24Hours implements Fault
{
    @Override
    public String FaultName()
    {
        return "Subtract 24 hours from the time";
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
        try
        {
            DateTime dateTime = DateTime.parse(value);
            dateTime = dateTime.minusHours(24);
            
            return dateTime.toString("yyyy-MM-dd'T'HH:mm:ss");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return value;
        }
    }
}
