package pt.uc.dei.rest_api_robustness_tester.AI;

import org.apache.commons.lang3.RandomUtils;
import pt.uc.dei.rest_api_robustness_tester.media.FormatterManager;
import pt.uc.dei.rest_api_robustness_tester.media.SchemaFormatter;
import pt.uc.dei.rest_api_robustness_tester.request.ParameterInstance;
import pt.uc.dei.rest_api_robustness_tester.request.RequestBodyInstance;
import pt.uc.dei.rest_api_robustness_tester.schema.Schema;
import pt.uc.dei.rest_api_robustness_tester.schema.SchemaInstance;
import pt.uc.dei.rest_api_robustness_tester.schema.TypeManager;
import pt.uc.dei.rest_api_robustness_tester.specification.RestApi;
import pt.uc.dei.rest_api_robustness_tester.workload.GeneratedWorkloadRequest;
import pt.uc.dei.rest_api_robustness_tester.workload.LocalWorkloadExecutor;
import pt.uc.dei.rest_api_robustness_tester.workload.ParameterFilter;
import pt.uc.dei.rest_api_robustness_tester.workload.PayloadFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GeneticAlgorithm extends Algorithm{

    protected int generationSize    = 50;
    protected int cols              = 2000;
    protected double crossoverProb  = 0.7;
    public GeneticAlgorithm(LocalWorkloadExecutor workloadExecutor){
        super(workloadExecutor);
    }


    public void run(){
        // initialize population
        // evaluate population

        // for
        // select individuals
        // crossover
        // mutation
        // evaluate offspring


        // solution can be the archive with the best individuals
    }


    /*public void crossover(Individual parent1, Individual parent2) throws Exception {

        this.deepCopy(ind);
        GeneratedWorkloadRequest generatedWorkloadRequest = ind.getWkr();

        if(generatedWorkloadRequest.hasParameters()){
            List<ParameterInstance> parametersToRemove = new ArrayList<>();
            for(int i = 0 ; i < generatedWorkloadRequest.getParameters().size() ; i++) {

                ParameterInstance pp = generatedWorkloadRequest.getParameters().get(i);
                ParameterFilter pFilter = ind.getWrkExecutor().getRestAPI().Config().GetParameterFilterFor(pp.Base().name,
                        pp.Base().location, ind.getWkr().httpMethod, ind.getWkr().endpoint);
                SchemaInstance schemaInstance = pp.Schema();
                // this if statement probably will never be used but just for security
                //   and coherence with the workload generator is here

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
                SchemaFormatter f = FormatterManager.Instance().GetDefaultFormatter();
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
    }*/
}
