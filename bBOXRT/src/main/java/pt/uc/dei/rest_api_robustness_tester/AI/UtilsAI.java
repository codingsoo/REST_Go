package pt.uc.dei.rest_api_robustness_tester.AI;
import org.apache.commons.lang3.RandomUtils;
import pt.uc.dei.rest_api_robustness_tester.media.FormatterManager;
import pt.uc.dei.rest_api_robustness_tester.media.SchemaFormatter;
import pt.uc.dei.rest_api_robustness_tester.request.ParameterInstance;
import pt.uc.dei.rest_api_robustness_tester.request.RequestBody;
import pt.uc.dei.rest_api_robustness_tester.request.RequestBodyInstance;
import pt.uc.dei.rest_api_robustness_tester.schema.*;
import pt.uc.dei.rest_api_robustness_tester.specification.RestApi;
import pt.uc.dei.rest_api_robustness_tester.workload.*;

import java.util.*;

public class UtilsAI {

    private List<Double> averageNeigh = new ArrayList<>();
    private List<Individual> optimizeIndividuals = new ArrayList<>();
    private int optimizeIndividualsSize = 10;
    public int totalIterations = 1;

    public UtilsAI(){}

    public Individual simulatedAnnealing(Individual t , double inicialTemperature) throws Exception {

        Random random = new Random();
        Individual current =  this.copyIndividual(t);
        Individual next;
        int iteration = 0;
        double currentTemperature = inicialTemperature;

        for(;;){

            if(Math.round(currentTemperature)  == 0){ //halt when temperature = 0
                return current;
            }

            next = this.newIndividual(current.getWrkExecutor(),current.getWkr());
            this.deepCopy(next);
            next = constructNeighbor( next, iteration );

            // If positive, next is better than current.
            // Otherwise, next is worse than current.
            double deltaE = current.getFitnessValue() - next.getFitnessValue();

            if(deltaE > 0){
                current = next;
            }
            else{ // as T -> 0, p -> 0; as deltaE -> -infin, p -> 0
                double p = Math.exp(deltaE / currentTemperature);
                if(p > random.nextDouble()){
                    current = next;
                }
            }

            iteration ++;
            currentTemperature *= 0.9;  //current temperature, which is monotonically decreasing with t
        }
    }

    public Individual copyIndividual(Individual i){
        //return new Individual(i);
        Individual ind = new Individual(
                new LocalWorkloadExecutor(i.getWrkExecutor().getWorkload(),i.getWrkExecutor().getRestAPI(), i.getWrkExecutor().getConfig())
                , new GeneratedWorkloadRequest(i.getWkr().server, i.getWkr().httpMethod, i.getWkr().endpoint, i.getWkr().operationID
                , i.getWkr().getParameters(), i.getWkr().getRequestBody(), i.getWkr().securityRequirements));
        ind.setIndexInArray(i.getIndexInArray());
        ind.setFitnessValue(i.getFitnessValue());
        return new Individual(ind);
    }

    public Individual newIndividual(WorkloadExecutor we, GeneratedWorkloadRequest ge){
        return new Individual(
                new LocalWorkloadExecutor(
                        we.getWorkload() ,
                        we.getRestAPI()  ,
                        we.getConfig()
                ),
                new GeneratedWorkloadRequest(
                        ge.server               ,
                        ge.httpMethod           ,
                        ge.endpoint             ,
                        ge.operationID          ,
                        ge.getParameters()      ,
                        ge.getRequestBody()     ,
                        ge.securityRequirements
                )
        );
    }

    public IndividualForIS newIndividualForIS(WorkloadExecutor we, GeneratedWorkloadRequest ge){
        return new IndividualForIS(
                new LocalWorkloadExecutor(
                        we.getWorkload() ,
                        we.getRestAPI()  ,
                        we.getConfig()
                ),
                new GeneratedWorkloadRequest(
                        ge.server               ,
                        ge.httpMethod           ,
                        ge.endpoint             ,
                        ge.operationID          ,
                        ge.getParameters()      ,
                        ge.getRequestBody()     ,
                        ge.securityRequirements
                )
        );
    }

    public void deepCopy(Individual ind) throws Exception {

        GeneratedWorkloadRequest generatedWorkloadRequest = ind.getWkr();

        if(generatedWorkloadRequest.hasParameters())
        {
            List<ParameterInstance> pps = new ArrayList<>();
            for(ParameterInstance param : generatedWorkloadRequest.getParameters()){
                ParameterInstance aux = param.Base().Instantiate(param.Schema().Schema().value.toString());
                aux.Schema().Schema().New(param.Schema().Schema().value.toString());
                aux.Schema().setValue(param.Schema().Schema().value.toString());
                pps.add(aux);
            }
            generatedWorkloadRequest.setParameters(pps);
        }

        if(generatedWorkloadRequest.hasRequestBody())
        {
            RequestBodyInstance rqOld =  generatedWorkloadRequest.getRequestBody();
            SchemaInstance si = rqOld.Schema();
            Schema s = si.Schema().copySchema(rqOld.Base().schema);

            SchemaInstance ss = new SchemaInstance(si.Value(), s);
            ss.setValue(String.valueOf(s.value));

            RequestBodyInstance rqNew = rqOld.Base().Instantiate(null,ss.Value());
            rqNew.setMediaType(rqOld.MediaType());
            rqNew.setSchema(ss);
            rqNew.setBase(rqOld.Base());
            generatedWorkloadRequest.setRequestBody(rqNew);
        }
    }


    public void newValueWithCurrent(Individual ind) throws Exception {

        this.deepCopy(ind);
        GeneratedWorkloadRequest generatedWorkloadRequest = ind.getWkr();
        if(generatedWorkloadRequest.hasParameters()){
            for(int i = 0 ; i < ind.getWkr().getParameters().size() ; i++) {
                ParameterInstance pp = ind.getWkr().getParameters().get(i);
                pp.Schema().Schema().New();
                pp.Schema().setValue(String.valueOf(pp.Schema().Schema().value));
            }
        }

        if(generatedWorkloadRequest.hasRequestBody()){
            SchemaInstance ss = ind.getWkr().getRequestBody().Schema();
            ss.Schema().New();
            ss.setValue(String.valueOf(ss.Schema().value));
        }
    }

    public void randomizeValueWithCurrent(Individual ind) throws Exception {

        this.deepCopy(ind);
        GeneratedWorkloadRequest generatedWorkloadRequest = ind.getWkr();

        if(generatedWorkloadRequest.hasParameters()){
            List<ParameterInstance> parametersToRemove = new ArrayList<>();
            for(int i = 0 ; i < generatedWorkloadRequest.getParameters().size() ; i++) {

                ParameterInstance pp = generatedWorkloadRequest.getParameters().get(i);
                ParameterFilter pFilter = ind.getWrkExecutor().getRestAPI().Config().GetParameterFilterFor(pp.Base().name,
                        pp.Base().location, ind.getWkr().httpMethod, ind.getWkr().endpoint);
                SchemaInstance schemaInstance = pp.Schema();
                /* this if statement probably will never be used but just for security
                    and coherence with the workload generator is here
                */
                if(pFilter != null && pFilter.IsIgnore()){
                    parametersToRemove.add(pp);
                    continue;
                }

                schemaInstance.Schema().randomizeWithCurrentValue(ind.indexInArray, this.totalIterations);

                if(pFilter != null && pFilter.IsSetValue()){
                    if (pFilter.AppliesToAll() ||
                            (pFilter.AppliesToOperation() &&
                                    pFilter.OperationIsEqual(ind.getWkr().httpMethod, ind.getWkr().endpoint)))
                    {
                        System.out.println("[WorkloadGenerator] Filtered " + pp.Base().location + " parameter "
                                + pp.Base().name + " (Filter: " + pFilter.GetFilterType() + ", Scope: " +
                                pFilter.GetFilterScope() + ")");
                        if(pFilter.GetValues() != null && !pFilter.GetValues().isEmpty()){
                            // will get a random value from the array given at the Rest API java file
                            String value = pFilter.GetValues().get(RandomUtils.nextInt(0, pFilter.GetValues().size()));
                            schemaInstance.Schema().New(value);
                        }
                    }
                }

                Class<? extends Schema> c;
                if(TypeManager.Instance().HasFormat(schemaInstance.Schema().format))
                    c = TypeManager.Instance().GetFormat(schemaInstance.Schema().format);
                else
                    c = TypeManager.Instance().GetType(schemaInstance.Schema().type);
                SchemaFormatter  f = FormatterManager.Instance().GetDefaultFormatter();
                schemaInstance.setValue(f.Serialize(schemaInstance.Schema()));
            }

            for(ParameterInstance p : parametersToRemove){
                ind.getWkr().getParameters().remove(p);
            }
        }

        if(generatedWorkloadRequest.hasRequestBody())
        {
            RequestBodyInstance requestBody = ind.getWkr().getRequestBody();
            RestApi rest = ind.getWrkExecutor().getRestAPI();
            PayloadFilter pFilter = rest.Config().
                    GetPayloadFilterFor(ind.getWkr().httpMethod, ind.getWkr().endpoint);
            SchemaInstance schemaInstance = requestBody.Schema();
            schemaInstance.Schema().randomizeWithCurrentValue(ind.indexInArray, this.totalIterations);

            if(pFilter != null && pFilter.IsSetValue() && pFilter.OperationIsEqual(ind.getWkr().httpMethod, ind.getWkr().endpoint)){
                HashMap<String, List<String>> namesWithValues = pFilter.getNamesWithValues();
                List<String> names =  new ArrayList<>(namesWithValues.keySet());
                List<String> values;
                for(String name : names){
                    values = namesWithValues.get(name);
                    schemaInstance.Schema().FindParameterToFilter(
                            schemaInstance.Schema(), name, values.get(RandomUtils.nextInt( 0, values.size())));
                }
            }

            Class<? extends Schema> c;
            if(TypeManager.Instance().HasFormat(schemaInstance.Schema().format))
                c = TypeManager.Instance().GetFormat(schemaInstance.Schema().format);
            else
                c = TypeManager.Instance().GetType(schemaInstance.Schema().type);
            SchemaFormatter f = FormatterManager.Instance().GetFormatter(requestBody.MediaType(), c);
            schemaInstance.setValue(f.Serialize(schemaInstance.Schema()));
        }
    }




    // need to change this method to the different schemas
    public Individual constructNeighbor(Individual neighbor, int iteration) throws Exception {

        boolean positiveIncrement;

        List<ParameterInstance> pps = new ArrayList<>();
        /*for(int i = 0 ; i < neighbor.getWkr().getParameters().size() ; i++){
            pps.add(new ParameterInstance(neighbor.getWkr().getParameters().get(i))));
        }*/

        for(ParameterInstance p : neighbor.getWkr().getParameters()){

            SchemaInstance schm = p.Schema();
            if(schm.Schema().type.equals("integer") && (schm.Schema().format.equals("int64"))){

                positiveIncrement = (new Random().nextBoolean());
                LongSchema ls = (LongSchema) schm.Schema();
                long currentValue = ls.value;

                long nextValue = currentValue;
                Long min = ls.minimum;
                Long max = ls.maximum;
                long increment = (long) Math.round(Math.pow(2,iteration));

                if(positiveIncrement && currentValue + increment <= max){
                    nextValue = currentValue + increment;
                }
                else if(!positiveIncrement && currentValue - increment >= min){
                    nextValue = currentValue - increment;
                }
                else{
                    positiveIncrement = !positiveIncrement;
                    if(positiveIncrement && currentValue + increment <= max)
                        nextValue = currentValue + increment;
                    else if(!positiveIncrement && currentValue - increment >= min)
                        nextValue = currentValue - increment;
                }

                currentValue = nextValue;
                pps.add(p.Base().Instantiate(String.valueOf(currentValue)));
                /*p.Schema().setValue(String.valueOf(currentValue));
                p.Schema().Schema().value = currentValue;*/

            }
            else if(schm.Schema().type.equals("integer") && (schm.Schema().format.equals("int32"))){

                positiveIncrement = (new Random().nextBoolean());
                IntegerSchema is = (IntegerSchema) schm.Schema();
                int currentValue = is.value;

                int nextValue = currentValue;
                Integer min = is.minimum;
                Integer max = is.maximum;

                int increment = (int) Math.round(Math.pow(2,iteration));

                if(positiveIncrement && currentValue + increment <= max){
                    nextValue = currentValue + increment;
                }
                else if(!positiveIncrement && currentValue - increment >= min){
                    nextValue = currentValue - increment;
                }
                else{
                    positiveIncrement = !positiveIncrement;
                    if(positiveIncrement && currentValue + increment <= max)
                        nextValue = currentValue + increment;
                    else if(!positiveIncrement && currentValue - increment >= min)
                        nextValue = currentValue - increment;
                }


                currentValue = nextValue;
                ParameterInstance prti = p.Base().Instantiate(String.valueOf(currentValue));
                prti.Schema().Schema().value = currentValue;
                pps.add(prti);

                /*p.Schema().setValue(String.valueOf(currentValue));
                p.Schema().Schema().value = currentValue;*/

            }
            else if(p.Schema().Schema().type.equals("boolean")){
                boolean currentBolean = Boolean.parseBoolean(p.Schema().Value());
                currentBolean = !currentBolean;

                /*p.Schema().setValue(String.valueOf(currentBolean));
                p.Schema().Schema().value = currentBolean; */

                ParameterInstance prti = p.Base().Instantiate(String.valueOf(currentBolean));
                prti.Schema().Schema().value = currentBolean;
                pps.add(prti);


            }
        }


        neighbor.getWkr().setParameters(pps);
        neighbor.evaluate();

        if(optimizeIndividuals.size() < optimizeIndividualsSize  &&  neighbor.getFitnessValue() == 0.0){
            Individual newInd = this.copyIndividual(neighbor);
            newInd.setIndexInArray(optimizeIndividuals.size());
            optimizeIndividuals.add(newInd);
        }

        return neighbor;

    }

    public Individual getBestNeighbor(Individual t, int windowSize) throws Exception {

        List<Individual> neighbors = new ArrayList<>();
        Individual next;
        Individual best =  this.copyIndividual(t);

        for(int i = 0; i < windowSize; i ++){
            next = this.copyIndividual(t);
            next = constructNeighbor(next, i );
            neighbors.add(next);
        }

        // evaluate average of neighboors here
        this.neighboorsAvg(neighbors);

        for(Individual i : neighbors){
            if(i.getFitnessValue() < best.getFitnessValue())
                best = i;
        }
        return best;
    }

    public void neighboorsAvg(List<Individual> neighbors){
        double sum = 0;
        for(Individual i : neighbors){
            sum = sum + i.getFitnessValue();
        }
        this.averageNeigh.add(sum / neighbors.size());
    }



    public List<Double> getAverageNeigh() { return averageNeigh; }
    public void setAverageNeigh(List<Double> averageNeigh) { this.averageNeigh = averageNeigh; }

    public List<Individual> getOptimizeIndividuals() { return optimizeIndividuals; }
    public void setOptimizeIndividuals(List<Individual> optimizeIndividuals) { this.optimizeIndividuals = optimizeIndividuals; }

    public int getOptimizeIndividualsSize() { return optimizeIndividualsSize; }
    public void setOptimizeIndividualsSize(int optimizeIndividualsSize) { this.optimizeIndividualsSize = optimizeIndividualsSize; }
}
