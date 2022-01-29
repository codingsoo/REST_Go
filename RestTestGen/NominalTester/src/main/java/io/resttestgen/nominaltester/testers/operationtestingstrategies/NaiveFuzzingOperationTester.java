package io.resttestgen.nominaltester.testers.operationtestingstrategies;

import io.resttestgen.nominaltester.cli.ExAppConfig;
import io.resttestgen.nominaltester.fieldgenerator.exceptions.FieldGenerationException;
import io.resttestgen.nominaltester.fieldgenerator.exceptions.TypeNotHandledException;
import io.resttestgen.nominaltester.helper.exceptions.ApiResponseParsingException;
import io.resttestgen.nominaltester.models.*;
import io.resttestgen.nominaltester.models.coverage.Coverage;
import io.resttestgen.nominaltester.models.coverage.OperationCoverage;
import io.resttestgen.nominaltester.models.coverage.ResponseCoverage;
import io.resttestgen.nominaltester.models.exceptions.ParametersMismatchException;
import io.resttestgen.nominaltester.reports.reportwriter.ReportWriter;
import io.resttestgen.nominaltester.testcases.junitwriter.JunitWriter;
import io.resttestgen.nominaltester.testcases.junitwriter.exceptions.JunitBuilderException;
import io.resttestgen.nominaltester.testers.OperationTester;
import io.resttestgen.nominaltester.testers.exceptions.OperationExecutionException;
import io.resttestgen.swaggerschema.models.exceptions.SchemaValidationException;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tester class contains methods and fields required to test operations
 * E.g. It has a response dictionary which is used during the parameter generation and
 * the method testOperation to execute the operation, getting the coverage
 */
public class NaiveFuzzingOperationTester extends OperationTester {

    protected int DEFAULT_NUMBER_OF_TRIES = 5;
    static final Logger logger = LogManager.getLogger(NaiveFuzzingOperationTester.class);

    protected int numberOfTries;
    public NaiveFuzzingOperationTester(OpenAPI openAPI, Map<String, List<OperationInfo>> operationsPerApiClass) throws SchemaValidationException {
        super(openAPI, operationsPerApiClass);
        setNumberOfTries(DEFAULT_NUMBER_OF_TRIES);
    }

    public Coverage run() {
        OperationCoverage operationCoverage = new OperationCoverage();

        List<OperationInfo> operations = this.operationsPerApiClass.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toList());

        operationCoverage.setNumberOfDocumentedOperations(operations.size());

        // Instantiate writers
        ReportWriter reportWriter = new ReportWriter(this.openAPI, Paths.get(ExAppConfig.outputFolder,"reports/"));
        JunitWriter junitWriter = new JunitWriter(this.openAPI, Paths.get(ExAppConfig.outputFolder,"src/test/java/"));

        for (OperationInfo operation : operations) {
            try {
                logger.info("Testing operation " + operation.toString());
                OperationResult operationResult = testOperation(operation);
                ResponseCoverage responseCoverage = operationResult.getResponseCoverage();
                operationCoverage.addOperationResult(operation.getOperationId(), operationResult);
                logger.info(operationResult.getResponseCoverage().toString());
                logger.info("Writing report on file /reports/" + operation.getOperationId() + ".json");
                reportWriter.toJsonFile(responseCoverage, operation.getOperationId());
                logger.info("Writing junit test cases /src/test/java/" + operation.getOperationId() + "_*.java");
                junitWriter.fromResponseCoverage(responseCoverage);
            } catch (IOException | JunitBuilderException e) {
                logger.error("Cannot write report/tests", e);
                operationCoverage.addOperationResult(operation.getOperationId(), new OperationResult(operation));
            }

            try {
                reportWriter.writeOperationCoverage(operationCoverage);
            } catch (IOException e) {
                logger.error("Cannot write summary report", e);
                e.printStackTrace();
            }
        }

        logger.info("All the operations have been processed");

        return operationCoverage;
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
     * @throws OperationExecutionException error during operation execution
     * @throws TypeNotHandledException error during field generation
     * @throws FieldGenerationException error during parameter generation
     * @throws ApiResponseParsingException  error during response parsing
     * @throws SchemaValidationException error during response validation
     */
    public OperationResult testOperation(OperationInfo operationInfo) {

        // Invoke hooks
        boolean resetSuccess = reset();
        boolean authSuccess = authenticate();

        logger.debug("Reset method result: " + resetSuccess);
        logger.debug("Auth method result: " + authSuccess);

        OperationResult operationResult = new OperationResult(operationInfo);

        // To store all operation executions (both successful and not)
        List<TestStep> successfulTestcaseSteps = new ArrayList<>();

        // Exec max 5 times the target operation
        for (int i = 0; i < numberOfTries; i++) {
            TestStep testStep = null;
            try {
                testStep = execOperation(operationInfo, operationResult);
                ExecutionResult executionResult = testStep.getExecutionResult();
                successfulTestcaseSteps.add(testStep);

                // Add execution result in operation coverage
                // if successful (2xx) -> stop the test
                int statusCode = executionResult.getStatusCode();

                if (statusCode == 429) {
                    pauseTestingAccordingToCurrentSleepTime();
                } else {
                    resetCurrentSpleepTime();
                }

                if (executionResult.isSuccessful()) {
                    TestCase successfulTestcase = new TestCase(successfulTestcaseSteps);
                    successfulTestcase.setAuthentication(this.authentication);
                    operationResult.getResponseCoverage().addTestCase(statusCode, successfulTestcase);
                    return operationResult;
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

    private void setNumberOfTries(int numberOfTries) {
        this.numberOfTries = numberOfTries;
    }

}
