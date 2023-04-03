package pt.uc.dei.rest_api_robustness_tester.workload;

import pt.uc.dei.rest_api_robustness_tester.Path;
import pt.uc.dei.rest_api_robustness_tester.request.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParameterFilter
{
    public enum Type {Ignore, SetValue}
    public enum Scope {Global, Operation}
    
    private final String name;
    private final Parameter.Location location;
    
    private Type type = Type.Ignore;
    private List<String> values = null;
    
    private Scope scope = Scope.Global;
    private Path.HttpMethod method = null;
    private String uri = null;
    
    public ParameterFilter(ParameterFilter parameterFilter)
    {
        this.name = parameterFilter.name;
        this.location = parameterFilter.location;
        this.type = parameterFilter.type;
        this.values = parameterFilter.values;
        this.scope = parameterFilter.scope;
        this.method = parameterFilter.method;
        this.uri = parameterFilter.uri;
    }
    
    public ParameterFilter(String name, Parameter.Location location)
    {
        this.name = name;
        this.location = location;
    }
    
    public String GetName()
    {
        return name;
    }
    
    public Parameter.Location GetLocation()
    {
        return location;
    }
    
    public ParameterFilter Ignore()
    {
        type = Type.Ignore;
        this.values = null;
        return this;
    }
    
    public boolean IsIgnore()
    {
        return type == Type.Ignore;
    }
    
    public ParameterFilter SetValue(String value)
    {
        type = Type.SetValue;
        this.values = new ArrayList<>();
        this.values.add(value);
        return this;
    }
    
    public ParameterFilter SetValues(String ... values)
    {
        type = Type.SetValue;
        this.values = new ArrayList<>(Arrays.asList(values));
        return this;
    }
    
    public ParameterFilter SetValues(List<String> values)
    {
        type = Type.SetValue;
        this.values = new ArrayList<>(values);
        return this;
    }
    
    public List<String> GetValues()
    {
        return values;
    }
    
    public boolean IsSetValue()
    {
        return type == Type.SetValue && values != null;
    }
    
    public Type GetFilterType()
    {
        return type;
    }
    
    public ParameterFilter ApplyToAll()
    {
        scope = Scope.Global;
        return this;
    }
    
    public boolean AppliesToAll()
    {
        return scope == Scope.Global;
    }
    
    public ParameterFilter ApplyToOperation(Path.HttpMethod method, String uri)
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
}
