package pt.uc.dei.rest_api_robustness_tester.workload;

import pt.uc.dei.rest_api_robustness_tester.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PayloadFilter
{
    public enum Type {Ignore, SetValue}
    public enum MediaFilter {Specific, Any}
    
    //TODO: Not used for now - it should allow for specifying the name of the payload element to apply the filter to
    //      (as opposed to applying the filter to the entire payload)
    //private String name = null;

    //added this [Carlos], methods also changed, now is an arraylist of parameters names to filter in the payload
    private HashMap<String, List<String>> namesWithValues = null;
    
    private final Path.HttpMethod method;
    private final String uri;
    
    private Type type = Type.Ignore;
    private List<String> values = null;
    
    private MediaFilter mediaFilter = MediaFilter.Any;
    private String mediaType = null;
    
    public PayloadFilter(PayloadFilter payloadFilter)
    {
        this.method = payloadFilter.method;
        this.uri = payloadFilter.uri;
        this.type = payloadFilter.type;
        this.values = payloadFilter.values;
        this.mediaFilter = payloadFilter.mediaFilter;
        this.mediaType = payloadFilter.mediaType;
    }
    
    public PayloadFilter(Path.HttpMethod method, String uri)
    {
        this.method = method;
        this.uri = uri;
    }
    
    public Path.HttpMethod GetOperationMethod()
    {
        return method;
    }
    
    public String GetOperationUri()
    {
        return uri;
    }
    
    public boolean OperationIsEqual(Path.HttpMethod method, String uri)
    {
        return uri.equals(this.uri) && this.method == method;
    }
    
    public PayloadFilter Ignore()
    {
        type = Type.Ignore;
        this.values = null;
        return this;
    }
    
    public boolean IsIgnore()
    {
        return type == Type.Ignore;
    }
    
    public PayloadFilter SetValue(String value)
    {
        type = Type.SetValue;
        this.values = new ArrayList<>();
        this.values.add(value);
        return this;
    }
    
    public PayloadFilter SetValues(String ... values)
    {
        type = Type.SetValue;
        this.values = new ArrayList<>(Arrays.asList(values));
        return this;
    }
    
    public PayloadFilter SetValues(List<String> values)
    {
        type = Type.SetValue;
        this.values = new ArrayList<>(values);
        return this;
    }
    
    public List<String> GetValues()
    {
        return values;
    }


    //changed this for the hashmap [Carlos]
    public boolean IsSetValue() { return type == Type.SetValue && namesWithValues != null; }

    /*public boolean IsSetValue()
    {
        return type == Type.SetValue && values != null;
    }*/

    
    public Type GetFilterType()
    {
        return type;
    }
    
    public PayloadFilter AnyMedia()
    {
        mediaFilter = MediaFilter.Any;
        return this;
    }
    
    public boolean AppliesToAnyMedia()
    {
        return mediaFilter == MediaFilter.Any;
    }
    
    public PayloadFilter SpecificMedia(String mediaType)
    {
        mediaFilter = MediaFilter.Specific;
        this.mediaType = mediaType;
        return this;
    }
    
    public String GetMediaType()
    {
        return mediaType;
    }
    
    public boolean MediaTypeIsEqual(String mediaType)
    {
        return mediaType.equals(this.mediaType);
    }
    
    public boolean AppliesToSpecificMedia()
    {
        return mediaFilter == MediaFilter.Specific && mediaType != null;
    }
    
    public MediaFilter GetMediaFilter()
    {
        return mediaFilter;
    }

    public HashMap<String, List<String>> getNamesWithValues() { return namesWithValues; }
    public PayloadFilter SetNamesWithValues(HashMap<String, List<String>> namesWithValues) {
        this.type = Type.SetValue;
        this.namesWithValues = namesWithValues;
        return this;
    }
}
