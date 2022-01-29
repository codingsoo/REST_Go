package pt.uc.dei.rest_api_robustness_tester.faultload.faults.date;

import org.joda.time.DateTime;
import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;

import java.util.Arrays;

public class ReplaceWithFirstDayOfCurrentMillennium implements Fault
{
    @Override
    public String FaultName()
    {
        return "Replace with the first day of the current millennium";
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
        try
        {
            DateTime dateTime = DateTime.parse(value);
            dateTime = dateTime.withDate(2000, 1, 1);
            
            if(value.contains("T"))
                return dateTime.toString("yyyy-MM-dd'T'HH:mm:ss");
            else
                return dateTime.toString("yyyy-MM-dd");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return value;
        }
    }
}
