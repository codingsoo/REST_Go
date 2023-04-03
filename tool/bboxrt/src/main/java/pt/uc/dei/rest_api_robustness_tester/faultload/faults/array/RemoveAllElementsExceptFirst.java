package pt.uc.dei.rest_api_robustness_tester.faultload.faults.array;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class RemoveAllElementsExceptFirst implements Fault
{
    @Override
    public String FaultName()
    {
        return "Remove all elements in the array except the first one";
    }
    
    @Override
    public String[] AcceptedTypes()
    {
        return new String[]
                {
                        TypeManager.Type.Array.Value()
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
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            ArrayList array = mapper.readValue(value, new TypeReference<ArrayList>() {});
            if(array.size() > 1)
            {
                Object first = array.remove(0);
                array.clear();
                array.add(first);
            }
            
            return mapper.writeValueAsString(array);
        }
        catch (JsonProcessingException e)
        {
            e.printStackTrace();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return value;
    }
}
