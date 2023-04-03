package pt.uc.dei.rest_api_robustness_tester.workload;

import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import pt.uc.dei.rest_api_robustness_tester.Path;
import pt.uc.dei.rest_api_robustness_tester.Server;
import pt.uc.dei.rest_api_robustness_tester.request.Parameter;
import pt.uc.dei.rest_api_robustness_tester.request.ParameterInstance;
import pt.uc.dei.rest_api_robustness_tester.request.RequestBodyInstance;
import pt.uc.dei.rest_api_robustness_tester.security.SecurityRequirement;

import java.net.URLEncoder;
import java.util.List;

public class GeneratedWorkloadRequest extends WorkloadRequest
{
    private List<ParameterInstance> parameters;
    private RequestBodyInstance requestBody;


    public GeneratedWorkloadRequest(Server server, Path.HttpMethod httpMethod, String endpoint, String operationID,
                                    List<SecurityRequirement> securityRequirements)
    {
        this(server, httpMethod, endpoint, operationID, null, null, securityRequirements);
    }
    public GeneratedWorkloadRequest(Server server, Path.HttpMethod httpMethod, String endpoint, String operationID,
                                    List<ParameterInstance> parameters, List<SecurityRequirement> securityRequirements)
    {
        this(server, httpMethod, endpoint, operationID, parameters, null, securityRequirements);
    }
    public GeneratedWorkloadRequest(Server server, Path.HttpMethod httpMethod, String endpoint, String operationID,
                           RequestBodyInstance requestBody, List<SecurityRequirement> securityRequirements)
    {
        this(server, httpMethod, endpoint, operationID, null, requestBody, securityRequirements);
    }
    public GeneratedWorkloadRequest(Server server, Path.HttpMethod httpMethod, String endpoint, String operationID,
                           List<ParameterInstance> parameters, RequestBodyInstance requestBody,
                           List<SecurityRequirement> securityRequirements)
    {
        super(server, httpMethod, endpoint, operationID, securityRequirements);
        this.parameters = parameters;
        this.requestBody = requestBody;
    }

    public List<ParameterInstance> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterInstance> parameters) {
        this.parameters = parameters;
    }

    public RequestBodyInstance getRequestBody() {
        return requestBody;
    }

    public boolean hasRequestBody() {
        return this.requestBody != null;
    }

    public boolean hasParameters() {
        return this.parameters != null;
    }


    public void setRequestBody(RequestBodyInstance requestBody) {
        this.requestBody = requestBody;
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
        if(parameters != null)
        {
            //Path parameters should be taken care of first, so it is necessary to reorder the parameters list
            for(int i = 0; i < parameters.size(); i++)
                if(parameters.get(i).Location() == Parameter.Location.Path)
                    parameters.add(0, parameters.remove(i));
            
            for(ParameterInstance p : parameters)
            {
                switch(p.Location())
                {
                    case Cookie:
                        //TODO: requires cookie store and cannot be directly used through RequestBuilder
                        System.err.println("Cookie parameters are not supported");
                        break;
                    case Header:
                        requestBuilder.addHeader(p.Name(), "" + p.Schema().Value());
                        break;
                    case Path:
                        try
                        {
                            uri = uri.replace("{" + p.Name() + "}",
                                    URLEncoder.encode("" + p.Schema().Value(), "UTF-8"));
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                        break;
                    case Query:
                        try
                        {
                            uri = new URIBuilder(uri).addParameter(p.Name(), "" + p.Schema().Value()).build().toString();
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }
        
        if(requestBody != null)
        {
            String requestBodyContent = requestBody.Schema().Value();
            
            StringEntity entity = new StringEntity(requestBodyContent, ContentType.getByMimeType(requestBody.MediaType()));
            requestBuilder.setEntity(entity);
        }
        //This is weird, but required for some APIs with bodiless POST and PUT requests
        else
            requestBuilder.setHeader(HTTP.CONTENT_LEN, "0");
        
        requestBuilder.setUri(uri);
        
        return requestBuilder;
    }
}
