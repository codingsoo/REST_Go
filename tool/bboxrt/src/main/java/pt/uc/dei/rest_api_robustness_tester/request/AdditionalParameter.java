package pt.uc.dei.rest_api_robustness_tester.request;

import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import pt.uc.dei.rest_api_robustness_tester.Path;

public class AdditionalParameter
{
    public enum Scope {Global, Operation}
    
    private final String name;
    private final Parameter.Location location;
    private final String value;
    
    private Scope scope = Scope.Global;
    private Path.HttpMethod method = null;
    private String uri = null;
    
    public AdditionalParameter(String name, Parameter.Location location, String value)
    {
        this.name = name;
        this.location = location;
        this.value = value;
    }
    
    public String GetName()
    {
        return name;
    }
    
    public Parameter.Location GetLocation()
    {
        return location;
    }
    
    public String GetValue()
    {
        return value;
    }
    
    public AdditionalParameter ApplyToAll()
    {
        scope = Scope.Global;
        return this;
    }
    
    public boolean AppliesToAll()
    {
        return scope == Scope.Global;
    }
    
    public AdditionalParameter ApplyToOperation(Path.HttpMethod method, String uri)
    {
        scope = Scope.Operation;
        this.method = method;
        this.uri = uri;
        return this;
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
    
    public boolean AppliesToOperation()
    {
        return scope == Scope.Operation && uri != null && method != null;
    }
    
    public Scope GetFilterScope()
    {
        return scope;
    }
    
    public void Apply(RequestBuilder requestBuilder) throws Exception
    {
        switch(location)
        {
            case Cookie:
                System.err.println("Additional cookie parameters are not supported");
                break;
            case Header:
                requestBuilder.addHeader(name, value);
                break;
            case Path:
                System.err.println("Additional path parameters are not supported");
                break;
            case Query:
                requestBuilder.setUri(new URIBuilder(requestBuilder.getUri()).addParameter(name, value).build());
                break;
        }
    }
}
