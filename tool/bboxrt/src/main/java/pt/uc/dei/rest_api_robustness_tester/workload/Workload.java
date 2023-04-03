package pt.uc.dei.rest_api_robustness_tester.workload;

import pt.uc.dei.rest_api_robustness_tester.response.Response;
import pt.uc.dei.rest_api_robustness_tester.response.StatusCode;
import pt.uc.dei.rest_api_robustness_tester.security.SecurityRequirement;

import java.util.List;
import java.util.Map;

public class Workload
{
    private List<WorkloadRequest> workloadRequests;

    // information that is given in the openAPI schema
    private final Map<String, Map<StatusCode, Response>> responsesPerOperationID;
    private final List<SecurityRequirement> securityRequirements;

    public Workload(List<WorkloadRequest> workloadRequests, Map<String, Map<StatusCode, Response>> responsesPerOperationID,
                    List<SecurityRequirement> securityRequirements)
    {
        this.workloadRequests = workloadRequests;
        this.responsesPerOperationID = responsesPerOperationID;
        this.securityRequirements = securityRequirements;
    }
    
    public List<WorkloadRequest> WorkloadRequests()
    {
        return this.workloadRequests;
    }

    public void setWorkloadRequests(List<WorkloadRequest> workloadRequests) {
        this.workloadRequests = workloadRequests;
    }

    public List<WorkloadRequest> getWorkloadRequests() {
        return workloadRequests;
    }

    public Map<String, Map<StatusCode, Response>> AllResponses()
    {
        return this.responsesPerOperationID;
    }
    
    public Response Response(String operationID, StatusCode statusCode)
    {
        if(responsesPerOperationID.containsKey(operationID))
            if(responsesPerOperationID.get(operationID).containsKey(statusCode))
                return responsesPerOperationID.get(operationID).get(statusCode);
        
        return null;
    }

    public Map<String, Map<StatusCode, Response>> getResponsesPerOperationID() {
        return responsesPerOperationID;
    }

    public List<SecurityRequirement> getSecurityRequirements() {
        return securityRequirements;
    }

    public boolean HasResponse(String operationID, StatusCode statusCode)
    {
        return Response(operationID, statusCode) != null;
    }
    
    public List<SecurityRequirement> SecurityRequirements()
    {
        return this.securityRequirements;
    }
}
