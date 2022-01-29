package io.resttestgen.nominaltester.models;

import io.resttestgen.requestbuilder.Request;
import io.resttestgen.requestbuilder.parameters.RequestParameter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Identifies an Operation Execution
 * it is composed of a target operation and its parameters
 */
public class TestStep {

    static final Logger logger = LogManager.getLogger(TestStep.class);

    private OperationInfo targetOperation;
    private List<ExecutionParameter> executionParameters;
    private ExecutionResult executionResult;

    // Deserialization only
    private Request request;
    private List<RequestParameter> requestParameters;
    private int statusCode;

    public TestStep(OperationInfo operation, List<ExecutionParameter> parameters) {
        this.targetOperation = operation;
        this.executionParameters = parameters;
    }

    /*
    * Getter and setters
    * */
    public OperationInfo getTargetOperation() {
        return targetOperation;
    }

    public List<ExecutionParameter> getExecutionParameters() {
        return executionParameters;
    }

    public ExecutionResult getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(ExecutionResult executionResult) {
        this.executionResult = executionResult;
        this.statusCode = executionResult.getStatusCode();
    }

    @Override
    public String toString() {
        String executedStatus = (executionResult != null) ? "Executed with code " + executionResult.getStatusCode() : "Not executed";
        return String.format("Operation %s (%s)", targetOperation.getOperationId(), executedStatus );
    }

    /**
     * Returns the execution parameters of the test step mapped by their name
     * @return a Map of ExecutionParameters representing the execution parameters mapped by their name
     */
    public Map<String, ExecutionParameter> getParametersMap() {
        return executionParameters.stream().collect(Collectors.toMap(ExecutionParameter::getSanitizedName,
                executionParameter -> executionParameter));
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public void setRequestParameters(List<RequestParameter> requestParameters) {
        this.requestParameters = requestParameters;
    }

    public List<RequestParameter> getRequestParameters() {
        return requestParameters;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return executionResult == null ? 0 : executionResult.getStatusCode();
    }
}
