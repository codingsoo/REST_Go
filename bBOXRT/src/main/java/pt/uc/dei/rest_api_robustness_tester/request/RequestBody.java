package pt.uc.dei.rest_api_robustness_tester.request;

import pt.uc.dei.rest_api_robustness_tester.media.MediaType;

import java.util.ArrayList;
import java.util.List;

public class RequestBody
{
    public boolean required = false;
    
    public List<MediaType> mediaTypes = new ArrayList<>();
    
    public MediaType GetMediaType(String mediaType)
    {
        for(MediaType t : mediaTypes)
            if(t.mediaType.equals(mediaType))
                return t;
        
        return null;
    }
}
