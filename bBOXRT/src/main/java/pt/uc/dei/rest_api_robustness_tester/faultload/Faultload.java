package pt.uc.dei.rest_api_robustness_tester.faultload;

import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.faultload.faults.NoOpFault;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Faultload
{
    private final Map<String, List<Fault>> faultsByTypeOrFormat;
    
    public Faultload()
    {
        faultsByTypeOrFormat = new HashMap<>();
    }
    
    //FIXME: this algorithm is probably the culprit for Empty/Null faults being used more than FL_REP
    //       the reason is probably they are stored twice, once for type Any, and once for format Any,
    //       and are probably treated as 2 separate faults
    public void RegisterFault(Fault fault)
    {
        for(String type : fault.AcceptedTypes())
        {
            EnsureListExists(type);
            faultsByTypeOrFormat.get(type).add(fault);
        }
    
        for(String format : fault.AcceptedFormats())
        {
            EnsureListExists(format);
            faultsByTypeOrFormat.get(format).add(fault);
        }
    }
    
    public List<Fault> GetFaults(String typeOrFormat)
    {
        return new ArrayList<>(faultsByTypeOrFormat.get(typeOrFormat));
    }
    
    public List<Fault> GetApplicableFaults(SchemaBuilder schema)
    {
        List<Fault> applicableFaults = new ArrayList<>();
        
        String typeOrFormat = null;
        if(schema.format != null && faultsByTypeOrFormat.get(schema.format) != null)
            typeOrFormat = schema.format;
        else
        {
            if(schema.format != null && faultsByTypeOrFormat.get(schema.format) == null)
                System.out.println("[Faultload] Format " + schema.format + " has no applicable faults - " +
                        "defaulting to its base Type " + schema.type);
            typeOrFormat = schema.type;
        }
        
        if(faultsByTypeOrFormat.get(typeOrFormat) != null)
        {
            for (Fault fault : faultsByTypeOrFormat.get(typeOrFormat))
                if (fault.IsPreconditionRespected(schema))
                    applicableFaults.add(fault);
        }
        else
        {
            System.out.println("[Faultload] Type " + typeOrFormat + " has no applicable faults - " +
                    "defaulting to the No-op Fault implementation");
            applicableFaults.add(new NoOpFault());
        }
        
        if(faultsByTypeOrFormat.containsKey(TypeManager.Type.Any.Value()))
            applicableFaults.addAll(faultsByTypeOrFormat.get(TypeManager.Type.Any.Value()));
        
        return applicableFaults;
    }
    
    private void EnsureListExists(String dataType)
    {
        if(!faultsByTypeOrFormat.containsKey(dataType))
            faultsByTypeOrFormat.put(dataType, new ArrayList<>());
    }
}
