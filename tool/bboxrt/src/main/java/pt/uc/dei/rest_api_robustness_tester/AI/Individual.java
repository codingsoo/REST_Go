package pt.uc.dei.rest_api_robustness_tester.AI;

import pt.uc.dei.rest_api_robustness_tester.response.StatusCode;
import pt.uc.dei.rest_api_robustness_tester.utils.Config;
import pt.uc.dei.rest_api_robustness_tester.utils.XlsxWriter;
import pt.uc.dei.rest_api_robustness_tester.workload.GeneratedWorkloadRequest;
import pt.uc.dei.rest_api_robustness_tester.workload.Workload;
import pt.uc.dei.rest_api_robustness_tester.workload.WorkloadExecutor;
import pt.uc.dei.rest_api_robustness_tester.workload.WorkloadRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Individual {

    protected WorkloadExecutor wrkExecutor;
    protected GeneratedWorkloadRequest wkr;
    protected double fitnessValue = 1.0;
    public int indexInArray = -1;


    public Individual( WorkloadExecutor wrkExecutor , GeneratedWorkloadRequest genWrk){
        this.wrkExecutor = wrkExecutor;
        this.wkr = genWrk;
    }

    public Individual(Individual newind){
        this.wrkExecutor = newind.wrkExecutor;
        this.wkr = newind.wkr;

        this.fitnessValue = newind.fitnessValue;
        this.indexInArray = newind.indexInArray;
    }


    public void evaluate(){
        List<WorkloadRequest> wreqs = new ArrayList<>();
        wreqs.add(this.wkr);
        this.wrkExecutor.getWorkload().setWorkloadRequests(wreqs);


        //only saves the last result
        if(this.wrkExecutor.getWorkload().getWorkloadRequests().get(0).getResponseInRequest().size() == 1){
            this.wrkExecutor.getWorkload().getWorkloadRequests().get(0).getResponseInRequest().remove(0);
        }

        this.wrkExecutor.Execute();
        StatusCode sc = this.wrkExecutor.getWorkload().getWorkloadRequests().get(0).getResponseInRequest().get(0).getStatusCode();


        if(sc.Is2xx()){
            this.setFitnessValue(1.0);
        }
        else if(sc.Is5xx()){
            this.setFitnessValue(0.0);
        }
        else{
            this.setFitnessValue(0.4);
        }
    }


    public GeneratedWorkloadRequest getWkr() { return wkr; }
    public void setWkr(GeneratedWorkloadRequest wkr) {
        this.wkr = wkr;
    }

    public WorkloadExecutor getWrkExecutor() { return wrkExecutor; }
    public void setWrkExecutor(WorkloadExecutor wrkExecutor) { this.wrkExecutor = wrkExecutor; }

    public double getFitnessValue() { return fitnessValue; }
    public void setFitnessValue(double fitnessValue) { this.fitnessValue = fitnessValue; }

    public int getIndexInArray() { return indexInArray; }
    public void setIndexInArray(int indexInArray) { this.indexInArray = indexInArray; }

}
