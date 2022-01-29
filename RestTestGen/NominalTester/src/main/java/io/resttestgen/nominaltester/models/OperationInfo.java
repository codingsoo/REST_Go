package io.resttestgen.nominaltester.models;

import io.resttestgen.nominaltester.models.exceptions.ParametersMismatchException;
import io.resttestgen.swagger2depgraph.SwaggerOperation;
import io.swagger.codegen.v3.CodegenContent;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenParameter;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * OperationInfo is a classed used to group together API operationSchema information such as:
 * - Swagger code-gen invocation invocationMethod
 * - Corresponding swagger schema
 */
public class OperationInfo extends SwaggerOperation {
    private String operationPath;
    private HTTPMethod httpMethod;
    private String invocationClassName;
    private Method invocationMethod;
    private CodegenOperation codegenOperation;

    public OperationInfo(Operation operationSchema, String sanitizedOperationId) {
        super(operationSchema, sanitizedOperationId);
    }

    public Method getInvocationMethod() {
        return invocationMethod;
    }

    public void setInvocationMethod(Method invocationMethod) {
        this.invocationMethod = invocationMethod;
    }

    public io.swagger.v3.oas.models.Operation getOperationSchema() {
        return operationSchema;
    }

    public void setOperationSchema(io.swagger.v3.oas.models.Operation operationSchema) {
        this.operationSchema = operationSchema;
    }

    public String getInvocationClassName() {
        return invocationClassName;
    }

    public void setInvocationClassName(String invocationClassName) {
        this.invocationClassName = invocationClassName;
    }

    public String getOperationPath() {
        return operationPath;
    }

    public void setOperationPath(String operationPath) {
        this.operationPath = operationPath;
    }

    public HTTPMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HTTPMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public void setCodegenOperation(CodegenOperation codegenOperation) {
        this.codegenOperation = codegenOperation;
    }

    public CodegenOperation getCodegenOperation() {
        return codegenOperation;
    }

    /**
     * Get a list of execution parameters (without any generated value)
     * @return list of execution parameters
     */
    public List<ExecutionParameter> getExecutionParameters() throws ParametersMismatchException {
        Method swaggerGenMethod = getInvocationMethod();

        List<ExecutionParameter> executionParameters = new ArrayList<>();
        java.lang.reflect.Parameter[] methodParameters = swaggerGenMethod.getParameters();

        Optional<CodegenContent> matchingCodegenContent = getCodegenOperation().contents.stream().
                filter(c -> c.getParameters().size() == methodParameters.length)
                .findFirst();

        if (!matchingCodegenContent.isPresent()) {
            throw new ParametersMismatchException(operationId);
        }

        CodegenContent codegenContent = matchingCodegenContent.get();
        List<CodegenParameter> codegenParameters = codegenContent.getParameters();

        for (int i = 0; i < methodParameters.length; i++) {
            // Match method's parameter and codegen parameter
            CodegenParameter codegenParameter = codegenParameters.get(i);
            java.lang.reflect.Parameter methodParameter = methodParameters[i];
            Schema parameterSchema = codegenParameter.getSchema();
            String parameterSanitizedName = codegenParameter.getParamName();
            String parameterName = codegenParameter.getBaseName();
            boolean isRequired = codegenParameter.getRequired();

            // Create execution parameters
            ExecutionParameter executionParameter = new ExecutionParameter(parameterName);
            executionParameter.setParameterSchema(parameterSchema);
            executionParameter.setSanitizedName(parameterSanitizedName);
            executionParameter.setParameter(methodParameter);
            executionParameter.setRequired(isRequired);
            executionParameters.add(executionParameter);
        }

        return executionParameters;
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationSchema, operationPath, httpMethod, invocationClassName, invocationMethod, operationId);
    }

    @Override
    public String toString() {
        return "OperationInfo: " + getOperationId();
    }


}
