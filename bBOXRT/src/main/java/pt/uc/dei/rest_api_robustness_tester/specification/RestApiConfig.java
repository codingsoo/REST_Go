package pt.uc.dei.rest_api_robustness_tester.specification;

import pt.uc.dei.rest_api_robustness_tester.Path;
import pt.uc.dei.rest_api_robustness_tester.request.AdditionalParameter;
import pt.uc.dei.rest_api_robustness_tester.request.Parameter;
import pt.uc.dei.rest_api_robustness_tester.workload.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RestApiConfig
{
    public enum WorkloadType {Generated, Loaded}
    
    private WorkloadType workloadType = WorkloadType.Generated;
    //TODO: this should probably be changed; there are some options:
    //      1 - load the workload directly from a file (e.g., text file?)
    //      2 - use a pre-built Workload object (which should would be created programmatically?)
    //      The 2nd alternative is not as attractive and would not work as well when the tool is released
    //      so maybe the 1st alternative is best - however, a robust implementation of the HTTP request parser
    //      would take some time to create
    private Workload loadedWorkload = null;
    
    private RateLimiter rateLimiter = null;
    
    //TODO: keys should be different for parameter and payload filters because
    //      different entries may result in the same key
    private Map<String, ParameterFilter> parameterFilters = new LinkedHashMap<>();
    private Map<String, PayloadFilter> payloadFilters = new LinkedHashMap<>();
    private Map<String, OperationFilter> operationFilters = new LinkedHashMap<>();
    
    private List<AdditionalParameter> additionalParameters = new ArrayList<>();
    
    public RestApiConfig GenerateWorkload()
    {
        workloadType = WorkloadType.Generated;
        return this;
    }
    
    public boolean ShouldGenerateWorkload()
    {
        return workloadType == WorkloadType.Generated;
    }
    
    public RestApiConfig LoadWorkload(Workload workload)
    {
        workloadType = WorkloadType.Loaded;
        loadedWorkload = workload;
        return this;
    }
    
    public boolean ShouldLoadWorkload()
    {
        return workloadType == WorkloadType.Loaded && loadedWorkload != null;
    }
    
    public WorkloadType GetWorkloadType()
    {
        return workloadType;
    }
    
    public RestApiConfig SetRateLimiter(RateLimiter rateLimiter)
    {
        this.rateLimiter = rateLimiter;
        return this;
    }
    
    public boolean HasRateLimiter()
    {
        return rateLimiter != null;
    }
    
    public RateLimiter GetRateLimiter()
    {
        return rateLimiter;
    }
    
    public RestApiConfig AddParameterFilter(ParameterFilter filter)
    {
        parameterFilters.put(KeyFromParameterFilter(filter.GetName(), filter.GetLocation(), filter.GetOperationMethod(),
                filter.GetOperationUri()), filter);
        return this;
    }
    
    public boolean HasParameterFilterFor(String name, Parameter.Location location, Path.HttpMethod method, String uri)
    {
        return parameterFilters.containsKey(KeyFromParameterFilter(name, location, method, uri)) ||
                parameterFilters.containsKey(KeyFromParameterFilter(name, location, null, null));
    }
    
    public ParameterFilter GetParameterFilterFor(String name, Parameter.Location location, Path.HttpMethod method, String uri)
    {
        if(parameterFilters.containsKey(KeyFromParameterFilter(name, location, method, uri)))
            return parameterFilters.get(KeyFromParameterFilter(name, location, method, uri));
        else
            return parameterFilters.get(KeyFromParameterFilter(name, location, null, null));
    }
    
    public boolean HasParameterFilters()
    {
        return !parameterFilters.isEmpty();
    }
    
    private String KeyFromParameterFilter(String name, Parameter.Location location, Path.HttpMethod method, String uri)
    {
        return name + ":" + location +":" + method + ":" + uri;
    }
    
    public RestApiConfig AddPayloadFilter(PayloadFilter filter)
    {
        payloadFilters.put(KeyFromPayloadFilter(filter.GetOperationMethod(), filter.GetOperationUri()), filter);
        return this;
    }
    
    public boolean HasPayloadFilterFor(Path.HttpMethod method, String uri)
    {
        return payloadFilters.containsKey(KeyFromPayloadFilter(method, uri));
    }
    
    public PayloadFilter GetPayloadFilterFor(Path.HttpMethod method, String uri)
    {
        return payloadFilters.get(KeyFromPayloadFilter(method, uri));
    }
    
    public boolean HasPayloadFilters()
    {
        return !payloadFilters.isEmpty();
    }
    
    private String KeyFromPayloadFilter(Path.HttpMethod method, String uri)
    {
        return method + ":" + uri;
    }
    
    public RestApiConfig AddOperationFilter(OperationFilter filter)
    {
        operationFilters.put(KeyFromOperationFilter(filter.GetMethod(), filter.GetUri()), filter);
        return this;
    }
    
    public boolean HasOperationFilterFor(Path.HttpMethod method, String uri)
    {
        return operationFilters.containsKey(KeyFromOperationFilter(method, uri));
    }
    
    public OperationFilter GetOperationFilterFor(Path.HttpMethod method, String uri)
    {
        return operationFilters.get(KeyFromOperationFilter(method, uri));
    }
    
    public boolean HasOperationFilters()
    {
        return !operationFilters.isEmpty();
    }
    
    private String KeyFromOperationFilter(Path.HttpMethod method, String uri)
    {
        return method + ":" + uri;
    }
    
    public RestApiConfig AddAdditionalParameter(AdditionalParameter parameter)
    {
        additionalParameters.add(parameter);
        return this;
    }
    
    public boolean HasAdditionalParametersFor(Path.HttpMethod method, String uri)
    {
        for(AdditionalParameter p : additionalParameters)
            if(p.AppliesToOperation() && p.OperationIsEqual(method, uri))
                return true;
        return false;
    }
    
    public List<AdditionalParameter> GetAdditionalParametersFor(Path.HttpMethod method, String uri)
    {
        List<AdditionalParameter> defaultParams = new ArrayList<>();
        for(AdditionalParameter p : additionalParameters)
            if(p.AppliesToOperation() && p.OperationIsEqual(method, uri))
                defaultParams.add(p);
        return defaultParams;
    }
    
    public boolean HasGlobalAdditionalParameters()
    {
        for(AdditionalParameter p : additionalParameters)
            if(p.AppliesToAll())
                return true;
        return false;
    }
    
    public List<AdditionalParameter> GetGlobalAdditionalParameters()
    {
        List<AdditionalParameter> defaultParams = new ArrayList<>();
        for(AdditionalParameter p : additionalParameters)
            if(p.AppliesToAll())
                defaultParams.add(p);
        return defaultParams;
    }
    
    public boolean HasAdditionalParameters()
    {
        return !additionalParameters.isEmpty();
    }
}
