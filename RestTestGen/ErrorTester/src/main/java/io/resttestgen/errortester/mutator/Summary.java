package io.resttestgen.errortester.mutator;

import java.util.Map;

public class Summary {
    private int testedRequiredMissingMutations, successfulRequiredMissingMutations;
    private Map<Integer, Integer> requiredMissingViolationsStatusCodes;
    private int testedWrongDataTypeMutations, successfulWrongDataTypeMutations;
    private Map<Integer, Integer> wrongDataTypeViolationsStatusCodes;
    private int testedConstraintViolationMutations, successfulConstraintViolationMutations;
    private Map<Integer, Integer> constraintViolationsStatusCodes;
    private int totalSuccessfulTestCases;
    private int testedSuccessfulTestCases;

    public Summary() {
    }

    public int getTestedRequiredMissingMutations() {
        return testedRequiredMissingMutations;
    }

    public void setTestedRequiredMissingMutations(int testedRequiredMissingMutations) {
        this.testedRequiredMissingMutations = testedRequiredMissingMutations;
    }

    public int getSuccessfulRequiredMissingMutations() {
        return successfulRequiredMissingMutations;
    }

    public void setSuccessfulRequiredMissingMutations(int successfulRequiredMissingMutations) {
        this.successfulRequiredMissingMutations = successfulRequiredMissingMutations;
    }

    public Map<Integer, Integer> getRequiredMissingViolationsStatusCodes() {
        return requiredMissingViolationsStatusCodes;
    }

    public void setRequiredMissingViolationsStatusCodes(Map<Integer, Integer> requiredMissingViolationsStatusCodes) {
        this.requiredMissingViolationsStatusCodes = requiredMissingViolationsStatusCodes;
    }


    public int getTestedWrongDataTypeMutations() {
        return testedWrongDataTypeMutations;
    }

    public void setTestedWrongDataTypeMutations(int testedWrongDataTypeMutations) {
        this.testedWrongDataTypeMutations = testedWrongDataTypeMutations;
    }

    public int getSuccessfulWrongDataTypeMutations() {
        return successfulWrongDataTypeMutations;
    }

    public void setSuccessfulWrongDataTypeMutations(int successfulWrongDataTypeMutations) {
        this.successfulWrongDataTypeMutations = successfulWrongDataTypeMutations;
    }

    public int getTestedConstraintViolationMutations() {
        return testedConstraintViolationMutations;
    }

    public void setTestedConstraintViolationMutations(int testedConstraintViolationMutations) {
        this.testedConstraintViolationMutations = testedConstraintViolationMutations;
    }

    public int getSuccessfulConstraintViolationMutations() {
        return successfulConstraintViolationMutations;
    }

    public void setSuccessfulConstraintViolationMutations(int successfulConstraintViolationMutations) {
        this.successfulConstraintViolationMutations = successfulConstraintViolationMutations;
    }

    public int getTotalSuccessfulTestCases() {
        return totalSuccessfulTestCases;
    }

    public void setTotalSuccessfulTestCases(int totalSuccessfulTestCases) {
        this.totalSuccessfulTestCases = totalSuccessfulTestCases;
    }

    public int getTestedSuccessfulTestCases() {
        return testedSuccessfulTestCases;
    }

    public void setTestedSuccessfulTestCases(int testedSuccessfulTestCases) {
        this.testedSuccessfulTestCases = testedSuccessfulTestCases;
    }

    public Map<Integer, Integer> getWrongDataTypeViolationsStatusCodes() {
        return wrongDataTypeViolationsStatusCodes;
    }

    public void setWrongDataTypeViolationsStatusCodes(Map<Integer, Integer> wrongDataTypeViolationsStatusCodes) {
        this.wrongDataTypeViolationsStatusCodes = wrongDataTypeViolationsStatusCodes;
    }

    public Map<Integer, Integer> getConstraintViolationsStatusCodes() {
        return constraintViolationsStatusCodes;
    }

    public void setConstraintViolationsStatusCodes(Map<Integer, Integer> constraintViolationsStatusCodes) {
        this.constraintViolationsStatusCodes = constraintViolationsStatusCodes;
    }
}
