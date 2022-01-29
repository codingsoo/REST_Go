package io.resttestgen.nominaltester.models;

import com.squareup.okhttp.Request;
import io.resttestgen.swaggerschema.models.ValidationError;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class containing the result of an ApiOperation execution
 */
public class ExecutionResult {
    private int statusCode;
    private Map<String, List<String>> responseHeaders;
    private Map<String, List<String>> requestHeaders;
    private Object data;
    private String responseBody;
    private Type returnType;
    private List<ValidationError> errors;
    private Request request;
    private String requestUrl;

    public ExecutionResult() {
        statusCode = -1;
        errors = new ArrayList<>();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public void setValidationErrors(List<ValidationError> errors) {
        this.errors = errors;
    }

    public List<ValidationError> getValidationErrors() {
        return errors;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * Returns a boolean indicating if the operation execution has returned a successful status code
     * @return true if status code is (2xx), false otherwise
     */
    public boolean isSuccessful() {
        return "2".equals(String.valueOf(statusCode).substring(0, 1));
    }

    public Map<String, List<String>> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Map<String, List<String>> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestUrl() {
        return requestUrl;
    }
}
