package io.resttestgen.nominaltester.testers.operationtestingstrategies;

import io.resttestgen.nominaltester.fieldgenerator.FieldsGeneratorDictionary;
import io.resttestgen.nominaltester.fieldgenerator.exceptions.FieldGenerationException;
import io.resttestgen.nominaltester.fieldgenerator.exceptions.TypeNotHandledException;
import io.resttestgen.nominaltester.helper.exceptions.ApiResponseParsingException;
import io.resttestgen.nominaltester.models.*;
import io.resttestgen.nominaltester.models.exceptions.ParametersMismatchException;
import io.resttestgen.nominaltester.testers.DictionaryBasedTester;
import io.resttestgen.nominaltester.testers.exceptions.OperationExecutionException;
import io.resttestgen.swaggerschema.models.exceptions.SchemaValidationException;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tester class contains methods and fields required to test operations
 * E.g. It has a response dictionary which is used during the parameter generation and
 * the method testOperation to execute the operation, getting the coverage
 */
public class FuzzingWithDictionaryOperationTester extends NaiveFuzzingOperationTester implements DictionaryBasedTester {

    static final Logger logger = LogManager.getLogger(FuzzingWithDictionaryOperationTester.class);

    private ResponseDictionary responseDictionary;
    private List<TestStep> successfulTestSteps;

    public FuzzingWithDictionaryOperationTester(OpenAPI openAPI, Map<String, List<OperationInfo>> operationsPerApiClass) throws SchemaValidationException {
        super(openAPI, operationsPerApiClass);
        responseDictionary = new ResponseDictionary();
        fieldsGenerator = new FieldsGeneratorDictionary(openAPI, responseDictionary);
        successfulTestSteps = new ArrayList<>();
    }

    /**
     * Tries to execute successfully (2xx) the target operation
     * In the mean time, it fills the operation coverage map
     * with other obtained results.
     *
     * If the execution is not successful, execute one of the dependencies
     * and re-try.
     *
     * @param operationInfo operation to test
     * @return Operation Coverage object, filled with the results of the test
     */
    public OperationResult testOperation(OperationInfo operationInfo, Integer operationTimeBudget) {

        // Invoke hooks
        boolean resetSuccess = reset();
        boolean authSuccess = authenticate();

        logger.debug("Reset method result: " + resetSuccess);
        logger.debug("Auth method result: " + authSuccess);

        OperationResult operationResult = new OperationResult(operationInfo);

        // Exec max 5 times the target operation
        //for (int i = 0; i < numberOfTries; i++) {

        Long operationTestEndTime = System.currentTimeMillis() + operationTimeBudget * 1000;

        // Exec the operation for the time budget
        while (System.currentTimeMillis() < operationTestEndTime) {
            try {
                TestStep testStep = execAndAddToDictionary(operationInfo, operationResult);
                ExecutionResult executionResult = testStep.getExecutionResult();

                // Add execution result in operation coverage
                // if successful (2xx) -> stop the test
                int statusCode = executionResult.getStatusCode();

                if (statusCode == 429) {
                    pauseTestingAccordingToCurrentSleepTime();
                } else {
                    resetCurrentSpleepTime();
                }

                if (executionResult.isSuccessful()) {
                    successfulTestSteps.add(testStep);
                    TestCase successfulTestcase = new TestCase(successfulTestSteps);
                    successfulTestcase.setAuthentication(this.authentication);
                    operationResult.getResponseCoverage().addTestCase(statusCode, successfulTestcase);
                    //break;    // removed break to continue testing ao when 200 status code is obtained
                } else {
                    TestCase unsuccessfulTestCase = new TestCase();
                    unsuccessfulTestCase.setAuthentication(this.authentication);
                    unsuccessfulTestCase.addTestStep(testStep);
                    operationResult.getResponseCoverage().addTestCase(statusCode, unsuccessfulTestCase);
                }
            } catch (OperationExecutionException | ApiResponseParsingException |
                    TypeNotHandledException | FieldGenerationException | ParametersMismatchException e) {
                logger.error("Error during operation execution", e);
                String exceptionRep = e.getClass().getName();
                if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                    exceptionRep = String.format("%s:%s", e.getClass().getName(), e.getMessage());
                }
                operationResult.getExceptions().add(exceptionRep);
            }
        }

        return operationResult;
    }

    /**
     * Executes the operation and, if successful, add the response into the dictionary
     * @param target operation to execute
     * @param operationResult operation results to store exceptions during execution
     * @return Executed test step
     * @throws OperationExecutionException error during operation execution
     * @throws ApiResponseParsingException error during response parsing
     * @throws TypeNotHandledException error during field generation
     * @throws FieldGenerationException error during parameter generation
     */
    private TestStep execAndAddToDictionary(OperationInfo target, OperationResult operationResult) throws OperationExecutionException, ApiResponseParsingException, TypeNotHandledException, FieldGenerationException, ParametersMismatchException {
        TestStep testStep = execOperation(target, operationResult);
        logger.debug("Executed method " + testStep);

        // If  successful add response content to Tester's dictionary
        ExecutionResult executionResult = testStep.getExecutionResult();
        if (executionResult.isSuccessful()) {
            logger.debug("Operation " + testStep);
            logger.debug("Adding response data on dictionary");
            this.responseDictionary.addJSONFieldsToDictionary(executionResult.getResponseBody());
        }

        return testStep;
    }

    /**
     * Replace the ResponseDictionary object
     * @param responseDictionary new responseDictionary object
     */
    public void setResponseDictionary(ResponseDictionary responseDictionary) {
        this.responseDictionary = responseDictionary;
    }

    /**
     * Returns the tester response dictionary
     * @return ResponseDictionary object
     */
    public ResponseDictionary getResponseDictionary() {
        return responseDictionary;
    }


    /**
     * Reset ResponseDictionary
     */
    public void resetResponseDictionary() {
        responseDictionary = new ResponseDictionary();
        fieldsGenerator = new FieldsGeneratorDictionary(openAPI, responseDictionary);
    }
}
