package pt.uc.dei.rest_api_robustness_tester.request;

import pt.uc.dei.rest_api_robustness_tester.media.MediaType;
import pt.uc.dei.rest_api_robustness_tester.schema.Schema;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaBuilder;

import java.util.Objects;

//TODO: Review properties that are important but not yet implemented
public class Parameter
{
    //TODO: consider parameter examples -> may be used as hints for random value generation
    
    public enum Location
    {
        Path(),
        Query(),
        Header(),
        Cookie();
        
        private String value;
        
        private Location()
        {
            this.value = this.name().toLowerCase();
        }
        
        public String Value()
        {
            return this.value;
        }

        public static Location GetLocationForValue(String value)
        {
            for(Location loc : Location.values())
                if(value.equalsIgnoreCase(loc.Value()))
                    return loc;
            return null;
        }
    }
    
    public String name = null;
    public Location location = null;
    public boolean required = false;
    public boolean deprecated = false;
    
    public int pathPositionFromTheEnd = -1;
    
    //TODO: consider creating 2 parameter classes instead
    //      SimpleParameter -> has schema
    //      StructuredParameter -> has content
    public SchemaBuilder schema = null;
    // OR
    public MediaType content = null;
    
    public ParameterInstance Instantiate() throws Exception
    {
        return Instantiate(null);
    }
    
    public ParameterInstance Instantiate( String value) throws Exception
    {
        Schema s = schema.Build();
        if(content != null)
        {
            if(value != null){
                return new ParameterInstance(name, location, content.mediaType, s.Instantiate(null,value, content.mediaType), this);

            }
            else
                return new ParameterInstance(name, location, content.mediaType, s.Instantiate(null,null, content.mediaType), this);
        }
        else
        {
            if(value != null)
                return new ParameterInstance(name, location, s.Instantiate(null,value, null), this);
            else
                return new ParameterInstance(name, location, s.Instantiate(null,null, null), this);
        }
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parameter parameter = (Parameter) o;
        return required == parameter.required &&
                deprecated == parameter.deprecated &&
                Objects.equals(name, parameter.name) &&
                location == parameter.location &&
                Objects.equals(schema, parameter.schema) &&
                Objects.equals(content, parameter.content);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(name, location, required, deprecated, schema, content);
    }
}
