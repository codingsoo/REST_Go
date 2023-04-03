package pt.uc.dei.rest_api_robustness_tester.schema;

import pt.uc.dei.rest_api_robustness_tester.media.FormatterManager;
import pt.uc.dei.rest_api_robustness_tester.media.ObjectJsonFormatter;
import pt.uc.dei.rest_api_robustness_tester.media.SchemaFormatter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Schema<T>
{
    //TODO: should be Global constant
    protected static float DEFAULT_PROBABILITY = 0.25f;

    //TODO: add support for XML property
    public boolean nullable = false;
    public boolean readOnly = false;
    public boolean writeOnly = false;
    
    public T value = null;
    
    public String type = null;
    public String format = null;
    
    Schema(SchemaBuilder builder)
    {
        this.nullable = builder.nullable;
        this.readOnly = builder.readOnly;
        this.writeOnly = builder.writeOnly;
        this.type = builder.type;
        this.format = builder.format;
    }

    // String name will be use to filter parameters in payload, like in json
    public SchemaInstance Instantiate( String name, String valueParam, String mediaType) throws Exception
    {

        if(valueParam != null ){
            //this.value = New(value);
            return new SchemaInstance(valueParam, this);
        }
        else{
            New();
        }
        
        Class<? extends Schema> c;
        if(TypeManager.Instance().HasFormat(format))
            c = TypeManager.Instance().GetFormat(format);
        else
            c = TypeManager.Instance().GetType(type);

        SchemaFormatter f;
        if(mediaType != null){
            f = FormatterManager.Instance().GetFormatter(mediaType, c);
        }
        else{
            f = FormatterManager.Instance().GetDefaultFormatter();
        }

        return new SchemaInstance(f.Serialize(this), this);
    }


    //added this [Carlos]
    public SchemaInstance InstantiateBody(List<String> names, List<String> valuesParam, String mediaType) throws Exception
    {
        New();
        Class<? extends Schema> c;
        if(TypeManager.Instance().HasFormat(format))
            c = TypeManager.Instance().GetFormat(format);
        else
            c = TypeManager.Instance().GetType(type);

        SchemaFormatter f;
        /*if(type != null && type.equals("object") && names != null
                && valuesParam != null && valuesParam.size() == names.size() /***&& mediaType.equals("application/json")**//*){

            for(int i = 0; i < names.size() ; i++){
                if( ((ObjectSchema<String,Schema<T>>)this).properties.containsKey(names.get(i))){
                    ((ObjectSchema<String,Schema<T>>)this).properties.get(names.get(i)).value = (T) valuesParam.get(i);
                }
            }
        }*/
        if(valuesParam != null && valuesParam.size() == names.size()){
            for(int i = 0; i < names.size() ; i++){
                FindParameterToFilter(this, names.get(i), valuesParam.get(i));
            }
        }

        if(mediaType != null){
            f = FormatterManager.Instance().GetFormatter(mediaType, c);
        }
        else{
            f = FormatterManager.Instance().GetDefaultFormatter();
        }
        return new SchemaInstance(f.Serialize(this), this);
    }

    public void FindParameterToFilter(Schema<T> current, String name, String newValue) throws Exception {
        if(current.type != null && current.type.equals("object")){
            /* if there isn't a property with the same value as the variable name (name is the parameter's name to filter)
             then will call recursive function to find others objectSchemas and arraySchemas (can be an array of objects) */
            ObjectSchema<String,Schema<T>> currentAux = (ObjectSchema<String,Schema<T>>)current;
            if(!currentAux.properties.containsKey(name)){

                for(String key : currentAux.properties.keySet()){
                    if(currentAux.properties.get(key).type.equals("object")){
                        FindParameterToFilter( currentAux.properties.get(key),  name,  newValue);
                    }
                    if(currentAux.properties.get(key).type.equals("array")){
                        ArraySchema<Schema<T>> currentArrayAux =  (ArraySchema<Schema<T>>)currentAux.properties.get(key);
                        /* if the schema of each item in the array is an objectSchema then it will call the recursive
                            function to find the parameter with name equals to the variable "name" */
                        if(currentArrayAux.itemsBuilder.type.equals("object")){
                            FindParameterToFilter(currentAux.properties.get(key), name, newValue);
                        }
                    }
                }
            }
            else{
                /* this will not work in arrays or in objects, be aware of that!*/
                currentAux.properties.get(name).New(newValue);
            }
        }
        if(current.type != null && current.type.equals("array")){
            ArraySchema<Schema<T>> currentArrayAux =  (ArraySchema<Schema<T>>)current;
            /* if it is an array and each element of the array is an object then it will search in
            each object for the parameter with the name equals to the variable "name" and will set
            the value equal to "newValue" */
            if(currentArrayAux.itemsBuilder.type.equals("object")){
                for(Schema<T> schemaObject : currentArrayAux.value){
                    FindParameterToFilter(schemaObject,  name, newValue);
                }
            }
        }
    }
    
    public abstract Schema<T> New() throws Exception;
    //TODO: instead of deserializing the *value* parameter, should just check if
    //      parameter's content agrees with the Schema's data type
    //      (e.g., for an ObjectSchema, should check if content is structured as a map and
    //      for each of its elements, check if its value also agrees with its respective schema)
    //      (the latter part of the e.g. probably depends on deserialization, though)
    //public abstract T New(String value) throws Exception;

    public abstract void New(String value) throws Exception;

    public abstract Schema<T> randomizeWithCurrentValue(int iteration, int totalIterations) throws Exception;

    public abstract Schema<T> mutation(int iteration, int totalIterations, int mutationProb) throws Exception;

    public abstract Schema<T> copySchema(SchemaBuilder builderInput/*, Schema<T> newSchema*/) throws Exception;

    @Override
    public String toString()
    {
        return value.toString();
    }
    
    public static SchemaBuilder Builder()
    {
        return new SchemaBuilder();
    }
}
