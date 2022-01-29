package pt.uc.dei.rest_api_robustness_tester.workload;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import pt.uc.dei.rest_api_robustness_tester.Server;
import pt.uc.dei.rest_api_robustness_tester.request.AdditionalParameter;
import pt.uc.dei.rest_api_robustness_tester.request.HttpConnect;
import pt.uc.dei.rest_api_robustness_tester.response.ResponseInRequest;
import pt.uc.dei.rest_api_robustness_tester.response.StatusCode;
import pt.uc.dei.rest_api_robustness_tester.security.SecurityRequirement;
import pt.uc.dei.rest_api_robustness_tester.specification.RestApi;
import pt.uc.dei.rest_api_robustness_tester.utils.Config;
import pt.uc.dei.rest_api_robustness_tester.utils.CountdownTimer;
import pt.uc.dei.rest_api_robustness_tester.utils.Utils;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Callable;

public class LocalWorkloadExecutor extends WorkloadExecutor
{
    private final List<WorkloadRequest> validWorkloadRequests;
    
    public LocalWorkloadExecutor(Workload workload, RestApi restAPI, WorkloadExecutorConfig config)
    {
        super(workload, restAPI, config);
        this.validWorkloadRequests = new ArrayList<>();
    }
    
    @Override
    public void Execute()
    {
        HttpClientBuilder httpClientBuilder = Utils.DisableSSL(HttpClientBuilder.create()).
                setDefaultRequestConfig(RequestConfig.copy(RequestConfig.DEFAULT).
                        setConnectTimeout(Config.Instance().connTimeout * 1000).
                        setSocketTimeout(Config.Instance().connTimeout * 1000).
                        setConnectionRequestTimeout(Config.Instance().connTimeout * 1000).
                        build()).
                disableCookieManagement();
    
        Timer timer = new Timer();
        
        try (CloseableHttpClient httpClient = httpClientBuilder.build())
        {
            
            final List<WorkloadRequest> workloadRequests = new ArrayList<>(workload.WorkloadRequests());
            if(config.GetExecutionMode() == WorkloadExecutorConfig.ExecutionMode.Random && !config.HasFaultloadExecutorHook())
                Collections.shuffle(workloadRequests);
            
            final MutableBoolean stop = new MutableBoolean(workloadRequests.isEmpty());
            if(config.GetStoppingCondition() == WorkloadExecutorConfig.StoppingCondition.TimeBased)
                timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        stop.setTrue();
                    }
                }, config.GetDurationMillis());
            
            int count = 0;
            final MutableInt idx = new MutableInt(-1);
            Callable<Integer> nextRequestIndex;
            if(config.GetExecutionMode() == WorkloadExecutorConfig.ExecutionMode.Random && config.HasFaultloadExecutorHook())
                nextRequestIndex = () -> NextRequestIndex(workloadRequests);
            else
                nextRequestIndex = idx::incrementAndGet;
    
            HttpHost lastHost = null;
            
            while (stop.isFalse())
            {
                count++;
                idx.setValue(nextRequestIndex.call() % workloadRequests.size());
                
                if(config.GetStoppingCondition() == WorkloadExecutorConfig.StoppingCondition.AllDone)
                {
                    if(config.HasFaultloadExecutorHook())
                        stop.setValue(config.GetFaultloadExecutorHook().FaultsLeft() == 0);
                    else
                        stop.setValue(count >= workloadRequests.size());
                }
                
                WorkloadRequest req = workloadRequests.get(idx.intValue());
    
                //TODO: some paths and/or operations may use different servers! (i.e., server object should be overridden)
                RequestBuilder requestBuilder = FixRequestUri(req.server, req.Instantiate());
                
                HttpHost httpHost = new HttpHost(req.server.GetHost(), req.server.GetPort(), req.server.GetScheme().Value());

                if(config.HasProxyHost())
                {
                    if(HostHasChanged(lastHost, httpHost))
                    {
                        if (ProxyConnectAccepted(config.GetProxyHost(), httpHost, httpClient))
                            System.out.println("Established CONNECT tunnel with proxy " + config.GetProxyHost());
                        else
                        {
                            System.out.println("Failed to establish CONNECT tunnel with proxy " + config.GetProxyHost());
                            break;
                        }
                    }
                    lastHost = httpHost;
                    httpHost = config.GetProxyHost();
                }
    
                if(restAPI.Config().HasRateLimiter())
                    restAPI.Config().GetRateLimiter().WaitAndResetIfNecessary();
    
                //Global (API-level) security requirements
                for(SecurityRequirement secReq : workload.SecurityRequirements())
                    for(String reqName : secReq.requirement.keySet())
                        if(restAPI.AuthHandlers().GetAllAuthNames().contains(reqName))
                            restAPI.AuthHandlers().GetAuthHandler(reqName).HandleAuth(requestBuilder);
    
                //Local (operation-level) security requirements
                //Only handles the security mechanisms which have not been handled globally (i.e., previous loop)
                    for(SecurityRequirement secReq : req.securityRequirements)
                        for(String reqName : secReq.requirement.keySet())
                            if(restAPI.AuthHandlers().GetAllAuthNames().contains(reqName) &&
                                    !workload.SecurityRequirements().contains(secReq))
                                restAPI.AuthHandlers().GetAuthHandler(reqName).HandleAuth(requestBuilder);
    
                System.out.println("[Request ID" + req.id + "]");
                System.out.println("\tServer: " + req.server);
                System.out.println("\tMethod: " + req.httpMethod);
                System.out.println("\tEndpoint: " + req.endpoint);
                System.out.println("\tOperation ID: " + req.operationID);
    
                config.GetWriter().Add("Request ID", "" + req.id).
                        Add("Server", "" + req.server).
                        Add("Method", "" + req.httpMethod).
                        Add("Endpoint", req.endpoint).
                        Add("Operation ID", req.operationID);
                
                if(restAPI.Config().HasAdditionalParameters())
                {
                    List<AdditionalParameter> defaultParams = null;
                    if(restAPI.Config().HasGlobalAdditionalParameters())
                        defaultParams = restAPI.Config().GetGlobalAdditionalParameters();
                    if(restAPI.Config().HasAdditionalParametersFor(req.httpMethod, req.endpoint))
                        defaultParams = restAPI.Config().GetAdditionalParametersFor(req.httpMethod, req.endpoint);
        
                    if(defaultParams != null)
                    {
                        for (AdditionalParameter p : defaultParams)
                        {
                            try
                            {
                                p.Apply(requestBuilder);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                
                System.out.println("\tRequest:");
                System.out.println("\t\t" + Utils.GetRawRequest(requestBuilder.build()).replace("\n", "\n\t\t").trim());
                
                config.GetWriter().Add("Request", Utils.GetRawRequest(requestBuilder.build()));
                
                if(config.HasFaultloadExecutorHook())
                    config.GetFaultloadExecutorHook().Execute(requestBuilder);
                
                try (CloseableHttpResponse httpResponse = httpClient.execute(httpHost, requestBuilder.build()))
                {
                    if(restAPI.Config().HasRateLimiter())
                        restAPI.Config().GetRateLimiter().Tick();
                    
                    StatusCode statusCode = StatusCode.FromInt(httpResponse.getStatusLine().getStatusCode());

                    System.out.println("\tResponse:");
                    System.out.println("\t\tStatus code: " + statusCode);
                    System.out.println("\t\tStatus reason: " + httpResponse.getStatusLine().getReasonPhrase());


                    ResponseInRequest responseInreq = new ResponseInRequest();
                    responseInreq.setStatusCode(statusCode);
                    responseInreq.setStatusReason(httpResponse.getStatusLine().getReasonPhrase());

    
                    config.GetWriter().Add("Status code", "" + statusCode).
                            Add("Status reason", httpResponse.getStatusLine().getReasonPhrase());
                    if (workload.HasResponse(req.operationID, statusCode))
                    {
                        responseInreq.setSpecificationDescription(workload.Response(req.operationID,
                                statusCode).description);
                        System.out.println("\t\tSpecification description: " + workload.Response(req.operationID,
                                statusCode).description);
                        config.GetWriter().Add("Spec description", workload.Response(req.operationID,
                                statusCode).description);
                    }
                    else
                    {
                        responseInreq.setSpecificationDescription("(Response not in specification)");
                        System.out.println("\t\t(Response not in specification)");
                        config.GetWriter().Add("Spec description", "(Response not in specification)");
                    }
    
                    if(httpResponse.getEntity() != null)
                    {
                        System.out.println("\t\tContent:");
                        String content = new String(IOUtils.toByteArray(httpResponse.getEntity().getContent()));
                        System.out.println("\t\t" + content.replace("\n", "\n\t\t").trim() + "\n");

                        responseInreq.setContent(content);

                        
                        config.GetWriter().Add("Response", content);
                    }

                    req.responseInRequest.add(responseInreq);

                    //TODO: for both of the cases below, whenever possible, the response payload (if any)
                    //      should be completely checked against the specification
                    if (statusCode.Is2xx())
                    {
                        req.RegisterResult(WorkloadRequest.Result.Pass);
                        validWorkloadRequests.add(req);
                    }
                    else
                    {
                        if(!config.HasFaultloadExecutorHook())
                        {
                            //TODO: Not OK response
                            //      Request should be retried later up to a maximum R times
                            //      In the end store the request along with information regarding all the retries
                            //      and whether or not the request ended up being successful
                            req.RegisterResult(WorkloadRequest.Result.Fail);
                            if (req.FailCount() <= config.GetMaxRetriesOnFail())
                            {
                                count--;
                                workloadRequests.add(workloadRequests.remove(idx.getAndDecrement()));
                            }
                            if (config.ShouldKeepFailedRequests() && !validWorkloadRequests.contains(req))
                                validWorkloadRequests.add(req);
                        }
                    }
                }
                catch(IOException e)
                {
                    System.out.println("IOException: " + e);
                    req.RegisterResult(WorkloadRequest.Result.Exception);
                    if (req.ExceptionCount() <= config.GetMaxRetriesOnFail())
                    {
                        count--;
                        workloadRequests.add(workloadRequests.remove(idx.getAndDecrement()));
                        int diff = config.GetMaxRetriesOnFail() - req.ExceptionCount();
                        System.out.println("Request was pushed to the end and will be retried " + diff + " more time(s)");
                    }
                    System.out.println("Waiting " + Config.Instance().connTimeout + " second(s) to avoid " +
                            "overloading the server");
                    //FIXME: should NOT be done on the main thread!!!
                    new CountdownTimer().Wait(Config.Instance().connTimeout * 1000);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    
        timer.cancel();
    }
    
    public Workload GetResults()
    {
        return new Workload(validWorkloadRequests, workload.AllResponses(), workload.SecurityRequirements());
    }
    
    private RequestBuilder FixRequestUri(Server server, RequestBuilder requestBuilder)
    {
        String originalRequestUri = requestBuilder.getUri().toString();
        
        String requestUriPrefix = server.GetRemainder();
        
        requestBuilder.setUri((requestUriPrefix + originalRequestUri).replace("//", "/"));

        return requestBuilder;
    }
    
    private boolean HostHasChanged(HttpHost lastHost, HttpHost newHost)
    {
        if(lastHost == null)
            return true;
        
        return !newHost.toString().equals(lastHost.toString());
    }
    
    private boolean ProxyConnectAccepted(HttpHost proxyHost, HttpHost finalHost, CloseableHttpClient httpClient)
    {
        String urlWithoutScheme = finalHost.getHostName() + ":" + finalHost.getPort();
        RequestBuilder requestBuilder = RequestBuilder.copy(new HttpConnect(urlWithoutScheme));
        requestBuilder.setHeader(HTTP.TARGET_HOST, finalHost.toString());
        try(CloseableHttpResponse proxyResponse = httpClient.execute(proxyHost, requestBuilder.build()))
        {
            StatusCode statusCode = StatusCode.FromInt(proxyResponse.getStatusLine().getStatusCode());
            if(statusCode.Is2xx())
                return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return false;
    }
    
    private int NextRequestIndex(List<WorkloadRequest> workloadRequests)
    {
        Map<String, Integer> faultsLeftPerOp = config.GetFaultloadExecutorHook().FaultsLeftPerOperation();
        int sum = faultsLeftPerOp.values().stream().reduce(0, Integer::sum);
        
        TreeMap<Double, List<String>> opProbabilities = new TreeMap<>();
        for(String op : faultsLeftPerOp.keySet())
        {
            double p = (double)faultsLeftPerOp.get(op) / (double)sum;
            if(!opProbabilities.containsKey(p))
                opProbabilities.put(p, new ArrayList<>());
            opProbabilities.get(p).add(op);
        }
    
        double value;
        try
        {
            value = RandomUtils.nextDouble(0d, opProbabilities.lastKey());
        }
        catch(Exception ignored)
        {
            value = opProbabilities.lastKey();
        }
        
        List<String> ops = opProbabilities.ceilingEntry(value).getValue();
        String op = ops.get(RandomUtils.nextInt(0, ops.size()));
        
        Map<String, List<Integer>> wlRequestsPerOp = new LinkedHashMap<>();
        for(int i = 0; i < workloadRequests.size(); i++)
        {
            WorkloadRequest req = workloadRequests.get(i);
            if(!wlRequestsPerOp.containsKey(req.operationID))
                wlRequestsPerOp.put(req.operationID, new ArrayList<>());
            wlRequestsPerOp.get(req.operationID).add(i);
        }
        
        List<Integer> idxs = wlRequestsPerOp.get(op);
        //FIXME: the line below triggers an NPE if the selected operation has faults for FL
        //       but was discarded from WL because of some error (e.g., conn timetout) and
        //       has no WL results
        return idxs.get(RandomUtils.nextInt(0, idxs.size()));
    }
}
