package pt.uc.dei.rest_api_robustness_tester.workload;

import org.apache.commons.lang3.RandomUtils;
import pt.uc.dei.rest_api_robustness_tester.Operation;
import pt.uc.dei.rest_api_robustness_tester.Path;
import pt.uc.dei.rest_api_robustness_tester.specification.RestApi;
import pt.uc.dei.rest_api_robustness_tester.Server;
import pt.uc.dei.rest_api_robustness_tester.media.MediaType;
import pt.uc.dei.rest_api_robustness_tester.request.RequestBodyInstance;
import pt.uc.dei.rest_api_robustness_tester.response.Response;
import pt.uc.dei.rest_api_robustness_tester.response.StatusCode;
import pt.uc.dei.rest_api_robustness_tester.request.Parameter;
import pt.uc.dei.rest_api_robustness_tester.request.ParameterInstance;
import pt.uc.dei.rest_api_robustness_tester.media.FormatterManager;

import java.lang.reflect.Array;
import java.util.*;

public class WorkloadGenerator
{
    private final RestApi restAPI;
    private final WorkloadGeneratorConfig config;
    
    public WorkloadGenerator(RestApi restAPI, WorkloadGeneratorConfig config)
    {
        this.restAPI = restAPI;
        this.config = config;
    }
    
    public Workload Generate() throws Exception
    {
        WorkloadRequest.ResetID();
        
        List<WorkloadRequest> workloadRequests = new ArrayList<>();
        Map<String, Map<StatusCode, Response>> responsesPerOperationID = new HashMap<>();
        
        for(Path path : restAPI.Specification().paths)
        {
            for(Path.HttpMethod httpMethod : path.operations.keySet())
            {
                if(restAPI.Config().HasOperationFilterFor(httpMethod, path.uri))
                {
                    OperationFilter opFilter = restAPI.Config().GetOperationFilterFor(httpMethod, path.uri);
                    if (opFilter.IsIgnore())
                    {
                        System.out.println("[WorkloadGenerator] Filtered operation " + httpMethod + " " + path.uri +
                                " (Filter: " + opFilter.GetFilterType() + ")");
                        continue;
                    }
                }
                
                Operation operation = path.operations.get(httpMethod);
                
                List<ParameterInstance[]> parameters = new ArrayList<>();
                Map<String, RequestBodyInstance[]> payloads = new LinkedHashMap<>();
                
                if(operation.parameters != null && !operation.parameters.isEmpty())
                {
                    for(Parameter parameter : operation.parameters)
                    {
                        // if no filter exists in the API's java file then pfilter will be equal to null
                        ParameterFilter pFilter = restAPI.Config().GetParameterFilterFor(parameter.name,
                                parameter.location, httpMethod, path.uri);
                        List<ParameterInstance> defaultInstances = new ArrayList<>();
                        /*
                            this is type of filter is specify in the java file of the API
                            example: operations and parameters that revoke certain things,
                            if we want to avoid that then it will filter here
                        */
                        if(pFilter != null && pFilter.IsIgnore())
                        {
                            if (pFilter.AppliesToAll() ||
                                    (pFilter.AppliesToOperation() && pFilter.OperationIsEqual(httpMethod, path.uri)))
                            {
                                System.out.println("[WorkloadGenerator] Filtered " + parameter.location + " parameter "
                                        + parameter.name + " (Filter: " + pFilter.GetFilterType() + ", Scope: " +
                                        pFilter.GetFilterScope() + ")");
                                continue;
                            }
                        }
                        else if(pFilter != null && pFilter.IsSetValue())
                        {
                            if (pFilter.AppliesToAll() ||
                                    (pFilter.AppliesToOperation() && pFilter.OperationIsEqual(httpMethod, path.uri)))
                            {
                                System.out.println("[WorkloadGenerator] Filtered " + parameter.location + " parameter "
                                        + parameter.name + " (Filter: " + pFilter.GetFilterType() + ", Scope: " +
                                        pFilter.GetFilterScope() + ")");
                                for(String value : pFilter.GetValues())
                                    defaultInstances.add(parameter.Instantiate(value));
                            }
                        }
                        
                        ParameterInstance[] paramArray = new ParameterInstance[config.GetRepetitionsPerParameter()];
                        for (int i = 0; i < config.GetRepetitionsPerParameter(); i++)
                        {
                            if(!defaultInstances.isEmpty())
                                paramArray[i] = defaultInstances.get(RandomUtils.nextInt(0, defaultInstances.size()));
                            else
                                paramArray[i] = parameter.Instantiate();
                        }
                        parameters.add(paramArray);
                    }
                }
                
                if(operation.requestBody != null)
                {
                    int ignoreCount = 0;
                    for(MediaType mediaType : operation.requestBody.mediaTypes)
                    {
                        // mediaType.mediaType example: application/json
                        if(!FormatterManager.Instance().HasFormatterFor(mediaType.mediaType))
                        {
                            System.out.println("[WorkloadGenerator] Media type " + mediaType.mediaType + " required by " +
                                    "operation " + httpMethod + " " + path.uri + " is not supported - ignoring operation");
                            if(FormatterManager.MediaType.WILDCARD.Value().equals(mediaType.mediaType))
                                System.err.println("[WorkloadGenerator] Media type is a wildcard (" +
                                        FormatterManager.MediaType.WILDCARD.Value() + ") - is this the media type in the" +
                                        " API specification, or was it a convertion error?");
                            ignoreCount++;
                            continue;
                        }

                        // payloadFilter that is specify also in the java file for the API
                        PayloadFilter pFilter = restAPI.Config().GetPayloadFilterFor(httpMethod, path.uri);


                        List<RequestBodyInstance> defaultInstances = new ArrayList<>();
                        if (pFilter != null && pFilter.IsIgnore() && pFilter.OperationIsEqual(httpMethod, path.uri))
                        {
                            if (pFilter.AppliesToAnyMedia() ||
                                    (pFilter.AppliesToSpecificMedia() && pFilter.MediaTypeIsEqual(mediaType.mediaType)))
                            {
                                System.out.println("[WorkloadGenerator] Filtered payload of operation " + httpMethod +
                                        " " + path.uri +" (Filter: " + pFilter.GetFilterType() + ", Media: " +
                                        pFilter.GetMediaFilter() + ")");
                                ignoreCount++;
                                continue;
                            }
                        }
                        else if (pFilter != null && pFilter.IsSetValue() && pFilter.OperationIsEqual(httpMethod, path.uri))
                        {

                            if (pFilter.AppliesToAnyMedia() ||
                                    (pFilter.AppliesToSpecificMedia() && pFilter.MediaTypeIsEqual(mediaType.mediaType)))
                            {
                                System.out.println("[WorkloadGenerator] Filtered payload of operation " + httpMethod +
                                        " " + path.uri + " (Filter: " + pFilter.GetFilterType() + ", Media: " +
                                        pFilter.GetMediaFilter() + ")");


                                //added this [Carlos]
                                HashMap<String, List<String>> namesWithValues = pFilter.getNamesWithValues();
                                List<String> names =  new ArrayList<>(namesWithValues.keySet());
                                List<String> temp;
                                for(int i = 0 ; i < config.GetRepetitionsPerParameter(); i++){
                                    List<String> values = new ArrayList<>();
                                    for(String name : names){
                                        temp = namesWithValues.get(name);
                                        values.add( temp.get( RandomUtils.nextInt( 0, temp.size())));
                                    }
                                    defaultInstances.add(mediaType.InstantiateFiltering(names,values));
                                }

                                // original code
                                /*for(String value : pFilter.GetValues()){
                                    //defaultInstances.add(mediaType.Instantiate(null,value));
                                }*/

                            }
                        }
    
                        RequestBodyInstance[] payloadArray = new RequestBodyInstance[config.GetRepetitionsPerParameter()];
                        for(int i = 0; i < config.GetRepetitionsPerParameter(); i++)
                        {
                            if(!defaultInstances.isEmpty())
                                payloadArray[i] = defaultInstances.get(RandomUtils.nextInt(0, defaultInstances.size()));
                            else
                                payloadArray[i] = mediaType.Instantiate();
                        }
                        payloads.put(mediaType.mediaType, payloadArray);
                    }
                    if(ignoreCount == operation.requestBody.mediaTypes.size())
                        continue;
                }
                
                for(Server server : restAPI.Specification().servers)
                {
                    if(!parameters.isEmpty() && payloads.isEmpty())
                    {
                        for(int i = 0; i < config.GetRepetitionsPerParameter(); i++)
                        {
                            List<ParameterInstance> paramInst = new ArrayList<>();
                            for(ParameterInstance[] p : parameters)
                                paramInst.add(p[i]);
                            workloadRequests.add(new GeneratedWorkloadRequest(server, httpMethod, path.uri,
                                    operation.operationID, paramInst, operation.securityRequirements));
                        }
                    }
                    else if(parameters.isEmpty() && !payloads.isEmpty())
                    {
                        for(String mType : payloads.keySet())
                            for(RequestBodyInstance body : payloads.get(mType))
                                workloadRequests.add(new GeneratedWorkloadRequest(server, httpMethod, path.uri,
                                        operation.operationID, body, operation.securityRequirements));
                    }
                    else if(!parameters.isEmpty() && !payloads.isEmpty())
                    {
                        for(int i = 0; i < config.GetRepetitionsPerParameter(); i++)
                        {
                            List<ParameterInstance> paramInst = new ArrayList<>();
                            for(ParameterInstance[] p : parameters)
                                paramInst.add(p[i]);

                            for(String mType : payloads.keySet())
                                for(RequestBodyInstance body : payloads.get(mType))
                                    workloadRequests.add(new GeneratedWorkloadRequest(server, httpMethod, path.uri,
                                            operation.operationID, paramInst, body, operation.securityRequirements));
                        }
                    }
                    else if(parameters.isEmpty() && payloads.isEmpty())
                        workloadRequests.add(new GeneratedWorkloadRequest(server, httpMethod, path.uri,
                                operation.operationID, operation.securityRequirements));
                }
                
                responsesPerOperationID.put(operation.operationID, operation.responses);
            }
        }
        
        return new Workload(workloadRequests,
                responsesPerOperationID,
                restAPI.Specification().securityRequirements);
    }
}
