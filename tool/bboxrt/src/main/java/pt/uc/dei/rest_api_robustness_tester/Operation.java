package pt.uc.dei.rest_api_robustness_tester;

import pt.uc.dei.rest_api_robustness_tester.request.Parameter;
import pt.uc.dei.rest_api_robustness_tester.request.RequestBody;
import pt.uc.dei.rest_api_robustness_tester.response.Response;
import pt.uc.dei.rest_api_robustness_tester.response.StatusCode;
import pt.uc.dei.rest_api_robustness_tester.security.SecurityRequirement;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Operation
{
    public String operationID = null;
    
    public boolean deprecated = false;
    
    public List<Parameter> parameters = new ArrayList<>();
    
    public RequestBody requestBody = null;
    
    public Map<StatusCode, Response> responses = new HashMap<>();
    
    public List<SecurityRequirement> securityRequirements = new ArrayList<>();
    
    public List<Server> servers = new ArrayList<>();
}
