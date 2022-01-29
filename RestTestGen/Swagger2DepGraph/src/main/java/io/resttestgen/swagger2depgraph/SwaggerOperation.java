package io.resttestgen.swagger2depgraph;

import io.swagger.v3.oas.models.Operation;

import java.util.Objects;

public class SwaggerOperation {
    protected Operation operationSchema;

    public Operation getOperationSchema() {
        return operationSchema;
    }

    public void setOperationSchema(Operation operationSchema) {
        this.operationSchema = operationSchema;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    protected String operationId;

    /**
     * @param operation operation schema from swagger
     * @param operationId sanitized operationId
     */
    public SwaggerOperation(Operation operation, String operationId) {
        this.operationSchema = operation;
        this.operationId = operationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SwaggerOperation)) return false;
        SwaggerOperation that = (SwaggerOperation) o;
        return Objects.equals(operationId, that.operationId); // Check only operationId
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationSchema, operationId);
    }
}
