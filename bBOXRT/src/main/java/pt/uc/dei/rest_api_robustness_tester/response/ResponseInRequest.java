package pt.uc.dei.rest_api_robustness_tester.response;

import pt.uc.dei.rest_api_robustness_tester.AI.ResponseInterpreter;

import java.util.ArrayList;
import java.util.List;

public class ResponseInRequest {


    protected StatusCode statusCode;
    protected String statusReason;
    protected String specificationDescription;
    protected String content;
    protected ResponseInterpreter.ErrorType errorType = ResponseInterpreter.ErrorType.NotSet;


    public ResponseInRequest(){
        content = "No content in response";
    }


    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public String getSpecificationDescription() {
        return specificationDescription;
    }

    public void setSpecificationDescription(String specificationDescription) {
        this.specificationDescription = specificationDescription;
    }

    public ResponseInterpreter.ErrorType getErrorType() { return errorType; }

    public void setErrorType(ResponseInterpreter.ErrorType errorType) { this.errorType = errorType; }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
