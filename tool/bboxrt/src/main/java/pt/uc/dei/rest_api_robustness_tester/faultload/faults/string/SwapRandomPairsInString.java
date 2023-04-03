package pt.uc.dei.rest_api_robustness_tester.faultload.faults.string;

import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SwapRandomPairsInString implements Fault
{
    @Override
    public String FaultName()
    {
        return "Swap a random number of element pairs in the string";
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
                        TypeManager.Format.Byte.Value(),
                        TypeManager.Format.Binary.Value()
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
    
        List<Integer> idxs = new ArrayList<>();
        for(int i = 0; i < value.length(); i++)
            idxs.add(i);
    
        Collections.shuffle(idxs);
        
        for(int i = 0; i < 5; i++)
        {
            int c1idx = idxs.get(i);
            char c1 = r.charAt(c1idx);
            int c2idx = idxs.get(idxs.size() - 1 - i);
            char c2 = r.charAt(c2idx);
            r.setCharAt(c2idx, c1);
            r.setCharAt(c1idx, c2);
        }
        
        return r.toString();
    }
}
