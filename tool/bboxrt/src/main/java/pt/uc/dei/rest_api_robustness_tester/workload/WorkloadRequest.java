package pt.uc.dei.rest_api_robustness_tester.workload;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.http.client.methods.RequestBuilder;
import pt.uc.dei.rest_api_robustness_tester.Path;
import pt.uc.dei.rest_api_robustness_tester.Server;
import pt.uc.dei.rest_api_robustness_tester.response.ResponseInRequest;
import pt.uc.dei.rest_api_robustness_tester.response.StatusCode;
import pt.uc.dei.rest_api_robustness_tester.security.SecurityRequirement;

public abstract class WorkloadRequest
{
    public enum Result {Pass, Fail, Exception}

    private static int ID_counter = 1;

    public  int id;
    public  Server server;
    public  Path.HttpMethod httpMethod;
    public  String endpoint;
    public  String operationID;
    public  List<SecurityRequirement> securityRequirements;

    private List<Result> results;
    protected  List<ResponseInRequest> responseInRequest;



    public WorkloadRequest(Server server, Path.HttpMethod httpMethod, String endpoint, String operationID,
                           List<SecurityRequirement> securityRequirements)
    {
        this.id = ID_counter++;
        this.server = server;
        this.httpMethod = httpMethod;
        this.endpoint = endpoint;
        this.operationID = operationID;
        this.securityRequirements = securityRequirements;

        this.results = new ArrayList<>();

        this.responseInRequest = new ArrayList<>();
    }

    public static void ResetID()
    {
        ID_counter = 1;
    }

    public int ResultCount()
    {
        return this.results.size();
    }

    public int PassCount()
    {
        return Count(Result.Pass);
    }

    public int FailCount()
    {
        return Count(Result.Fail);
    }

    public int ExceptionCount()
    {
        return Count(Result.Exception);
    }

    private int Count(Result result)
    {
        return (int)this.results.stream().filter(r -> r == result).count();
    }

    public void RegisterResult(Result result)
    {
        this.results.add(result);
    }

    public List<Result> GetResults()
    {
        return this.results;
    }

    public List<ResponseInRequest> getResponseInRequest() {
        return responseInRequest;
    }

    public void setResponseInRequest(List<ResponseInRequest> responseInRequest) {
        this.responseInRequest = responseInRequest;
    }

    public boolean WasSuccessful()
    {
        return PassCount() > 0;
    }

    public abstract RequestBuilder Instantiate();


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkloadRequest that = (WorkloadRequest) o;
        return id == that.id;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }
}

