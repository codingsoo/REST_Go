package io.resttestgen.nominaltester.testcases.junitwriter.models;

import io.resttestgen.nominaltester.testcases.junitwriter.exceptions.JunitBuilderException;
import io.resttestgen.swaggerschema.SchemaEditor;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;

public class JunitTestFile {

    public JunitTestCase testCase;
    public ArrayList<String> imports;
    public String strRepresentation;

    private JunitTestFile(JunitTestCase testCase) {
        this.testCase = testCase;
    }

    public static class Builder {

        String junitTestFileTemplateString =
                        "$IMPORTS$" +
                        "\n" +
                        "\n" +
                        "public class $CLASSNAME$ {\n" +
                        "\n" +
                        "\tprivate static ResponseValidator responseValidator;\n" +
                        "\tprivate static String openAPIStr = $OPENAPI_STR$;\n" +
                        "\tprivate static OpenAPI openAPI;\n" +
                        "\n" +
                        "\t@BeforeClass\n" +
                        "\tpublic static void beforeClass() throws SchemaValidationException {\n" +
                        "\t\topenAPI = new OpenAPIV3Parser().readContents(openAPIStr).getOpenAPI();\n" +
                        "\t\tresponseValidator = new ResponseValidator(openAPI);\n" +
                        "\t\t$RESET$\n" +
                        "\t\t$AUTH$\n" +
                        "\t}\n" +
                        "\n" +
                        "$TESTCASE$\n" +
                        "\n" +
                        "}\n";

        private String operationId;
        private String statusCode;
        private ArrayList<String> imports;
        private JunitTestCase testCase;

        public Builder(String operationId, int statusCode) {
            this.operationId = operationId;
            this.statusCode = String.valueOf(statusCode);
            this.imports = new ArrayList<>();
            addDefaultImports();
        }

        private void addDefaultImports() {
            imports.add("import com.squareup.okhttp.OkHttpClient;");
            imports.add("import com.squareup.okhttp.Response;");
            imports.add("import io.resttestgen.nominaltester.helper.ResponseValidator;");
            imports.add("import io.resttestgen.requestbuilder.Request;");
            imports.add("import io.resttestgen.swaggerschema.models.exceptions.SchemaValidationException;");
            imports.add("import io.swagger.v3.oas.models.OpenAPI;");
            imports.add("import io.swagger.v3.parser.OpenAPIV3Parser;");
            imports.add("import org.junit.BeforeClass;");
            imports.add("import org.junit.Test;");
            imports.add("import java.util.ArrayList;");
            imports.add("import java.util.Arrays;");
            imports.add("import static org.junit.Assert.assertEquals;");
        }

        public Builder addTestCase(JunitTestCase testCase) {
            this.testCase = testCase;
            String[] split = testCase.toString().split("\n");
            StringBuilder testCaseStr = new StringBuilder();
            for (String s : split) {
                testCaseStr.append("\t").append(s).append("\n");
            }
            junitTestFileTemplateString = junitTestFileTemplateString.replace("$TESTCASE$", testCaseStr.toString());
            return this;
        }

        public Builder addResetHookClass(String resetHookClassName) {
            String resetString = String.format("%s.reset();", resetHookClassName);
            junitTestFileTemplateString = junitTestFileTemplateString.replace("$RESET$", resetString);
            return this;
        }

        public Builder addAuthenticationHookClass(String authenticationHookClassName) {
            String authString = String.format("%s.authenticate();", authenticationHookClassName);
            junitTestFileTemplateString = junitTestFileTemplateString.replace("$AUTH$", authString);
            return this;
        }

        public Builder addOpenAPI(String openAPI) {
            String escapedJson = StringEscapeUtils.escapeJson(openAPI);
            String openAPIParameter = String.format("\"%s\"", escapedJson);
            openAPIParameter = openAPIParameter.replace("\\/", "/");
            junitTestFileTemplateString = junitTestFileTemplateString.replace("$OPENAPI_STR$", openAPIParameter);
            return this;
        }

        public Builder addOpenAPI(OpenAPI openAPI) {
            String openAPIJsonStr = SchemaEditor.toJSONSchema(openAPI);
            return addOpenAPI(openAPIJsonStr);
        }

        public JunitTestFile build() throws JunitBuilderException {
            junitTestFileTemplateString = junitTestFileTemplateString.replace("$AUTH$", "// No auth class");
            junitTestFileTemplateString = junitTestFileTemplateString.replace("$RESET$", "// No reset class");
            junitTestFileTemplateString = junitTestFileTemplateString.replace("$IMPORTS$", String.join("\n", imports));

            String className = String.format("%s_%s", operationId, statusCode);
            junitTestFileTemplateString = junitTestFileTemplateString.replace("$CLASSNAME$", className);

            if (junitTestFileTemplateString.contains("$TESTCASE$")) {
                throw new JunitBuilderException("Missing main test case", this);
            }

            if (junitTestFileTemplateString.contains("$OPENAPI_STR$")) {
                throw new JunitBuilderException("Missing openAPI", this);
            }

            JunitTestFile junitTestFile = new JunitTestFile(testCase);
            junitTestFile.imports = this.imports;
            junitTestFile.strRepresentation = junitTestFileTemplateString;
            return junitTestFile;
        }
    }

    @Override
    public String toString() {
        return strRepresentation;
    }
}
