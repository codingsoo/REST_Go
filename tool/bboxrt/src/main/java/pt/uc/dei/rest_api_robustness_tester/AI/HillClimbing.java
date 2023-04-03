package pt.uc.dei.rest_api_robustness_tester.AI;
import pt.uc.dei.rest_api_robustness_tester.request.ParameterInstance;
import pt.uc.dei.rest_api_robustness_tester.workload.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HillClimbing {

    private LocalWorkloadExecutor workloadExecutor;
    private Workload workloadResults = null;
    private UtilsAI utils = new UtilsAI();

    private List<Individual> initialRandomPopulation    = new ArrayList<>();

    private double randomResetEnd               = 0.3    ; // first: 0.5 , second: it was 0.3
    private double randomResetStart             = 0.0    ;
    private double maxSearchNumberEnd           = 5.0    ; // first: 10.0, it was 5.0
    private double maxSearchNumberStartAtual    = 1.0    ;
    private double probSAstart                  = 0.7    ; // first: 0.3 , it was 0.25
    public HillClimbing(LocalWorkloadExecutor workloadExecutor){
        this.workloadExecutor = workloadExecutor;
    }


    //TODO
    // possibly make a input of an array of strings
    // that contain the endpoints to run with the AI
    public void run() throws Exception {

        String endpoint = "/GetUsername/getUserName_Vx0" ;
        String operationID = "getUserName_Vx0";

        // just to make one and only one request
        this.workloadExecutor.getConfig().setMaxRetries(0);
        this.workloadExecutor.getConfig().setKeepFailedRequests(true);

        List<WorkloadRequest> workloadRequests = workloadExecutor.getWorkload().getWorkloadRequests();
        List<WorkloadRequest> wkReqsFiltered = new ArrayList<>();

        System.out.println("Size filtered with endpoint: " + wkReqsFiltered.size());

        /* this for will run 10 times because default value of flag
        "--wl-rep" is equal to 10 for testing purposes only be using 1 request */
        for(WorkloadRequest w : workloadRequests){
            GeneratedWorkloadRequest gg = (GeneratedWorkloadRequest) w;

            if(gg.endpoint.equals(endpoint) && gg.operationID.equals(operationID)){
                wkReqsFiltered.add(gg);
                break;
            }
            // the code was here is available at: https://www.notion.so/Code-that-was-in-hillclimbing-class-7c0f50d9fe16417896504daa859b1368
            // break; //for only 1 request and not the 10
        }

        GeneratedWorkloadRequest test = (GeneratedWorkloadRequest) wkReqsFiltered.get(0);
        //size of archive
        this.utils.setOptimizeIndividualsSize(300);
        // randomSampling was 0.75
        manyObjetiveHill( 0.15,  0.05, 100, test,  1);

        this.printResults();
        // Aqui depois de mudar os valores dos parametros posso executar a workload.
    }



    // stopCondition numero de archives (optimizeIndividuals) com 0.2 de fitness (melhor fitness agora, depois
    // no futuro o valor maios proximo de 0 sera melhor
    public void manyObjetiveHill(double randomSampling, double mutationProb,
                                 int stopCondition, WorkloadRequest wkr, int sizeInitializePop) throws Exception {

        Random random = new Random();

        double decrement            = randomSampling        / stopCondition;
        double randomResetIncre     = randomResetEnd        / stopCondition;
        double maxSearchNumberIncre = maxSearchNumberEnd    / stopCondition;
        double probSAincre          = probSAstart           / stopCondition;

        int counter = 0;
        this.initializePopulation(initialRandomPopulation, sizeInitializePop, wkr);

        for(; counter != stopCondition ;){

            Individual t = this.utils.copyIndividual(initialRandomPopulation.get(random.nextInt(initialRandomPopulation.size())));
            double rand = random.nextDouble();
            if( randomSampling > rand){
                /*this.instantiateRandomIndividual(t);
                t.evaluate();*/
                this.utils.randomizeValueWithCurrent(t);
                t.evaluate();
            }
            else{

                if(this.getOptimizeIndividuals().size() != 0)
                    t =  this.utils.copyIndividual(this.getOptimizeIndividuals().get(random.nextInt(this.getOptimizeIndividuals().size())));
                else
                    t.evaluate(); // this is necessary here because initialRandomPopulation array is not evaluated yet

                if(mutationProb > random.nextDouble()){
                    // pode ser mutacao a nivel de estrutura dos parametros
                    // (se tiver 3, apagar 1 que nao seja requeried por exemplo)
                    // a little bit tricky  because different types of data
                    // will result in different mutations of that data (need to think about it)
                }

                // 25% (at the start and 0 % at the end) to perform simulated annealing instead of hill climbing
                if(probSAstart > random.nextDouble())
                    t = this.utils.simulatedAnnealing(t, 5);
                else
                    t = this.utils.copyIndividual(hillClimbing(t, stopCondition,  (int)Math.round(maxSearchNumberStartAtual)
                                        ,  randomResetStart, 3));

            }

            randomSampling = randomSampling - decrement;
            maxSearchNumberStartAtual = maxSearchNumberStartAtual + maxSearchNumberIncre;
            randomResetStart = randomResetStart + randomResetIncre;
            probSAstart = probSAstart - probSAincre;

            // the right side of this if needs to be thought about
            if(t.getIndexInArray()  != -1 &&  this.getOptimizeIndividuals().get(t.getIndexInArray()).getFitnessValue() > t.getFitnessValue()){
                this.getOptimizeIndividuals().set(t.getIndexInArray(),t);
            }
            else if(t.getIndexInArray()  != -1
                    && t.getFitnessValue() == 0.0
                    && this.getOptimizeIndividuals().size() < this.getOptimizeIndividualsSize() ){
                t.setIndexInArray(this.getOptimizeIndividuals().size());
                this.getOptimizeIndividuals().add(t);
            }
            else if(this.getOptimizeIndividuals().size() < this.getOptimizeIndividualsSize() && t.getFitnessValue() < 0.7 ){
                t.setIndexInArray(this.getOptimizeIndividuals().size());
                this.getOptimizeIndividuals().add(t);
            }
            counter++;
        }
    }

    // maxSearchNumber - max iterations per variable
    public Individual hillClimbing
            (Individual t, int stopCondition, int maxSearchNumber
                    , double probRandomReset, int windowSizeNeighbors) throws Exception {

        Random random = new Random();
        if(t.getWkr().getParameters().size() == 0)
            return t;

        Individual current = t;

        for(int j = 0; j < current.getWkr().getParameters().size(); j++){
            for(int i = 0 ; i < maxSearchNumber; i++){

//                if(/*this.conditionToStop(optimizeIndividuals) == stopCondition /// optimizeIndividuals.size() == sizeOfArchive */ )
//                    break;
                Individual n = this.utils.getBestNeighbor(t, windowSizeNeighbors);

                if(current.getFitnessValue() < n.getFitnessValue()){

                    if(current.getFitnessValue() == 0.0){
                        break;
                    }
                    if(probRandomReset > random.nextDouble()){
                        // randomize variable
                        /*this.instantiateRandomIndividual(current);
                        current.evaluate();*/
                        this.utils.randomizeValueWithCurrent(current);
                        current.evaluate();
                    }
                    return current;
                }
                current = n;
            }
        }
        return current;
    }



    //int index nao estava aqui, foi usado para especificar o valor de um parametro ID para ficar igual a 1
    // instantiate individual with random parameters
    public Individual instantiateRandomIndividual
            (Individual t, int index) throws Exception {

        List<ParameterInstance> pps = new ArrayList<>();

        // rethink this for, in it should be something like:
        // ParameterInstance p = t.getWkr().getParameters().get(i);
        // pps.add(p.Base().Instantiate(String.valueOf(p.Base().Instantiate(p.Schema().getValue()))));
        for(int i = 0 ; i < t.getWkr().getParameters().size() ; i++){
            pps.add(t.getWkr().getParameters().get(i));
        }

        for(int j = 0 ; j < pps.size(); j ++){

            ParameterInstance pp = pps.get(j).Base().Instantiate();
            //este if nao estava aqui, foi para especificar um parametro ID igual a 1
            if(pp.Schema().Schema().type.equals("integer") && pp.Schema().Schema().format.equals("int32") && index == 0){
                pp.Schema().setValue("1");
                pp.Schema().Schema().value = 0;
            }
            pps.set(j, pp);
        }
        t.getWkr().setParameters(pps);
        return t;
    }


    public void initializePopulation
            (List<Individual>  initialRandomPopulation, int size, WorkloadRequest   wkr) throws Exception {

        GeneratedWorkloadRequest gwrklr = (GeneratedWorkloadRequest)wkr;
        Individual ind;
        for(int i = 0; i < size; i++){

            ind = this.utils.newIndividual(this.workloadExecutor,gwrklr);
            initialRandomPopulation.add(this.instantiateRandomIndividual(ind, i));
        }
    }


    public void printResults(){
        for(Individual i : this.getOptimizeIndividuals())
            System.out.println("fitness: " + i.getFitnessValue());

        System.out.println("Neighboors avg");
        for(Double d : this.utils.getAverageNeigh())
            System.out.println(d);
    }

    public LocalWorkloadExecutor getWorkloadExecutor() { return workloadExecutor; }
    public void setWorkloadExecutor(LocalWorkloadExecutor workloadExecutor) { this.workloadExecutor = workloadExecutor; }

    public Workload getWorkloadResults() { return workloadResults; }
    public void setWorkloadResults(Workload workloadResults) { this.workloadResults = workloadResults; }

    public UtilsAI getUtils() { return utils; }
    public void setUtils(UtilsAI utils) { this.utils = utils; }

    public List<Individual> getInitialRandomPopulation() { return initialRandomPopulation; }

    public List<Individual> getOptimizeIndividuals() { return this.utils.getOptimizeIndividuals(); }

    public int getOptimizeIndividualsSize() { return this.utils.getOptimizeIndividualsSize(); }
}
