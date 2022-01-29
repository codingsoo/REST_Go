package io.resttestgen.swaggerschema.models;

import io.swagger.v3.oas.models.Operation;

import java.util.Objects;

public class SwaggerOperation {

    private final HTTPMethod httpMethod;
    protected Operation operationSchema;
    protected String operationId;
    private String path;

    /**
     * @param operation operation schema from swagger
     * @param operationId sanitized operationId
     */
    public SwaggerOperation(Operation operation, String operationId, HTTPMethod httpMethod) {
        this.operationSchema = operation;
        this.operationId = operationId;
        this.httpMethod = httpMethod;
    }

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

    public HTTPMethod getHttpMethod() {
        return httpMethod;
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

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
