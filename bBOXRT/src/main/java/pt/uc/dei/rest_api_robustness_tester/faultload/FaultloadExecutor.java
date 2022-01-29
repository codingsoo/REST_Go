package pt.uc.dei.rest_api_robustness_tester.faultload;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import pt.uc.dei.rest_api_robustness_tester.Operation;
import pt.uc.dei.rest_api_robustness_tester.Path;
import pt.uc.dei.rest_api_robustness_tester.media.FormatterManager;
import pt.uc.dei.rest_api_robustness_tester.media.SchemaFormatter;
import pt.uc.dei.rest_api_robustness_tester.specification.RestApi;
import pt.uc.dei.rest_api_robustness_tester.Server;
import pt.uc.dei.rest_api_robustness_tester.faultload.faults.Fault;
import pt.uc.dei.rest_api_robustness_tester.faultload.faults.LimitedUseFault;
import pt.uc.dei.rest_api_robustness_tester.media.MediaType;
import pt.uc.dei.rest_api_robustness_tester.request.Parameter;
import pt.uc.dei.rest_api_robustness_tester.schema.*;
import pt.uc.dei.rest_api_robustness_tester.workload.OperationFilter;
import pt.uc.dei.rest_api_robustness_tester.workload.ParameterFilter;
import pt.uc.dei.rest_api_robustness_tester.workload.PayloadFilter;

import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

//TODO: when missing an API's specification file, the faultload executor should also be able to
//      infer data types and dynamically build a view of the API by analyzing incoming requests
public class FaultloadExecutor
{
    private final Faultload faultload;
    private final RestApi restAPI;
    private final FaultloadExecutorConfig config;
    private final List<FaultloadRequest> faultloadRequests;
    
    public FaultloadExecutor(Faultload faultload, RestApi restAPI, FaultloadExecutorConfig config)
    {
        this.faultload = faultload;
        this.restAPI = restAPI;
        this.config = config;
        
        this.faultloadRequests = new ArrayList<>();
        MapFaultloadToApiSpec();
    }
    
    //Returns true if a fault was injected, false otherwise
    public boolean Execute(RequestBuilder requestBuilder)
    {
        FaultloadRequest faultloadRequest = FindFaultloadRequest(requestBuilder);
        if (faultloadRequest != null)
            return faultloadRequest.Handle(requestBuilder, config);
        else
            //TODO: when a request signature is not recognized, the Executor should be able to dynamically
            //      build a view of the API (e.g., infer data types, distinguish between operations)
            System.err.println("Request signature not found in API Spec (URI: " + requestBuilder.getUri() + ")");
        
        return false;
    }
    
    public int FaultsLeft()
    {
        int faultsLeft = 0;
        
        for(FaultloadRequest req : faultloadRequests)
            faultsLeft += req.FaultsLeft();
        
        return faultsLeft;
    }
    
    public Map<String, Integer> FaultsLeftPerOperation()
    {
        Map<String, Integer> map = new LinkedHashMap<>();
        
        for(FaultloadRequest req : faultloadRequests)
        {
            if(!map.containsKey(req.OperationID()))
                map.put(req.OperationID(), 0);
            map.put(req.OperationID(), map.get(req.OperationID()) + req.FaultsLeft());
        }
        
        return map;
    }
    
    private void MapFaultloadToApiSpec()
    {
        for (Path path : restAPI.Specification().paths)
        {
            for (Path.HttpMethod httpMethod : path.operations.keySet())
            {
                if(restAPI.Config().HasOperationFilterFor(httpMethod, path.uri))
                {
                    OperationFilter opFilter = restAPI.Config().GetOperationFilterFor(httpMethod, path.uri);
                    if (opFilter.IsIgnore())
                    {
                        System.out.println("[FaultloadExecutor] Filtered operation " + httpMethod + " " + path.uri +
                                " (Filter: " + opFilter.GetFilterType() + ")");
                        continue;
                    }
                }
                
                Operation operation = path.operations.get(httpMethod);
                
                List<SimpleParameter> simpleParameters = null;
                List<StructuredParameter> structuredParameters = null;
                if(!operation.parameters.isEmpty())
                {
                    for(Parameter parameter : operation.parameters)
                    {
                        ParameterFilter pFilter = restAPI.Config().GetParameterFilterFor(parameter.name,
                                parameter.location, httpMethod, path.uri);
                        if(pFilter != null && pFilter.IsIgnore())
                        {
                            if (pFilter.AppliesToAll() ||
                                    (pFilter.AppliesToOperation() && pFilter.OperationIsEqual(httpMethod, path.uri)))
                            {
                                System.out.println("[FaultloadExecutor] Filtered " + parameter.location + " parameter "
                                        + parameter.name + " (Filter: " + pFilter.GetFilterType() + ", Scope: " +
                                        pFilter.GetFilterScope() + ")");
                                continue;
                            }
                        }
                        
                        if(parameter.schema != null)
                        {
                            if(simpleParameters == null)
                                simpleParameters = new ArrayList<>();
                            
                            SchemaBuilder schema = parameter.schema;
                            List<Fault> faults = faultload.GetApplicableFaults(schema);
                            if(!faults.isEmpty())
                                simpleParameters.add(new SimpleParameter(parameter, faults, config));
                        }
                        //TODO: implement structured request parameters
                        else if(parameter.content != null)
                        {
                            System.err.println("Structured request parameters are not yet implemented");
//                            if(structuredParameters == null)
//                                structuredParameters = new ArrayList<>();
//
//                            MediaType mediaType = parameter.content;
//                            SchemaBuilder schema = mediaType.schema;
//                            Map<String, SchemaBuilder> schemaElements = schema.GetAllElements();
//
//                            Map<String, List<Fault>> applicableFaults = new HashMap<>();
//                            for (String schemaName : schemaElements.keySet())
//                            {
//                                List<Fault> faults = faultload.GetApplicableFaults(schemaElements.get(schemaName));
//                                if(!faults.isEmpty())
//                                    applicableFaults.put(schemaName, faults);
//                            }
//
//                            structuredParameters.add(new StructuredParameter(parameter, applicableFaults, config));
                        }
                    }
                }
                
                if (operation.requestBody != null && !operation.requestBody.mediaTypes.isEmpty())
                {
                    for (MediaType mediaType : operation.requestBody.mediaTypes)
                    {
                        if(!FormatterManager.Instance().HasFormatterFor(mediaType.mediaType))
                        {
                            System.out.println("[FaultloadExecutor] Media type " + mediaType.mediaType + " required by " +
                                    "operation " + httpMethod + " " + path.uri + " is not supported - ignoring operation");
                            if(FormatterManager.MediaType.WILDCARD.Value().equals(mediaType.mediaType))
                                System.err.println("[FaultloadExecutor] Media type is a wildcard (" +
                                        FormatterManager.MediaType.WILDCARD.Value() + ") - is this the media type in the" +
                                        " API specification, or was it a convertion error?");
                            continue;
                        }
                        
                        PayloadFilter pFilter = restAPI.Config().GetPayloadFilterFor(httpMethod, path.uri);
                        if (pFilter != null && pFilter.IsIgnore() && pFilter.OperationIsEqual(httpMethod, path.uri))
                        {
                            if (pFilter.AppliesToAnyMedia() ||
                                    (pFilter.AppliesToSpecificMedia() && pFilter.MediaTypeIsEqual(mediaType.mediaType)))
                            {
                                System.out.println("[FaultloadExecutor] Filtered payload of operation " + httpMethod +
                                        " " + path.uri +" (Filter: " + pFilter.GetFilterType() + ", Media: " +
                                        pFilter.GetMediaFilter() + ")");
                                continue;
                            }
                        }
                        
                        SchemaBuilder schema = mediaType.GetSchema();
                        Map<String, SchemaBuilder> schemaElements = schema.GetAllElements();
                        
                        Map<String, List<Fault>> applicableFaults = new HashMap<>();
                        for (String schemaName : schemaElements.keySet())
                        {
                            List<Fault> faults = faultload.GetApplicableFaults(schemaElements.get(schemaName));
                            if(!faults.isEmpty())
                                applicableFaults.put(schemaName, faults);
                        }
                        
                        Payload payload = new Payload(mediaType, applicableFaults, config);
    
                        faultloadRequests.add(new FaultloadRequest(path, httpMethod, simpleParameters, structuredParameters, payload));
                    }
                }
                else
                    faultloadRequests.add(new FaultloadRequest(path, httpMethod, simpleParameters, structuredParameters, null));
            }
        }
    }
    
    private FaultloadRequest FindFaultloadRequest(RequestBuilder requestBuilder)
    {
        try
        {
            String uri = requestBuilder.getUri().getPath();
            for (Server server : restAPI.Specification().servers)
                if(uri.startsWith(server.GetRemainder()) && !server.GetRemainder().equals(Server.PATH_SEP))
                    uri = uri.substring(server.GetRemainder().length());
            
            Path.HttpMethod method = Path.HttpMethod.GetMethodForValue(requestBuilder.getMethod());
            
            Path path = restAPI.Specification().GetPath(new URI(uri).getPath(), method);
            
            Operation operation = path.GetOperation(method);
            
            MediaType mediaType = null;
            if(requestBuilder.getEntity() != null)
            {
                String mediaTypeType = requestBuilder.getEntity().getContentType().getValue();
                if (FormatterManager.Instance().HasFormatterFor(mediaTypeType))
                    mediaType = operation.requestBody.GetMediaType(mediaTypeType);
            }
            
            for (FaultloadRequest faultloadRequest : faultloadRequests)
            {
                boolean result;
                if(mediaType == null)
                    result = faultloadRequest.Matches(path, method);
                else
                    result = faultloadRequest.Matches(path, method, mediaType);
                
                if(result)
                    return faultloadRequest;
            }
    
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
}

interface RequestHandler
{
    boolean CanHandleNewFaultInjection();
    int FaultsLeft();
    boolean Handle(RequestBuilder requestBuilder, FaultloadExecutorConfig config);
}

abstract class ParameterHandler
{
    String GetParameterValue(String name, Parameter parameter, RequestBuilder requestBuilder)
    {
        String value = null;
        switch(parameter.location)
        {
            case Header:
            {
                Header[] headers = requestBuilder.getHeaders(name);
                if(headers.length == 1)
                    value = headers[0].getValue();
                else if(headers.length > 1)
                {
                    StringJoiner stringJoiner = new StringJoiner(", ");
                    for(Header h : headers)
                        stringJoiner.add(h.getValue());
                    
                    value = stringJoiner.toString();
                }
                break;
            }
            case Cookie:
                //TODO: not yet supported
                System.err.println("Cookie parameter parsing is not supported");
                break;
            case Query:
            {
                URIBuilder uriBuilder = new URIBuilder(requestBuilder.getUri());
                List<NameValuePair> queryParams = uriBuilder.getQueryParams();
                Map<String, List<String>> keyValues = new HashMap<>();
                queryParams.forEach(pair -> {
                    if(!keyValues.containsKey(pair.getName()))
                        keyValues.put(pair.getName(), new ArrayList<>());
                    keyValues.get(pair.getName()).add(pair.getValue());
                });
                if(keyValues.containsKey(name))
                {
                    List<String> queryParameter = keyValues.get(name);
                    if (queryParameter.size() == 1)
                        value = queryParameter.get(0);
                    else if (queryParameter.size() > 1)
                        value = queryParameter.toString();
                }
                break;
            }
            case Path:
            {
                String uri = requestBuilder.getUri().toString();
                String[] uriSplit = uri.split(Server.PATH_SEP);
                value = uriSplit[uriSplit.length - 1 - parameter.pathPositionFromTheEnd];
                break;
            }
        }
        
        return value;
    }
    
    void SetParameterValue(String name, String value, String newValue, Parameter parameter, RequestBuilder requestBuilder)
            throws Exception
    {
        switch(parameter.location)
        {
            case Header:
                //FIXME: replace with null
                if(newValue.equals("ReplaceWithNull"))
                    requestBuilder.removeHeaders(name);
                else
                    requestBuilder.removeHeaders(name).
                            setHeader(name, newValue);
                break;
            case Cookie:
                //TODO: not yet supported
                System.err.println("Cookie parameter replacing is not supported");
                break;
            case Query:
            {
                URIBuilder uriBuilder = new URIBuilder(requestBuilder.getUri());
                //FIXME: replace with null
                if(newValue.equals("ReplaceWithNull"))
                {
                    List<NameValuePair> params = uriBuilder.getQueryParams();
                    uriBuilder.clearParameters();
                    for(NameValuePair p : params)
                        if(!p.getName().equals(name))
                            uriBuilder.addParameter(p.getName(), p.getValue());
                }
                else
                    uriBuilder.setParameter(name, newValue);
                requestBuilder.setUri(uriBuilder.build());
                break;
            }
            case Path:
            {
                String uri = requestBuilder.getUri().toString();
                //FIXME: replace with null
                if(newValue.equals("ReplaceWithNull"))
                    uri = uri.replace(value, URLEncoder.encode("", "UTF-8"));
                else
                    uri = uri.replace(value, URLEncoder.encode(newValue, "UTF-8"));
                requestBuilder.setUri(uri);
                break;
            }
        }
    }
}

class SimpleParameter extends ParameterHandler implements RequestHandler
{
    private final String name;
    private final Parameter parameter;
    private final SchemaBuilder schema;
    private final List<LimitedUseFault> applicableFaults;
    
    SimpleParameter(Parameter parameter, List<Fault> applicableFaults, FaultloadExecutorConfig config)
    {
        this.name = parameter.name;
        this.parameter = parameter;
        this.schema = parameter.schema;
        this.applicableFaults = new ArrayList<>();
        applicableFaults.forEach(f -> {this.applicableFaults.add(new LimitedUseFault(f, config.GetMaxInjectionsPerFault()));});
    }
    
    @Override
    public boolean CanHandleNewFaultInjection()
    {
        return !applicableFaults.isEmpty();
    }
    
    @Override
    public int FaultsLeft()
    {
        return applicableFaults.size();
    }
    
    @Override
    public boolean Handle(RequestBuilder requestBuilder, FaultloadExecutorConfig config)
    {
        Fault targetFault;
        if(applicableFaults.get(0).UsesLeft() > 1)
            targetFault = applicableFaults.get(0).Use();
        else
            targetFault = applicableFaults.remove(0).Use();
    
        System.out.println("Location: " + parameter.location);
        System.out.println("Parameter: " + this.name);
        System.out.println("Fault: " + targetFault.FaultName());
    
        config.GetWriter().Add("Location", "" + parameter.location).
                Add("Parameter", this.name).
                Add("Fault", targetFault.FaultName());
        
        String value = GetParameterValue(this.name, parameter, requestBuilder);
        String newValue = null;
        if(value != null)
        {
            newValue = targetFault.Inject(value, schema);
    
            System.out.println("Original input: " + value);
            System.out.println("Faulty input: " + newValue);
    
            config.GetWriter().Add("Original input", value).
                    Add("Faulty input", newValue);
    
            try
            {
                SetParameterValue(this.name, value, newValue, parameter, requestBuilder);
                return true;
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println(parameter.location + " parameter \"" + this.name + "\" could not be found");
    
            config.GetWriter().Add("Original input", "Error: parameter could not be found").
                    Add("Faulty input", "Error: parameter could not be found");
        }
        
        return false;
    }
}

class StructuredParameter extends ParameterHandler implements RequestHandler
{
    private final String name;
    private final Parameter parameter;
    private final String mediaType;
    private final SchemaBuilder schema;
    private final Map<String, List<LimitedUseFault>> applicableFaults;
    
    StructuredParameter(Parameter parameter, Map<String, List<Fault>> applicableFaults, FaultloadExecutorConfig config)
    {
        this.name = parameter.name;
        this.parameter = parameter;
        this.mediaType = parameter.content.mediaType;
        this.schema = parameter.content.schema;
        this.applicableFaults = new HashMap<>();
        applicableFaults.forEach((name, faults) -> {
            this.applicableFaults.put(name, new ArrayList<>());
            applicableFaults.get(name).forEach(f ->
            {
                this.applicableFaults.get(name).add(new LimitedUseFault(f, config.GetMaxInjectionsPerFault()));
            });
        });
    }
    
    @Override
    public boolean CanHandleNewFaultInjection()
    {
        return !applicableFaults.isEmpty();
    }
    
    @Override
    public int FaultsLeft()
    {
        int faultsLeft = 0;
        
        for(String name : applicableFaults.keySet())
            faultsLeft += applicableFaults.get(name).size();
        
        return faultsLeft;
    }
    
    @Override
    public boolean Handle(RequestBuilder requestBuilder, FaultloadExecutorConfig config)
    {
        String targetParameter = new ArrayList<>(applicableFaults.keySet()).get(0);
        Fault targetFault;
        if(applicableFaults.get(targetParameter).get(0).UsesLeft() > 1)
            targetFault = applicableFaults.get(targetParameter).get(0).Use();
        else
            targetFault = applicableFaults.remove(targetParameter).get(0).Use();
        
        if(applicableFaults.get(targetParameter).isEmpty())
            applicableFaults.remove(targetParameter);
    
        System.out.println("Location: " + parameter.location);
        System.out.println("Parameter: " + this.name);
        System.out.println("Fault: " + targetFault.FaultName());
    
        config.GetWriter().Add("Location", "" + parameter.location).
                Add("Parameter", this.name).
                Add("Fault", targetFault.FaultName());
        
        String value = GetParameterValue(this.name, parameter, requestBuilder);
        String newValue = null;
        if(value != null)
        {
            //TODO: implement
            System.err.println("Structured parameter handling not yet implemented");
        }
        else
        {
            System.out.println(parameter.location + " parameter \"" + this.name + "\" could not be found");
    
            config.GetWriter().Add("Original input", "Error: parameter could not be found").
                    Add("Faulty input", "Error: parameter could not be found");
        }
        
        return false;
    }
}

class Payload implements RequestHandler
{
    private final String mediaType;
    private final SchemaBuilder schema;
    private final Map<String, List<LimitedUseFault>> applicableFaults;
    
    Payload(MediaType mediaType, Map<String, List<Fault>> applicableFaults, FaultloadExecutorConfig config)
    {
        this.mediaType = mediaType.mediaType;
        this.schema = mediaType.schema;
        this.applicableFaults = new HashMap<>();
        applicableFaults.forEach((name, faults) -> {
            this.applicableFaults.put(name, new ArrayList<>());
            applicableFaults.get(name).forEach(f ->
            {
                this.applicableFaults.get(name).add(new LimitedUseFault(f, config.GetMaxInjectionsPerFault()));
            });
        });
    }
    
    String GetMediaType()
    {
        return this.mediaType;
    }
    
    @Override
    public boolean CanHandleNewFaultInjection()
    {
        return !applicableFaults.isEmpty();
    }
    
    @Override
    public int FaultsLeft()
    {
        int faultsLeft = 0;
    
        for(String name : applicableFaults.keySet())
            faultsLeft += applicableFaults.get(name).size();
    
        return faultsLeft;
    }
    
    @Override
    public boolean Handle(RequestBuilder requestBuilder, FaultloadExecutorConfig config)
    {
        try
        {
            String targetParameter = new ArrayList<>(applicableFaults.keySet()).get(0);
            Fault targetFault;
            if(applicableFaults.get(targetParameter).get(0).UsesLeft() > 1)
                targetFault = applicableFaults.get(targetParameter).get(0).Use();
            else
                targetFault = applicableFaults.get(targetParameter).remove(0).Use();
            
            if(applicableFaults.get(targetParameter).isEmpty())
                applicableFaults.remove(targetParameter);
            
            if(requestBuilder.getEntity() != null && requestBuilder.getEntity().getContentType() != null)
            {
                Class<? extends Schema> c;
                if(TypeManager.Instance().HasFormat(schema.format))
                    c = TypeManager.Instance().GetFormat(schema.format);
                else
                    c = TypeManager.Instance().GetType(schema.type);
                
                String mediaTypeType = requestBuilder.getEntity().getContentType().getValue();
                SchemaFormatter formatter = FormatterManager.Instance().GetFormatter(mediaTypeType, c);
    
                BufferedHttpEntity entity = new BufferedHttpEntity(requestBuilder.getEntity());
                String content = new String(IOUtils.toByteArray(entity.getContent()));
                
                String value;
                //anonymous Schema
                if(targetParameter.equals(SchemaBuilder.NAME_SEP))
                    value = content;
                else
                {
                    if (targetParameter.contains(SchemaBuilder.NAME_SEP))
                        value = formatter.GetElementValue(content, targetParameter.split(SchemaBuilder.NAME_SEP));
                    else
                        value = formatter.GetElementValue(content, targetParameter);
                }
                
                System.out.println("Location: " + "Payload");
                String targetParameterPretty = targetParameter.replace(SchemaBuilder.NAME_SEP, ".");
                System.out.println("Parameter: " + targetParameterPretty);
                System.out.println("Fault: " + targetFault.FaultName());
                String newValue;
                if (targetParameter.contains(SchemaBuilder.NAME_SEP))
                    newValue = targetFault.Inject(value, schema.GetAllElements().get(targetParameter));
                else
                    newValue = targetFault.Inject(value, schema);
    
                String newContent;
                //anonymous Schema
                if(targetParameter.equals(SchemaBuilder.NAME_SEP))
                    newContent = newValue;
                else
                {
                    if (targetParameter.contains(SchemaBuilder.NAME_SEP))
                        newContent = formatter.SetElementValue(content, newValue, targetParameter.split(SchemaBuilder.NAME_SEP));
                    else
                        newContent = formatter.SetElementValue(content, newValue, targetParameter);
                }
    
                System.out.println("Original input: " + content);
                System.out.println("Faulty input: " + newContent);
    
                requestBuilder.setEntity(new StringEntity(newContent, ContentType.getByMimeType(mediaTypeType)));
                
                config.GetWriter().Add("Location", "Payload").
                        Add("Parameter", targetParameterPretty).
                        Add("Fault", targetFault.FaultName()).
                        Add("Original input", value).
                        Add("Faulty input", newValue);
            }
            
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return false;
    }
}

class FaultloadRequest implements RequestHandler
{
    private final Path path;
    private final Path.HttpMethod httpMethod;
    private final List<SimpleParameter> simpleParameters;
    private final List<StructuredParameter> structuredParameters;
    private final Payload payload;
    
    FaultloadRequest(Path path, Path.HttpMethod httpMethod, List<SimpleParameter> simpleParameters,
                            List<StructuredParameter> structuredParameters, Payload payload)
    {
        this.path = path;
        this.httpMethod = httpMethod;
        this.simpleParameters = simpleParameters;
        this.structuredParameters = structuredParameters;
        this.payload = payload;
    }
    
    boolean Matches(Path path, Path.HttpMethod httpMethod)
    {
        return this.path.equals(path) && this.httpMethod.equals(httpMethod);
    }
    
    boolean Matches(Path path, Path.HttpMethod httpMethod, MediaType payloadMediaType)
    {
        return this.path.equals(path) && this.httpMethod.equals(httpMethod) &&
                payload.GetMediaType().equals(payloadMediaType.mediaType);
    }
    
    public int FaultsLeft()
    {
        int faultsLeft = 0;
    
        if(simpleParameters != null)
            for (SimpleParameter p : simpleParameters)
                faultsLeft += p.FaultsLeft();
    
        if(structuredParameters != null)
            for (StructuredParameter p : structuredParameters)
                faultsLeft += p.FaultsLeft();
    
        if(payload != null)
            faultsLeft += payload.FaultsLeft();
        
        return faultsLeft;
    }
    
    public String OperationID()
    {
        return this.path.GetOperation(this.httpMethod).operationID;
    }
    
    @Override
    public boolean CanHandleNewFaultInjection()
    {
        return true;
    }
    
    @Override
    public boolean Handle(RequestBuilder requestBuilder, FaultloadExecutorConfig config)
    {
        if(simpleParameters != null)
            for (SimpleParameter p : simpleParameters)
                if (p.CanHandleNewFaultInjection())
                    return p.Handle(requestBuilder, config);
    
        if(structuredParameters != null)
            for (StructuredParameter p : structuredParameters)
                if (p.CanHandleNewFaultInjection())
                    return p.Handle(requestBuilder, config);
        
        if(payload != null)
            if (payload.CanHandleNewFaultInjection())
                return payload.Handle(requestBuilder, config);
        
        return false;
    }
}
