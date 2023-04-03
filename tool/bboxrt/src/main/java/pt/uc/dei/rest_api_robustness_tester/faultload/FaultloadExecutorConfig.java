package pt.uc.dei.rest_api_robustness_tester.faultload;

import pt.uc.dei.rest_api_robustness_tester.utils.Writer;
import pt.uc.dei.rest_api_robustness_tester.utils.NoOpWriter;

public class FaultloadExecutorConfig
{
    private int maxInjectionsPerFault = 3;
    private Writer writer = new NoOpWriter();
    
    public FaultloadExecutorConfig MaxInjectionsPerFault(int maxInjections)
    {
        maxInjectionsPerFault = maxInjections;
        return this;
    }
    
    public int GetMaxInjectionsPerFault()
    {
        return maxInjectionsPerFault;
    }
    
    public FaultloadExecutorConfig WriteTo(Writer writer)
    {
        this.writer = writer;
        return this;
    }
    
    public Writer GetWriter()
    {
        return this.writer;
    }
}
