package pt.uc.dei.rest_api_robustness_tester.AI;
import pt.uc.dei.rest_api_robustness_tester.request.ParameterInstance;
import pt.uc.dei.rest_api_robustness_tester.schema.ObjectSchema;
import pt.uc.dei.rest_api_robustness_tester.schema.Schema;
import pt.uc.dei.rest_api_robustness_tester.workload.GeneratedWorkloadRequest;
import pt.uc.dei.rest_api_robustness_tester.workload.WorkloadExecutor;


public class IndividualForIS extends Individual{

    protected double statusCodeWeight       = 0.7;
    protected double levenshteinsDistWeight = 0.3;

    protected double fitnessDistance        = 1.0;
    protected double fitnessStatusCode      = 1.0;
    protected double mutationPercentage     = 0.30;

    public IndividualForIS( WorkloadExecutor wrkExecutor , GeneratedWorkloadRequest genWrk){
        super(wrkExecutor,genWrk);
    }

    // IndividualForIS is the individual that originated the news ones, the new ones are the current from this object
    public void fitnessWithLevenshteins(IndividualForIS originatedFrom){

        this.evaluate();
        fitnessStatusCode   = fitnessValue * statusCodeWeight;
        fitnessValue        = fitnessStatusCode;

        String responseInContentCurrent = "";
        String responseInContentOrig = "";
        if(originatedFrom.getWkr().getResponseInRequest().size() > 0
                && !originatedFrom.getWkr().getResponseInRequest().get(0).getContent().equals("No content in response"))
            responseInContentOrig = originatedFrom.getWkr().getResponseInRequest().get(0).getContent();

        if(wkr.getResponseInRequest().size() > 0
                && !wkr.getResponseInRequest().get(0).getContent().equals("No content in response"))
            responseInContentCurrent = wkr.getResponseInRequest().get(0).getContent();


        double levenshteinsDistFinal = this.differenceRatio(responseInContentCurrent,responseInContentOrig); //this value is in % (ex: 0.2)
        fitnessDistance     =   ((1.0-levenshteinsDistFinal) * levenshteinsDistWeight);
        fitnessValue        +=  fitnessDistance;
    }


    public double differenceRatio(String s1, String s2 ){
        int max = maxDistlevenshteins(s1, s2);
        int dist = levenshteinsDist(s1.toCharArray(), s2.toCharArray());

        if(max == 0)
            return 0;
       return (((double) dist) / max);
    }

    public int maxDistlevenshteins(String s1, String s2){
        return s1.length() >= s2.length()? s1.length() : s2.length();
    }

    public int levenshteinsDist( char[] s1, char[] s2 ) {

        // memoize only previous line of distance matrix
        int[] prev = new int[ s2.length + 1 ];
        for( int j = 0; j < s2.length + 1; j++ ) {
            prev[ j ] = j;
        }

        for( int i = 1; i < s1.length + 1; i++ ) {

            // calculate current line of distance matrix
            int[] curr = new int[ s2.length + 1 ];
            curr[0] = i;

            for( int j = 1; j < s2.length + 1; j++ ) {
                int d1 = prev[ j ] + 1;
                int d2 = curr[ j - 1 ] + 1;
                int d3 = prev[ j - 1 ];
                if ( s1[ i - 1 ] != s2[ j - 1 ] ) {
                    d3 += 1;
                }
                curr[ j ] = Math.min( Math.min( d1, d2 ), d3 );
            }

            // define current line of distance matrix as previous
            prev = curr;
        }
        return prev[ s2.length ];
    }

    public double getFitnessDistance() { return fitnessDistance; }
    public void setFitnessDistance(double fitnessDistance) { this.fitnessDistance = fitnessDistance; }

    public double getMutationPercentage() { return mutationPercentage; }
    public void setMutationPercentage(double mutationPercentage) { this.mutationPercentage = mutationPercentage; }

    public double getFitnessStatusCode() { return fitnessStatusCode; }
    public void setFitnessStatusCode(double fitnessStatusCode) { this.fitnessStatusCode = fitnessStatusCode; }
}
