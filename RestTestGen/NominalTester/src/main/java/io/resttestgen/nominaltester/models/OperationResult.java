package io.resttestgen.nominaltester.models;

import io.resttestgen.nominaltester.models.coverage.ResponseCoverage;

import java.util.HashSet;
import java.util.Set;

public class OperationResult {
    private String operationId;
    private Set<String> exceptions;
    private ResponseCoverage responseCoverage;

    public OperationResult(OperationInfo operation) {
        this.operationId = operation.getOperationId();
        exceptions = new HashSet<>();
        responseCoverage = new ResponseCoverage(operation);
    }

    public ResponseCoverage getResponseCoverage() {
        return responseCoverage;
    }

    public Set<String> getExceptions() {
        return exceptions;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setResponseCoverage(ResponseCoverage responseCoverage) {
        this.responseCoverage = responseCoverage;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
}
