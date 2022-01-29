package io.resttestgen.errortester.mutator;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import io.resttestgen.errortester.violators.*;
import io.resttestgen.requestbuilder.parameters.*;
import io.resttestgen.nominaltester.helper.ResponseValidator;
import io.resttestgen.nominaltester.models.*;
import io.resttestgen.nominaltester.models.coverage.ResponseCoverage;
import io.resttestgen.swaggerschema.models.ValidationError;
import io.resttestgen.swaggerschema.models.exceptions.SchemaValidationException;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static io.resttestgen.errortester.mutator.ParameterMutationHelper.getParameterValue;

public class Mutator {
    static final Logger logger = LogManager.getLogger(Mutator.class);
    private OpenAPI openAPI;
    private String outputFolder;
    private int testedMutations;

    public Mutator(OpenAPI openAPI) {
        this.openAPI = openAPI;
        this.testedMutations = 0;
    }

    private Map<String, String> storeParametersValues(List<RequestParameter> parameterList) {
        Map<String, String> parameterValues = new HashMap<>();
        for (RequestParameter requestParameter : parameterList) {
            String value = getParameterValue(requestParameter);
            parameterValues.put(requestParameter.getParameterName(), value);
        }

        return parameterValues;
    }

    private void restoreParametersValues(Map<String, String> parameterValues, List<RequestParameter> parameterList) {
        for (RequestParameter requestParameter : parameterList) {
            String value = parameterValues.get(requestParameter.getParameterName());
            switch (requestParameter.getParameterType()) {
                case QUERY:
                    QueryParameter queryParameter = (QueryParameter) requestParameter;
                    String[] split = value.split("&SEPARATOR&");
                    ArrayList<String> queryParameterValuesStr = new ArrayList<>(Arrays.asList(split));
                    queryParameter.setParameterValues(queryParameterValuesStr);
                    break;
                case PATH:
                    PathParameter pathParameter = (PathParameter) requestParameter;
                    pathParameter.setParameterValue(value);
                    break;
                case HEADER:
                    HeaderParameter headerParameter = (HeaderParameter) requestParameter;
                    headerParameter.setParameterValue(value);
                    break;
            }
        }
    }

    private List<ExecutionParameter> createExecutionParameters(List<RequestParameter> requestParameters) {
        List<ExecutionParameter> executionParameters = new ArrayList<>();

        for (RequestParameter requestParameter : requestParameters) {
            Object value = requestParameter.getParameterValue();
            String parameterName = requestParameter.getParameterName();

            String valueString = new Gson().toJson(value);
            Object copiedValue = new Gson().fromJson(valueString, Object.class);

            ExecutionParameter executionParameter = new ExecutionParameter(parameterName);
            executionParameter.setParameterName(parameterName);
            executionParameter.setSanitizedName(parameterName);
            executionParameter.setValue(copiedValue);

            executionParameters.add(executionParameter);
        }

        return executionParameters;
    }

    public TestStep executeRequiredMissingParameter(OperationInfo operationInfo,
                                                    List<RequestParameter> parameterList,
                                                    RequestParameter requestParameter) {
        List<RequestParameter> parameterListCopy = new ArrayList<>(parameterList);
        RequiredMissingViolator requiredMissingViolator = new RequiredMissingViolator();

        if (requiredMissingViolator.applyViolation(requestParameter) != null) {
            return null;
        }

        testedMutations++;
        parameterListCopy.remove(requestParameter);
        Response response = executeRequest(operationInfo.getOperationId(), parameterListCopy);
        if (response == null)
            return null;

        List<ExecutionParameter> missingExecutionParameters =
                createExecutionParameters(Collections.singletonList(requestParameter));

        TestStep testStep;
        try {
            testStep = createTestStep(operationInfo, parameterListCopy, response);

            ValidationError validationError = new ValidationError();
            validationError.setMessage("Request was successful despite a parameter with a missing required parameter");
            String missingParameterName = missingExecutionParameters.get(0).getParameterName();
            validationError.setMissing(Collections.singletonList(missingParameterName));
            testStep.getExecutionResult().getValidationErrors().add(validationError);

            return testStep;
        } catch (IOException e) {
            logger.error("Exception occurred while creating test step", e);
            e.printStackTrace();
            return null;
        }
    }

    private TestStep createTestStep(OperationInfo operationInfo, List<RequestParameter> parameterList,
                                    Response response) throws IOException {
        // Get Request object
        Request okHttpRequest = response.request();

        // Create executionResult
        ExecutionResult executionResult = new ExecutionResult();
        executionResult.setRequest(okHttpRequest);
        executionResult.setResponseBody(response.body().string());
        executionResult.setResponseHeaders(response.headers().toMultimap());
        executionResult.setStatusCode(response.code());
        executionResult.setRequestHeaders(okHttpRequest.headers().toMultimap());
        executionResult.setRequestUrl(okHttpRequest.urlString());

        List<ExecutionParameter> executionParameters = createExecutionParameters(parameterList);

        TestStep testStep = new TestStep(operationInfo, executionParameters);
        testStep.setExecutionResult(executionResult);
        testStep.setStatusCode(response.code());
        testStep.setRequestParameters(parameterList);

        return testStep;
    }

    public TestStep executeMutateParameterDataType(OperationInfo operationInfo,
                                                   List<RequestParameter> parameterList,
                                                   RequestParameter requestParameter) {
        List<ExecutionParameter> originalParametersList = createExecutionParameters(Collections.singletonList(requestParameter));
        ExecutionParameter originalExecutionParameter = originalParametersList.get(0);

        DataTypeViolator dataTypeViolator = new DataTypeViolator();

        if (dataTypeViolator.applyViolation(requestParameter) == null)
            return null;

        testedMutations++;
        Response response = executeRequest(operationInfo.getOperationId(), parameterList);
        if (response == null)
            return null;

        TestStep testStep;
        try {
            testStep = createTestStep(operationInfo, parameterList, response);
            ValidationError validationError = new ValidationError();
            validationError.setMessage("Request was successful despite a parameter with a wrong data type");
            validationError.setFieldDetails("originalRequestParameter", originalExecutionParameter);
            testStep.getExecutionResult().getValidationErrors().add(validationError);
            return testStep;
        } catch (IOException e) {
            logger.error("Exception occurred while creating test step", e);
            e.printStackTrace();
            return null;
        }

    }

    public TestStep executeViolateParameterConstraint(OperationInfo operationInfo,
                                                      List<RequestParameter> parameterList,
                                                      RequestParameter requestParameter) {
        List<ExecutionParameter> originalParametersList = createExecutionParameters(Collections.singletonList(requestParameter));
        ExecutionParameter originalExecutionParameter = originalParametersList.get(0);

        List<Violator> violatorList = getViolatorList();

        for (Violator violator : violatorList) {
            String constraintName = violator.getClass().getSimpleName().split("Violator")[0];
            logger.info("Trying " + constraintName.toLowerCase() + " constraint violation");

            if (violator.applyViolation(requestParameter) == null) continue;

            testedMutations++;
            Response response = executeRequest(operationInfo.getOperationId(), parameterList);
            if (response == null)
                return null;

            int statusCode = response.code();
            if (!isExpectedStatusCode(statusCode)) {
                logger.info("" + constraintName + " constraint violation vulnerability found for parameter " +
                        requestParameter.getParameterName());
                TestStep testStep;
                try {
                    testStep = createTestStep(operationInfo, parameterList, response);
                    ValidationError validationError = new ValidationError();

                    validationError.setMessage("Violated " + constraintName.toLowerCase() + " constraint");
                    validationError.setFieldDetails("originalRequestParameter", originalExecutionParameter);
                    testStep.getExecutionResult().getValidationErrors().add(validationError);
                    return testStep;
                } catch (IOException e) {
                    logger.error("Exception occurred while creating test step", e);
                    e.printStackTrace();
                    return null;
                }
            }
        }

        return null;
    }

    private List<Violator> getViolatorList() {
        List<Violator> violatorList = new ArrayList<>();
        violatorList.add(new EnumViolator());
        violatorList.add(new MaximumViolator());
        violatorList.add(new MaxItemsViolator());
        violatorList.add(new MaxLengthViolator());
        violatorList.add(new MinimumViolator());
        violatorList.add(new MinItemsViolator());
        violatorList.add(new MinLengthViolator());
        violatorList.add(new UniqueItemsViolator());

        return violatorList;
    }

    private TestCase generateTestCase(TestStep testStep, Authentication authentication) {
        TestCase newTestCase = new TestCase();
        newTestCase.addTestStep(testStep);
        newTestCase.setAuthentication(authentication);
        return newTestCase;
    }

    public MutatorResult mutateTestCase(String operationId,
                                        TestCase testCase,
                                        MutationType mutationType) throws SchemaValidationException {

        List<RequestParameter> parameterList = testCase.getMainTestStep().getRequestParameters();
        parameterList = parameterList.stream().filter(p -> !(p instanceof BodyParameter)).collect(Collectors.toList());
        Map<String, String> parameterValues = storeParametersValues(parameterList);

        OperationInfo operationInfo = testCase.getMainTestStep().getTargetOperation();
        TestCase newTestCase;
        TestStep testStep;

        ResponseCoverage responseCoverage = new ResponseCoverage(operationInfo);
        MutatorResult mutatorResult = new MutatorResult();

        // Reset the mutation counters
        testedMutations = 0;

        ResponseValidator responseValidator = new ResponseValidator(openAPI);

        // Check the mutation to apply
        switch (mutationType) {
            case REQUIRED_MISSING:
                for (RequestParameter requestParameter : parameterList) {
                    logger.info("Performing required missing mutation on parameter " + requestParameter.getParameterName());
                    testStep = executeRequiredMissingParameter(operationInfo, parameterList, requestParameter);
                    if (testStep != null) {
                        newTestCase = generateTestCase(testStep, testCase.getAuthentication());
                        int statusCode = testStep.getExecutionResult().getStatusCode();
                        if (!isExpectedStatusCode(statusCode)) {
                            List<ValidationError> validationErrors = responseValidator.checkResponseSchemaValidity(testStep);
                            testStep.getExecutionResult().getValidationErrors().addAll(validationErrors);
                            mutatorResult.addViolation(validationErrors);
                            // TODO: add response violation
                            mutatorResult.addViolation(statusCode);
                            logger.info("Required missing mutation vulnerability found for parameter " + requestParameter.getParameterName());
                            responseCoverage.addTestCase(statusCode, newTestCase);
                        }
                    }
                }

                logger.info("Found " + mutatorResult.getMutationsWithViolations() + " out of " + testedMutations +
                        " tested missing required mutations for operation " + operationId);
                break;
            case WRONG_DATATYPE:
                for (RequestParameter requestParameter : parameterList) {
                    logger.info("Performing wrong data type mutation on parameter " + requestParameter.getParameterName());
                    testStep = executeMutateParameterDataType(operationInfo, parameterList, requestParameter);
                    if (testStep != null) {
                        newTestCase = generateTestCase(testStep, testCase.getAuthentication());
                        int statusCode = testStep.getExecutionResult().getStatusCode();
                        if (!isExpectedStatusCode(statusCode)) {
                            List<ValidationError> validationErrors = responseValidator.checkResponseSchemaValidity(testStep);
                            testStep.getExecutionResult().getValidationErrors().addAll(validationErrors);
                            mutatorResult.addViolation(validationErrors);
                            mutatorResult.addViolation(statusCode);
                            logger.info("Wrong data type mutation vulnerability found for parameter " + requestParameter.getParameterName());
                            responseCoverage.addTestCase(statusCode, newTestCase);
                        }
                    }
                    restoreParametersValues(parameterValues, parameterList);
                }

                logger.info("Found " + mutatorResult.getMutationsWithViolations() + " out of " + testedMutations +
                        " tested wrong data type mutations for operation " + operationId);
                break;
            case VIOLATED_CONSTRAINT:
                for (RequestParameter requestParameter : parameterList) {
                    logger.info("Performing constraint violation on parameter " + requestParameter.getParameterName());
                    testStep = executeViolateParameterConstraint(operationInfo, parameterList, requestParameter);
                    if (testStep != null) {
                        newTestCase = generateTestCase(testStep, testCase.getAuthentication());
                        int statusCode = testStep.getExecutionResult().getStatusCode();
                        if (!isExpectedStatusCode(statusCode)) {
                            List<ValidationError> validationErrors = responseValidator.checkResponseSchemaValidity(testStep);
                            testStep.getExecutionResult().getValidationErrors().addAll(validationErrors);
                            mutatorResult.addViolation(validationErrors);
                            mutatorResult.addViolation(statusCode);
                            responseCoverage.addTestCase(statusCode, newTestCase);
                        }
                    }
                    restoreParametersValues(parameterValues, parameterList);
                }

                logger.info("Found " + mutatorResult.getMutationsWithViolations() + " out of " + testedMutations +
                        " tested constraint violation mutations for operation " + operationId);
                break;
        }

        mutatorResult.setExecutedMutations(testedMutations);

        if (!responseCoverage.getResponseCoverageMap().isEmpty()) {
            try {
                generateReport(operationId, responseCoverage, mutationType);
            } catch (IOException e) {
                logger.error("Exception occurred while trying to write the report", e);
                e.printStackTrace();
            }
        }

        return mutatorResult;
    }

    private void generateReport(String operationId, ResponseCoverage responseCoverage,
                                MutationType mutationType) throws IOException {
        MalformedMutationReportWriter malformedMutationReportWriter = new MalformedMutationReportWriter(openAPI);
        malformedMutationReportWriter.toJsonFile(responseCoverage,
                this.outputFolder + "/" + operationId + "_" + mutationType + ".json");
    }

    private boolean isExpectedStatusCode(int statusCode) {
        return statusCode >= 400 && statusCode < 500;
    }

    private Response executeRequest(String operationId, List<RequestParameter> parameterListCopy) {
        io.resttestgen.requestbuilder.Request requestBuild = new io.resttestgen.requestbuilder.Request.Builder(openAPI, operationId).addRequestParameters(parameterListCopy).build();
        Request request = requestBuild.okHttpRequest;
        try {
            return new OkHttpClient().newCall(request).execute();
        } catch (IOException e) {
            logger.error("Exception thrown while executing request", e);
            e.printStackTrace();
            return null;
        }
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }
}
