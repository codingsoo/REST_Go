package pt.uc.dei.rest_api_robustness_tester.workload;

public class WorkloadGeneratorConfig
{
    private int repetitionsPerParameter = 10;
    
    public WorkloadGeneratorConfig RepetitionsPerParameter(int repetitionsPerParameter)
    {
        this.repetitionsPerParameter = repetitionsPerParameter;
        return this;
    }
    
    public int GetRepetitionsPerParameter()
    {
        return this.repetitionsPerParameter;
    }
}
