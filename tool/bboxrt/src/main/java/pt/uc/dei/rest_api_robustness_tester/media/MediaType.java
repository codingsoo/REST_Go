package pt.uc.dei.rest_api_robustness_tester.media;

import pt.uc.dei.rest_api_robustness_tester.request.RequestBodyInstance;
import pt.uc.dei.rest_api_robustness_tester.schema.Schema;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class   MediaType
{
    public String mediaType = null;
    public SchemaBuilder schema = null;
    
    public SchemaBuilder GetSchema()
    {
        return schema;
    }
    
    public RequestBodyInstance Instantiate() throws Exception
    {
        return Instantiate( null,null);
    }
    
    public RequestBodyInstance Instantiate(String name, String value) throws Exception
    {
        Schema s = schema.Build();
        if(value != null)
            return new RequestBodyInstance(mediaType, s.Instantiate(name,   value, mediaType), this);
        else
            return new RequestBodyInstance(mediaType, s.Instantiate(null,null, mediaType), this);
    }

    public RequestBodyInstance InstantiateFiltering(List<String> names, List<String> values) throws Exception
    {
        Schema s = schema.Build();
        return new RequestBodyInstance(mediaType, s.InstantiateBody(names,   values, mediaType), this);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaType mediaType1 = (MediaType) o;
        return Objects.equals(mediaType, mediaType1.mediaType) &&
                Objects.equals(schema, mediaType1.schema);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(mediaType, schema);
    }
}
