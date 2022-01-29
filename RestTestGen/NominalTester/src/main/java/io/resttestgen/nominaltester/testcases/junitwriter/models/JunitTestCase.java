package io.resttestgen.nominaltester.testcases.junitwriter.models;

import io.resttestgen.requestbuilder.Request;

import java.util.Arrays;
import java.util.stream.Collectors;

public class JunitTestCase {


    public String operationId;
    public String statusCode;
    public String strRepresentation;

    private JunitTestCase(String operationId, int statusCode) {
        this.operationId = operationId;
        this.statusCode = String.valueOf(statusCode);
    }

    public static class Builder {
        String junitTestCaseTemplateString = "" +
                "/*\n" +
                "$COMMENT$" +
                "*/\n"+
                "@Test(timeout=5000)\n" +
                "public void $OPERATION_NAME$_TEST_$STATUSCODE$() throws Exception {\n" +
                "\tOkHttpClient client = new OkHttpClient();\n" +
                "$BODY$" +
                "}" +
                "";

        private String operationId;
        private int statusCode;

        public Builder(String operationId, int statusCode) {
            this.operationId = operationId;
            this.statusCode = statusCode;
        }

        public Builder addRequest(Request request) {
            String requestStr = String.join("\n\t", request.traces);
            junitTestCaseTemplateString = junitTestCaseTemplateString.
                    replace("$BODY$", String.format("\n\t%s\n$BODY$", requestStr));
            return this;
        }

        public Builder addExecutionRequestStatement(Request request) {
            String requestId = request.requestId;
            String operationId = request.operationId;
            String reqVariableName = request.operationId + "_req_" + requestId;
            String responseStatement = String.format("Response %s_res_%s = client.newCall(%s.okHttpRequest).execute();",
                    operationId, requestId, reqVariableName);
            junitTestCaseTemplateString = junitTestCaseTemplateString.
                    replace("$BODY$", String.format("\n\t%s\n$BODY$", responseStatement));

            // Add in test case summary
            String commentLine = String.format("\t%s. %s\n", requestId, request.operationId);
            junitTestCaseTemplateString = junitTestCaseTemplateString.
                    replace("$COMMENT$", String.format("%s$COMMENT$", commentLine));

            return this;
        }

        public Builder addAssertEqualsStatement(String expected, String actual) {
            String assertionStr = String.format("assertEquals(%s, %s);", expected, actual);
            junitTestCaseTemplateString = junitTestCaseTemplateString.
                    replace("$BODY$", String.format("\n\t%s\n$BODY$", assertionStr));
            return this;
        }

        public Builder addAssertEqualsStatement(String expected, String actual, String comment) {
            if (comment.isEmpty()) return addAssertEqualsStatement(expected, actual);
            String commentFormat = Arrays.stream(comment.split("\n")).map(x -> "// " + x).collect(Collectors.joining("\n"));
            String assertionStr = String.format("%sassertEquals(%s, %s);", commentFormat, expected, actual);
            junitTestCaseTemplateString = junitTestCaseTemplateString.
                    replace("$BODY$", String.format("\n\t%s\n$BODY$", assertionStr));
            return this;
        }

        public JunitTestCase build() {
            junitTestCaseTemplateString = junitTestCaseTemplateString.
                    replace("$BODY$", "");

            junitTestCaseTemplateString = junitTestCaseTemplateString.
                    replace("$COMMENT$", "");

            junitTestCaseTemplateString = junitTestCaseTemplateString.
                    replace("$OPERATION_NAME$", this.operationId);

            junitTestCaseTemplateString = junitTestCaseTemplateString.
                    replace("$STATUSCODE$", String.valueOf(this.statusCode));

            JunitTestCase junitTestCase = new JunitTestCase(this.operationId, this.statusCode);
            junitTestCase.strRepresentation = junitTestCaseTemplateString;
            return junitTestCase;
        }
    }

    @Override
    public String toString() {
        return strRepresentation;
    }
}
