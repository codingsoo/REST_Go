package io.resttestgen.nominaltester.helper;

import com.squareup.okhttp.Request;
import io.resttestgen.nominaltester.helper.exceptions.ApiResponseParsingException;
import io.resttestgen.nominaltester.models.ExecutionResult;
import io.resttestgen.nominaltester.models.OperationInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

/**
 * ApiResponseParser contains method to parse ApiResponse object and ApiException object
 * returned from a swagger codegen invocation call.
 */
public class ApiResponseParser {

    static final Logger logger = LogManager.getLogger(ApiResponseParser.class);

    public ApiResponseParser() {}

    /**
     * @param operation operationInfo
     * @param e Swagger codegen ApiException
     * @return ExecutionResult object
     * @throws ApiResponseParsingException throw an exception if parsing fails
     */
    public ExecutionResult parseApiExceptionObject(OperationInfo operation, Throwable e) throws ApiResponseParsingException {
        Class<?> apiExceptionClass = e.getClass();

        Map<String, List<String>> headers = null;
        try {
            // Get data from ApiException object
            int responseStatusCode = (int)apiExceptionClass.getMethod("getCode").invoke(e);
            String responseBody = (String)apiExceptionClass.getMethod("getResponseBody").invoke(e);
            headers = (Map<String, List<String>>)apiExceptionClass.getMethod("getResponseHeaders").invoke(e);
            Request request = (Request)apiExceptionClass.getMethod("getRequest").invoke(e);

            // Fill up executionResult object
            ExecutionResult executionResult = new ExecutionResult();
            executionResult.setResponseBody(responseBody);
            executionResult.setStatusCode(responseStatusCode);
            executionResult.setResponseHeaders(headers);
            executionResult.setRequest(request);

            return executionResult;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            throw new ApiResponseParsingException("Cannot parse ApiException", e, ex);
        }
    }

    /**
     * @param e Swagger codegen ApiException
     * @return ExecutionResult object
     * @throws ApiResponseParsingException throw an exception if parsing fails
     */
    public ExecutionResult parseApiExceptionObject(Throwable e) throws ApiResponseParsingException {
        Class<?> apiExceptionClass = e.getClass();

        Map<String, List<String>> headers = null;
        try {
            // Get data from ApiException object
            int responseStatusCode = (int)apiExceptionClass.getMethod("getCode").invoke(e);
            String responseBody = (String)apiExceptionClass.getMethod("getResponseBody").invoke(e);
            headers = (Map<String, List<String>>)apiExceptionClass.getMethod("getResponseHeaders").invoke(e);
            Request request = (Request)apiExceptionClass.getMethod("getRequest").invoke(e);


            // Fill up executionResult object
            ExecutionResult executionResult = new ExecutionResult();
            executionResult.setResponseBody(responseBody);
            executionResult.setStatusCode(responseStatusCode);
            executionResult.setResponseHeaders(headers);
            executionResult.setRequest(request);

            return executionResult;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            throw new ApiResponseParsingException("Cannot parse ApiException", e, ex);
        }
    }

    /**
     * @param operation operationInfo
     * @param returnValue Swagger codegen ApiResponse
     * @return ExecutionResult object
     * @throws ApiResponseParsingException throw an exception if parsing fails
     */
    public ExecutionResult parseApiResponseObject(OperationInfo operation, Object returnValue) throws ApiResponseParsingException {
        Method swaggerGenMethod = operation.getInvocationMethod();
        return parseApiResponseObject(swaggerGenMethod, returnValue);
    }

    /**
     * @param swaggerGenMethod swagger gen method
     * @param returnValue Swagger codegen ApiResponse
     * @return ExecutionResult object
     * @throws ApiResponseParsingException throw an exception if parsing fails
     */
    public ExecutionResult parseApiResponseObject(Method swaggerGenMethod, Object returnValue) throws ApiResponseParsingException {
        Class<?> swaggerApiResponseClass = swaggerGenMethod.getReturnType();
        ParameterizedType annotatedReturnType = (ParameterizedType)swaggerGenMethod.getAnnotatedReturnType().getType();

        try {
            // Get data from ExecutionResult object
            Object responseData = swaggerApiResponseClass.getMethod("getData").invoke(returnValue);
            int responseStatusCode = (int)swaggerApiResponseClass.getMethod("getStatusCode").invoke(returnValue);
            Map<String, List<String>> headers = (Map<String, List<String>>)swaggerApiResponseClass.getMethod("getHeaders").invoke(returnValue);
            String responseBody = (String)swaggerApiResponseClass.getMethod("getResponseBody").invoke(returnValue);
            Request request = (Request)swaggerApiResponseClass.getMethod("getRequest").invoke(returnValue);

            // Fill up executionResult object
            ExecutionResult executionResult = new ExecutionResult();
            executionResult.setData(responseData);
            executionResult.setStatusCode(responseStatusCode);
            executionResult.setResponseHeaders(headers);
            executionResult.setReturnType(annotatedReturnType);
            executionResult.setResponseBody(responseBody);
            executionResult.setRequest(request);

            return executionResult;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            throw new ApiResponseParsingException("Cannot parse ApiResponse", returnValue, ex);
        }
    }

}
