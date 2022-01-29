package io.resttestgen.nominaltester.helper;

import io.resttestgen.nominaltester.helper.exceptions.ApiResponseParsingException;
import io.resttestgen.nominaltester.models.ExecutionParameter;
import io.resttestgen.nominaltester.models.ExecutionResult;
import io.resttestgen.nominaltester.models.OperationInfo;
import io.resttestgen.nominaltester.models.TestStep;
import io.resttestgen.nominaltester.models.exceptions.ParametersMismatchException;
import io.resttestgen.nominaltester.testers.exceptions.OperationExecutionException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class TestStepExecutor {

    /**
     * Execute the TestStep and fill up the executionResponse object
     * @return Execution Response object
     * @throws ApiResponseParsingException failed to parse the operation's response, documentation mismatch error (?)
     * @throws OperationExecutionException failed to execute the operation with the given parameters
     */
    public static ExecutionResult execute(TestStep testStep) throws ApiResponseParsingException, OperationExecutionException, ParametersMismatchException {
        OperationInfo targetOperation = testStep.getTargetOperation();
        Method swaggerGenMethod = testStep.getTargetOperation().getInvocationMethod();
        List<ExecutionParameter> executionParameters = testStep.getExecutionParameters();

        // Instantiate API class object
        Class<?> declaringClass = swaggerGenMethod.getDeclaringClass();
        Object apiObject;

        try {
            apiObject = declaringClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new OperationExecutionException("Cannot create API class", e);
        }

        Class<?>[] parameterTypes = swaggerGenMethod.getParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];

        for (int i = 0; i < parameters.length; i++) {
            try {
                if (i < executionParameters.size()) {
                    parameters[i] = parameterTypes[i].cast(executionParameters.get(i).getValue());
                } else {
                    // Set to null the possibly optional parameters which have not been generated
                    parameters[i] = null;
                }
            } catch (ClassCastException c) {
                throw new ParametersMismatchException("Cannot convert " + executionParameters.get(i).getValue().getClass().getName() +
                        " to " + parameterTypes[i].toString());
            }
        }

        ApiResponseParser apiResponseParser = new ApiResponseParser();

        ExecutionResult executionResult;
        try {
            // Exec
            Object returnValue = swaggerGenMethod.invoke(apiObject, parameters);
            executionResult = apiResponseParser.parseApiResponseObject(targetOperation, returnValue);
        } catch (InvocationTargetException e) {
            // Here, if request executed with http error code
            Throwable targetException = e.getTargetException();
            if (targetException.toString().contains("io.swagger.client.ApiException")){
                executionResult = apiResponseParser.parseApiExceptionObject(targetOperation, targetException);
            } else {
                throw new OperationExecutionException("Exception during the execution of operation" + targetOperation.toString(), e);
            }
        } catch (IllegalAccessException e) {
            throw new OperationExecutionException("Illegal access to method " + swaggerGenMethod.getName(), e);
        }

        return executionResult;
    }

}
