package pt.uc.dei.rest_api_robustness_tester.schema;

import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ArraySchema<T extends Schema> extends CompositeSchema<ArrayList<T>>
{
    //TODO: maybe by default use Array.MIN and Array.MAX?
    //      or
    //      some MIN and MAX Global constants?
    public Integer minItems = 1;
    public Integer maxItems = 10;
    
    public T items = null;
    public final SchemaBuilder itemsBuilder;
    
    ArraySchema(SchemaBuilder builder)
    {
        super(builder);
        itemsBuilder = builder.items;
        try
        {
            if(builder.minimum != null)
                this.minItems = Integer.valueOf(builder.minimum);
            if(builder.maximum != null)
                this.maxItems = Integer.valueOf(builder.maximum);
            if(builder.items != null)
                this.items = (T)builder.items.Build();
        }
        catch(Exception ignored) {}
    }
    
    @Override
    public Schema<ArrayList<T>> New() throws Exception
    {
        value = new ArrayList<>();
        for(int i = 0; i < RandomUtils.nextInt(minItems, maxItems); i++)
            value.add((T) itemsBuilder.Build().New());
        return this;
    }

    @Override
    public Schema<ArrayList<T>> randomizeWithCurrentValue(int iteration, int totalIterations) throws Exception{
        for(T t : value){
            t.randomizeWithCurrentValue(iteration, totalIterations);
        }
        return this;
    }


    @Override
    public Schema<ArrayList<T>>  mutation(int iteration, int totalIterations, int mutationProb) throws Exception{
        for(T t : value){
            t.mutation(iteration, totalIterations,mutationProb);
        }
        return this;
    }

    @Override
    public Schema<ArrayList<T>> copySchema(SchemaBuilder builderInput/*, Schema<ArrayList<T>> newSchema*/) throws Exception{
        //Schema<ArrayList<T>> newSchema = new ArraySchema<>(builderInput);
        Schema<ArrayList<T>> newSchema = new ArraySchema<>(builderInput);
        newSchema.value = (new ArrayList<>());

        for(Schema t : value){
            newSchema.value.add((T) t.copySchema(this.itemsBuilder));
        }

        return newSchema;
    }

    @Override
    public void New(String value) throws Exception{ }
    
//    @Override
//    protected String New(String value, MediaTypeFormatter formatter) throws Exception
//    {
//        List<String> formattedList = new ArrayList<>();
//        ArrayList rawList = formatter.Deserialize(value, ArrayList.class);
//        for(Object t : rawList)
//        {
//            String _t = "" + t;
//            formattedList.add(items.New(_t, formatter));
//        }
//        return formatter.Serialize(formattedList);
//    }
}
