package pt.uc.dei.rest_api_robustness_tester.AI;

import pt.uc.dei.rest_api_robustness_tester.utils.TimeUnit;
import pt.uc.dei.rest_api_robustness_tester.workload.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Algorithm {

    protected LocalWorkloadExecutor workloadExecutor;
    protected Workload workloadResults    = null;
    protected UtilsAI utils               = new UtilsAI();
    protected Counter counter             = new Counter();
    protected RateLimiter rt              = new RateLimiter(1000, 1, TimeUnit.Second);
    protected List<WorkloadRequest> workloadRequests = null;
    List<IndividualForIS> indivs = null;

    public Algorithm(LocalWorkloadExecutor workloadExecutor){
        this.workloadExecutor = workloadExecutor;
    }

    public void generate_valid_Population(){
        workloadExecutor.Execute(); // execute random requests
        workloadRequests = workloadExecutor.GetResults().getWorkloadRequests(); // get just the valid ones
        indivs = this.onlyValidIndividuals(workloadRequests);  //creates an array of individuals, only individuals with valid requests
    }

    public void generate_validAndInvalid_Population(){
        workloadExecutor.Execute(); // execute random requests
        workloadRequests = workloadExecutor.getWorkload().getWorkloadRequests();
        indivs = this.onlyValidIndividuals(workloadRequests);  //creates an array of individuals, only individuals with valid requests
    }


    public void filteredIndivs(List<IndividualForIS> indivs){
        List<IndividualForIS> indivsToRemove = new ArrayList<>();
        for(int i  = 1; i < indivs.size() ; i ++){
            boolean exists = false;
            GeneratedWorkloadRequest current = indivs.get(i).getWkr();
            for(int j = i-1 ; j > -1 ; j--){
                GeneratedWorkloadRequest old = indivs.get(j).getWkr();
                if(current.server.toString().equals(old.server.toString())
                        && current.httpMethod == old.httpMethod && current.endpoint.equals(old.endpoint)
                        && current.operationID.equals(old.operationID)){
                    exists = true;
                    break;
                }
            }
            if(exists)
                indivsToRemove.add(indivs.get(i));
        }

        for(IndividualForIS i : indivsToRemove){
            indivs.remove(i);
        }


        Collections.sort(indivs, new Comparator<IndividualForIS>() {
            public int compare(IndividualForIS v1, IndividualForIS v2) {
                String first = v1.getWkr().endpoint + v1.getWkr().httpMethod + v1.getWkr().operationID;
                String second = v2.getWkr().endpoint + v2.getWkr().httpMethod + v2.getWkr().operationID;
                return first.compareTo(second);
            }
        });
    }


    public List<IndividualForIS> onlyValidIndividuals(List<WorkloadRequest> workloadRequests){
        List<IndividualForIS> indivs = new ArrayList<>();
        for(WorkloadRequest w : workloadRequests){
            GeneratedWorkloadRequest gg = (GeneratedWorkloadRequest) w;
            IndividualForIS i =  this.utils.newIndividualForIS(this.workloadExecutor,gg);
            i.evaluate();
            if(((WorkloadRequest)i.getWkr()).GetResults().get(0).equals(WorkloadRequest.Result.Pass))
                indivs.add(i);
        }
        return indivs;
    }
}
