package pt.uc.dei.rest_api_robustness_tester.workload;

import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.Callable;

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
import pt.uc.dei.rest_api_robustness_tester.specification.RestApi;
import pt.uc.dei.rest_api_robustness_tester.Server;
import pt.uc.dei.rest_api_robustness_tester.request.HttpConnect;
import pt.uc.dei.rest_api_robustness_tester.response.StatusCode;
import pt.uc.dei.rest_api_robustness_tester.security.SecurityRequirement;
import pt.uc.dei.rest_api_robustness_tester.utils.Utils;

public abstract class WorkloadExecutor
{
    protected Workload workload;
    protected RestApi restAPI;
    protected WorkloadExecutorConfig config;
    
    public WorkloadExecutor(Workload workload, RestApi restAPI, WorkloadExecutorConfig config)
    {
        this.workload = workload;
        this.restAPI = restAPI;
        this.config = config;
    }


    public Workload getWorkload() {
        return workload;
    }

    public WorkloadExecutorConfig getConfig() {
        return config;
    }


    public void setWorkload(Workload workload) {
        this.workload = workload;
    }

    public RestApi getRestAPI() {
        return restAPI;
    }

    public void setRestAPI(RestApi restAPI) {
        this.restAPI = restAPI;
    }

    public void setConfig(WorkloadExecutorConfig config) {
        this.config = config;
    }

    public abstract void Execute();
}
