package io.resttestgen.nominaltester.testers;

import io.resttestgen.nominaltester.fieldgenerator.FieldsGeneratorDictionary;
import io.resttestgen.nominaltester.fieldgenerator.exceptions.FieldGenerationException;
import io.resttestgen.nominaltester.fieldgenerator.exceptions.TypeNotHandledException;
import io.resttestgen.nominaltester.fieldgenerator.manufacturetraces.ManufactureTraces;
import io.resttestgen.nominaltester.helper.OperationDependenciesHelper;
import io.resttestgen.nominaltester.helper.ResponseValidator;
import io.resttestgen.nominaltester.helper.TestStepExecutor;
import io.resttestgen.nominaltester.helper.exceptions.ApiResponseParsingException;
import io.resttestgen.nominaltester.helper.exceptions.ClassLoaderNotInitializedException;
import io.resttestgen.nominaltester.models.*;
import io.resttestgen.nominaltester.models.coverage.Coverage;
import io.resttestgen.nominaltester.models.coverage.TransitionCoverage;
import io.resttestgen.nominaltester.models.exceptions.ParametersMismatchException;
import io.resttestgen.nominaltester.testers.exceptions.OperationExecutionException;
import io.resttestgen.nominaltester.testers.operationtestingstrategies.BruteForceDependenciesOperationTester;
import io.resttestgen.swagger2depgraph.OperationDependencyGraph;
import io.resttestgen.swagger2depgraph.OperationNode;
import io.resttestgen.swagger2depgraph.RelationshipEdge;
import io.resttestgen.swaggerschema.models.ValidationError;
import io.resttestgen.swaggerschema.models.exceptions.SchemaValidationException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TransitionTester extends Tester {
    static final Logger logger = LogManager.getLogger(OperationTester.class);

    private ResponseValidator responseValidator;
    private ResponseDictionary responseDictionary;
    private OperationDependenciesHelper operationDependenciesHelper;
    private FieldsGeneratorDictionary fieldsGenerator;
    private Map<String, List<OperationInfo>> operationsPerApiClass;
    private OperationDependencyGraph operationDependencyGraph;
    private BruteForceDependenciesOperationTester operationTester;

    public TransitionTester(OpenAPI openAPI, Map<String, List<OperationInfo>> operationsPerApiClass) throws SchemaValidationException, ClassLoaderNotInitializedException {
        super(openAPI, operationsPerApiClass);
        operationTester = new BruteForceDependenciesOperationTester(openAPI, operationsPerApiClass);
        operationDependencyGraph = new OperationDependencyGraph(openAPI);
        this.operationsPerApiClass = operationsPerApiClass;
        responseDictionary = new ResponseDictionary();
        responseValidator = new ResponseValidator(openAPI);
        operationDependenciesHelper = new OperationDependenciesHelper(operationsPerApiClass, operationDependencyGraph);
        fieldsGenerator = new FieldsGeneratorDictionary(openAPI, responseDictionary);
    }

    public Coverage run() {
        Set<RelationshipEdge> edges = this.operationDependencyGraph.getEdges();
        TransitionCoverage transitionCoverage = new TransitionCoverage(edges);

        List<OperationInfo> operations = this.operationsPerApiClass.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toList());

        for (RelationshipEdge edge : edges) {
            operationTester.resetResponseDictionary();
            OperationInfo nodeWithDependencies = getMatchingOperationInfo(operations, (OperationNode)edge.getSourceNode());
            OperationInfo nodeToExecuteFirst =  getMatchingOperationInfo(operations, (OperationNode)edge.getTargetNode());
            TestCase targetNodeTestCase = reachTestableState(nodeToExecuteFirst);
            if (targetNodeTestCase != null) {
                // testable state reached (source node is successfully tested)
                // now try to execute the transition (source -> target)
                copyResponseDictionaryFrom(operationTester.getResponseDictionary());
                TestStep testStep = testSuccessfulOperationExecution(nodeWithDependencies, 5);
                if (testStep != null) {
                    targetNodeTestCase.addTestStep(testStep);
                    transitionCoverage.addTransitionCoverage(edge, targetNodeTestCase);
                }
            }
        }

        return transitionCoverage;
    }

    /**
     * Copy the content of the dictionary passed as parameter in the ResponseDictionary object
     * inside the tester
     * @param newResponseDictionary dictionary to copy from
     */
    private void copyResponseDictionaryFrom(ResponseDictionary newResponseDictionary) {
        responseDictionary = newResponseDictionary;
        fieldsGenerator = new FieldsGeneratorDictionary(openAPI, responseDictionary);
    }

    /**
     * Tries to test a successful operation execution
     * @param operationInfo target operation to test
     * @param maxNumOfTrials maximum number of tries (timeout)
     * @return testStep object if successfully tested, null otherwise
     */
    private TestStep testSuccessfulOperationExecution(OperationInfo operationInfo, int maxNumOfTrials) {
        int count = 0;
        TestStep testStep = null;
        while (count < maxNumOfTrials) {
            try {
                testStep = execOperation(operationInfo);
                ExecutionResult executionResult = testStep.getExecutionResult();
                if (executionResult.isSuccessful()) {
                    return testStep;
                }
            } catch (OperationExecutionException | TypeNotHandledException | FieldGenerationException | ApiResponseParsingException | SchemaValidationException | ParametersMismatchException e) {
                e.printStackTrace();
            }
            count += 1;
        }
        return null;
    }

    /**
     * Generates the parameter and call the operation's function
     * @param operation operation to execute
     * @return executed test step
     * @throws ApiResponseParsingException error during parsing of the server's response
     * @throws OperationExecutionException error during operation execution
     * @throws SchemaValidationException error during response validation
     * @throws TypeNotHandledException error during field generation
     * @throws FieldGenerationException error during parameter generation
     */
    public TestStep execOperation(OperationInfo operation) throws ApiResponseParsingException, OperationExecutionException, SchemaValidationException, TypeNotHandledException, FieldGenerationException, ParametersMismatchException {
        List<ExecutionParameter> generatedActualParameters = getActualParameters(operation);
        TestStep testStep = new TestStep(operation, generatedActualParameters);
        ExecutionResult executionResult = TestStepExecutor.execute(testStep);
        testStep.setExecutionResult(executionResult);
        List<ValidationError> errors = responseValidator.checkResponseSchemaValidity(testStep);
        executionResult.setValidationErrors(errors);
        return testStep;
    }

    /**
     * Extracts and generates values for input parameter required for the target operation
     * @param operation operation from which extract actual parameter
     * @return List of parameters (filled) ready to be executed
     * @throws TypeNotHandledException error during the field generation
     * @throws FieldGenerationException error during parameter generation
     */
    public List<ExecutionParameter> getActualParameters(OperationInfo operation) throws TypeNotHandledException, FieldGenerationException, ParametersMismatchException {
        // Get parameters
        List<ExecutionParameter> requestParameters = operation.getExecutionParameters();

        // Create values
        for (ExecutionParameter requestParameter : requestParameters) {
            // Get target class type
            Type parameterizedType = requestParameter.getParameter().getParameterizedType();
            Class<?> targetClass = requestParameter.getParameter().getType();
            if (parameterizedType instanceof ParameterizedType) {
                targetClass = (Class<?>) ((ParameterizedType)parameterizedType).getActualTypeArguments()[0];
            }

            // Get information from parameters
            Schema swaggerSchema = requestParameter.getParameterSchema();
            String parameterName = requestParameter.getSanitizedName();
            ManufactureTraces manufactureTraces = new ManufactureTraces();

            // Create a new object and add it as a value inside the ExecutionParameter
            Object value = this.fieldsGenerator.manufacturePojo(targetClass, swaggerSchema, manufactureTraces, parameterName);
            requestParameter.setValue(value);
            requestParameter.setManufactureTraces(manufactureTraces);

        }
        return requestParameters;
    }


    /**
     * Uses the OperationTester trying to successful test the target operationInfo
     * @param operationInfo target operationInfo to test
     * @return a testCase object or 'null' in case of unsuccessful test
     */
    private TestCase reachTestableState(OperationInfo operationInfo) {
        OperationResult operationResult = this.operationTester.testOperation(operationInfo);
        return operationResult.getResponseCoverage().getSuccessfulTestCase();
    }

    /**
     * Get the corresponding operationInfo object from OperationNode
     * @param operationInfos list of operationInfo objects
     * @param operationNode operation node
     * @return Matching operationInfo
     */
    private OperationInfo getMatchingOperationInfo(List<OperationInfo> operationInfos, OperationNode operationNode) {
        return operationInfos.get(operationInfos.indexOf(operationNode));
    }
}
