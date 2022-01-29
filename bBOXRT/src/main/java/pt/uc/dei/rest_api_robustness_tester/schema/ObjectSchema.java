package pt.uc.dei.rest_api_robustness_tester.schema;
import java.util.*;

public class ObjectSchema<K extends String, V extends Schema> extends CompositeSchema<LinkedHashMap<K, V>>
{
    public List<K> required = new ArrayList<>();
    
    public Map<K, V> properties = new LinkedHashMap<>();
    
    ObjectSchema(SchemaBuilder builder)
    {
        super(builder);
        try
        {
            if(!builder.properties.isEmpty())
                for(String s : builder.properties.keySet())
                    this.properties.put((K)s, (V) builder.properties.get(s).Build());
            if(!builder.required.isEmpty())
                for(String s : builder.required)
                    this.required.add((K)s);
        }
        catch(Exception ignored) {}
    }

    
    @Override
    public Schema<LinkedHashMap<K, V>> New() throws Exception
    {
        value = new LinkedHashMap<>();
        for(K k : properties.keySet())
            value.put(k, (V)properties.get(k).New());
        return this;
    }

    @Override
    public Schema<LinkedHashMap<K, V>> randomizeWithCurrentValue(int iteration, int totalIterations) throws Exception{
        value = new LinkedHashMap<>();
        for(K k : properties.keySet())
            value.put(k, (V)properties.get(k).randomizeWithCurrentValue(iteration, totalIterations));
        return this;
    }

    @Override
    public Schema<LinkedHashMap<K, V>> mutation(int iteration, int totalIterations, int mutationProb) throws Exception{
        value = new LinkedHashMap<>();
        for(K k : properties.keySet())
            value.put(k, (V)properties.get(k).mutation(iteration, totalIterations,mutationProb));
        return this;
    }

    @Override
    public Schema<LinkedHashMap<K, V>> copySchema(SchemaBuilder builderInput /*, Schema<LinkedHashMap<K, V>> newSchema*/) throws Exception{
        //Schema<LinkedHashMap<K, V>> newSchema = new ObjectSchema<>(builderInput);
        ObjectSchema<K , V> newSchema = new ObjectSchema<>(builderInput);
        newSchema.value = new LinkedHashMap<>();
        newSchema.required = new ArrayList<>();
        newSchema.properties =  new LinkedHashMap<>();

        for(K k : this.required)
            newSchema.required.add(k);

        for(K k : this.properties.keySet())
            ((ObjectSchema<K, V>) newSchema).properties.put(k, (V) this.properties.get(k).copySchema(builderInput.properties.get(k)));

        for(K k : newSchema.properties.keySet())
            newSchema.value.put(k, newSchema.properties.get(k));

        return newSchema;
    }


    @Override
    public void New(String value) throws Exception{

    }
    
//    @Override
//    protected LinkedHashMap<K, V> New(String value) throws Exception
//    {
//        Map<String, String> formattedMap = new LinkedHashMap<>();
//        LinkedHashMap rawMap = formatter.Deserialize(value, LinkedHashMap.class);
//        for(Object k : rawMap.keySet())
//        {
//            String _k = "" + k;
//            String _v = "" + rawMap.get(k);
//            if(properties.containsKey(_k))
//            {
//                V schemaV = properties.get(_k);
//                formattedMap.put(_k, schemaV.New(_v, formatter));
//            }
//        }
//        return formatter.Serialize(formattedMap);
//    }
}
