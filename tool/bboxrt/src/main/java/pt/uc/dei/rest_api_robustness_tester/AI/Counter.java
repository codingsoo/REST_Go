package pt.uc.dei.rest_api_robustness_tester.AI;

import pt.uc.dei.rest_api_robustness_tester.Path;

import java.util.ArrayList;

public class Counter {

    ArrayList<String> numberOPSpassPrint = new ArrayList<>();
    public double  ifs_fitnessMean         = 0.0   , bs_fitnessMean     = 0.0,
            ifs_StatusCodeMean      = 0.0   , bs_StatusCodeMean  = 0.0,
            ifs_distanceMean        = 0.0   , bs_distanceMean    = 0.0;

    public Counter(){

    }

    public void IncrementInformedCounters(double fitnessValue, double fitnessStatusCode, double fitnessDistance){
        ifs_fitnessMean     += fitnessValue;
        ifs_StatusCodeMean  += fitnessStatusCode;
        ifs_distanceMean    += fitnessDistance;
    }

    public void IncrementRandomCounters(double fitnessValue, double fitnessStatusCode, double fitnessDistance){
        bs_fitnessMean     += fitnessValue;
        bs_StatusCodeMean  += fitnessStatusCode;
        bs_distanceMean    += fitnessDistance;
    }


    public void CalculateMeans(int rows , int cols){
        ifs_fitnessMean     = ifs_fitnessMean       / (rows * cols);
        ifs_StatusCodeMean  = ifs_StatusCodeMean    / (rows * cols);
        ifs_distanceMean    = ifs_distanceMean      / (rows * cols);
        bs_fitnessMean      = bs_fitnessMean        / (rows * cols);
        bs_StatusCodeMean   = bs_StatusCodeMean     / (rows * cols);
        bs_distanceMean     = bs_distanceMean       / (rows * cols);
    }

    public void printMeanAndPassCounter(int ifsPassCounter, int bsPassCounter)
    {
        System.out.println("informative searching Fitness mean: " + ifs_fitnessMean );
        System.out.println("Random searching Fitness mean: " + bs_fitnessMean );

        System.out.println("informative searching StatusCode Component mean: " + ifs_StatusCodeMean );
        System.out.println("Random searching StatusCode Component mean: " + bs_StatusCodeMean );

        System.out.println("informative searching Levenshteins Distance mean: " + ifs_distanceMean );
        System.out.println("Random searching Levenshteins Distance mean: " + bs_distanceMean );

        System.out.println("informative searching pass counter: " + ifsPassCounter);
        System.out.println("Random searching(base line) pass counter: " + bsPassCounter);
    }


    public void addTotalPassPerOperation(String endpoint, Path.HttpMethod method, int totalPass){
        numberOPSpassPrint.add("Informative pass for endpoint: " + endpoint + " Method: "+ method + " - passº: " + totalPass);
    }

    public void addTotalPassPerOperationRandom(String endpoint, Path.HttpMethod method, int totalPass){
        numberOPSpassPrint.add("basic lane pass for endpoint: " + endpoint + " Method: "+ method + " - passº: " + totalPass);
    }

}
