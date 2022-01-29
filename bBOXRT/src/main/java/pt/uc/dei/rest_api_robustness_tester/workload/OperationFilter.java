package pt.uc.dei.rest_api_robustness_tester.workload;

import pt.uc.dei.rest_api_robustness_tester.Path;

public class OperationFilter
{
    public enum Type {Ignore}
    
    private Type type = Type.Ignore;
    
    private final Path.HttpMethod method;
    private final String uri;
    
    public OperationFilter(Path.HttpMethod method, String uri)
    {
        this.method = method;
        this.uri = uri;
    }
    
    public String GetUri()
    {
        return uri;
    }
    
    public Path.HttpMethod GetMethod()
    {
        return method;
    }
    
    public OperationFilter Ignore()
    {
        type = Type.Ignore;
        return this;
    }
    
    public boolean IsIgnore()
    {
        return type == Type.Ignore;
    }
    
    public Type GetFilterType()
    {
        return type;
    }
}
