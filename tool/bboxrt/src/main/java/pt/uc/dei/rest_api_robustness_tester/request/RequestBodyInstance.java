package pt.uc.dei.rest_api_robustness_tester.request;

import pt.uc.dei.rest_api_robustness_tester.media.MediaType;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaInstance;

public class RequestBodyInstance
{
    private String mediaType;
    private SchemaInstance schema;
    private MediaType base;
    
    public RequestBodyInstance(String mediaType, SchemaInstance schema, MediaType base)
    {
        this.mediaType = mediaType;
        this.schema = schema;
        this.base = base;
    }


    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public void setSchema(SchemaInstance schema) { this.schema = schema; }

    public void setBase(MediaType base) { this.base = base; }

    public String MediaType() { return this.mediaType; }

    public SchemaInstance Schema() { return this.schema; }
    
    public MediaType Base()
    {
        return this.base;
    }
}
