package io.resttestgen.nominaltester.models.summaries;

import io.resttestgen.nominaltester.models.OperationResult;

import java.util.HashMap;

public class OperationCoverageSummary {

    private int numberOfDocumentedOperations = 0;
    private int numberOfProcessedOperations = 0;
    private int numberOfOperationsWithValidationErrors = 0;
    private int numberOfOperationsWithTestedErrorStatusCode = 0;
    private int numberOfOperationsWithTestedSuccessfulStatusCode = 0;
    private int numberOfOperationsWithTestedFailureStatusCode = 0;
    private int numberOfOperationsWhichRequireAuthorization = 0;
    private int numberOfOperationsWithExceptions = 0;

    public int getNumberOfOperationsWithExceptions() {
        return numberOfOperationsWithExceptions;
    }

    public void setNumberOfOperationsWithExceptions(int numberOfOperationsWithExceptions) {
        this.numberOfOperationsWithExceptions = numberOfOperationsWithExceptions;
    }

    public int getNumberOfOperationsWhichRequireAuthorization() {
        return numberOfOperationsWhichRequireAuthorization;
    }

    public void setNumberOfOperationsWhichRequireAuthorization(int numberOfOperationsWhichRequireAuthorization) {
        this.numberOfOperationsWhichRequireAuthorization = numberOfOperationsWhichRequireAuthorization;
    }

    private double successfulOperationsRatio = 0; // numberOfOperationsWithTestedSuccessfulStatusCode / all

    public int getNumberOfOperationsWithValidationErrors() {
        return numberOfOperationsWithValidationErrors;
    }

    public void setNumberOfOperationsWithValidationErrors(int numberOfOperationsWithValidationErrors) {
        this.numberOfOperationsWithValidationErrors = numberOfOperationsWithValidationErrors;
    }

    public int getNumberOfOperationsWithTestedErrorStatusCode() {
        return numberOfOperationsWithTestedErrorStatusCode;
    }

    public void setNumberOfOperationsWithTestedErrorStatusCode(int numberOfOperationsWithTestedErrorStatusCode) {
        this.numberOfOperationsWithTestedErrorStatusCode = numberOfOperationsWithTestedErrorStatusCode;
    }

    public int getNumberOfOperationsWithTestedSuccessfulStatusCode() {
        return numberOfOperationsWithTestedSuccessfulStatusCode;
    }

    public void setNumberOfOperationsWithTestedSuccessfulStatusCode(int numberOfOperationsWithTestedSuccessfulStatusCode) {
        this.numberOfOperationsWithTestedSuccessfulStatusCode = numberOfOperationsWithTestedSuccessfulStatusCode;
    }

    public double getSuccessfulOperationsRatio() {
        return successfulOperationsRatio;
    }

    public void setSuccessfulOperationsRatio(double successfulOperationsRatio) {
        this.successfulOperationsRatio = successfulOperationsRatio;
    }

    public int getNumberOfOperationsWithTestedFailureStatusCode() {
        return numberOfOperationsWithTestedFailureStatusCode;
    }

    public void setNumberOfOperationsWithTestedFailureStatusCode(int numberOfOperationsWithTestedFailureStatusCode) {
        this.numberOfOperationsWithTestedFailureStatusCode = numberOfOperationsWithTestedFailureStatusCode;
    }

    public int getNumberOfProcessedOperations() {
        return numberOfProcessedOperations;
    }

    public void setNumberOfProcessedOperations(int numberOfProcessedOperations) {
        this.numberOfProcessedOperations = numberOfProcessedOperations;
    }

    private HashMap<String, OperationResult> operationsResults;

    public HashMap<String, OperationResult> getOperationsResults() {
        return operationsResults;
    }

    public void setOperationsResults(HashMap<String, OperationResult> operationsResults) {
        this.operationsResults = operationsResults;
    }

    public void setNumberOfDocumentedOperations(int numberOfDocumentedOperations) {
        this.numberOfDocumentedOperations = numberOfDocumentedOperations;
    }

    public int getNumberOfDocumentedOperations() {
        return numberOfDocumentedOperations;
    }
}
