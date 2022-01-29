package io.resttestgen.nominaltester.models.coverage;

import io.resttestgen.nominaltester.models.OperationInfo;
import io.resttestgen.nominaltester.models.TestCase;
import io.resttestgen.nominaltester.models.TestStep;
import io.resttestgen.nominaltester.models.summaries.ResponseCoverageSummary;
import io.resttestgen.swaggerschema.models.ValidationError;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class containing a map with the coverage of the testable status code
 */
public class ResponseCoverage {
    static final Logger logger = LogManager.getLogger(ResponseCoverage.class);

    private Map<Integer, List<TestCase>> responseCoverage;

    public OperationInfo getTarget() {
        return target;
    }

    private OperationInfo target;

    public ResponseCoverage(OperationInfo target) {
        this.target = target;

        // Initialize response coverage hashmap
        responseCoverage = new HashMap<>();
        ApiResponses responses = target.getOperationSchema().getResponses();
        Set<Map.Entry<String, ApiResponse>> statusCodeResponseSchema = responses.entrySet();
        for (Map.Entry<String, ApiResponse> stringApiResponseEntry : statusCodeResponseSchema) {
            try {
                int statusCode = Integer.valueOf(stringApiResponseEntry.getKey());
                responseCoverage.put(statusCode, new ArrayList<>());
            } catch (NumberFormatException e) {
                logger.warn(stringApiResponseEntry.getKey() + " is not an HTTP status code");
            }
        }
    }

    /**
     * Add list of OperationExection used to cover a given status code
     * @param statusCode status code to cover
     * @param testCase test case (list of tests-steps)
     */
    public void addTestCase(int statusCode, TestCase testCase) {
        responseCoverage.putIfAbsent(statusCode, new ArrayList<>());
        responseCoverage.get(statusCode).add(testCase);
    }

    /**
     * Get a list of TestCase that lead to a status code 200
     * @param statusCode status code
     * @return Operation Test
     */
    public List<TestCase> getTestCases(int statusCode) {
        return responseCoverage.get(statusCode);
    }

    /**
     * Get the coverage map
     * @return return Map (status-code -> List<TestCase>)
     */
    public Map<Integer, List<TestCase>> getResponseCoverageMap() {
        return this.responseCoverage;
    }

    /**
     * Checks if it has been covered a successful (2xx) status code
     * @return true if there is a successful test case for status code (2xx), false otherwise
     */
    public boolean containsSuccessfulExecution() {
        Set<Integer> statusCodes = this.responseCoverage.keySet();
        for (Integer statusCode : statusCodes) {
            String firstLetterStatusCode = statusCode.toString().substring(0, 1);
            if (firstLetterStatusCode.equals("2") && this.responseCoverage.get(statusCode).size() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the first test case associated with a successful operation execution
     * @return TestCase for a successful execution (2xx) status code, null if does not exist any test case
     */
    public TestCase getSuccessfulTestCase() {
        Set<Integer> statusCodes = this.responseCoverage.keySet();
        for (Integer statusCode : statusCodes) {
            String firstLetterStatusCode = statusCode.toString().substring(0, 1);
            if (firstLetterStatusCode.equals("2") && this.responseCoverage.get(statusCode).size() > 0) {
                return this.responseCoverage.get(statusCode).get(0); // first one
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String repr = "";
        for (Map.Entry<Integer, List<TestCase>> statusCoverage : responseCoverage.entrySet()) {
            Integer statusCode = statusCoverage.getKey();
            List<TestCase> value = statusCoverage.getValue();
            repr += String.format(" %d: %d; ", statusCode, value.size());
        }
        return repr;
    }

    /**
     * Merge another response coverage object with the existing map
     * @param responseCoverage response coverage to merge
     */
    public void mergeResponseCoverage(ResponseCoverage responseCoverage) {
        Map<Integer, List<TestCase>> existingResponseCoverage = this.getResponseCoverageMap();
        Map<Integer, List<TestCase>> responseCoverageToMerge = responseCoverage.getResponseCoverageMap();
        for (Map.Entry<Integer, List<TestCase>> entry : responseCoverageToMerge.entrySet()) {
            Integer statusCode = entry.getKey();// status code
            List<TestCase> value = entry.getValue(); // test case
            existingResponseCoverage.computeIfAbsent(statusCode, k -> new ArrayList<>());
            existingResponseCoverage.get(statusCode).addAll(value);
        }
    }

    /**
     * Create a ResponseCoverageReport
     * @return a responseCoverageReport
     */
    public ResponseCoverageSummary getSummary() {
        ResponseCoverageSummary report = new ResponseCoverageSummary();
        report.setOperationId(target.getOperationId());
        report.setOperationPath(target.getOperationPath());

        // Set response coverage fields
        ApiResponses responses = target.getOperationSchema().getResponses();
        Set<Map.Entry<String, ApiResponse>> statusCodeResponseSchema = responses.entrySet();
        int numberOfDocumentedStatusCode = statusCodeResponseSchema.size();
        int numberOfTestedStatusCode = (int) responseCoverage.entrySet().stream().filter(x -> x.getValue().size() > 0 && x.getKey() > 0).count();
        double statusCodeCoverageRatio = (double)numberOfTestedStatusCode / (double)numberOfDocumentedStatusCode * 100;

        report.setNumberOfDocumentedStatusCodes(numberOfDocumentedStatusCode);
        report.setNumberOfTestedStatusCodes(numberOfTestedStatusCode);
        report.setStatusCodeCoverageRatio(statusCodeCoverageRatio);

        // Get documented list of status codes
        List<String> listOfDocumentedStatusCodes = statusCodeResponseSchema.stream()
                .map(Map.Entry::getKey).collect(Collectors.toList());
        report.setListOfDocumentedStatusCodes(listOfDocumentedStatusCodes);

        // Get list of tested status codes
        List<String> listOfTestedStatusCodes  = responseCoverage.entrySet().stream()
                .filter(x -> x.getValue().size() > 0 && x.getKey() > 0)
                .map(x -> x.getKey().toString()).collect(Collectors.toList());
        report.setListOfTestedStatusCodes(listOfTestedStatusCodes);

        // Counters of tested status codes
        int testedErrorStatusCodes = 0;
        int testedFailureStatusCodes = 0;
        int testedSuccessfulStatusCodes = 0;
        Set<Integer> statusCodes = this.responseCoverage.keySet();
        for (Integer statusCode : statusCodes) {
            String firstLetterStatusCode = statusCode.toString().substring(0, 1);
            int numberOfTestCase = this.responseCoverage.get(statusCode).size();
            if (numberOfTestCase > 0) {
                switch (firstLetterStatusCode) {
                    case "5":
                        testedErrorStatusCodes += 1;
                        break;
                    case "2":
                        testedSuccessfulStatusCodes += 1;
                        break;
                    case "4":
                        // 4xx but not 401 and 403
                        if (statusCode != 401 && statusCode != 403)
                            testedFailureStatusCodes += 1;
                }
            }
        }
        report.setNumberOfTestedErrorStatusCodes(testedErrorStatusCodes);
        report.setNumberOfTestedSuccessfulStatusCodes(testedSuccessfulStatusCodes);
        report.setNumberOfTestedFailureStatusCodes(testedFailureStatusCodes);

        // Check number of validation errors
        int numberOfValidationErrors = 0;
        for (Integer statusCode : statusCodes) {
            List<TestCase> testCases = this.responseCoverage.get(statusCode);
            for (TestCase testCase : testCases) {
                List<TestStep> testSteps = testCase.getTestSteps();
                for (TestStep testStep : testSteps) {
                    List<ValidationError> validationErrors = testStep.getExecutionResult().getValidationErrors();
                    numberOfValidationErrors += validationErrors.size();
                }
            }
        }
        report.setValidationErrors(numberOfValidationErrors);

        // Set number of timeouts
        int numberOfTimeouts = 0;
        List<TestCase> testCases = this.responseCoverage.get(0);
        if (testCases != null) {
            for (TestCase testCase : testCases) {
                numberOfTimeouts += testCase.getTestSteps().size();
            }
        }
        report.setNumberOfTimeouts(numberOfTimeouts);
        return report;
    }
}
