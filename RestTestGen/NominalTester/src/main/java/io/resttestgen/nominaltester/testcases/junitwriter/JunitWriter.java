package io.resttestgen.nominaltester.testcases.junitwriter;

import io.resttestgen.requestbuilder.Request;
import io.resttestgen.requestbuilder.parameters.RequestParameter;
import io.resttestgen.nominaltester.helper.RequestParameterHelper;
import io.resttestgen.nominaltester.models.ExecutionParameter;
import io.resttestgen.nominaltester.models.OperationInfo;
import io.resttestgen.nominaltester.models.TestCase;
import io.resttestgen.nominaltester.models.TestStep;
import io.resttestgen.nominaltester.models.coverage.ResponseCoverage;
import io.resttestgen.nominaltester.testcases.junitwriter.exceptions.JunitBuilderException;
import io.resttestgen.nominaltester.testcases.junitwriter.models.JunitTestCase;
import io.resttestgen.nominaltester.testcases.junitwriter.models.JunitTestFile;
import io.resttestgen.swaggerschema.models.ValidationError;
import io.swagger.v3.oas.models.OpenAPI;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class JunitWriter {

    Path destinationFolder;
    OpenAPI openAPI;

    public JunitWriter(OpenAPI openAPI, String destinationFolder) {
        this.openAPI = openAPI;
        this.destinationFolder = Paths.get(destinationFolder);
        boolean mkdirs = new File(destinationFolder).mkdirs(); // create destination folder if not exists
    }

    public JunitWriter(OpenAPI openAPI, Path destinationFolder) {
        this.openAPI = openAPI;
        this.destinationFolder = destinationFolder;
        boolean mkdirs = destinationFolder.toFile().mkdirs(); // create destination folder if not exists
    }

    public void fromTestCase(TestCase testCase) throws JunitBuilderException, IOException {
        TestStep mainTestStep = testCase.getMainTestStep();
        String operationId = mainTestStep.getTargetOperation().getOperationId();
        int statusCode = mainTestStep.getStatusCode();

        JunitTestCase junitTestCase = createJunitTestCase(testCase);

        JunitTestFile.Builder builder = new JunitTestFile.Builder(operationId, statusCode);
        builder.addOpenAPI(openAPI);
        builder.addTestCase(junitTestCase);
        JunitTestFile junitTestFile = builder.build();

        String juniTestFileStr = junitTestFile.toString();

        String junitFileName = String.format("%s_%d.java", operationId, statusCode);
        Path junitFilePath = destinationFolder.resolve(junitFileName);

        Files.write(junitFilePath, juniTestFileStr.getBytes());
    }

    public void fromResponseCoverage(ResponseCoverage responseCoverage) throws JunitBuilderException, IOException {
        for (Map.Entry<Integer, List<TestCase>> codeCoverage : responseCoverage.getResponseCoverageMap().entrySet()) {
            if (codeCoverage.getValue().size() > 0) {
                TestCase testCase = codeCoverage.getValue().get(0);
                fromTestCase(testCase);
            }
        }
    }

    private JunitTestCase createJunitTestCase(TestCase testCase) {
        List<TestStep> testSteps = testCase.getTestSteps();
        TestStep mainTestStep = testCase.getMainTestStep();
        String operationId = mainTestStep.getTargetOperation().getOperationId();
        int statusCode = mainTestStep.getStatusCode();

        JunitTestCase.Builder builder = new JunitTestCase.Builder(operationId, statusCode);

        int counter = 0;
        for (TestStep testStep : testSteps) {
            counter = counter + 1;

            Request request;
            if (testStep.getRequest() != null) {
                request = testStep.getRequest();
            } else {
                OperationInfo targetOperation = testStep.getTargetOperation();
                List<ExecutionParameter> executionParameters = testStep.getExecutionParameters();
                RequestParameterHelper requestParameterHelper = new RequestParameterHelper(openAPI);

                List<RequestParameter> requestParameters = requestParameterHelper.createRequestParameters(
                        targetOperation.getOperationId(),
                        executionParameters);
                request = new Request.Builder(openAPI, targetOperation.getOperationId())
                        .addRequestParameters(requestParameters).build();

                testStep.setRequest(request);
                testStep.setRequestParameters(requestParameters);
            }
            
            request.setRequestId(String.valueOf(counter));
            builder.addRequest(request);
            builder.addExecutionRequestStatement(request);
        }

        String resVariableName = operationId + "_res_" + counter;
        String actualValueString = String.format("responseValidator.checkResponseSchemaValidity(\"%s\", %s).size()", operationId, resVariableName);
        String actualStatusCode = String.format("%s.code()", resVariableName);
        String expectedStatusCode = String.valueOf(mainTestStep.getStatusCode());
        String expectedValidationErrors = String.valueOf(mainTestStep.getExecutionResult().getValidationErrors().size());

        builder.addAssertEqualsStatement(expectedStatusCode, actualStatusCode);

        // Validation Error Comments
        StringBuilder validationErrorComment = new StringBuilder();
        List<ValidationError> validationErrors = mainTestStep.getExecutionResult().getValidationErrors();
        for (ValidationError validationError : validationErrors) {
            String message = validationError.getMessage();
            validationErrorComment.append(message).append("\n");
        }
        builder.addAssertEqualsStatement(expectedValidationErrors, actualValueString, validationErrorComment.toString());
        return builder.build();
    }
}
