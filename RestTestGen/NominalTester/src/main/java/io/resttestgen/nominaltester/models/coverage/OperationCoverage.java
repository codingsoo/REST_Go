package io.resttestgen.nominaltester.models.coverage;

import io.resttestgen.nominaltester.models.OperationResult;
import io.resttestgen.nominaltester.models.TestCase;
import io.resttestgen.nominaltester.models.summaries.OperationCoverageSummary;
import io.resttestgen.nominaltester.models.summaries.ResponseCoverageSummary;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class OperationCoverage extends Coverage {
    Map<String, OperationResult> operationResultMaps;
    private int numberOfDocumentedOperations = 0;

    public OperationCoverage() {
        operationResultMaps = new HashMap<>();
    }

    public void setNumberOfDocumentedOperations(int numberOfDocumentedOperations) {
        this.numberOfDocumentedOperations = numberOfDocumentedOperations;
    }

    public int getNumberOfDocumentedOperations() {
        return numberOfDocumentedOperations;
    }

    public Map<String, OperationResult> getOperationResultMap() {
        return operationResultMaps;
    }

    public void addOperationResult(String operationId, OperationResult operationResult) {
        operationResultMaps.put(operationId, operationResult);
    }

    public void addOrMergeOperationResult(String operationId, OperationResult operationResult) {
        OperationResult existingOperationResult = operationResultMaps.get(operationId);
        if (existingOperationResult == null) {
            operationResultMaps.put(operationId, operationResult);
        } else {
            ResponseCoverage existingResponseCoverage = existingOperationResult.getResponseCoverage();
            ResponseCoverage responseCoverage = operationResult.getResponseCoverage();
            existingResponseCoverage.mergeResponseCoverage(responseCoverage);
        }
    }

    public OperationResult getOperationCoverage(String operationId) {
        return operationResultMaps.get(operationId);
    }

    @Override
    public String toString() {
        Collection<ResponseCoverage> responseCoverages = operationResultMaps.values()
                .stream().map(OperationResult::getResponseCoverage).collect(Collectors.toList());
        int successfulResponseCoverage = (int) responseCoverages.stream()
                .filter(r -> r.getSuccessfulTestCase() != null).count();
        double successfulPercentage = (double)successfulResponseCoverage / (double)responseCoverages.size() * 100;
        return String.format("%d operation; %d with a 2xx test case (%s%%)", responseCoverages.size(), successfulResponseCoverage, new DecimalFormat("#.##").format(successfulPercentage));
    }

    public OperationCoverageSummary getReport() {
        Collection<OperationResult> operationResults = operationResultMaps.values();
        HashMap<String, OperationResult> reports = new HashMap<>();

        int numberOfOperationsWithValidationErrors = 0;
        int numberOfOperationsWithTestedErrorStatusCode = 0;
        int numberOfOperationsWithTestedSuccessfulStatusCode = 0;
        int numberOfOperationsWithTestedFailureStatusCode = 0;
        int numberOfOperationsWhichRequireAuthorization = 0;
        int numberOfProcessedOperations = operationResults.size();
        int numberOfOperationsWithExceptions = 0;

        for (OperationResult operationResult : operationResults) {
            ResponseCoverage responseCoverage = operationResult.getResponseCoverage();
            Map<Integer, List<TestCase>> responseCoverageMap = responseCoverage.getResponseCoverageMap();
            ResponseCoverageSummary report = responseCoverage.getSummary();
            reports.put(operationResult.getOperationId(), operationResult);

            // Get list of tested status code
            Set<Integer> testedStatusCodes = responseCoverageMap.entrySet().stream()
                    .filter((e -> e.getValue() != null && e.getValue().size() > 0))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            if (operationResult.getExceptions() != null && operationResult.getExceptions().size() > 0) {
                numberOfOperationsWithExceptions++;
            }

            if (testedStatusCodes.contains(401) || testedStatusCodes.contains(403)) {
                numberOfOperationsWhichRequireAuthorization++;
            }

            if (report.getValidationErrors() > 0) {
                numberOfOperationsWithValidationErrors++;
            }

            if (report.getNumberOfTestedErrorStatusCodes() > 0) {
                numberOfOperationsWithTestedErrorStatusCode++;
            }

            if (report.getNumberOfTestedSuccessfulStatusCodes() > 0) {
                numberOfOperationsWithTestedSuccessfulStatusCode++;
            }

            if (report.getNumberOfTestedFailureStatusCodes() > 0) {
                numberOfOperationsWithTestedFailureStatusCode++;
            }
        }

        double successfulPercentage = 0;
        if (numberOfDocumentedOperations > 0)
            successfulPercentage = (double)numberOfOperationsWithTestedSuccessfulStatusCode / (double)numberOfDocumentedOperations * 100;

        OperationCoverageSummary operationCoverageReport = new OperationCoverageSummary();
        operationCoverageReport.setNumberOfProcessedOperations(numberOfProcessedOperations);
        operationCoverageReport.setOperationsResults(reports);
        operationCoverageReport.setSuccessfulOperationsRatio(successfulPercentage);
        operationCoverageReport.setNumberOfOperationsWithValidationErrors(numberOfOperationsWithValidationErrors);
        operationCoverageReport.setNumberOfOperationsWithTestedErrorStatusCode(numberOfOperationsWithTestedErrorStatusCode);
        operationCoverageReport.setNumberOfOperationsWithTestedFailureStatusCode(numberOfOperationsWithTestedFailureStatusCode);
        operationCoverageReport.setNumberOfOperationsWithTestedSuccessfulStatusCode(numberOfOperationsWithTestedSuccessfulStatusCode);
        operationCoverageReport.setNumberOfOperationsWhichRequireAuthorization(numberOfOperationsWhichRequireAuthorization);
        operationCoverageReport.setNumberOfOperationsWithExceptions(numberOfOperationsWithExceptions);
        operationCoverageReport.setNumberOfDocumentedOperations(numberOfDocumentedOperations);

        return operationCoverageReport;
    }

}
