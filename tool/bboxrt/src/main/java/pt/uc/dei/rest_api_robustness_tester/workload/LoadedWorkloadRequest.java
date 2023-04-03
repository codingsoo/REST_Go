package pt.uc.dei.rest_api_robustness_tester.workload;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import pt.uc.dei.rest_api_robustness_tester.Path;
import pt.uc.dei.rest_api_robustness_tester.Server;
import pt.uc.dei.rest_api_robustness_tester.request.Parameter;

import java.util.ArrayList;
import java.util.List;

public class LoadedWorkloadRequest extends WorkloadRequest
{
    //TODO: this does not support structured Parameters (i.e., with a media type)
    //Elements:         name,   location,           value
    private List<Triple<String, Parameter.Location, String>> parameters;
    //Elements:  media,  content
    private Pair<String, String> payload;
    
    public LoadedWorkloadRequest(Server server, Path.HttpMethod httpMethod, String endpoint, String operationID)
    {
        super(server, httpMethod, endpoint, operationID, new ArrayList<>());
        this.parameters = new ArrayList<>();
        this.payload = null;
    }
    
    public LoadedWorkloadRequest AddParameter(String name, Parameter.Location location, String value)
    {
        parameters.add(new ImmutableTriple<>(name, location, value));
        return this;
    }
    
    public LoadedWorkloadRequest SetPayload(String mediaType, String content)
    {
        payload = new ImmutablePair<>(mediaType, content);
        return this;
    }
    
    @Override
    public RequestBuilder Instantiate()
    {
        RequestBuilder requestBuilder = RequestBuilder.get();
        
        String uri = endpoint;
        
        switch(httpMethod)
        {
            case GET:
                requestBuilder = RequestBuilder.get();
                break;
            case PUT:
                requestBuilder = RequestBuilder.put();
                break;
            case POST:
                requestBuilder = RequestBuilder.post();
                break;
            case DELETE:
                requestBuilder = RequestBuilder.delete();
                break;
        }
        
        //TODO: parameters may have structured schema (i.e., content)
        if(!parameters.isEmpty())
        {
            //Path parameters should be taken care of first, so it is necessary to reorder the parameters list
            for(int i = 0; i < parameters.size(); i++)
                if(parameters.get(i).getMiddle() == Parameter.Location.Path)
                    parameters.add(0, parameters.remove(i));
            
            for(Triple<String, Parameter.Location, String> p : parameters)
            {
                switch(p.getMiddle())
                {
                    case Cookie:
                        //TODO: requires cookie store and cannot be directly used through RequestBuilder
                        System.err.println("Cookie parameters are not supported");
                        break;
                    case Header:
                        requestBuilder.addHeader(p.getLeft(), "" + p.getRight());
                        break;
                    case Path:
                        uri = uri.replace("{" + p.getLeft() + "}", "" + p.getRight());   // .replace(" ", "_");
                        break;
                    case Query:
                        try
                        {
                            uri = new URIBuilder(uri).addParameter(p.getLeft(), "" + p.getRight()).build().toString();
                        }
                        catch(Exception e) {e.printStackTrace();}
                        break;
                }
            }
        }
        
        if(payload != null)
        {
            String mediaType = payload.getLeft();
            String payloadContent = payload.getRight();
            
            StringEntity entity = new StringEntity(payloadContent, ContentType.getByMimeType(mediaType));
            requestBuilder.setEntity(entity);
        }
        
        requestBuilder.setUri(uri);
        
        return requestBuilder;
    }
}
