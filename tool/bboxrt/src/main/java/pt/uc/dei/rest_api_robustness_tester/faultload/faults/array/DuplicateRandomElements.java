package pt.uc.dei.rest_api_robustness_tester.faultload.faults.array;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomUtils;
import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class DuplicateRandomElements implements Fault
{
    @Override
    public String FaultName()
    {
        return "Duplicate random elements in the array";
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
            int size = array.size();
            int amount = RandomUtils.nextInt(1, 5);
            for(int i = 0; i < amount; i++)
                array.add(array.get(RandomUtils.nextInt(0, size)));
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
