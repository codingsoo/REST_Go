package pt.uc.dei.rest_api_robustness_tester.workload;

import org.apache.http.HttpHost;
import pt.uc.dei.rest_api_robustness_tester.faultload.FaultloadExecutor;
import pt.uc.dei.rest_api_robustness_tester.security.AuthHandlers;
import pt.uc.dei.rest_api_robustness_tester.utils.NoOpWriter;
import pt.uc.dei.rest_api_robustness_tester.utils.Writer;

public class WorkloadExecutorConfig
{
    public enum StoppingCondition {AllDone, TimeBased}
    public enum ExecutionMode {Sequential, Random}
    
    private int maxRetries = 1;
    private HttpHost proxyHost = null;
    private StoppingCondition stoppingCondition = StoppingCondition.AllDone;
    private long timeBasedMilliseconds = 60 * 1000;
    private ExecutionMode executionMode = ExecutionMode.Sequential;
    private FaultloadExecutor faultloadExecutorHook = null;
    private Writer writer = new NoOpWriter();
    private boolean keepFailedRequests = false;
    
    public WorkloadExecutorConfig MaxRetriesOnFail(int maxRetries)
    {
        this.maxRetries = maxRetries;
        return this;
    }


    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getMaxRetries() { return maxRetries; }

    public void setKeepFailedRequests(boolean keepFailedRequests) {
        this.keepFailedRequests = keepFailedRequests;
    }

    public int GetMaxRetriesOnFail()
    {
        return maxRetries;
    }
    
    public HttpHost GetProxyHost()
    {
        return proxyHost;
    }
    
    public boolean HasProxyHost()
    {
        return proxyHost != null;
    }
    
    public WorkloadExecutorConfig ProxyHost(HttpHost proxyHost)
    {
        this.proxyHost = proxyHost;
        return this;
    }
    
    public WorkloadExecutorConfig StopWhenAllDone()
    {
        stoppingCondition = StoppingCondition.AllDone;
        return this;
    }
    
    public WorkloadExecutorConfig StopWhenTimeEnds(long durationMillis)
    {
        stoppingCondition = StoppingCondition.TimeBased;
        timeBasedMilliseconds = durationMillis;
        return this;
    }
    
    public long GetDurationMillis()
    {
        return stoppingCondition == StoppingCondition.TimeBased? timeBasedMilliseconds : 0;
    }
    
    public StoppingCondition GetStoppingCondition()
    {
        return stoppingCondition;
    }
    
    public WorkloadExecutorConfig ExecuteSequentially()
    {
        executionMode = ExecutionMode.Sequential;
        return this;
    }
    
    public WorkloadExecutorConfig ExecuteRandomly()
    {
        executionMode = ExecutionMode.Random;
        return this;
    }
    
    public ExecutionMode GetExecutionMode()
    {
        return executionMode;
    }
    
    public WorkloadExecutorConfig Hook(FaultloadExecutor faultloadExecutor)
    {
        this.faultloadExecutorHook = faultloadExecutor;
        return this;
    }
    
    public boolean HasFaultloadExecutorHook()
    {
        return faultloadExecutorHook != null;
    }
    
    public FaultloadExecutor GetFaultloadExecutorHook()
    {
        return faultloadExecutorHook;
    }
    
    public WorkloadExecutorConfig WriteTo(Writer writer)
    {
        this.writer = writer;
        return this;
    }
    
    public Writer GetWriter()
    {
        return this.writer;
    }
    
    public WorkloadExecutorConfig KeepFailedRequests()
    {
        this.keepFailedRequests = true;
        return this;
    }
    
    public WorkloadExecutorConfig DiscardFailedRequests()
    {
        this.keepFailedRequests = false;
        return this;
    }
    
    public boolean ShouldKeepFailedRequests()
    {
        return keepFailedRequests;
    }
}
