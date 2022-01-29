package pt.uc.dei.rest_api_robustness_tester.AI;
import pt.uc.dei.rest_api_robustness_tester.Path;
import pt.uc.dei.rest_api_robustness_tester.utils.TimeUnit;
import pt.uc.dei.rest_api_robustness_tester.workload.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InformedSearch extends Algorithm {
    protected int generationSize    = 50;
    protected int cols              = 2000;

    public InformedSearch(LocalWorkloadExecutor workloadExecutor){
        super(workloadExecutor);
    }

    public void run(int runNumber) throws Exception {
        int     ifsPassCounter          = 0     , bsPassCounter      = 0;

        IndividualForIS bestind, currentBestind;
        generate_valid_Population();
        if(!indivs.isEmpty())  //just get one individual per operation
            filteredIndivs(indivs);

        int rows = indivs.size();  //rows number of different operations, cols how many reps of each request should be made (with new values)
        utils.totalIterations = cols;

        IndividualForIS[][] matrixIndivs         =  new IndividualForIS[rows][cols + 1]; /* matrix for informed search */
        IndividualForIS[][] matrixIndivsBaseLine =  new IndividualForIS[rows][cols + 1]; /*  matrix for random sampling (base line) */
        populationInitialization(matrixIndivs ,  matrixIndivsBaseLine, indivs);

        //Firstly run the informed search
        for(int row = 0 ; row < rows ; row ++){
            currentBestind = null;
            int ifsPassCounterOp = 0;
            for(int col = 1; col < cols + 1; col ++){
                rt.WaitAndResetIfNecessary();
                IndividualForIS ind;

                // [validIndiv, 1 , 2 ,3 ,4 ,5 ,6 ,7 ,8, 9 ,10 ,11 ]
                int currentCol = col;
                bestind = currentBestind;

                for(int k = 0; k < generationSize; k++){
                    if(currentCol == 1){
                        ind = utils.newIndividualForIS(matrixIndivs[row][currentCol-1].getWrkExecutor(),matrixIndivs[row][currentCol-1].getWkr());
                        // use as an iteration index in population, starts at 0
                        ind.indexInArray = col - 1;
                        utils.randomizeValueWithCurrent(ind);
                        ind.fitnessWithLevenshteins( matrixIndivs[row][currentCol-1]);
                        if(k == 0){
                            bestind = ind;
                        }
                    }
                    else{
                        ind = utils.newIndividualForIS(currentBestind.getWrkExecutor(),currentBestind.getWkr());
                        // use as an iteration index in population, starts at 0
                        ind.indexInArray = col - 1;
                        utils.randomizeValueWithCurrent(ind);
                        ind.fitnessWithLevenshteins(currentBestind);
                    }
                    rt.Tick();

                    matrixIndivs[row][col] = ind;
                    if(ind.getFitnessValue() >= bestind.getFitnessValue()){
                        bestind = ind;
                    }
                    if(matrixIndivs[row][col].getWkr().GetResults().get(0).equals(WorkloadRequest.Result.Pass)){
                        ifsPassCounterOp++;
                    }

                    counter.IncrementInformedCounters(matrixIndivs[row][col].getFitnessValue(),
                            matrixIndivs[row][col].getFitnessStatusCode(), matrixIndivs[row][col].getFitnessDistance());
                    col++;
                }

                /*if(currentBestind != null && bestind.getFitnessValue() < currentBestind.getFitnessValue()) {
                    System.out.println("Teste");
                }*/
                currentBestind = bestind;
                col--; // just to control the col++ inside for statement
            }
            ifsPassCounter += ifsPassCounterOp;
            GeneratedWorkloadRequest ge = matrixIndivs[row][0].getWkr();
            counter.addTotalPassPerOperation(ge.endpoint, ge.httpMethod, ifsPassCounterOp);
        }

        //Secondly run random search (base line)
        for(int row = 0 ; row < rows ; row ++){
            int bsPassCounterOp = 0;
            for(int col = 1; col < cols + 1 ; col ++){
                rt.WaitAndResetIfNecessary();

                IndividualForIS indbs = utils.newIndividualForIS(matrixIndivsBaseLine[row][col-1].getWrkExecutor(),matrixIndivsBaseLine[row][col-1].getWkr());
                utils.newValueWithCurrent(indbs);
                indbs.fitnessWithLevenshteins( matrixIndivsBaseLine[row][col-1]);
                matrixIndivsBaseLine[row][col] = indbs;

                rt.Tick();
                if(matrixIndivsBaseLine[row][col].getWkr().GetResults().get(0).equals(WorkloadRequest.Result.Pass)) {
                    bsPassCounterOp++;
                }
                counter.IncrementRandomCounters(matrixIndivsBaseLine[row][col].getFitnessValue()
                        ,  matrixIndivsBaseLine[row][col].getFitnessStatusCode(), matrixIndivsBaseLine[row][col].getFitnessDistance());
            }
            bsPassCounter += bsPassCounterOp;
            counter.addTotalPassPerOperationRandom(matrixIndivsBaseLine[row][0].getWkr().endpoint
                    , matrixIndivsBaseLine[row][0].getWkr().httpMethod, bsPassCounterOp);
        }

        for(String passPrint : counter.numberOPSpassPrint){
            System.out.println(passPrint);
        }
        counter.CalculateMeans(rows, cols);
        counter.printMeanAndPassCounter(ifsPassCounter, bsPassCounter);
        new UtilsFile("data_if_" + workloadExecutor.getRestAPI().Name() + "_" + runNumber +".txt"
                ,"/Users/thedemean/Documents/blackbox_git_dei_inv/bBOXRT/data_for_graphs/tpcapp").writeFitnessValues(matrixIndivs);
        new UtilsFile("data_bs_" + workloadExecutor.getRestAPI().Name() + "_" + runNumber + ".txt"
                ,"/Users/thedemean/Documents/blackbox_git_dei_inv/bBOXRT/data_for_graphs/tpcapp").writeFitnessValues(matrixIndivsBaseLine);
    }



    public void printMatrixs(Individual[][] matrixIndivs, Individual[][] matrixIndivsBaseLine, int rows, int cols){
        for(int row = 0 ; row < rows ; row ++){
            System.out.println("---------------------------------------------");
            System.out.println(matrixIndivs[row][0].getWkr().endpoint  + "\t\t"
                    + matrixIndivs[row][0].getWkr().operationID + "\t\t"
                    + matrixIndivs[row][0].getWkr().httpMethod + "\t\t");
            for(int col = 0; col < cols ; col ++){
                System.out.print(matrixIndivs[row][col].getWkr().GetResults().get(0) + "\t");
            }
            System.out.println();
            System.out.println("---------------------------------------------");
            System.out.println();
        }


        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("************************************************ BASE LINE PRINT ************************************************");
        for(int row = 0 ; row < rows ; row ++){
            System.out.println("---------------------------------------------");
            System.out.println(matrixIndivsBaseLine[row][0].getWkr().endpoint  + "\t\t"
                    + matrixIndivsBaseLine[row][0].getWkr().operationID + "\t\t"
                    + matrixIndivsBaseLine[row][0].getWkr().httpMethod + "\t\t");
            for(int col = 0; col < cols ; col ++){
                System.out.print(matrixIndivsBaseLine[row][col].getWkr().GetResults().get(0) + "\t");
            }
            System.out.println();
            System.out.println("---------------------------------------------");
            System.out.println();
        }
    }


    public void populationInitialization(IndividualForIS[][] matrixIndivs , IndividualForIS[][] matrixIndivsBaseLine,
                                         List<IndividualForIS> indivs){

        /*  first column of both matrix's will be populated with valid individuals,
            each row represents a different operation (http method, endpoint, operationID and server)
         */
        int j=0;
        for(IndividualForIS i : indivs){
            matrixIndivs[j][0] = i;
            matrixIndivsBaseLine[j][0] = utils.newIndividualForIS(i.getWrkExecutor(),i.getWkr());
            matrixIndivsBaseLine[j][0].evaluate();

            System.out.println("server: " + i.getWkr().server);
            System.out.println("Http method: " + i.getWkr().httpMethod + "\n" + "Operation id: "
                    + i.getWkr().operationID + "\n" + "endpoint: " + i.getWkr().endpoint + "\n\n");
            j++;
        }
    }


    public int getGenerationSize() { return generationSize; }
    public void setGenerationSize(int generationSize) { this.generationSize = generationSize; }
}
