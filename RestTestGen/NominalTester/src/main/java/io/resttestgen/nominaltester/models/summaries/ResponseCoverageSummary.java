package io.resttestgen.nominaltester.models.summaries;

import java.util.List;

public class ResponseCoverageSummary {
    private String operationId;
    private String operationPath;
    private int numberOfDocumentedStatusCodes;
    private int numberOfTestedStatusCodes;
    private double statusCodeCoverageRatio;
    private int validationErrors;
    private int numberOfTimeouts;
    private List<String> listOfDocumentedStatusCodes; // [200, 404]
    private List<String> listOfTestedStatusCodes; // [404]
    private int numberOfTestedSuccessfulStatusCodes; // occurrences of 2xx
    private int numberOfTestedFailureStatusCodes; // occurrences of 4xx
    private int numberOfTestedErrorStatusCodes;  // occurrences of 5xx

    /*
    * GETTERS AND SETTERS
    * */

    public String getOperationPath() {
        return operationPath;
    }

    public void setOperationPath(String operationPath) {
        this.operationPath = operationPath;
    }

    public List<String> getListOfDocumentedStatusCodes() {
        return listOfDocumentedStatusCodes;
    }

    public void setListOfDocumentedStatusCodes(List<String> listOfDocumentedStatusCodes) {
        this.listOfDocumentedStatusCodes = listOfDocumentedStatusCodes;
    }

    public List<String> getListOfTestedStatusCodes() {
        return listOfTestedStatusCodes;
    }

    public void setListOfTestedStatusCodes(List<String> listOfTestedStatusCodes) {
        this.listOfTestedStatusCodes = listOfTestedStatusCodes;
    }

    public int getNumberOfTestedSuccessfulStatusCodes() {
        return numberOfTestedSuccessfulStatusCodes;
    }

    public void setNumberOfTestedSuccessfulStatusCodes(int numberOfTestedSuccessfulStatusCodes) {
        this.numberOfTestedSuccessfulStatusCodes = numberOfTestedSuccessfulStatusCodes;
    }

    public int getNumberOfTestedFailureStatusCodes() {
        return numberOfTestedFailureStatusCodes;
    }

    public void setNumberOfTestedFailureStatusCodes(int numberOfTestedFailureStatusCodes) {
        this.numberOfTestedFailureStatusCodes = numberOfTestedFailureStatusCodes;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public int getNumberOfDocumentedStatusCodes() {
        return numberOfDocumentedStatusCodes;
    }

    public void setNumberOfDocumentedStatusCodes(int numberOfDocumentedStatusCodes) {
        this.numberOfDocumentedStatusCodes = numberOfDocumentedStatusCodes;
    }

    public int getNumberOfTestedStatusCodes() {
        return numberOfTestedStatusCodes;
    }

    public void setNumberOfTestedStatusCodes(int numberOfTestedStatusCodes) {
        this.numberOfTestedStatusCodes = numberOfTestedStatusCodes;
    }

    public double getStatusCodeCoverageRatio() {
        return statusCodeCoverageRatio;
    }

    public void setStatusCodeCoverageRatio(double statusCodeCoverageRatio) {
        this.statusCodeCoverageRatio = statusCodeCoverageRatio;
    }

    public int getNumberOfTestedErrorStatusCodes() {
        return numberOfTestedErrorStatusCodes;
    }

    public void setNumberOfTestedErrorStatusCodes(int numberOfTestedErrorStatusCodes) {
        this.numberOfTestedErrorStatusCodes = numberOfTestedErrorStatusCodes;
    }

    public int getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(int validationErrors) {
        this.validationErrors = validationErrors;
    }

    public void setNumberOfTimeouts(int numberOfTimeouts) {
        this.numberOfTimeouts = numberOfTimeouts;
    }

    public int getNumberOfTimeouts() {
        return numberOfTimeouts;
    }
}
