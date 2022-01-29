package pt.uc.dei.rest_api_robustness_tester.response;

import pt.uc.dei.rest_api_robustness_tester.media.MediaType;

import java.util.ArrayList;
import java.util.List;

public class Response
{
    public String description = null;
    public List<MediaType> mediaTypes = new ArrayList<>();
}
