package io.resttestgen.errortester.cli;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import io.resttestgen.errortester.mutator.*;
import io.resttestgen.nominaltester.models.OperationResult;
import io.resttestgen.nominaltester.models.TestCase;
import io.resttestgen.nominaltester.models.TestStep;
import io.resttestgen.nominaltester.models.coverage.OperationCoverage;
import io.resttestgen.nominaltester.reports.reportreader.ReportReader;
import io.resttestgen.swaggerschema.SchemaEditor;
import io.resttestgen.swaggerschema.models.exceptions.SchemaValidationException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class App {

    private static final String toolVersion = "1.0.0";
    private static final String helpMessage = "Malformed Mutation Tester " + toolVersion + "\n\n"
            + "Parameters:\n"
            + "(REQUIRED) --service <path to the service folder> (e.g. /home/user/petstore)\n"
            + "(REQUIRED) --reports <path to the reports folder> (e.g. /home/user/some_folder/)\n"
            + "(REQUIRED) --swagger <swagger-specification-file> (e.g. /my-rest/petstore.json)\n"
            + "(OPTIONAL) --verbose <log-verbosity level> (e.g. 0 for error, 1 for info, 2 for debug)\n\n"
            + "(OPTIONAL) --mutation <type of malformed mutation to test> (possible values: \"required\", " +
            "\"datatypes\", \"constraints\", \"all\"; default: \"all\")\n"
            + "(OPTIONAL) --output <output folder for the reports (default: \"service/malformedReports/\")>";

    static final Logger logger = LogManager.getLogger(App.class);
    private static String outputFolder = null;

    public static void main(String[] args) throws SchemaValidationException {

        // Check command-line args validity
        checkArgsValidity(args);

        // Get args
        CommandOptions cmd = new CommandOptions(args);
        String serviceFolder = cmd.valueOf("--service");
        String mutationTypeString = cmd.valueOf("--mutation");
        String reportsFolder = cmd.valueOf("--reports");
        String swaggerFilePath = cmd.valueOf("--swagger");
        outputFolder = cmd.valueOf("--output");

        logger.info("cli-param1: service: {}", serviceFolder);
        logger.info("cli-param1: mutation type: {}", mutationTypeString);
        logger.info("cli-param2: reports folder: {}", reportsFolder);
        logger.info("cli-param3: swagger-file: {}", swaggerFilePath);
        logger.info("cli-param4: output-folder: {}", outputFolder);

        if (outputFolder == null || outputFolder.isEmpty()) {
            outputFolder = serviceFolder + "/malformedReports";
        }

        // Read swagger file; extract operations and link the swagger-gen methods
        logger.info("Reading swagger file");
        OpenAPI openAPI = parseSwaggerFile(swaggerFilePath);

        // Read the reports
        logger.info("Reading reports from directory");
        ReportReader reportReader = new ReportReader(openAPI);
        OperationCoverage operationCoverage = null;
        try {
            operationCoverage = reportReader.readReportsFromDirectory(reportsFolder);
        } catch (FileNotFoundException e) {
            logger.error("Unable to import reports from the report folder", e);
            System.err.println("Unable to import reports from the report folder \"" + reportsFolder + "\"");
            System.exit(-1);
        }

        // Get the mutation type
        logger.info("Getting the mutation type");
        MutationType mutationType = getMutationType(mutationTypeString);

        logger.info("Initialising mutator");
        Mutator mutator = new Mutator(openAPI);
        mutator.setOutputFolder(outputFolder);

        MutatorResult mutatorResult;
        Map<MutationType, List<MutatorResult>> mutatorResultMap = initMutatorResultMap();

        int totalSuccessfulTestCases = 0, testedSuccessfulTestCases = 0;

        for (Map.Entry<String, OperationResult> entry : operationCoverage.getOperationResultMap().entrySet()) {
            String operationId = entry.getKey();
            logger.info("Testing operation " + operationId);
            Map<MutationType, List<MutatorResult>> mutatorResultOperationMap = initMutatorResultMap();
            OperationResult operationResult = entry.getValue();
            Map<Integer, List<TestCase>> responseCoverage = operationResult.getResponseCoverage().getResponseCoverageMap();
            List<TestCase> successfulTestCases = getSuccessfulTestCases(responseCoverage);
            totalSuccessfulTestCases += successfulTestCases.size();

            logger.info("" + successfulTestCases.size() + " successful test case(s) detected");
            if (successfulTestCases.isEmpty()) {
                logger.info("Skipping operation " + operationId);
                continue;
            }

            // Get the test case
            TestCase randomSuccessfulTestCase = successfulTestCases.get(new Random().nextInt(successfulTestCases.size()));

            // Execute main test step's dependencies
            List<TestStep> dependenciesTestSteps = randomSuccessfulTestCase.getMainTestStepDependencies();
            logger.info("Executing " + dependenciesTestSteps.size() + " test step dependencies");
            executeTestSteps(dependenciesTestSteps);
            logger.info("Finished executing test step dependencies");

            if (mutationType.equals(MutationType.ALL)) {
                logger.info("Performing required missing mutations");
                mutatorResult = mutator.mutateTestCase(operationId, randomSuccessfulTestCase, MutationType.REQUIRED_MISSING);
                mutatorResultMap.get(MutationType.REQUIRED_MISSING).add(mutatorResult);
                mutatorResultOperationMap.get(MutationType.REQUIRED_MISSING).add(mutatorResult);

                logger.info("Performing wrong datatype mutations");
                mutatorResult = mutator.mutateTestCase(operationId, randomSuccessfulTestCase, MutationType.WRONG_DATATYPE);
                mutatorResultMap.get(MutationType.WRONG_DATATYPE).add(mutatorResult);
                mutatorResultOperationMap.get(MutationType.WRONG_DATATYPE).add(mutatorResult);

                logger.info("Performing constraint violation mutations");
                mutatorResult = mutator.mutateTestCase(operationId, randomSuccessfulTestCase, MutationType.VIOLATED_CONSTRAINT);
                mutatorResultMap.get(MutationType.VIOLATED_CONSTRAINT).add(mutatorResult);
                mutatorResultOperationMap.get(MutationType.VIOLATED_CONSTRAINT).add(mutatorResult);
            } else {
                logger.info("Performing " + mutationType + " mutations");
                mutatorResult = mutator.mutateTestCase(operationId, randomSuccessfulTestCase, mutationType);
                mutatorResultMap.get(mutationType).add(mutatorResult);
                mutatorResultOperationMap.get(mutationType).add(mutatorResult);
            }

            if (mutatorResultOperationMap.get(MutationType.REQUIRED_MISSING).get(0).getExecutedMutations() > 0 ||
                    mutatorResultOperationMap.get(MutationType.WRONG_DATATYPE).get(0).getExecutedMutations() > 0 ||
                    mutatorResultOperationMap.get(MutationType.VIOLATED_CONSTRAINT).get(0).getExecutedMutations() > 0) {
                testedSuccessfulTestCases++;
                writeSummary(mutatorResultOperationMap, successfulTestCases.size(), 1, operationId);
            } else {
                writeSummary(mutatorResultOperationMap, successfulTestCases.size(), 0, operationId);
            }
        }

        writeSummary(mutatorResultMap, totalSuccessfulTestCases, testedSuccessfulTestCases, null);
    }

    private static void writeSummary(Map<MutationType, List<MutatorResult>> mutatorResultMap,
                                     int totalSuccessfulTestCases, int testedSuccessfulTestCases, String operationId) {
        // Print the total number of tested test cases
        logger.info("Tested test cases: " + testedSuccessfulTestCases);

        // Get the correct values for the numbers of tested and successful mutations
        Summary summary = generateSummary(mutatorResultMap, totalSuccessfulTestCases, testedSuccessfulTestCases);

        // Writes the summary
        MalformedMutationSummaryReportWriter writer = new MalformedMutationSummaryReportWriter();
        try {
            String summaryFile = outputFolder + "/summary.json";
            if (operationId != null) {
                summaryFile = outputFolder + "/summary_" + operationId + ".json";
            }

            writer.toJsonFile(summary, summaryFile);
        } catch (IOException e) {
            logger.error("Exception while trying to write the summary", e);
            e.printStackTrace();
        }
    }

    private static Map<MutationType, List<MutatorResult>> initMutatorResultMap() {
        Map<MutationType, List<MutatorResult>> mutatorResultMap = new HashMap<>();
        mutatorResultMap.put(MutationType.REQUIRED_MISSING, new ArrayList<>());
        mutatorResultMap.put(MutationType.WRONG_DATATYPE, new ArrayList<>());
        mutatorResultMap.put(MutationType.VIOLATED_CONSTRAINT, new ArrayList<>());
        return mutatorResultMap;
    }

    private static List<TestCase> getSuccessfulTestCases(Map<Integer, List<TestCase>> responseCoverage) {
        List<TestCase> successfulTestCases = new ArrayList<>();
        for (int i = 200; i < 300; i++) {
            List<TestCase> testCases = responseCoverage.get(i);
            if (testCases != null && !testCases.isEmpty())
                successfulTestCases.addAll(testCases);
        }
        return successfulTestCases;
    }

    /**
     * Gets the mutation type from the specified command line option
     * @param mutationTypeString the specified command line option
     * @return the mutation type to test
     */
    private static MutationType getMutationType(String mutationTypeString) {
        MutationType mutationType = null;

        // If the specified mutation type is null or empty, set it to "all"
        if (mutationTypeString == null || mutationTypeString.isEmpty())
            mutationTypeString = "all";

        switch (mutationTypeString) {
            case "required":
                mutationType = MutationType.REQUIRED_MISSING;
                break;
            case "datatypes":
                mutationType = MutationType.WRONG_DATATYPE;
                break;
            case "constraints":
                mutationType = MutationType.VIOLATED_CONSTRAINT;
                break;
            case "all":
                mutationType = MutationType.ALL;
                break;
            default:
                logger.error("Mutation \"" + mutationTypeString + "\" is not valid");
                System.err.println("Mutation \"" + mutationTypeString + "\" is not valid");
                System.exit(-1);
        }

        return mutationType;
    }

    /**
     * Parse swagger file and enforce some properties
     * - No additional properties
     * - No Empty Strings
     *
     * @param swaggerFilePath swagger file specification
     * @return OpenAPI POJO class
     */
    private static OpenAPI parseSwaggerFile(String swaggerFilePath) {
        OpenAPI openAPI = new OpenAPIV3Parser().read(swaggerFilePath);
        SchemaEditor schemaEditor = new SchemaEditor(openAPI);
        try {
            schemaEditor.setDefaultAdditionalPropertiesToAll(false);
            schemaEditor.setDefaultStringMinLength(0);
        } catch (Exception e) {
            logger.warn("Cannot using enforce strict rules on swagger-validator");
            e.printStackTrace();
        }
        return openAPI;
    }

    private static void executeTestSteps(List<TestStep> testStepDependencies) {
        // Execute all but main test step requests
        for (int i = 0; i < testStepDependencies.size(); i++) {
            TestStep testStepDependency = testStepDependencies.get(i);
            Request request = testStepDependency.getRequest().okHttpRequest;
            try {
                int statusCode = new OkHttpClient().newCall(request).execute().code();
                if (statusCode < 200 || statusCode >= 300) {
                    logger.warn("Received status code " + statusCode + " while executing test step dependency " + (i+1) +
                            "out of " + testStepDependencies.size() + ": following API calls may fail");
                }
            } catch (Exception e) {
                logger.error("Exception occurred while executing test step dependency " + (i+1) +
                        " out of " + testStepDependencies.size() + ": following API calls may fail", e);
                e.printStackTrace();
            }
        }
    }

    private static Summary generateSummary(Map<MutationType, List<MutatorResult>> mutatorResultMap,
                                           int totalSuccessfulTestCases, int testedSuccessfulTestCases) {
        int testedMissingRequiredMutations = 0, successfulMissingRequiredMutations = 0;
        Map<Integer, Integer> requiredMissingViolationsStatusCodes = new HashMap<>();
        int testedWrongDataTypeMutations = 0, successfulWrongDataTypeMutations = 0;
        Map<Integer, Integer> wrongDataTypeViolationsStatusCodes = new HashMap<>();
        int testedConstraintViolationMutations = 0, successfulConstraintViolationMutations = 0;
        Map<Integer, Integer> constraintViolationsStatusCodes = new HashMap<>();

        for (Map.Entry<MutationType, List<MutatorResult>> mutationTypeListEntry : mutatorResultMap.entrySet()) {
            MutationType type = mutationTypeListEntry.getKey();
            List<MutatorResult> mutatorResultList = mutationTypeListEntry.getValue();

            for (MutatorResult result : mutatorResultList) {
                switch (type) {
                    case REQUIRED_MISSING:
                        testedMissingRequiredMutations += result.getExecutedMutations();
                        successfulMissingRequiredMutations += result.getMutationsWithViolations();
                        mergeMaps(requiredMissingViolationsStatusCodes, result.getStatusCodeMap());
                        break;
                    case WRONG_DATATYPE:
                        testedWrongDataTypeMutations += result.getExecutedMutations();
                        successfulWrongDataTypeMutations += result.getMutationsWithViolations();
                        mergeMaps(wrongDataTypeViolationsStatusCodes, result.getStatusCodeMap());
                        break;
                    case VIOLATED_CONSTRAINT:
                        testedConstraintViolationMutations += result.getExecutedMutations();
                        successfulConstraintViolationMutations += result.getMutationsWithViolations();
                        mergeMaps(constraintViolationsStatusCodes, result.getStatusCodeMap());
                        break;
                }
            }

        }

        Summary summary = new Summary();

        summary.setTestedRequiredMissingMutations(testedMissingRequiredMutations);
        summary.setSuccessfulRequiredMissingMutations(successfulMissingRequiredMutations);
        summary.setRequiredMissingViolationsStatusCodes(requiredMissingViolationsStatusCodes);

        summary.setTestedWrongDataTypeMutations(testedWrongDataTypeMutations);
        summary.setSuccessfulWrongDataTypeMutations(successfulWrongDataTypeMutations);
        summary.setWrongDataTypeViolationsStatusCodes(wrongDataTypeViolationsStatusCodes);

        summary.setTestedConstraintViolationMutations(testedConstraintViolationMutations);
        summary.setSuccessfulConstraintViolationMutations(successfulConstraintViolationMutations);
        summary.setConstraintViolationsStatusCodes(constraintViolationsStatusCodes);

        summary.setTotalSuccessfulTestCases(totalSuccessfulTestCases);
        summary.setTestedSuccessfulTestCases(testedSuccessfulTestCases);

        return summary;
    }

    private static void mergeMaps(Map<Integer, Integer> violationsStatusCodes,
                                  Map<Integer, Integer> statusCodeMap) {
        for (Map.Entry<Integer, Integer> integerIntegerEntry : statusCodeMap.entrySet()) {
            int statusCode = integerIntegerEntry.getKey();
            violationsStatusCodes.merge(statusCode, integerIntegerEntry.getValue(), Integer::sum);
        }
    }

    /**
     * Parse command args and raise an error if not valid
     * @param args Command args
     */
    private static void checkArgsValidity(String[] args) {
        CommandOptions cmd = new CommandOptions(args);

        String serviceFolder = cmd.valueOf("--service");
        String reportsFolder = cmd.valueOf("--reports");
        String swaggerFilePath = cmd.valueOf("--swagger");
        String outputFolder = cmd.valueOf("--output");
        String logVerbosity = cmd.valueOf("--verbose");

        int help = cmd.searchOption("--help");
        int version = cmd.searchOption("--version");

        if (help >= 0 || version >= 0) {
            System.out.println(helpMessage);
            System.exit(0);
        }

        if (serviceFolder == null || reportsFolder == null || swaggerFilePath == null) {
            System.err.println("Error -1: Missing required parameter(s)");
            System.err.println(helpMessage);
            System.exit(-1);
        }

        if (!CommandOptions.checkDirectoryExists(serviceFolder)) {
            //System.err.println("Error -2: directory \"" + serviceFolder + "\" does not exist");
            //System.exit(-2);
            boolean mkdirs = new File(serviceFolder).mkdirs();
        }

        if (!CommandOptions.checkDirectoryExists(reportsFolder)) {
            System.err.println("Error -2: directory \"" + reportsFolder + "\" does not exist");
            System.exit(-2);
        }

        if (!CommandOptions.checkFileExists(swaggerFilePath)) {
            System.err.println("Error -2: file \"" + swaggerFilePath + "\" does not exist");
            System.exit(-2);
        }

        if (outputFolder != null && !outputFolder.isEmpty()) {
            if (!CommandOptions.checkDirectoryExists(outputFolder)) {
                boolean mkdirs = new File(outputFolder).mkdirs();
            }
        }
        else {
            boolean mkdirs = new File(serviceFolder + "/malformedReports").mkdirs();
        }

        if (logVerbosity != null) {
            int verbosityLevel = Integer.parseInt(logVerbosity);
            switch (verbosityLevel){
                case 1:
                    Configurator.setLevel(App.class.toString(), Level.INFO);
                    break;
                case 2:
                    Configurator.setLevel(App.class.toString(), Level.DEBUG);
                    break;
                default:
                    Configurator.setLevel(App.class.toString(), Level.ERROR);
                    break;
            }
        }

    }
}
