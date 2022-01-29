package io.resttestgen.errortester.mutator;

import io.resttestgen.swaggerschema.models.ValidationError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MutatorResult {
    
    private int executedMutations;
    private int mutationsWithViolations;
    private Map<Integer, Integer> statusCodeMap;
    private List<ValidationError> validationErrorList;

    public MutatorResult(int executedMutations, int mutationsWithViolations) {
        this.executedMutations = executedMutations;
        this.mutationsWithViolations = mutationsWithViolations;
        this.statusCodeMap = new HashMap<>();
        validationErrorList = new ArrayList<>();
    }

    public MutatorResult() {
        this.statusCodeMap = new HashMap<>();
        if (validationErrorList == null) {
            validationErrorList = new ArrayList<>();
        }
    }

    public int getExecutedMutations() {
        return executedMutations;
    }

    public void setExecutedMutations(int executedMutations) {
        this.executedMutations = executedMutations;
    }

    public void incrementExecutedMutations() {
        this.executedMutations++;
    }

    public int getMutationsWithViolations() {
        return mutationsWithViolations;
    }

    public void setMutationsWithViolations(int mutationsWithViolations) {
        this.mutationsWithViolations = mutationsWithViolations;
    }

    public void incrementMutationsWithViolations() {
        this.mutationsWithViolations++;
    }

    public Map<Integer, Integer> getStatusCodeMap() {
        return statusCodeMap;
    }

    public void setStatusCodeMap(Map<Integer, Integer> statusCodeMap) {
        this.statusCodeMap = statusCodeMap;
    }

    public void addStatusCode(int statusCode) {
        this.statusCodeMap.putIfAbsent(statusCode, 0);
        int i = this.statusCodeMap.get(statusCode);
        this.statusCodeMap.put(statusCode, i + 1);
    }

    public void addViolation(int statusCode) {
        addStatusCode(statusCode);
        incrementMutationsWithViolations();
    }

    public void addViolation(ValidationError validationError) {
        validationErrorList.add(validationError);
        incrementMutationsWithViolations();
    }

    public void addViolation(List<ValidationError> validationErrors) {
        for (ValidationError validationError : validationErrors) {
            addViolation(validationError);
        }
    }
}
