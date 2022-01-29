package io.resttestgen.swagger2depgraph;


import io.swagger.v3.oas.models.Operation;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class OperationNode extends SwaggerOperation {

    private Set<OperationParameter> inputParameters;
    private Set<OperationParameter> outputParameters;

    /**
     * @param operation target operation schema
     * @param operationId sanitized operation id
     * @param inputParameters set of input parameters (recursively)
     * @param outputParameters set of output parameters
     */
    public OperationNode(Operation operation, String operationId, Set<String> inputParameters, Set<String> outputParameters) {
        super(operation, operationId);
        this.inputParameters = inputParameters.stream().map(OperationParameter::new).collect(Collectors.toSet());
        this.outputParameters = outputParameters.stream().map(OperationParameter::new).collect(Collectors.toSet());;
    }

    public String getOperationId() {
        return operationId;
    }

    public Set<OperationParameter> getOutputParameters() {
        return outputParameters;
    }

    public Set<OperationParameter> getInputParameters() {
        return inputParameters;
    }

    @Override
    public String toString() {
        return "Node: " + this.getOperationId();
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationSchema, operationId, inputParameters, outputParameters);
    }
}
